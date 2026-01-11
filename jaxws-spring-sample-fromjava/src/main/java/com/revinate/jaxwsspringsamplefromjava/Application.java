/*
 * This project is dual-licensed under the terms of the Common Development and Distribution License (CDDL) v1.0 and the GNU General Public License (GPL) v2.0 with Classpath Exception.
 *
 * You may choose either license.
 *
 * See LICENSE-CDDL-1.0 and LICENSE-GPL-2.0-CE for full license texts.
 */
package com.revinate.jaxwsspringsamplefromjava;

import com.revinate.ws.spring.SpringService;
import com.revinate.ws.spring.internal.SpringBinding;
import com.revinate.ws.spring.internal.WSSpringServlet;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    @Autowired
    private FibonacciPortImpl fibonacciPortImpl;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    WSSpringServlet jaxwsServlet() {
        return new WSSpringServlet();
    }

    @Bean
    ServletRegistrationBean<WSSpringServlet> jaxwsServletRegistration() {
        ServletRegistrationBean<WSSpringServlet> bean = new ServletRegistrationBean<>(jaxwsServlet(), "/service/*");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    SpringService fibonacciService() throws IOException {
        SpringService service = new SpringService();
        service.setBean(fibonacciPortImpl);
        service.setServiceName(new QName("http://www.revinate.com/sample", "SampleService"));
        service.setPortName(new QName("http://www.revinate.com/sample", "FibonacciPort"));
        return service;
    }

    @Bean
    SpringBinding fibonacciBinding() throws Exception {
        SpringBinding binding = new SpringBinding();
        binding.setUrl("/service/fibonacci");
        binding.setService(fibonacciService().getObject());
        return binding;
    }
}
