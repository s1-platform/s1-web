package org.s1.web.services.formats;

import groovy.json.JsonOutput;
import groovy.json.JsonSlurper;
import org.apache.commons.io.IOUtils;
import org.s1.web.services.WebOperationOutput;
import org.s1.web.formats.Maps;
import org.s1.web.formats.Types;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
        this.data.putAll(fromWire((Map<String, Object>) s.parseText(json)));
        this.encoding = encoding;
    }

    /**
     * @param req Request
     * @throws IOException IOException
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
        Map<String, Object> m = Types.copy(data);
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
        return Maps.get(data, path, def);
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
        return Maps.get(t, data, path, def);
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
        return Maps.get(t, data, path);
    }

    /**
     * Get
     *
     * @param path Path
     * @param <T>  Type
     * @return Object
     */
    public <T> T get(String path) {
        return Maps.get(data, path);
    }


    protected Map<String, Object> toWire(Map<String, Object> json) {
        return (Map<String, Object>) Maps.iterate(json, new Maps.IterateFunction() {
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

    protected Map<String, Object> fromWire(Map<String, Object> json) {
        return (Map<String, Object>) Maps.iterate(json, new Maps.IterateFunction() {
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

    @Override
    public void render(HttpServletResponse response) throws IOException {
        response.setCharacterEncoding(encoding);
        response.setContentType("application/json");
        response.getOutputStream().write(toJSON().getBytes(encoding));
    }
}
