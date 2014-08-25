package org.s1.web.formats;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

/**
 * XML Format helper. Contains useful methods
 *
 * @author Grigory Pykhov
 */
public class Xml {

    private Xml(){}


    /**
     * Get element
     *
     * @param el Element
     * @param path Path
     * @return Element
     */
    public static Element getElement(Element el, String path){
        return getElement(el,path,null);
    }

    /**
     * Get element
     *
     * @param el Element
     * @param path Path
     * @param namespaces Namespaces
     * @return Element
     */
    public static Element getElement(Element el, String path, Map<String,String> namespaces){
        if(namespaces==null)
            namespaces = new HashMap<String, String>();
        Element ret = null;
        try {
            String[] parts = tokenizePath(path);
            Element o = el;
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
     *
     * @param el Element
     * @param path Path
     * @param namespaces Namespaces
     * @return Founded elements
     */
    public static List<Element> getElementList(Element el, String path, Map<String,String> namespaces){
        if(namespaces==null)
            namespaces = new HashMap<String, String>();
        List<Element> ret = new ArrayList<Element>();
        String[] parts = tokenizePath(path);
        fillElementList(el, Arrays.asList(el), parts, namespaces, ret);
        return ret;
    }

    /**
     * Get element list
     *
     * @param el Element
     * @param path Path
     * @return Founded elements
     */
    public static List<Element> getElementList(Element el, String path){
        return getElementList(el, path,null);
    }

    private static void fillElementList(Element el, List<Element> roots, String[] parts, Map<String, String> namespaces, List<Element> res){
        for(Element ret: roots){
            try {
                Element o = el;
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
                            fillElementList(el, l, parts2, namespaces, res);
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
     * @param el Element
     * @param path Path
     * @param <T> Type
     * @return Casted object
     */
    public static  <T> T get(Class<T> t, Element el, String path){
        return Types.cast(get(el,path, null, null), t);
    }

    /**
     * Get and cast
     *
     * @param t Type
     * @param el Element
     * @param path Path
     * @param namespaces Namespaces
     * @param <T> Type
     * @return Casted object
     */
    public static  <T> T get(Class<T> t, Element el, String path, Map<String,String> namespaces){
        return Types.cast(get(el,path, namespaces, null), t);
    }

    /**
     * Get and cast
     *
     * @param t Type
     * @param el Element
     * @param path Path
     * @param namespaces Namespaces
     * @param def Default value
     * @param <T> Type
     * @return Casted object
     */
    public static  <T> T get(Class<T> t, Element el, String path, Map<String,String> namespaces, T def){
        T v = Types.cast(get(el,path, namespaces, null), t);
        if(v==null)
            v = def;
        return v;
    }

    /**
     * Get
     *
     * @param el Element
     * @param path Path
     * @return Object
     */
    public static String get(Element el, String path){
        return get(el,path,null,null);
    }

    /**
     * Get
     *
     * @param el Element
     * @param path Path
     * @param namespaces Namespaces
     * @return Object
     */
    public static String get(Element el, String path, Map<String,String> namespaces){
        return get(el, path,namespaces,null);
    }

    /**
     * Get
     *
     * @param el Element
     * @param path Path
     * @param namespaces Namespaces
     * @param defVal Default value
     * @return Object
     */
    public static String get(Element el, String path, Map<String,String> namespaces, String defVal){
        if(namespaces==null)
            namespaces = new HashMap<String, String>();
        String ret = null;
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
    public static Document fromXMLString(String xml) {
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

    private static String[] tokenizePath(String path) {
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

    private static int getNumber(String name) {
        if(name.indexOf("[")<name.indexOf("]"))
            return Integer.parseInt(name.substring(name.indexOf("[") + 1, name.indexOf("]")));
        else
            return 0;
    }

    private static String getLocalName(String name) {
        if(name.startsWith("@"))
            name = name.substring(1);
        if(name.contains("["))
            name = name.substring(0,name.indexOf("["));
        if(name.contains(":"))
            name = name.substring(name.indexOf(":") + 1);
        return name;
    }

    private static String getNamespaceURI(String name, Map<String,String> ns) {
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

    public static Element getChildElement(int number, Element el, String name, String ns){
        List<Element> lst = getChildElementList(el,name,ns);
        if(number<0)
            number = lst.size()-1;

        if(number>=0 && number<lst.size())
            return lst.get(number);
        return null;
    }

    public static List<Element> getChildElementList(Element el, String name, String ns){
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

}
