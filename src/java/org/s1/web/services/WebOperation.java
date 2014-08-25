package org.s1.web.services;

import org.s1.web.session.RequestScope;
import org.s1.web.session.SessionScope;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Base class for web operations.
 *
 * @author Grigory Pykhov
 */
public abstract class WebOperation {

    /**
     * Process request
     *
     * @param request  Request
     * @param response Response
     * @throws ServletException ServletException
     * @throws IOException IOException
     */
    public void request(HttpServletRequest request,
                        HttpServletResponse response) throws ServletException, IOException {
        WebOperationInput input = new WebOperationInput(request, response);
        String id = null;
        try {
            id = SessionScope.start(getSessionId(input));
            RequestScope.start(new RequestScope.Context(request, response));

            WebOperationOutput output = process(input);
            output.render(response);

        } finally {
            SessionScope.finish(id);
            RequestScope.finish();
        }
    }

    /**
     * Get session id from request
     *
     * @param input Input data
     * @return Session id
     */
    protected String getSessionId(WebOperationInput input) {
        return SessionScope.retrieveSessionIdFromRequest(input.getRequest(), input.getResponse());
    }

    /**
     * Process web operation business logic
     *
     * @param input Input data
     * @return Output data
     */
    protected abstract WebOperationOutput process(WebOperationInput input);

}
