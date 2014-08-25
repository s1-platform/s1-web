package org.s1.web.formats;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Element;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.soap.*;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * SOAP helper. Contains useful methods
 *
 * @author Grigory Pykhov
 */
public class Soap {

    private Soap() {
    }


    /**
     * @param msg SOAP Message
     * @param el  Element with file
     * @return Data
     */
    public static byte[] readFile(SOAPMessage msg, Element el) {
        Element include = Xml.getChildElement(0, el, "Include", "http://www.w3.org/2004/08/xop/include");
        byte[] data = null;
        if (include != null) {
            //LOG.debug("Reading XOP file")
            String id = "<" + include.getAttribute("href").substring("cid:".length()) + ">";
            Iterator<AttachmentPart> it = msg.getAttachments();
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
     * @param msg SOAP Message
     * @param el  Element with file
     * @return Data
     */
    public static DataHandler readDataHandler(SOAPMessage msg, Element el) {
        Element include = Xml.getChildElement(0, el, "Include", "http://www.w3.org/2004/08/xop/include");
        DataHandler is = null;
        if (include != null) {
            //LOG.debug("Reading XOP file")
            String id = "<" + include.getAttribute("href").substring("cid:".length()) + ">";
            Iterator<AttachmentPart> it = msg.getAttachments();
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
     * @param msg  SOAP Message
     * @param mtom Use MTOM
     * @param el   Element with file
     * @param dh   Data
     * @throws java.io.IOException IOException
     */
    public static void writeDataHandler(SOAPMessage msg, boolean mtom, Element el, DataHandler dh) throws IOException {
        //LOG.debug("Writing DataHandler, mtom: "+mtom+", contentType: "+dh.getContentType());
        if (mtom) {
            Element inc = el.getOwnerDocument().createElementNS("http://www.w3.org/2004/08/xop/include", "Include");
            el.appendChild(inc);
            String id = UUID.randomUUID().toString() + "@s1-platform.org";
            AttachmentPart ap = msg.createAttachmentPart();
            ap.setDataHandler(dh);
            ap.setContentId("<" + id + ">");
            msg.addAttachmentPart(ap);
            inc.setAttribute("href", "cid:" + id);
            //LOG.debug("File contentId: "+id);
        } else {
            el.setTextContent(new String(org.apache.commons.codec.binary.Base64.encodeBase64(IOUtils.toByteArray(dh.getInputStream()))));
        }
    }

    /**
     * @param msg  SOAP Message
     * @param mtom Use MTOM
     * @param el   Element with file
     * @param b    Data
     */
    public static void writeFile(SOAPMessage msg, boolean mtom, Element el, byte[] b) {
        //LOG.debug("Writing file, mtom: "+mtom+", size: "+b.length);
        if (mtom) {
            Element inc = el.getOwnerDocument().createElementNS("http://www.w3.org/2004/08/xop/include", "Include");
            el.appendChild(inc);
            String id = UUID.randomUUID().toString() + "@s1-platform.org";
            AttachmentPart ap = msg.createAttachmentPart();
            try {
                ap.setRawContentBytes(b, 0, b.length, "application/octet-stream");
            } catch (SOAPException e) {
                throw new RuntimeException(e);
            }
            ap.setContentId("<" + id + ">");
            msg.addAttachmentPart(ap);
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
    public static SOAPMessage fromSOAPString(String soap, String encoding) {
        return fromSOAPString(null, soap, encoding);
    }

    /**
     * @param protocol Protocol (SOAP 1.1 or SOAP 1.2)
     * @param soap     SOAP String
     * @param encoding Encoding
     * @return SOAP Message
     */
    public static SOAPMessage fromSOAPString(String protocol, String soap, String encoding) {
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
    public static SOAPMessage fromInputStream(Map<String, String> headers, InputStream is) {
        return fromInputStream(null, headers, is);
    }

    /**
     * @param protocol Protocol (SOAP 1.1 or SOAP 1.2)
     * @param headers  Headers
     * @param is       Input stream
     * @return SOAP Message
     */
    public static SOAPMessage fromInputStream(String protocol, Map<String, String> headers, InputStream is) {
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
        return Xml.toXMLString(msg.getSOAPPart().getDocumentElement(), encoding);
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

}
