package org.s1.web.services.formats;

import org.s1.web.services.WebOperationOutput;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Simple form url-encoded data
 *
 * @author Grigory Pykhov
 */
public class FormData extends WebOperationOutput {

    private Map<String, List<String>> data = new HashMap<String, List<String>>();
    private String encoding = "UTF-8";

    /**
     *
     */
    public FormData() {

    }

    /**
     * @param encoding Encoding
     */
    public FormData(String encoding) {
        this.encoding = encoding;
    }

    /**
     * @param data Data
     */
    public FormData(Map<String, List<String>> data) {
        this.data.putAll(data);
    }

    /**
     * @param data     Data
     * @param encoding Encoding
     */
    public FormData(Map<String, List<String>> data, String encoding) {
        this.data.putAll(data);
        this.encoding = encoding;
    }

    /**
     * @param request Request
     */
    public FormData(HttpServletRequest request) {
        this.encoding = request.getCharacterEncoding();

        //GET
        String q = request.getQueryString();
        if (q != null && !q.isEmpty()) {
            String[] arr = q.split("&", -1);
            for (String it : arr) {
                if (it == null || it.isEmpty())
                    continue;
                String nv[] = it.split("=", -1);
                String n = nv[0];
                if (n == null || n.isEmpty())
                    continue;
                try {
                    n = URLDecoder.decode(n, encoding);
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalArgumentException("Unsupported encoding: " + encoding, e);
                }
                String v = null;
                if (nv.length > 1) {
                    v = nv[1];
                    try {
                        v = URLDecoder.decode(v, encoding);
                    } catch (UnsupportedEncodingException e) {
                        throw new IllegalArgumentException("Unsupported encoding: " + encoding, e);
                    }
                }
                if (!data.containsKey(nv))
                    data.put(n, new ArrayList<String>());
                List<String> list = data.get(n);
                list.add(v);
            }
        }
        if (request.getContentType() != null
                && request.getContentType().contains("application/x-www-form-urlencoded")
                ) {
            //POST
            Iterator<Map.Entry<String, String[]>> it = request
                    .getParameterMap().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String[]> e = it.next();
                String n = e.getKey();
                if (n == null || n.isEmpty())
                    continue;
                String v = e.getValue()[0];
                if (!data.containsKey(n))
                    data.put(n, new ArrayList<String>());
                List<String> list = data.get(n);
                list.add(v);
            }
        }
    }

    /**
     * @return Data
     */
    public Map<String, List<String>> getData() {
        return data;
    }

    /**
     * @param t     Type
     * @param param Param name
     * @param <T>   Type
     * @return Casted value
     */
    public <T> T getFirst(Class<T> t, String param) {
        if (!data.containsKey(param))
            data.put(param, new ArrayList<String>());
        return ObjectTypes.cast(data.get(param).get(0), t);
    }

    /**
     * @param param Param name
     * @return Value
     */
    public String getFirst(String param) {
        if (!data.containsKey(param))
            data.put(param, new ArrayList<String>());
        return data.get(param).get(0);
    }

    /**
     * @param param Param name
     * @param value Value
     * @return FormData
     */
    public FormData set(String param, String value) {
        if (!data.containsKey(param))
            data.put(param, new ArrayList<String>());
        List<String> list = data.get(param);
        list.add(value);
        return this;
    }

    /**
     * @return Encoding
     */
    public String getEncoding() {
        return encoding;
    }

    @Override
    public void render(HttpServletResponse response) throws IOException {
        response.setContentType("application/x-www-form-urlencoded");
        response.setCharacterEncoding(encoding);
        String urlEncoded = "";
        for (String key : data.keySet()) {
            for (String val : data.get(key)) {
                if (!urlEncoded.isEmpty())
                    urlEncoded += "&";
                urlEncoded += URLEncoder.encode(key, encoding) + "=" + URLEncoder.encode(val, encoding);
            }
        }
        response.getOutputStream().write(urlEncoded.getBytes(Charset.forName(encoding)));
    }
}
