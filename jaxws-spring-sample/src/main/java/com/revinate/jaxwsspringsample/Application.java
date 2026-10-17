/*
 * This project is dual-licensed under the terms of the Common Development and Distribution License (CDDL) v1.0 and the GNU General Public License (GPL) v2.0 with Classpath Exception.
 *
 * You may choose either license.
 *
 * See LICENSE-CDDL-1.0 and LICENSE-GPL-2.0-CE for full license texts.
 */
package com.revinate.jaxwsspringsample;

import com.revinate.sample.service.FactorialPort;
import com.revinate.sample.service.FibonacciPort;
import com.revinate.ws.spring.SDDocumentCollector;
import com.revinate.ws.spring.SpringService;
import com.revinate.ws.spring.internal.SpringBinding;
import com.revinate.ws.spring.internal.WSSpringServlet;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import javax.xml.namespace.QName;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * The Class Application.
 */
@SpringBootApplication
public class Application {

    /** The Constant SAMPLESERVICE_METADATA. */
    private static final Collection<Object> SAMPLESERVICE_METADATA;

    /** The Constant SAMPLESERVICE_PRIMARY_WSDL. */
    private static final Object SAMPLESERVICE_PRIMARY_WSDL;

    static {
        ClassLoader cl = Application.class.getClassLoader();
        Map<URL, Object> docs = SDDocumentCollector.collectDocs("sample", cl);
        SAMPLESERVICE_METADATA = docs.values();
        SAMPLESERVICE_PRIMARY_WSDL = docs.get(cl.getResource("sample/wsdl/SampleService.wsdl"));
    }

    /** The fibonacci port. */
    @Autowired
    private FibonacciPort fibonacciPort;

    /** The factorial port. */
    @Autowired
    private FactorialPort factorialPort;

    /**
     * The main method.
     *
     * @param args
     *            the arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * Jaxws servlet.
     *
     * @return the WS spring servlet
     */
    @Bean
    WSSpringServlet jaxwsServlet() {
        return new WSSpringServlet();
    }

    /**
     * Jaxws servlet registration.
     *
     * @return the servlet registration bean
     */
    @Bean
    ServletRegistrationBean<WSSpringServlet> jaxwsServletRegistration() {
        ServletRegistrationBean<WSSpringServlet> bean = new ServletRegistrationBean<>(jaxwsServlet(), "/service/*");
        bean.setLoadOnStartup(1);
        return bean;
    }

    /**
     * Fibonacci service.
     *
     * @return the spring service
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Bean
    SpringService fibonacciService() throws IOException {
        SpringService service = new SpringService();
        service.setBean(fibonacciPort);
        service.setServiceName(new QName("http://www.revinate.com/sample", "SampleService"));
        service.setPortName(new QName("http://www.revinate.com/sample", "FibonacciPort"));
        service.setMetadata(SAMPLESERVICE_METADATA);
        service.setPrimaryWsdl(SAMPLESERVICE_PRIMARY_WSDL);
        return service;
    }

    /**
     * Factorial service.
     *
     * @return the spring service
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Bean
    SpringService factorialService() throws IOException {
        SpringService service = new SpringService();
        service.setBean(factorialPort);
        service.setServiceName(new QName("http://www.revinate.com/sample", "SampleService"));
        service.setPortName(new QName("http://www.revinate.com/sample", "FactorialPort"));
        service.setMetadata(SAMPLESERVICE_METADATA);
        service.setPrimaryWsdl(SAMPLESERVICE_PRIMARY_WSDL);
        return service;
    }

    /**
     * Fibonacci binding.
     *
     * @return the spring binding
     *
     * @throws Exception
     *             the exception
     */
    @Bean
    SpringBinding fibonacciBinding() throws Exception {
        SpringBinding binding = new SpringBinding();
        binding.setUrl("/service/fibonacci");
        binding.setService(fibonacciService().getObject());
        return binding;
    }

    /**
     * Factorial binding.
     *
     * @return the spring binding
     *
     * @throws Exception
     *             the exception
     */
    @Bean
    SpringBinding factorialBinding() throws Exception {
        SpringBinding binding = new SpringBinding();
        binding.setUrl("/service/factorial");
        binding.setService(factorialService().getObject());
        return binding;
    }
}
