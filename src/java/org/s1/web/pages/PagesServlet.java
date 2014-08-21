package org.s1.web.pages;

import groovy.text.GStringTemplateEngine;
import groovy.text.Template;
import org.s1.web.services.WebOperationInput;
import org.s1.web.session.RequestScope;
import org.s1.web.session.SessionScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serving GSP pages.
 * <pre><code>
 * &lt;servlet>
 *  &lt;servlet-name>pages&lt;/servlet-name>
 *  &lt;servlet-class>org.s1.web.pages.PagesServlet&lt;/servlet-class>
 *  &lt;load-on-startup>1&lt;/load-on-startup>
 * &lt;/servlet>
 * &lt;servlet-mapping>
 *  &lt;servlet-name>pages&lt;/servlet-name>
 *  &lt;url-pattern>*.gsp&lt;/url-pattern>
 * &lt;/servlet-mapping&gt;
 * </code></pre>
 * <br>
 * Your pages should be written with Groovy templates (http://groovy.codehaus.org/Groovy+Templates).
 * <br>
 * Notice: <code>${...}</code> code style is disabled (it may conflicts with some Javascript libraries).
 *
 * @author Grigory Pykhov
 */
public class PagesServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(PagesServlet.class);

    private static Map<String, TemplateCacheRecord> templateCache = new ConcurrentHashMap<String, TemplateCacheRecord>();

    /**
     * Layout content variable
     */
    private static final String LAYOUT_CONTENT_VARIABLE = "$layout_context";

    private static final String CURRENT_PATH = "pages:currentPath";
    private static final String DIRECTORY = "pages:currentPath";
    private static final String PARAMS = "pages:currentPath";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    /**
     * Process request
     *
     * @param req  Request
     * @param resp Response
     * @throws ServletException
     * @throws IOException
     */
    protected void process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = null;
        try {
            id = SessionScope.retrieveSessionIdFromRequest(req, resp);
            SessionScope.start(id);
            RequestScope.start(new RequestScope.Context(req, resp));

            String path = req.getServletContext().getRealPath("/");
            if (path.startsWith("WEB-INF")) {
                resp.setStatus(403);
                return;
            }
            String page = req.getRequestURI().substring(req.getContextPath().length());
            resp.setContentType("text/html");
            resp.setCharacterEncoding("UTF-8");
            String res = "";
            res = render("", path, page, new WebOperationInput(req, resp), new HashMap<String, Object>(), req, resp);
            resp.getOutputStream().write(res.getBytes(Charset.forName("UTF-8")));
        } finally {
            RequestScope.finish();
            SessionScope.finish(id);
        }
    }

    private static class TemplateCacheRecord {
        private Template template;
        private long updateTime;
        private long size;

        public TemplateCacheRecord(Template template, long updateTime, long size) {
            this.template = template;
            this.updateTime = updateTime;
            this.size = size;
        }

    }

    /**
     * Renders page
     *
     * @param currentFileDirectoryPath Current directory path
     * @param webappDirectoryPath      Webapp root path
     * @param pagePath                 Requested page path
     * @param params                   Request params
     * @param pageArguments            Included page arguments (or for Layout page)
     * @param req                      Request
     * @param resp                     Response
     * @return Rendered page
     */
    protected static String render(final String currentFileDirectoryPath, final String webappDirectoryPath, final String pagePath,
                                   final WebOperationInput params, final Map<String, Object> pageArguments,
                                   final HttpServletRequest req, final HttpServletResponse resp) {
        String pageRealPath = "";
        if (pagePath.startsWith("/")) {
            //absolute
            pageRealPath = webappDirectoryPath + "/" + pagePath;
        } else {
            //relative
            pageRealPath = currentFileDirectoryPath + "/" + pagePath;
        }
        pageRealPath = pageRealPath.replaceAll("\\\\", "/").replaceAll("/+", "/").replace("/", File.separator);
        try {


            final String currentPath = pageRealPath.substring(0, pageRealPath.lastIndexOf(File.separator));

            //LOG.debug("Rendering page: " + page);
            Path p1 = FileSystems.getDefault().getPath(pageRealPath);
            if (templateCache.containsKey(pageRealPath)) {
                if (templateCache.get(pageRealPath).updateTime != Files.getLastModifiedTime(p1).toMillis()
                        || templateCache.get(pageRealPath).size != Files.size(p1)) {
                    templateCache.remove(pageRealPath);
                }
            }
            if (!templateCache.containsKey(pageRealPath)) {
                GStringTemplateEngine engine = new GStringTemplateEngine();
                String temp = new String(Files.readAllBytes(p1), StandardCharsets.UTF_8);
                temp = temp.replace("$", "\\$");
                Template template = engine.createTemplate(
                        temp);
                templateCache.put(pageRealPath, new TemplateCacheRecord(template, Files.getLastModifiedTime(p1).toMillis(),
                        Files.size(p1)));
            }
            Template template = templateCache.get(pageRealPath).template;

            if (template == null) {
                resp.setStatus(404);
                LOG.warn("Page not found: " + pageRealPath);
                return "";
            }

            Map<String, Object> ctx = new HashMap<String, Object>();
            PageContext pageContext = new PageContext(req, resp, params, currentPath, webappDirectoryPath);
            ctx.put("page", pageContext);
            ctx.put("args", pageArguments);

            String text = template.make(ctx).toString();

            if (pageContext.layoutPath != null && !pageContext.layoutPath.isEmpty()) {
                pageContext.layoutArgs.put(LAYOUT_CONTENT_VARIABLE, text);
                text = render(currentPath, webappDirectoryPath, pageContext.layoutPath, params, pageContext.layoutArgs, req, resp);
            }
            return text;
        } catch (Throwable e) {
            LOG.warn("Layout page: " + pageRealPath + " error " + e.getClass().getName() + ": " + e.getMessage(), e);
            return "";
        }
    }

    /**
     * Page context
     */
    public static class PageContext {
        private HttpServletRequest request;
        private HttpServletResponse response;
        private WebOperationInput params;
        private String currentPath;
        private String directory;

        /**
         * @param request  Request
         * @param response Response
         * @param params   Request params
         * @param currentPath Current directory path
         * @param directory Webapp root directory
         */
        public PageContext(HttpServletRequest request, HttpServletResponse response, WebOperationInput params, String currentPath, String directory) {
            this.request = request;
            this.response = response;
            this.params = params;
            this.currentPath = currentPath;
            this.directory = directory;
        }

        private String layoutPath;
        private Map<String, Object> layoutArgs = new HashMap<String, Object>();

        /**
         * Do layout
         *
         * @param path Path to layout page
         */
        public void layout(String path) {
            this.layoutPath = path;
        }

        /**
         * Do layout
         *
         * @param path Path to layout page
         * @param args Layout page arguments
         */
        public void layout(String path, Map<String, Object> args) {
            this.layoutPath = path;
            this.layoutArgs = args;
        }

        /**
         * Includes page
         *
         * @param pagePath Path to page
         * @param args     Page arguments
         * @return Rendered page
         */
        public String include(String pagePath, Map<String, Object> args) {
            String t = render(currentPath, directory, pagePath, params, args, request, response);
            return t;
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

        /**
         * @return Params
         */
        public WebOperationInput getParams() {
            return params;
        }
    }

}
