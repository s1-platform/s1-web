package org.s1.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Base class for you application.
 * <br>
 * It covers lifecycle: start-and-configure, release-and-stop
 *
 * @author Grigory Pykhov
 */
public abstract class ApplicationFilter implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationFilter.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            startAndConfigure();
            LOG.info("S1-WEB APPLICATION STARTED");
        } catch (Throwable e) {
            LOG.error("S1-WEB APPLICATION FAILED TO START! Got exception " + e.getClass().getName() + ": " + e.getMessage() + "", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            releaseAndStop();
        } catch (Throwable e) {
            LOG.error("S1-WEB APPLICATION FAILED TO STOP PROPERLY! Got exception " + e.getClass().getName() + ": " + e.getMessage() + "", e);
        }
        LOG.info("S1-WEB APPLICATION STOPPED");
    }

    /**
     * Make all configurations and start all things in this method
     */
    protected abstract void startAndConfigure();

    /**
     * Release all resources and stop all running tasks in this method
     */
    protected abstract void releaseAndStop();

}
