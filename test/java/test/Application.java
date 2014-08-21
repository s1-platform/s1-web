package test;

import org.s1.web.ApplicationFilter;
import org.s1.web.services.ServiceDispatcherServlet;
import org.s1.web.services.WebOperation;

/**
 * @author Grigory Pykhov
 */
public class Application extends ApplicationFilter {

    @Override
    protected void startAndConfigure() {
        ServiceDispatcherServlet.getOperations()
                .add("test1",new Test1Service());
    }

    @Override
    protected void releaseAndStop() {

    }
}
