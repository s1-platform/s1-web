package org.s1.web.services.formats;

import org.s1.web.services.WebOperationOutput;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Raw data, use it if you work with HttpServletResponse#outputStream manually
 *
 * @author Grigory Pykhov
 */
public class RawData extends WebOperationOutput {

    @Override
    public void render(HttpServletResponse response) throws IOException {
        //do nothing, it is raw data
    }
}
