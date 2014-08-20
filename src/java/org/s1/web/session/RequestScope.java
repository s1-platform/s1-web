package org.s1.web.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Request scope.
 * <br>
 * Stores request, and may store some params, cleans up after request finishes.
 *
 * @author Grigory Pykhov
 */
public class RequestScope {

    private static final Logger LOG = LoggerFactory.getLogger(RequestScope.class);

    private static ThreadLocal<Context> local = new ThreadLocal<Context>();

    private RequestScope() {
    }

    /**
     * Create scope
     *
     * @param context New context
     */
    public static void start(Context context) {
        local.set(context);
    }

    /**
     * Clean scope
     */
    public static void finish() {
        local.remove();
    }

    /**
     * Get context
     *
     * @return Context
     */
    public static Context get() {
        return local.get();
    }

    /**
     * Data containing in request scope
     */
    public static class Context {
        private final HttpServletRequest request;
        private final HttpServletResponse response;
        private final Map<String, Object> data = new HashMap<String, Object>();

        /**
         * @param request  Reqeust
         * @param response Response
         */
        public Context(HttpServletRequest request, HttpServletResponse response) {
            this.request = request;
            this.response = response;
        }

        /**
         * @param request  Request
         * @param response Response
         * @param data     Data
         */
        public Context(HttpServletRequest request, HttpServletResponse response, Map<String, Object> data) {
            this.request = request;
            this.response = response;
            this.data.putAll(data);
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
         * Get data
         *
         * @return Data
         */
        public Map<String, Object> getData() {
            return data;
        }

        /**
         * Get session parameter
         *
         * @param key Key
         * @param <T> Type
         * @return Value (or null if not found)
         */
        public <T> T get(String key) {
            return get(key, null);
        }

        /**
         * Get session parameter
         *
         * @param key Key
         * @param def Default (if !data.containsKey)
         * @param <T> Type
         * @return Value
         */
        public <T> T get(String key, T def) {
            if (!data.containsKey(key))
                return def;
            return (T) data.get(key);
        }

        /**
         * Set session parameter
         *
         * @param key Key
         * @param val Value
         * @return Context
         */
        public Context set(String key, Object val) {
            data.put(key, val);
            return this;
        }

    }

}
