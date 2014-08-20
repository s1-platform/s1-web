package org.s1.web.services;

import groovy.json.JsonSlurper;
import org.w3c.dom.Document;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.soap.SOAPMessage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * Web Operation input
 *
 * @author Grigory Pykhov
 */
public class WebOperationInput {
    private String id = UUID.randomUUID().toString();
    private HttpServletRequest request;
    private HttpServletResponse response;

    /**
     * @param request  Request
     * @param response Response
     */
    public WebOperationInput(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    /**
     * Get request id
     *
     * @return Request id
     */
    public String getId() {
        return id;
    }

    /**
     * @return Request
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * @return Response
     */
    public HttpServletResponse getResponse() {
        return response;
    }

    public Map<String, Object> asJSON() {
        JsonSlurper s = new JsonSlurper();
        String json = null;
        try {
            json = IStoString(request.getInputStream(), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException();
        }
        return (Map<String, Object>) s.parseText(json);
    }

    public static String IStoString(InputStream is, String charset) {
        if (is == null)
            return null;
        Scanner s = new Scanner(is, charset).useDelimiter("\\A");
        return s.hasNext() ? s.next() : null;
    }

    public Document asXML() {
        return null;
    }

    public Map<String, String[]> asForm() {
        return null;
    }

    public InputStream asPlain() {
        try {
            return request.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Map<String, Object> asMultipart() {
        return null;
    }

    public SOAPMessage asSOAP() {
        return null;
    }

    public SOAPMessage asSOAP11() {
        return null;
    }

    public SOAPMessage asSOAP12() {
        return null;
    }

    public static Map<String, Object> convertRequestToMap(
            HttpServletRequest request) throws IOException, ServletException {

        Map<String, Object> inParams = new HashMap<String, Object>();

        //GET
        String q = request.getQueryString();
        if (q != null && !q.isEmpty()) {
            String[] arr = q.split("&");
            for (String it : arr) {
                String nv[] = it.split("=");
                String n = nv[0];
                String v = null;
                if (nv.length > 1) {
                    v = nv[1];
                    try {
                        v = URLDecoder.decode(v, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        //throw S1SystemError.wrap(e);
                    }
                }
                /*if(PARAMS_PARAMETER.equals(n)){
                    inParams.putAll(Objects.fromWire(JSONFormat.evalJSON(v)));
                }else{*/
                inParams.put(n, v);
                //}
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
                String v = "" + e.getValue()[0];
                inParams.put(e.getKey(), v);
            }
        } else if (request.getContentType() != null
                && request.getContentType().contains("multipart/form-data")
                ) {
            //parts
            for (Part p : request.getParts()) {
                if (p.getContentType() != null) {
                    if (p.getSize() == 0)
                        continue;
                    String name = p.getName();
                    //file
                    for (String content : p.getHeader("content-disposition").split(";")) {
                        if (content.trim().startsWith("filename")) {
                            name = content.substring(
                                    content.indexOf('=') + 1).trim().replace("\"", "");
                        }
                    }
                    String ext = "";
                    int ei = name.lastIndexOf(".");
                    if (ei != -1 && ei < name.length() - 1) {
                        ext = name.substring(ei + 1);
                        name = name.substring(0, ei);
                    }

                    inParams.put(p.getName(), new FileParameter(p.getInputStream(), name, ext, p.getContentType(), p.getSize()));
                } else {
                    //param
                    inParams.put(p.getName(), IStoString(p.getInputStream(), "UTF-8"));
                }
            }
        }
        return inParams;
    }

    /**
     * Bean for file request parameter
     */
    public static class FileParameter {
        private InputStream inputStream;
        private String name;
        private String ext;
        private String contentType;
        private long size;

        public FileParameter(InputStream inputStream, String name, String ext, String contentType, long size) {
            this.inputStream = inputStream;
            this.name = name;
            this.ext = ext;
            this.contentType = contentType;
            this.size = size;
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public String getName() {
            return name;
        }

        public String getExt() {
            return ext;
        }

        public String getContentType() {
            return contentType;
        }

        public long getSize() {
            return size;
        }

        public String toString() {
            return "FileParameter {name: " + name + ", ext: " + ext + ", contentType: " + contentType + ", size: " + size + "}";
        }
    }
}
