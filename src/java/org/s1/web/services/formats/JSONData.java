package org.s1.web.services.formats;

import groovy.json.JsonOutput;
import groovy.json.JsonSlurper;
import org.apache.commons.io.IOUtils;
import org.s1.web.services.WebOperationOutput;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * JSON Data
 *
 * @author Grigory Pykhov
 */
public class JSONData extends WebOperationOutput {

    private final Map<String, Object> data = new HashMap<String, Object>();
    private String encoding = "UTF-8";

    /**
     *
     */
    public JSONData() {

    }

    /**
     * @param data Data
     */
    public JSONData(Map<String, Object> data) {
        this.data.putAll(data);
    }

    /**
     * @param data     Data
     * @param encoding Encoding
     */
    public JSONData(Map<String, Object> data, String encoding) {
        this.data.putAll(data);
        this.encoding = encoding;
    }

    /**
     * @param json JSON String
     */
    public JSONData(String json) {
        this(json, "UTF-8");
    }

    /**
     * @param json     JSON String
     * @param encoding Encoding
     */
    public JSONData(String json, String encoding) {
        JsonSlurper s = new JsonSlurper();
        this.data.putAll((Map<String, Object>) s.parseText(json));
        this.encoding = encoding;
    }

    /**
     * @param req      Request
     * @throws IOException
     */
    public JSONData(HttpServletRequest req) throws IOException {
        this(IOUtils.toString(req.getInputStream(), req.getCharacterEncoding()), req.getCharacterEncoding());
        this.encoding = req.getCharacterEncoding();
    }

    /**
     * @return Data
     */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * @return Encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * To JSON String
     *
     * @return JSON String
     */
    public String toJSON() {
        Map<String, Object> m = copy(data);
        m = toWire(m);
        return JsonOutput.toJson(m);
    }

    /**
     * Get value from path
     *
     * @param path Path
     * @param def  Default
     * @param <T>  Type
     * @return Object
     */
    public <T> T get(String path, T def) {
        Object ret = def;
        try {
            String[] parts = tokenizePath(path);
            Object o = data;
            for (int i = 0; i < parts.length; i++) {
                int[] j = getNumber(parts[i]);
                String name = getLocalName(parts[i]);
                o = ((Map) o).get(name);
                if (j != null) {
                    for (int k = 0; k < j.length; k++) {
                        o = ((List) o).get(j[k]);
                    }
                }
            }
            if (o != null)
                ret = o;
            else
                ret = def;
        } catch (Throwable e) {
        }
        return (T) ret;
    }

    /**
     * Get and cast
     *
     * @param t    Type
     * @param path Path
     * @param def  Default
     * @param <T>  Type
     * @return Casted object
     */
    public <T> T get(Class<T> t, String path, T def) {
        return ObjectTypes.cast(get(path, def), t);
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
        return ObjectTypes.cast(get(path, null), t);
    }

    /**
     * Get
     *
     * @param path Path
     * @param <T>  Type
     * @return Object
     */
    public <T> T get(String path) {
        return get(path, null);
    }

    /**
     * Set value to key identified by path
     *
     * @param path Path
     * @param val  Value
     * @return JSONData
     */
    public JSONData set(String path, Object val) {
        String[] parts = tokenizePath(path);
        Map<String, Object> o = data;
        for (int i = 0; i < parts.length; i++) {
            int[] j = getNumber(parts[i]);
            String name = getLocalName(parts[i]);
            if (i == parts.length - 1) {
                if (j != null) {
                    if (!o.containsKey(name)) {
                        o.put(name, new ArrayList());
                    }
                    List<Object> o1 = (List<Object>) o.get(name);

                    for (int k = 0; k < j.length; k++) {
                        if (o1.size() <= j[k]) {
                            for (int ii = 0; ii <= j[k] - o1.size(); ii++)
                                o1.add(null);
                            if (k == j.length - 1) {
                                o1.set(j[k], new HashMap());
                            } else {
                                o1.set(j[k], new ArrayList());
                            }
                        }
                        if (k == j.length - 1) {
                            o1.set(j[k], val);
                        } else {
                            o1 = (List<Object>) o1.get(j[k]);
                        }
                    }
                } else {
                    o.put(name, val);
                }
            } else {

                if (j != null) {
                    if (!o.containsKey(name)) {
                        o.put(name, new ArrayList());
                    }
                    List<Object> o1 = (List<Object>) o.get(name);

                    for (int k = 0; k < j.length; k++) {
                        if (o1.size() <= j[k]) {
                            for (int ii = o1.size(); ii <= j[k]; ii++) {
                                o1.add(null);
                            }
                            if (k == j.length - 1) {
                                o1.set(j[k], new HashMap());
                            } else {
                                o1.set(j[k], new ArrayList());
                            }
                        }
                        if (k == j.length - 1) {
                            o = (Map<String, Object>) o1.get(j[k]);
                        } else {
                            o1 = (List<Object>) o1.get(j[k]);
                        }
                    }
                } else {
                    if (!o.containsKey(name)) {
                        o.put(name, new HashMap());
                    }
                    o = (Map<String, Object>) o.get(name);
                }
            }
        }
        return this;
    }

    /**
     *
     * @param args Key, Value array
     * @return HashMap
     */
    public static Map<String,Object> newSOHashMap(Object ... args){
        return newHashMap(args);
    }

    /**
     *
     * @param args Key, Value array
     * @param <K> Key type
     * @param <V> Value type
     * @return HashMap
     */
    public static <K,V> Map<K,V> newHashMap(Object ... args){
        Map<K, V> m = new HashMap<K, V>();
        for (int i = 0; i < args.length; i += 2) {
            m.put((K) args[i], i + 1 >= args.length ? null : (V) args[i + 1]);
        }
        return m;
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

    protected static int[] getNumber(String name) {
        String s = name;
        s = s.replace("&", "&amp;");
        s = s.replace("\\\\", "&backslash;");
        s = s.replace("\\[", "&open;");
        s = s.replace("\\]", "&close;");
        if (s.indexOf("[") < s.indexOf("]")) {
            String s1 = s.substring(s.indexOf("[") + 1, s.lastIndexOf("]"));
            String[] s2 = s1.split("\\]\\[");
            int[] r = new int[s2.length];
            for (int i = 0; i < s2.length; i++) {
                r[i] = Integer.parseInt(s2[i]);
            }
            return r;
        }
        return null;
    }

    protected static String getLocalName(String name) {
        String s = name;
        s = s.replace("&", "&amp;");
        s = s.replace("\\\\", "&backslash;");
        s = s.replace("\\[", "&open;");
        s = s.replace("\\]", "&close;");
        String s1 = s;
        if (s.indexOf("[") < s.indexOf("]")) {
            s1 = s.substring(0, s.indexOf("["));
        }
        name = s1.replace("&open;", "[").replace("&close;", "]")
                .replace("&backslash;", "\\")
                .replace("&amp;", "&");
        //name = name.replace("\\[", "[").replace("\\]", "]");
        return name;
    }

    protected static <T> T iterate(Object o, Function closure) {
        return (T) iterateNamedObjectFromLeaf(null, "", o, closure);
    }

    protected static Object iterateNamedObjectFromLeaf(String name, String path, Object o, Function closure) {
        if (o instanceof Map) {
            final Map<String, Object> m1 = new HashMap<String, Object>();
            Map<String, Object> m = (Map<String, Object>) o;
            for (Map.Entry<String, Object> e : m.entrySet()) {
                m1.put(e.getKey(),
                        iterateNamedObjectFromLeaf(e.getKey(), (!path.isEmpty() ? path + "." + e.getKey() : e.getKey()), e.getValue(), closure));
            }
            return closure.call(name, m1, path);
        } else if (o instanceof List) {
            List l = new ArrayList();
            for (int i = 0; i < ((List) o).size(); i++) {
                l.add(iterateNamedObjectFromLeaf(null, path + "[" + i + "]", ((List) o).get(i), closure));
            }
            return closure.call(name, l, path);
        } else {
            return closure.call(name, o, path);
        }
    }

    protected static abstract class Function {
        public abstract Object call(String name, Object value, String path);
    }

    protected static Map<String, Object> toWire(Map<String, Object> json) {
        return (Map<String, Object>) iterate(json, new Function() {
            @Override
            public Object call(String name, Object value, String path) {
                try {
                    if (value instanceof Date) {
                        return "/Date(" + ((Date) value).getTime() + ")/";
                    }
                } catch (Throwable e) {
                }
                return value;
            }
        });
    }

    protected static Map<String, Object> fromWire(Map<String, Object> json) {
        return (Map<String, Object>) iterate(json, new Function() {
            @Override
            public Object call(String name, Object value, String path) {
                if (value instanceof String) {
                    String s = (String) value;
                    try {
                        if (s.startsWith("/") && s.endsWith("/")) {
                            s = s.substring(1, s.length() - 1);
                            String type = s.substring(0, s.indexOf("("));
                            String val = s.substring(s.indexOf("(") + 1,
                                    s.lastIndexOf(")"));
                            if ("Date".equalsIgnoreCase(type)) {
                                return new Date(Long.parseLong(val));
                            }
                        }
                    } catch (Throwable e) {
                    }
                }
                return value;
            }
        });
    }

    protected static <T> T copy(T orig) {
        if (orig == null)
            return null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(orig);
            oos.flush();
            ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bin);
            return (T) ois.readObject();
        } catch (Throwable e) {
            return null;
        }
    }

    @Override
    public void render(HttpServletResponse response) throws IOException {
        response.setCharacterEncoding(encoding);
        response.setContentType("application/json");
        response.getOutputStream().write(toJSON().getBytes(encoding));
    }
}
