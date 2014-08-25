/*
 * Copyright 2014 Grigory Pykhov
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.s1.web;

import javax.servlet.*;
import java.io.IOException;

/**
 * Web charset encoding filter
 * <br>
 * UTF-8 Example:
 * <pre><code>
 * &lt;filter&gt;
 *  &lt;filter-name&gt;CharsetEncodingFilter&lt;/filter-name&gt;
 *  &lt;filter-class&gt;org.s1.misc.CharsetEncodingFilter&lt;/filter-class&gt;
 * &lt;/filter&gt;
 * &lt;filter-mapping&gt;
 *  &lt;filter-name&gt;CharsetEncodingFilter&lt;/filter-name&gt;
 *  &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 * &lt;/filter-mapping&gt;
 * </code></pre>
 * <br>
 * Or you can define encoding manually:
 * <pre><code>
 * &lt;filter&gt;
 *  &lt;filter-name&gt;CharsetEncodingFilter&lt;/filter-name&gt;
 *  &lt;filter-class&gt;org.s1.misc.CharsetEncodingFilter&lt;/filter-class&gt;
 *  &lt;init-param&gt;
 *      &lt;param-name&gt;encoding&lt;/param-name&gt;
 *      &lt;param-value&gt;UTF-8&lt;/param-value&gt;
 *  &lt;/init-param&gt;
 * &lt;/filter&gt;
 * &lt;filter-mapping&gt;
 *  &lt;filter-name&gt;CharsetEncodingFilter&lt;/filter-name&gt;
 *  &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 * &lt;/filter-mapping&gt;
 * </code></pre>
 *
 * @author Grigory Pykhov
 */
public class CharsetEncodingFilter implements Filter {

    private String encoding = "UTF-8";

    public void doFilter(ServletRequest request,

                         ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        request.setCharacterEncoding(encoding);
        filterChain.doFilter(request, response);
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        String encodingParam = filterConfig.getInitParameter("encoding");
        if (encodingParam != null && !encodingParam.isEmpty()) {
            encoding = encodingParam;
        }
    }

    public void destroy() {
        // nothing to do
    }
}
