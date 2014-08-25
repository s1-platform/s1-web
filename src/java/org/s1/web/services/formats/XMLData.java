package org.s1.web.services.formats;

import org.apache.commons.io.IOUtils;
import org.s1.web.services.WebOperationOutput;
import org.s1.web.formats.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * XML data
 *
 * @author Grigory Pykhov
 */
public class XMLData extends WebOperationOutput {

    private Document xml;
    private String encoding = "UTF-8";

    /**
     * @param xml XML
     */
    public XMLData(Document xml) {
        this.xml = xml;
    }

    /**
     * @param xml      XML
     * @param encoding Encoding
     */
    public XMLData(Document xml, String encoding) {
        this.xml = xml;
        this.encoding = encoding;
    }

    /**
     * @param request Response
     * @throws IOException IOException
     */
    public XMLData(HttpServletRequest request) throws IOException {
        this.encoding = request.getCharacterEncoding();
        this.xml = Xml.fromXMLString(IOUtils.toString(request.getInputStream(), encoding));
    }

    /**
     * @return XML
     */
    public Document getXml() {
        return xml;
    }

    /**
     * @return Encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Get element
     *
     * @param path Path
     * @return Element
     */
    public Element getElement(String path) {
        return Xml.getElement(getXml().getDocumentElement(), path);
    }

    /**
     * Get element
     *
     * @param path       Path
     * @param namespaces Namespaces
     * @return Element
     */
    public Element getElement(String path, Map<String, String> namespaces) {
        return Xml.getElement(getXml().getDocumentElement(), path, namespaces);
    }

    /**
     * Get element list
     *
     * @param path       Path
     * @param namespaces Namespaces
     * @return Founded elements
     */
    public List<Element> getElementList(String path, Map<String, String> namespaces) {
        return Xml.getElementList(getXml().getDocumentElement(), path, namespaces);
    }

    /**
     * Get element list
     *
     * @param path Path
     * @return Founded elements
     */
    public List<Element> getElementList(String path) {
        return Xml.getElementList(getXml().getDocumentElement(), path);
    }

    /**
     * Get and cast
     *
     * @param t    Type
     * @param path Path
     * @param <T>  Type
     * @return Casted object
     */
    public <T> T get(Class<T> t, String path) {
        return Xml.get(t, getXml().getDocumentElement(), path);
    }

    /**
     * Get and cast
     *
     * @param t          Type
     * @param path       Path
     * @param namespaces Namespaces
     * @param <T>        Type
     * @return Casted object
     */
    public <T> T get(Class<T> t, String path, Map<String, String> namespaces) {
        return Xml.get(t, getXml().getDocumentElement(), path, namespaces);
    }

    /**
     * Get and cast
     *
     * @param t          Type
     * @param path       Path
     * @param namespaces Namespaces
     * @param def        Default value
     * @param <T>        Type
     * @return Casted object
     */
    public <T> T get(Class<T> t, String path, Map<String, String> namespaces, T def) {
        return Xml.get(t, getXml().getDocumentElement(), path, namespaces, def);
    }

    /**
     * Get
     *
     * @param path Path
     * @return Object
     */
    public String get(String path) {
        return Xml.get(getXml().getDocumentElement(), path);
    }

    /**
     * Get
     *
     * @param path       Path
     * @param namespaces Namespaces
     * @return Object
     */
    public String get(String path, Map<String, String> namespaces) {
        return Xml.get(getXml().getDocumentElement(), path, namespaces);
    }

    /**
     * Get
     *
     * @param path       Path
     * @param namespaces Namespaces
     * @param defVal     Default value
     * @return Object
     */
    public String get(String path, Map<String, String> namespaces, String defVal) {
        return Xml.get(getXml().getDocumentElement(), path, namespaces, defVal);
    }


    @Override
    public void render(HttpServletResponse response) throws IOException {
        response.setCharacterEncoding(encoding);
        response.setContentType("text/xml");
        response.getOutputStream().write(Xml.toXMLString(getXml().getDocumentElement(), encoding).getBytes(encoding));
    }
}
