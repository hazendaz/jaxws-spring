/*
 * This project is dual-licensed under the terms of the Common Development and Distribution License (CDDL) v1.0 and the GNU General Public License (GPL) v2.0 with Classpath Exception.
 *
 * You may choose either license.
 *
 * See LICENSE-CDDL-1.0 and LICENSE-GPL-2.0-CE for full license texts.
 */
package com.revinate.ws.spring.internal;

import com.sun.xml.ws.transport.http.servlet.ServletAdapterList;
import com.sun.xml.ws.transport.http.servlet.WSServletDelegate;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * {@link HttpServlet} that uses Spring to obtain a configured server set up, then routes incoming requests to it. This
 * version of the class also works with environments where the web application context is injected rather than created
 * internally (e.g. Servlet 3.0+ environments, and in embedded servlet containers.)
 *
 * @author Kohsuke Kawaguchi
 * @author Alex Leigh
 */
public class WSSpringServlet extends HttpServlet implements ApplicationContextAware {

    private static final long serialVersionUID = -2786173009814679145L;

    private WSServletDelegate delegate;

    private WebApplicationContext webApplicationContext;

    private boolean webApplicationContextInjected = false;

    public WSSpringServlet() {
    }

    public WSSpringServlet(WebApplicationContext webApplicationContext) {
        this.webApplicationContext = webApplicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        if (this.webApplicationContext == null && applicationContext instanceof WebApplicationContext) {
            this.webApplicationContext = (WebApplicationContext) applicationContext;
            this.webApplicationContextInjected = true;
        }
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        // get the configured adapters from Spring
        this.webApplicationContext = initWebApplicationContext();

        Set<SpringBinding> bindings = new LinkedHashSet<>();

        bindings.addAll(this.webApplicationContext.getBeansOfType(SpringBinding.class).values());

        // create adapters
        ServletAdapterList l = new ServletAdapterList(getServletContext());
        for (SpringBinding binding : bindings) {
            binding.create(l);
        }

        delegate = new WSServletDelegate(l, getServletContext());
    }

    protected WebApplicationContext initWebApplicationContext() {
        if (this.webApplicationContext != null) {
            return this.webApplicationContext;
        } else {
            return WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        }
    }

    /**
     * destroys the servlet and releases all associated resources, such as the Spring application context and the JAX-WS
     * delegate.
     */
    @Override
    public void destroy() {
        if (this.webApplicationContext instanceof ConfigurableApplicationContext
                && !this.webApplicationContextInjected) {
            ((ConfigurableApplicationContext) this.webApplicationContext).close();
        }
        delegate.destroy();
        delegate = null;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        delegate.doPost(request, response, getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        delegate.doGet(request, response, getServletContext());
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        delegate.doPut(request, response, getServletContext());
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        delegate.doDelete(request, response, getServletContext());
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        delegate.doHead(request, response, getServletContext());
    }
}
