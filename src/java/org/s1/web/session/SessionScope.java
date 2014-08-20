package org.s1.web.session;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Session level scope.
 * <br>
 * Scales horizontally with Hazelcast (http://hazelcast.org)
 * <br>
 * Should be initialized with Hazelcast instance (make it in {@link org.s1.web.ApplicationFilter}).
 *
 * @author Grigory Pykhov
 */
public class SessionScope {

    private static final Logger LOG = LoggerFactory.getLogger(SessionScope.class);

    private SessionScope() {
    }

    /**
     * Hazelcast IMap name
     */
    public static final String SESSION_MAP = "S1-Web::SessionMap";

    /**
     * Cookie name for storing session id
     */
    public static final String COOKIE = "S1_ID";

    private static IMap<String, Context> sessionMap;
    private static final ThreadLocal<String> idLocal = new ThreadLocal<String>();
    private static HazelcastInstance hazelcastInstance;
    private static long TTL = 0;

    /**
     * Initialize Session scope, call it from {@link org.s1.web.ApplicationFilter}
     *
     * @param hazelcastInstance Hazelcast instance
     * @param TTL               Max time while session can stay inactive (lastUsed in Context)
     */
    public static void initialize(HazelcastInstance hazelcastInstance, long TTL) {
        SessionScope.hazelcastInstance = hazelcastInstance;
        if (TTL < 0) {
            TTL = 0;
        }
        SessionScope.TTL = TTL;
    }

    /**
     * Reset and clean all scopes
     */
    public static void reset() {
        synchronized (SessionScope.class) {
            sessionMap = null;
        }
    }

    /**
     * Start new session scope (if already in scope - NOP)
     *
     * @param id Session id
     * @return Same id if new session scope started or null if already in session scope
     */
    public static String start(String id) {
        if (idLocal.get() != null) {
            return null;
        } else {
            MDC.put("sessionId", id);
            idLocal.set(id);
            return id;
        }
    }

    /**
     * Finish session scope
     *
     * @param id Session id (if null or empty - NOP)
     */
    public static void finish(String id) {
        if (id != null && !id.isEmpty()) {
            MDC.remove("sessionId");
            idLocal.remove();
        }
    }

    /**
     * Get current context
     *
     * @return Context (null if not in session scope)
     */
    public static Context getContext() {
        String id = idLocal.get();
        if (id == null)
            return null;
        Context s = getSessionMap().get(id);

        //check TTL
        if (s != null && (System.currentTimeMillis() - s.lastUsed) > TTL) {
            if (LOG.isDebugEnabled())
                LOG.debug("Discarding session " + id + " with lifetime=" + (System.currentTimeMillis() - s.lastUsed) + "ms (TTL is " + TTL + "ms)");
            s = null;
        }

        //new session
        if (s == null)
            s = new Context();

        s.lastUsed = System.currentTimeMillis();
        updateContext(s);
        s.id = id;
        return s;
    }

    /**
     * Retrieves session id from request, if it is empty - puts it back to response
     *
     * @param req  Request
     * @param resp Response
     * @return Session id
     */
    public static String retrieveSessionIdFromRequest(HttpServletRequest req, HttpServletResponse resp) {
        String id = null;
        if (req.getCookies() != null) {
            for (Cookie it : req.getCookies()) {
                if (COOKIE.equals(it.getName()))
                    id = it.getValue();
            }
        }
        if (id == null) {
            id = UUID.randomUUID().toString();
            Cookie cookie = new Cookie(COOKIE, id);
            cookie.setPath("/");
            resp.addCookie(cookie);
        }
        return id;
    }

    private static HazelcastInstance getHazelcastInstance() {
        if (hazelcastInstance == null) {
            throw new IllegalStateException("You`ve forget to call SessionScope#initialize(), hazelcastInstance is null");
        }
        return hazelcastInstance;
    }

    private static IMap<String, Context> getSessionMap() {
        if (sessionMap == null) {
            synchronized (SessionScope.class) {
                if (sessionMap == null) {
                    sessionMap = getHazelcastInstance().getMap(SESSION_MAP);
                }
            }
        }
        return sessionMap;
    }

    private static void updateContext(Context s) {
        String id = idLocal.get();
        if (id == null)
            return;
        getSessionMap().put(id, s);
    }

    /**
     * Session
     */
    public static class Context implements Serializable {
        private String id;
        private long created = System.currentTimeMillis();
        private long lastUsed = System.currentTimeMillis();
        private final Map<String, Object> data = new HashMap<String, Object>();

        /**
         * Session id
         *
         * @return
         */
        public String getId() {
            return id;
        }

        /**
         * Create timestamp
         *
         * @return
         */
        public long getCreated() {
            return created;
        }

        /**
         * Last used timestamp
         *
         * @return
         */
        public long getLastUsed() {
            return lastUsed;
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
            updateContext(this);
            return this;
        }

    }

}
