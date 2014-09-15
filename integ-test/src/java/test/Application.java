package test;

import org.s1.web.ApplicationFilter;
import org.s1.web.background.Background;
import org.s1.web.services.ServiceDispatcherServlet;

/**
 * @author Grigory Pykhov
 */
public class Application extends ApplicationFilter {

    @Override
    protected void startAndConfigure() {
        ServiceDispatcherServlet.getOperations()
                .add("test1",new Test1Service());
        Background.getHolder().addProcess("test","* * * * *",new Background1());
        Background.getHolder().startAll();
    }

    @Override
    protected void releaseAndStop() {
        Background.getHolder().stopAll();
    }
}
