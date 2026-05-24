/*
 * This project is dual-licensed under the terms of the Common Development and Distribution License (CDDL) v1.0 and the GNU General Public License (GPL) v2.0 with Classpath Exception.
 *
 * You may choose either license.
 *
 * See LICENSE-CDDL-1.0 and LICENSE-GPL-2.0-CE for full license texts.
 */
package com.revinate.jaxwsspringsample;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.revinate.sample.service.FactorialPort;
import com.revinate.sample.service.FibonacciPort;
import com.revinate.ws.spring.SpringService;
import com.revinate.ws.spring.internal.SpringBinding;
import com.revinate.ws.spring.internal.WSSpringServlet;
import com.sun.xml.ws.api.server.WSEndpoint;

import java.lang.reflect.Field;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.ServletRegistrationBean;

/**
 * Tests for {@link Application}.
 */
public class ApplicationTest {

    @Test
    void jaxwsServletBeansAreConfigured() {
        Application application = new Application();

        WSSpringServlet servlet = application.jaxwsServlet();
        ServletRegistrationBean<WSSpringServlet> registration = application.jaxwsServletRegistration();

        assertNotNull(servlet);
        assertNotNull(registration.getServlet());
        assertEquals(1, registration.getLoadOnStartup());
        assertEquals("/service/*", registration.getUrlMappings().iterator().next());
    }

    @Test
    void serviceBeansPopulateNamesAndWsdlMetadata() throws Exception {
        Application application = new Application();
        setField(application, "fibonacciPort", (FibonacciPort) index -> index);
        setField(application, "factorialPort", (FactorialPort) number -> number);

        SpringService fibonacciService = application.fibonacciService();
        SpringService factorialService = application.factorialService();

        assertEquals(new QName("http://www.revinate.com/sample", "SampleService"),
                readSpringServiceField(fibonacciService, "serviceName"));
        assertEquals(new QName("http://www.revinate.com/sample", "FibonacciPort"),
                readSpringServiceField(fibonacciService, "portName"));
        assertNotNull(readSpringServiceField(fibonacciService, "primaryWSDLResource"));
        assertNotNull(readSpringServiceField(fibonacciService, "metadataResources"));

        assertEquals(new QName("http://www.revinate.com/sample", "SampleService"),
                readSpringServiceField(factorialService, "serviceName"));
        assertEquals(new QName("http://www.revinate.com/sample", "FactorialPort"),
                readSpringServiceField(factorialService, "portName"));
        assertNotNull(readSpringServiceField(factorialService, "primaryWSDLResource"));
        assertNotNull(readSpringServiceField(factorialService, "metadataResources"));
    }

    @Test
    void bindingBeansUseExpectedUrls() throws Exception {
        TestableApplication application = new TestableApplication();

        SpringBinding fibonacciBinding = application.fibonacciBinding();
        SpringBinding factorialBinding = application.factorialBinding();

        assertEquals("/service/fibonacci", readField(fibonacciBinding, "urlPattern"));
        assertEquals("/service/factorial", readField(factorialBinding, "urlPattern"));
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field field = Application.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Object readSpringServiceField(SpringService service, String name) throws Exception {
        Field field = SpringService.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(service);
    }

    private Object readField(SpringBinding binding, String name) throws Exception {
        Field field = SpringBinding.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(binding);
    }

    private static final class TestableApplication extends Application {
        @Override
        SpringService fibonacciService() {
            return new StubSpringService();
        }

        @Override
        SpringService factorialService() {
            return new StubSpringService();
        }
    }

    private static final class StubSpringService extends SpringService {
        @Override
        public WSEndpoint<?> getObject() {
            return null;
        }
    }
}
