package org.s1.web.services;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service dispatcher servlet.
 * <br>
 * Add it to web.xml:
 * <pre><code>
 * &lt;servlet&gt;
 *  &lt;servlet-name&gt;services&lt;/servlet-name&gt;
 *  &lt;servlet-class&gt;org.s1.web.services.ServiceDispatcherServlet&lt;/servlet-class&gt;
 *  &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
 * &lt;/servlet&gt;
 * &lt;servlet-mapping&gt;
 *  &lt;servlet-name&gt;services&lt;/servlet-name&gt;
 *  &lt;url-pattern&gt;/services/*&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;
 * </code></pre>
 * Add your WebOperations from {@link org.s1.web.ApplicationFilter} <code>ServiceDispatcherServlet.getOperations().add("MyOperation", myWebOperationImpl)</code>
 *
 * @author Grigory Pykhov
 */
public class ServiceDispatcherServlet extends HttpServlet {

    private static final Operations operations = new Operations();

    /**
     * @return Operations
     */
    public static Operations getOperations() {
        return operations;
    }

    /**
     * Operations holder
     */
    public static class Operations {
        private Operations() {
        }

        private final Map<String, WebOperation> list = new ConcurrentHashMap<String, WebOperation>();

        /**
         * Add operation
         *
         * @param name      Operation name (operation will be accessible with /services/OperationName)
         * @param operation WebOperation instance
         * @return Operations
         */
        public Operations add(String name, WebOperation operation) {
            list.put(name, operation);
            return this;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {
        process(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
        process(request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {
        process(request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request,
                            HttpServletResponse response) throws ServletException, IOException {
        process(request, response);
    }

    @Override
    protected void doHead(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
        process(request, response);
    }

    @Override
    protected void doOptions(HttpServletRequest request,
                             HttpServletResponse response) throws ServletException, IOException {
        process(request, response);
    }

    @Override
    protected void doTrace(HttpServletRequest request,
                           HttpServletResponse response) throws ServletException, IOException {
        process(request, response);
    }

    /**
     * Get operation name from request
     *
     * @param request Request
     * @return Web operation name
     */
    protected String getWebOperationNameFromRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String q = uri.substring((request.getContextPath() + request.getServletPath()).length() + 1);
        return q;
    }

    /**
     * Get operation from holder
     *
     * @param name Name
     * @return Operation
     */
    protected WebOperation getOperationByName(String name) {
        return operations.list.get(name);
    }

    /**
     * Process request
     *
     * @param request  Request
     * @param response Response
     * @throws ServletException ServletException
     * @throws IOException IOException
     */
    protected void process(HttpServletRequest request,
                           HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        WebOperation op = getOperationByName(getWebOperationNameFromRequest(request));
        if (op == null) {
            response.setStatus(404);
            return;
        }
        op.request(request, response);
    }

}
