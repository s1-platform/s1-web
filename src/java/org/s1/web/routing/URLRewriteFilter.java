package org.s1.web.routing;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filter rewrites URLs with regular expressions.
 * <br>
 * Add this to web.xml:
 * <pre><code>
 * &lt;filter&gt;
 *  &lt;filter-name&gt;router&lt;/filter-name&gt;
 *  &lt;filter-class&gt;org.s1.web.router.URLRewriteFilter&lt;/filter-class&gt;
 * &lt;/filter&gt;
 * &lt;filter-mapping&gt;
 *  &lt;filter-name&gt;router&lt;/filter-name&gt;
 *  &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 * &lt;/filter-mapping&gt;
 * </code></pre>
 * Add new rules in {@link org.s1.web.ApplicationFilter}: <code>URLRewriteFilter.getMapping().add("test/(.+?)/(.+?)","test.gsp?id=$1&amp;p=$2")</code>
 *
 * @author Grigory Pykhov
 */
public class URLRewriteFilter implements Filter {

    private static final Mapping mapping = new Mapping();

    /**
     * @return Mapping
     */
    public static Mapping getMapping() {
        return mapping;
    }

    /**
     * Mapping
     */
    public static class Mapping {
        private Mapping() {
        }

        private Map<Pattern, String> routes = new HashMap<Pattern, String>();

        /**
         * @param regex  Regex
         * @param target Replace with
         * @return Mapping
         */
        public Mapping add(String regex, String target) {
            routes.put(Pattern.compile(regex), target);
            return this;
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        String url = request.getRequestURI().substring(request.getContextPath().length());
        if (request.getQueryString() != null && !request.getQueryString().isEmpty())
            url += "?" + request.getQueryString();
        boolean b = false;
        for (Pattern p : mapping.routes.keySet()) {
            Matcher m = p.matcher(url);
            if (m.find()) {
                url = m.replaceAll(mapping.routes.get(p));
                b = true;
                break;
            }
        }
        if (b) {
            RequestDispatcher dispatcher = request.getRequestDispatcher(url);
            dispatcher.forward(req, resp);
        } else {
            chain.doFilter(req, resp);
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }
}
