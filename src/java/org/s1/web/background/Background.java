package org.s1.web.background;

import it.sauronsoftware.cron4j.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Background processes manager.
 * <br>
 * Add your processes using <code>Background.getHolder().add("my-process-name", "5 * * * *", myProcess);</code>
 * <br>
 * Start all process with <code>Background.getHolder().startAll();</code>
 * <br>
 * Stop all process with <code>Background.getHolder().stopAll();</code>
 *
 * @author Grigory Pykhov
 */
public class Background {

    private static final Logger LOG = LoggerFactory.getLogger(Background.class);

    /**
     * Instantiation restricted
     */
    private Background() {
    }

    private static final ProcessHolder holder = new ProcessHolder();
    private static Scheduler scheduler = new Scheduler();

    public static class ProcessHolder {
        private ProcessHolder() {
        }

        private final Map<String, String> processes = new ConcurrentHashMap<String, String>();

        /**
         * Add new process
         *
         * @param name    Process name
         * @param cron    Cron4j config string (see http://www.sauronsoftware.it/projects/cron4j/manual.php)
         * @param process Process
         */
        public ProcessHolder addProcess(String name, String cron, Runnable process) {
            String id = scheduler.schedule(cron, process);
            processes.put(name, id);
            LOG.info("New background process registered: " + name + ", " + cron);
            return this;
        }

        /**
         * Start all processes
         */
        public ProcessHolder startAll() {
            scheduler.start();
            LOG.info("All background processes started");
            return this;
        }

        /**
         * Stop all processes
         */
        public ProcessHolder stopAll() {
            scheduler.stop();
            LOG.info("All background processes stopped");
            return this;
        }
    }

    /**
     * @return ProcessHolder
     */
    public static ProcessHolder getHolder() {
        return holder;
    }

}
