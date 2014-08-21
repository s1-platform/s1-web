package org.s1.web.services.formats;

import org.apache.commons.io.IOUtils;
import org.s1.web.services.WebOperationOutput;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * Plain text data. Default encoding is UTF-8
 *
 * @author Grigory Pykhov
 */
public class TextData extends WebOperationOutput {

    private String text;
    private String encoding = "UTF-8";

    /**
     *
     * @param req Request
     * @throws IOException
     */
    public TextData(HttpServletRequest req) throws IOException {
        this.encoding = req.getCharacterEncoding();
        this.text = IOUtils.toString(req.getInputStream(),encoding);
    }

    /**
     *
     * @param text Text
     */
    public TextData(String text) {
        this.text = text;
    }

    /**
     *
     * @param text Text
     * @param encoding Encoding
     */
    public TextData(String text, String encoding) {
        this.text = text;
        this.encoding = encoding;
    }

    /**
     *
     * @return Text
     */
    public String getText() {
        return text;
    }

    /**
     *
     * @return Encoding
     */
    public String getEncoding() {
        return encoding;
    }

    @Override
    public void render(HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        response.setCharacterEncoding(encoding);
        response.getOutputStream().write(text.getBytes(Charset.forName(encoding)));
    }
}
