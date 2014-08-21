package org.s1.web.services.formats;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.*;
import java.io.*;
import java.util.*;

/**
 * SOAP data
 *
 * @author Grigory Pykhov
 */
public class SOAPData extends XMLData {

    private SOAPMessage message;

    /**
     * @param msg SOAP Message
     */
    public SOAPData(SOAPMessage msg) {
        this(msg, "UTF-8");
    }

    /**
     * @param msg      SOAP Message
     * @param encoding Encoding
     */
    public SOAPData(SOAPMessage msg, String encoding) {
        super((Document) null, encoding);
        message = msg;
    }

    /**
     * @param request  Request
     * @throws IOException
     */
    public SOAPData(HttpServletRequest request) throws IOException {
        super((Document) null);
        Map<String, String> headers = new HashMap<String, String>();
        Enumeration<String> he = request.getHeaderNames();
        while (he.hasMoreElements()) {
            String h = he.nextElement();
            headers.put(h, request.getHeader(h));
        }
        message = createSoapFromStream(headers, request.getInputStream());
    }

    @Override
    public Document getXml() {
        try {
            return ((Element) message.getSOAPPart().getEnvelope()).getOwnerDocument();
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @return SOAPMessage
     */
    public SOAPMessage getMessage() {
        return message;
    }

    /**
     * @param el Element with file
     * @return Data
     */
    public byte[] readFile(Element el) {
        Element include = getChildElement(0, el, "Include", "http://www.w3.org/2004/08/xop/include");
        byte[] data = null;
        if (include != null) {
            //LOG.debug("Reading XOP file")
            String id = "<" + include.getAttribute("href").substring("cid:".length()) + ">";
            Iterator<AttachmentPart> it = getMessage().getAttachments();
            while (it.hasNext()) {
                AttachmentPart att = it.next();
                if (id.equals(att.getContentId())) {
                    try {
                        data = att.getRawContentBytes();
                    } catch (SOAPException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }
            }
        } else {
            String b = el.getTextContent();
            data = org.apache.commons.codec.binary.Base64.decodeBase64(b);
        }
        //if(LOG.isDebugEnabled())
        //    LOG.debug("Reading file from SOAP, length: "+(data==null?-1:data.length));
        return data;
    }

    /**
     * @param el Element with file
     * @return Data
     */
    public DataHandler readDataHandler(Element el) {
        Element include = getChildElement(0, el, "Include", "http://www.w3.org/2004/08/xop/include");
        DataHandler is = null;
        if (include != null) {
            //LOG.debug("Reading XOP file")
            String id = "<" + include.getAttribute("href").substring("cid:".length()) + ">";
            Iterator<AttachmentPart> it = getMessage().getAttachments();
            while (it.hasNext()) {
                AttachmentPart att = it.next();
                if (id.equals(att.getContentId())) {
                    try {
                        is = att.getDataHandler();
                    } catch (SOAPException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }
            }
        } else {
            String b = el.getTextContent();
            final byte[] data = org.apache.commons.codec.binary.Base64.decodeBase64(b);
            is = new DataHandler(new DataSource() {
                private InputStream is = new ByteArrayInputStream(data);

                @Override
                public InputStream getInputStream() throws IOException {
                    return is;
                }

                @Override
                public OutputStream getOutputStream() throws IOException {
                    return null;
                }

                @Override
                public String getContentType() {
                    return "application/octet-stream";
                }

                @Override
                public String getName() {
                    return null;
                }
            });
        }
        //if(LOG.isDebugEnabled())
        //    LOG.debug("Reading DataHandler from SOAP, contentType: "+(is==null?-1:is.getDataSource().getContentType()));
        return is;
    }

    /**
     * Write binary data to message as base64 or xop:Include depending on mtom parameter.
     *
     * @param mtom Use MTOM
     * @param el   Element with file
     * @param dh   Data
     */
    public void writeDataHandler(boolean mtom, Element el, DataHandler dh) throws IOException {
        //LOG.debug("Writing DataHandler, mtom: "+mtom+", contentType: "+dh.getContentType());
        if (mtom) {
            Element inc = el.getOwnerDocument().createElementNS("http://www.w3.org/2004/08/xop/include", "Include");
            el.appendChild(inc);
            String id = UUID.randomUUID().toString() + "@s1-platform.org";
            AttachmentPart ap = getMessage().createAttachmentPart();
            ap.setDataHandler(dh);
            ap.setContentId("<" + id + ">");
            getMessage().addAttachmentPart(ap);
            inc.setAttribute("href", "cid:" + id);
            //LOG.debug("File contentId: "+id);
        } else {
            el.setTextContent(new String(org.apache.commons.codec.binary.Base64.encodeBase64(IOUtils.toByteArray(dh.getInputStream()))));
        }
    }

    /**
     * @param mtom Use MTOM
     * @param el   Element with file
     * @param b    Data
     */
    public void writeFile(boolean mtom, Element el, byte[] b) {
        //LOG.debug("Writing file, mtom: "+mtom+", size: "+b.length);
        if (mtom) {
            Element inc = el.getOwnerDocument().createElementNS("http://www.w3.org/2004/08/xop/include", "Include");
            el.appendChild(inc);
            String id = UUID.randomUUID().toString() + "@s1-platform.org";
            AttachmentPart ap = getMessage().createAttachmentPart();
            try {
                ap.setRawContentBytes(b, 0, b.length, "application/octet-stream");
            } catch (SOAPException e) {
                throw new RuntimeException(e);
            }
            ap.setContentId("<" + id + ">");
            getMessage().addAttachmentPart(ap);
            inc.setAttribute("href", "cid:" + id);
            //LOG.debug("File contentId: "+id);
        } else {
            el.setTextContent(new String(org.apache.commons.codec.binary.Base64.encodeBase64(b)));
        }
    }

    /**
     * @param soap     SOAP String
     * @param encoding Encoding
     * @return SOAP Message
     */
    public static SOAPMessage createSoapFromString(String soap, String encoding) {
        return createSoapFromString(null, soap, encoding);
    }

    /**
     * @param protocol Protocol (SOAP 1.1 or SOAP 1.2)
     * @param soap     SOAP String
     * @param encoding Encoding
     * @return SOAP Message
     */
    public static SOAPMessage createSoapFromString(String protocol, String soap, String encoding) {
        if (protocol == null || protocol.isEmpty()) {
            if (soap.contains("http://www.w3.org/2003/05/soap-envelope") &&
                    !soap.contains("http://schemas.xmlsoap.org/soap/envelope/")) {
                protocol = SOAPConstants.SOAP_1_2_PROTOCOL;
            } else {
                protocol = SOAPConstants.SOAP_1_1_PROTOCOL;
            }
        }
        try {
            MessageFactory messageFactory = MessageFactory.newInstance(protocol);
            return messageFactory.createMessage(null, new ByteArrayInputStream(soap.getBytes(encoding)));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param headers Headers
     * @param is      Input stream
     * @return SOAP Message
     */
    public static SOAPMessage createSoapFromStream(Map<String, String> headers, InputStream is) {
        return createSoapFromStream(null, headers, is);
    }

    /**
     * @param protocol Protocol (SOAP 1.1 or SOAP 1.2)
     * @param headers  Headers
     * @param is       Input stream
     * @return SOAP Message
     */
    public static SOAPMessage createSoapFromStream(String protocol, Map<String, String> headers, InputStream is) {
        if (headers == null)
            headers = new HashMap<String, String>();
        MimeHeaders sh = new MimeHeaders();
        for (String k : headers.keySet()) {
            sh.addHeader(k, headers.get(k));
        }
        String p = protocol;
        if (protocol == null || protocol.isEmpty())
            p = SOAPConstants.SOAP_1_1_PROTOCOL;
        try {
            try {
                MessageFactory messageFactory = MessageFactory.newInstance(p);
                return messageFactory.createMessage(sh, is);
            } catch (SOAPException e) {
                if ("SOAPVersionMismatchException".equals(e.getClass().getSimpleName()) && (protocol == null || protocol.isEmpty())) {
                    MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
                    return messageFactory.createMessage(sh, is);
                } else {
                    throw e;
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param msg      SOAP Message
     * @param encoding Encoding
     * @return SOAP String
     */
    public static String toSOAPString(SOAPMessage msg, String encoding) {
        if (msg == null)
            return null;
        return toXMLString(msg.getSOAPPart().getDocumentElement(), encoding);
    }

    /**
     * @param msg SOAP Message
     * @return Input Stream
     */
    public static InputStream toInputStream(SOAPMessage msg) {
        if (msg == null)
            return null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            msg.writeTo(os);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return new ByteArrayInputStream(os.toByteArray());
    }

    @Override
    public void render(HttpServletResponse response) throws IOException {
        response.setContentType("text/xml");
        response.setCharacterEncoding(getEncoding());
        try {
            getMessage().writeTo(response.getOutputStream());
        } catch (SOAPException e) {
            throw new RuntimeException(e);
        }
    }
}
