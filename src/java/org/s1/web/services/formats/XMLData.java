package org.s1.web.services.formats;

import org.apache.commons.io.IOUtils;
import org.s1.web.services.WebOperationOutput;
import org.w3c.dom.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * XML data
 *
 * @author Grigory Pykhov
 */
public class XMLData extends WebOperationOutput{

    private Document xml;
    private String encoding = "UTF-8";

    /**
     *
     * @param xml XML
     */
    public XMLData(Document xml) {
        this.xml = xml;
    }

    /**
     *
     * @param xml XML
     * @param encoding Encoding
     */
    public XMLData(Document xml, String encoding) {
        this.xml = xml;
        this.encoding = encoding;
    }

    /**
     *
     * @param request Response
     * @throws IOException
     */
    public XMLData(HttpServletRequest request) throws IOException{
        this.encoding = request.getCharacterEncoding();
        this.xml = fromString(IOUtils.toString(request.getInputStream(), encoding));
    }

    /**
     *
     * @return XML
     */
    public Document getXml() {
        return xml;
    }

    /**
     *
     * @return Encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Get element
     * @param path Path
     * @return Element
     */
    public Element getElement(String path){
        return getElement(path,null);
    }

    /**
     * Get element
     * @param path Path
     * @param namespaces Namespaces
     * @return Element
     */
    public Element getElement(String path, Map<String,String> namespaces){
        if(namespaces==null)
            namespaces = new HashMap<String, String>();
        Element ret = null;
        try {
            String[] parts = tokenizePath(path);
            Element o = getXml().getDocumentElement();
            for (int i = 0; i < parts.length; i++) {
                int j = getNumber(parts[i]);
                String name = getLocalName(parts[i]);
                String ns = getNamespaceURI(parts[i],namespaces);
                if(name.equals("*"))
                    name = null;
                o = getChildElement(j,o,name,ns);
            }
            if (o != null){
                ret = o;
            }
        } catch (Throwable e) {
        }
        return ret;
    }

    /**
     * Get element list
     * @param path Path
     * @param namespaces Namespaces
     * @return Founded elements
     */
    public List<Element> getElementList(String path, Map<String,String> namespaces){
        if(namespaces==null)
            namespaces = new HashMap<String, String>();
        List<Element> ret = new ArrayList<Element>();
        String[] parts = tokenizePath(path);
        fillElementList(Arrays.asList(getXml().getDocumentElement()), parts, namespaces, ret);
        return ret;
    }

    /**
     * Get element list
     * @param path Path
     * @return Founded elements
     */
    public List<Element> getElementList(String path){
        return getElementList(path,null);
    }

    private void fillElementList(List<Element> roots, String[] parts, Map<String, String> namespaces, List<Element> res){
        for(Element ret: roots){
            try {
                Element o = getXml().getDocumentElement();
                for (int i = 0; i < parts.length; i++) {
                    int j = getNumber(parts[i]);
                    String name = getLocalName(parts[i]);
                    String ns = getNamespaceURI(parts[i],namespaces);
                    if(name.equals("*"))
                        name = null;
                    List<Element> l = getChildElementList(o,name,ns);
                    if(parts[i].contains("[")){
                        o = l.get(j);
                    }else if(l.size()>1){
                        if(parts.length-i-1>0){
                            String [] parts2 = new String[parts.length-i-1];
                            for(int k=0;k<parts2.length;k++){
                                parts2[k] = parts[k+i+1];
                            }
                            fillElementList(l, parts2, namespaces, res);
                        }else{
                            res.addAll(l);
                        }
                    }else{
                        o = l.size()==1?l.get(0):null;
                    }
                }
                if (o != null){
                    ret = o;
                }
            } catch (Throwable e) {
            }
            res.add(ret);
        }
    }

    /**
     * Get and cast
     *
     * @param t Type
     * @param path Path
     * @param <T> Type
     * @return Casted object
     */
    public <T> T get(Class<T> t, String path){
        return ObjectTypes.cast(get(path,null,null),t);
    }

    /**
     * Get and cast
     *
     * @param t Type
     * @param path Path
     * @param namespaces Namespaces
     * @param <T> Type
     * @return Casted object
     */
    public <T> T get(Class<T> t, String path, Map<String,String> namespaces){
        return ObjectTypes.cast(get(path,namespaces,null),t);
    }

    /**
     * Get and cast
     *
     * @param t Type
     * @param path Path
     * @param namespaces Namespaces
     * @param def Default value
     * @param <T> Type
     * @return Casted object
     */
    public <T> T get(Class<T> t, String path, Map<String,String> namespaces, T def){
        T v = ObjectTypes.cast(get(path,namespaces,null),t);
        if(v==null)
            v = def;
        return v;
    }

    /**
     * Get
     * @param path Path
     * @return Object
     */
    public String get(String path){
        return get(path,null,null);
    }

    /**
     * Get
     * @param path Path
     * @param namespaces Namespaces
     * @return Object
     */
    public String get(String path, Map<String,String> namespaces){
        return get(path,namespaces,null);
    }

    /**
     * Get
     * @param path Path
     * @param namespaces Namespaces
     * @param defVal Default value
     * @return Object
     */
    public String get(String path, Map<String,String> namespaces, String defVal){
        if(namespaces==null)
            namespaces = new HashMap<String, String>();
        String ret = null;
        Element el = getXml().getDocumentElement();
        try {
            String[] parts = tokenizePath(path);
            for (int i = 0; i < parts.length; i++) {
                int j = getNumber(parts[i]);
                String name = getLocalName(parts[i]);
                String ns = getNamespaceURI(parts[i],namespaces);
                if(parts[i].startsWith("@")){
                    NamedNodeMap attrs = el.getAttributes();
                    for(int k=0;k<attrs.getLength();k++){
                        Attr a = (Attr)attrs.item(k);
                        if(a.getLocalName().equals(name)){
                            if(ns!=null){
                                if(a.getNamespaceURI().equals(ns)) {
                                    ret = a.getValue();
                                    break;
                                }
                            }else{
                                ret = a.getValue();
                                break;
                            }
                        }
                    }
                    break;
                }else {
                    if(name.equals("*"))
                        name = null;
                    el = getChildElement(j, el, name, ns);
                }
            }
            if (ret == null && el != null)
                ret = el.getTextContent();
        } catch (Throwable e) {
        }
        if(ret==null)
            ret = defVal;
        return ret;
    }

    /**
     *
     * @param xml XML String
     * @return Document
     */
    public static Document fromString(String xml) {
        try{
            ByteArrayInputStream bis = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setIgnoringElementContentWhitespace(true);
            factory.setCoalescing(true);
            final DocumentBuilder loader = factory.newDocumentBuilder();
            final Document doc = loader.parse(bis);
            return doc;
        }catch (Throwable e){
            throw new IllegalArgumentException("Invalid XML: \n"+xml+"\n"+e.getClass().getName()+": "+e.getMessage(),e);
        }
    }

    /**
     *
     * @param el Element
     * @param encoding Encoding
     * @return XML String
     */
    public static String toXMLString(Element el, String encoding){
        if(encoding==null)
            encoding = "UTF-8";
        try{
            final TransformerFactory tf = TransformerFactory.newInstance();
            final Transformer transformer = tf.newTransformer();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(el),new StreamResult(os));
            return new String(os.toByteArray(),encoding);
        }catch (Throwable e){
            return null;
        }
    }

    protected static String[] tokenizePath(String path) {
        String s = path;
        s = s.replace("&", "&amp;");
        s = s.replace("\\\\", "&backslash;");
        s = s.replace("\\.", "&dot;");
        String[] p = s.split("\\.");
        String[] p2 = new String[p.length];
        for (int i = 0; i < p.length; i++) {
            p2[i] = p[i]
                    .replace("&dot;", ".")
                    .replace("&backslash;", "\\\\")
                    .replace("&amp;", "&");
        }
        return p2;
    }

    protected static int getNumber(String name) {
        if(name.indexOf("[")<name.indexOf("]"))
            return Integer.parseInt(name.substring(name.indexOf("[") + 1, name.indexOf("]")));
        else
            return 0;
    }

    protected static String getLocalName(String name) {
        if(name.startsWith("@"))
            name = name.substring(1);
        if(name.contains("["))
            name = name.substring(0,name.indexOf("["));
        if(name.contains(":"))
            name = name.substring(name.indexOf(":") + 1);
        return name;
    }

    protected static String getNamespaceURI(String name, Map<String,String> ns) {
        if(name.startsWith("@"))
            name = name.substring(1);
        if(name.contains("["))
            name = name.substring(0,name.indexOf("["));
        if(name.contains(":"))
            name = name.substring(0,name.indexOf(":"));
        else
            name = null;
        if(name!=null && !name.isEmpty())
            name = ns.get(name);
        return name;
    }

    protected static Element getChildElement(int number, Element el, String name, String ns){
        List<Element> lst = getChildElementList(el,name,ns);
        if(number<0)
            number = lst.size()-1;

        if(number>=0 && number<lst.size())
            return lst.get(number);
        return null;
    }

    protected static List<Element> getChildElementList(Element el, String name, String ns){
        List<Element> lst = new ArrayList<Element>();
        NodeList nl = el.getChildNodes();
        for(int i=0;i<nl.getLength();i++){
            Node n = nl.item(i);
            if(n instanceof Element){
                Element e = (Element)n;
                if(name!=null){
                    if(name.equals(n.getLocalName())){
                        if(ns!=null){
                            if(ns.equals(n.getNamespaceURI()))
                                lst.add(e);
                        }else
                            lst.add(e);
                    }
                }else{
                    lst.add(e);
                }
            }
        }
        return lst;
    }

    @Override
    public void render(HttpServletResponse response) throws IOException {
        response.setCharacterEncoding(encoding);
        response.setContentType("text/xml");
        response.getOutputStream().write(toXMLString(getXml().getDocumentElement(),encoding).getBytes(encoding));
    }
}
