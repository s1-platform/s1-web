package org.s1.web.services.formats;

import org.apache.commons.io.IOUtils;
import org.s1.web.services.WebOperationOutput;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * Wrapper for multipart format.
 *
 * @author Grigory Pykhov
 */
public class MultipartFormData extends WebOperationOutput {

    private Map<String, List<String>> params = new HashMap<String, List<String>>();
    private String encoding = "UTF-8";
    private String contentType = "multipart/form-data";
    private Map<String, List<FileParameter>> files = new HashMap<String, List<FileParameter>>();

    /**
     * @param contentType Content type
     */
    public MultipartFormData(String contentType) {
        this.contentType = contentType;
    }

    /**
     * @param files       Files
     * @param contentType Content type
     */
    public MultipartFormData(Map<String, List<FileParameter>> files, String contentType) {
        this.files.putAll(files);
        this.contentType = contentType;
    }

    /**
     * @param params      Params
     * @param files       Files
     * @param contentType Content type
     */
    public MultipartFormData(Map<String, List<String>> params, Map<String, List<FileParameter>> files, String contentType) {
        if (params != null)
            this.params.putAll(params);
        if (files != null)
            this.files.putAll(files);
        this.contentType = contentType;
    }

    /**
     * @param params      Params
     * @param files       Files
     * @param encoding    Encoding for params
     * @param contentType Content type
     */
    public MultipartFormData(Map<String, List<String>> params, Map<String, List<FileParameter>> files, String encoding, String contentType) {
        if (params != null)
            this.params.putAll(params);
        if (files != null)
            this.files.putAll(files);
        this.encoding = encoding;
        this.contentType = contentType;
    }

    /**
     * @param request Request
     * @throws IOException
     * @throws ServletException
     */
    public MultipartFormData(HttpServletRequest request) throws IOException, ServletException {
        this.encoding = request.getCharacterEncoding();

        if (request.getContentType() != null
                && request.getContentType().contains("multipart/")
                ) {
            this.contentType = request.getContentType().split(";")[0].trim();
            //parts
            for (Part p : request.getParts()) {
                String n = p.getName();
                if (n == null || n.isEmpty())
                    continue;
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

                    if (!files.containsKey(n))
                        files.put(n, new ArrayList<FileParameter>());
                    List<FileParameter> list = files.get(n);
                    list.add(new FileParameter(p.getInputStream(), name, ext, p.getContentType(), p.getSize()));
                } else {
                    //param
                    if (!params.containsKey(n))
                        params.put(n, new ArrayList<String>());
                    List<String> list = params.get(n);
                    list.add(IOUtils.toString(p.getInputStream(), encoding));
                }
            }
        }
    }

    /**
     * @return String params
     */
    public Map<String, List<String>> getParams() {
        return params;
    }

    /**
     * @return Encoding for string params
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * @return File params
     */
    public Map<String, List<FileParameter>> getFiles() {
        return files;
    }

    /**
     * @return ContentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @param t     Type
     * @param param Param name
     * @param <T>   Type
     * @return Casted value
     */
    public <T> T getFirst(Class<T> t, String param) {
        if (!params.containsKey(param))
            params.put(param, new ArrayList<String>());
        return ObjectTypes.cast(params.get(param).get(0), t);
    }

    /**
     * @param param Param name
     * @return Value
     */
    public String getFirst(String param) {
        if (!params.containsKey(param))
            params.put(param, new ArrayList<String>());
        return params.get(param).get(0);
    }

    /**
     * @param param Param name
     * @param value Value
     * @return MultipartFormData
     */
    public MultipartFormData set(String param, String value) {
        if (!params.containsKey(param))
            params.put(param, new ArrayList<String>());
        List<String> list = params.get(param);
        list.add(value);
        return this;
    }

    /**
     * @param file File name
     * @return File
     */
    public FileParameter getFirstFile(String file) {
        if (!files.containsKey(file))
            files.put(file, new ArrayList<FileParameter>());
        return files.get(file).get(0);
    }

    /**
     * @param file  File name
     * @param value Value
     * @return MultipartFormData
     */
    public MultipartFormData set(String file, FileParameter value) {
        if (!files.containsKey(file))
            files.put(file, new ArrayList<FileParameter>());
        List<FileParameter> list = files.get(file);
        list.add(value);
        return this;
    }

    @Override
    public void render(HttpServletResponse response) throws IOException {
        final String BOUNDARY = "----" + UUID.randomUUID().toString().replace("-", "");
        response.setContentType(contentType + "; boundary=" + BOUNDARY);
        OutputStream os = response.getOutputStream();
        for (String key : params.keySet()) {
            for (String val : params.get(key)) {
                os.write(BOUNDARY.getBytes(encoding));
                os.write(("Content-Disposition: form-data; name=\"" + key + "\"\n").getBytes(encoding));
                os.write(("\n").getBytes(encoding));
                os.write((val).getBytes(encoding));
                os.write(("\n").getBytes(encoding));
            }
        }

        for (String key : files.keySet()) {
            for (FileParameter val : files.get(key)) {
                String name = val.getName();
                if (val.getExt() != null && !val.getExt().isEmpty())
                    name += "." + val.getExt();
                os.write(("--").getBytes(encoding));
                os.write(BOUNDARY.getBytes(encoding));
                os.write(("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + name + "\"\n").getBytes(encoding));
                os.write(("Content-Type: " + val.getContentType() + "\n").getBytes(encoding));
                os.write(("\n").getBytes(encoding));
                copy(val.getInputStream(), os);
                os.write(("\n").getBytes(encoding));
            }
        }
        os.write(("--").getBytes(encoding));
        os.write(BOUNDARY.getBytes(encoding));
        os.write(("--").getBytes(encoding));
    }

    private long copy(InputStream is, OutputStream os) throws IOException {
        byte buffer[] = new byte[4096];
        long count = 0;
        int n = 0;
        while (-1 != (n = is.read(buffer))) {
            os.write(buffer, 0, n);
            count += n;
        }
        return count;
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

        /**
         * @param inputStream Input stream
         * @param name        File name
         * @param ext         File extension
         * @param contentType Content type
         * @param size        Size
         */
        public FileParameter(InputStream inputStream, String name, String ext, String contentType, long size) {
            this.inputStream = inputStream;
            this.name = name;
            this.ext = ext;
            this.contentType = contentType;
            this.size = size;
        }

        /**
         * @return Input stream
         */
        public InputStream getInputStream() {
            return inputStream;
        }

        /**
         * @return File name
         */
        public String getName() {
            return name;
        }

        /**
         * @return File extension
         */
        public String getExt() {
            return ext;
        }

        /**
         * @return ContentType
         */
        public String getContentType() {
            return contentType;
        }

        /**
         * @return Size
         */
        public long getSize() {
            return size;
        }

        @Override
        public String toString() {
            return "FileParameter {name: " + name + ", ext: " + ext + ", contentType: " + contentType + ", size: " + size + "}";
        }
    }
}
