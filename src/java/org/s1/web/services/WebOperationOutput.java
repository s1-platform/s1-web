package org.s1.web.services;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Web Operation output
 *
 * @author Grigory Pykhov
 */
public abstract class WebOperationOutput {

    /**
     * Render data to response according to format
     *
     * @param response Response
     * @throws IOException
     */
    public abstract void render(HttpServletResponse response) throws IOException;
}
