package org.s1.web.services.formats;

import org.s1.web.services.WebOperationOutput;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JSONP response
 *
 * @author Grigory Pykhov
 */
public class JSONPData extends WebOperationOutput {

    private JSONData data;
    private String function;

    /**
     *
     * @param data JSON
     * @param function Function name
     */
    public JSONPData(JSONData data, String function) {
        this.data = data;
        this.function = function;
    }

    /**
     *
     * @return JSON
     */
    public JSONData getData() {
        return data;
    }

    /**
     *
     * @return Function name
     */
    public String getFunction() {
        return function;
    }

    @Override
    public void render(HttpServletResponse response) throws IOException {
        response.setCharacterEncoding(data.getEncoding());
        response.setContentType("text/javascript");
        response.getOutputStream().write((function+"(").getBytes(data.getEncoding()));
        response.getOutputStream().write((data.toJSON()).getBytes(data.getEncoding()));
        response.getOutputStream().write((")").getBytes(data.getEncoding()));
    }
}
