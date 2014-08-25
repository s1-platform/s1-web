package org.s1.web.services.formats;

import org.s1.web.formats.Soap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

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
     * @param request Request
     * @throws IOException IOException
     */
    public SOAPData(HttpServletRequest request) throws IOException {
        super((Document) null);
        Map<String, String> headers = new HashMap<String, String>();
        Enumeration<String> he = request.getHeaderNames();
        while (he.hasMoreElements()) {
            String h = he.nextElement();
            headers.put(h, request.getHeader(h));
        }
        message = Soap.fromInputStream(headers, request.getInputStream());
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
        return Soap.readFile(getMessage(), el);
    }

    /**
     * @param el Element with file
     * @return Data
     */
    public DataHandler readDataHandler(Element el) {
        return Soap.readDataHandler(getMessage(), el);
    }

    /**
     * Write binary data to message as base64 or xop:Include depending on mtom parameter.
     *
     * @param mtom Use MTOM
     * @param el   Element with file
     * @param dh   Data
     * @throws java.io.IOException IOException
     */
    public void writeDataHandler(boolean mtom, Element el, DataHandler dh) throws IOException {
        Soap.writeDataHandler(getMessage(), mtom, el, dh);
    }

    /**
     * @param mtom Use MTOM
     * @param el   Element with file
     * @param b    Data
     */
    public void writeFile(boolean mtom, Element el, byte[] b) {
        Soap.writeFile(getMessage(), mtom, el, b);
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
