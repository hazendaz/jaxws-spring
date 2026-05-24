/*
 * This project is dual-licensed under the terms of the Common Development and Distribution License (CDDL) v1.0 and the GNU General Public License (GPL) v2.0 with Classpath Exception.
 *
 * You may choose either license.
 *
 * See LICENSE-CDDL-1.0 and LICENSE-GPL-2.0-CE for full license texts.
 */
package com.revinate.ws.spring.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.xml.ws.transport.http.servlet.WSServletDelegate;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

/**
 * Tests for {@link WSSpringServlet}.
 */
public class WSSpringServletTest {

    @Test
    void setApplicationContextInjectsOnlyWebApplicationContext() throws Exception {
        WSSpringServlet servlet = new WSSpringServlet();

        servlet.setApplicationContext(new GenericApplicationContext());
        assertNull(readField(servlet, "webApplicationContext"));

        GenericWebApplicationContext webContext = new GenericWebApplicationContext();
        servlet.setApplicationContext(webContext);

        assertSame(webContext, readField(servlet, "webApplicationContext"));
        assertEquals(true, readField(servlet, "webApplicationContextInjected"));
    }

    @Test
    void initWebApplicationContextReturnsExistingContext() {
        GenericWebApplicationContext webContext = new GenericWebApplicationContext();
        WSSpringServlet servlet = new WSSpringServlet(webContext);

        assertSame(webContext, servlet.initWebApplicationContext());
    }

    @Test
    void initResolvesContextFromServletContextAndCreatesDelegate() throws Exception {
        GenericWebApplicationContext webContext = new GenericWebApplicationContext();
        webContext.refresh();

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webContext);
        ServletContext servletContext = servletContext(attributes);

        WSSpringServlet servlet = new WSSpringServlet();
        servlet.init(new SimpleServletConfig(servletContext));

        assertSame(webContext, readField(servlet, "webApplicationContext"));
        assertNotNull(readField(servlet, "delegate"));

        webContext.close();
    }

    @Test
    void destroyClosesOwnedContextAndAlwaysDestroysDelegate() throws Exception {
        GenericWebApplicationContext webContext = new GenericWebApplicationContext();
        webContext.refresh();

        WSSpringServlet servlet = new WSSpringServlet();
        RecordingDelegate delegate = new RecordingDelegate(servletContext(new HashMap<>()));
        writeField(servlet, "webApplicationContext", webContext);
        writeField(servlet, "webApplicationContextInjected", false);
        writeField(servlet, "delegate", delegate);

        servlet.destroy();

        assertEquals(1, delegate.destroyCalls);
        assertTrue(!webContext.isActive());
        assertNull(readField(servlet, "delegate"));
    }

    @Test
    void destroyDoesNotCloseInjectedContext() throws Exception {
        GenericWebApplicationContext webContext = new GenericWebApplicationContext();
        webContext.refresh();

        WSSpringServlet servlet = new WSSpringServlet();
        RecordingDelegate delegate = new RecordingDelegate(servletContext(new HashMap<>()));
        writeField(servlet, "webApplicationContext", webContext);
        writeField(servlet, "webApplicationContextInjected", true);
        writeField(servlet, "delegate", delegate);

        servlet.destroy();

        assertTrue(webContext.isActive());
        webContext.close();
    }

    @Test
    void httpMethodsDelegateToWSServletDelegate() throws Exception {
        WSSpringServlet servlet = new WSSpringServlet();
        RecordingDelegate delegate = new RecordingDelegate(servletContext(new HashMap<>()));
        writeField(servlet, "delegate", delegate);

        HttpServletRequest request = proxy(HttpServletRequest.class);
        HttpServletResponse response = proxy(HttpServletResponse.class);

        servlet.doGet(request, response);
        servlet.doPost(request, response);
        servlet.doPut(request, response);
        servlet.doDelete(request, response);
        servlet.doHead(request, response);

        assertEquals(1, delegate.getCalls);
        assertEquals(1, delegate.postCalls);
        assertEquals(1, delegate.putCalls);
        assertEquals(1, delegate.deleteCalls);
        assertEquals(1, delegate.headCalls);
    }

    private Object readField(Object target, String name) throws Exception {
        Field field = WSSpringServlet.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private void writeField(Object target, String name, Object value) throws Exception {
        Field field = WSSpringServlet.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static ServletContext servletContext(Map<String, Object> attributes) {
        return proxy(ServletContext.class, (methodName, args) -> {
            if ("getAttribute".equals(methodName)) {
                return attributes.get(args[0]);
            }
            if ("setAttribute".equals(methodName)) {
                attributes.put((String) args[0], args[1]);
                return null;
            }
            if ("removeAttribute".equals(methodName)) {
                attributes.remove(args[0]);
                return null;
            }
            return null;
        });
    }

    private static <T> T proxy(Class<T> type) {
        return proxy(type, (name, args) -> null);
    }

    private static <T> T proxy(Class<T> type, Invocations invocations) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, (proxy, method, args) -> {
            Object value = invocations.call(method.getName(), args == null ? new Object[0] : args);
            if (value != null) {
                return value;
            }
            Class<?> returnType = method.getReturnType();
            if (!returnType.isPrimitive()) {
                return null;
            }
            if (returnType == boolean.class) {
                return false;
            }
            if (returnType == char.class) {
                return '\0';
            }
            return 0;
        }));
    }

    private interface Invocations {
        Object call(String methodName, Object[] args);
    }

    private static final class SimpleServletConfig implements ServletConfig {
        private final ServletContext servletContext;

        private SimpleServletConfig(ServletContext servletContext) {
            this.servletContext = servletContext;
        }

        @Override
        public String getServletName() {
            return "WSSpringServletTest";
        }

        @Override
        public ServletContext getServletContext() {
            return servletContext;
        }

        @Override
        public String getInitParameter(String name) {
            return null;
        }

        @Override
        public java.util.Enumeration<String> getInitParameterNames() {
            return java.util.Collections.emptyEnumeration();
        }
    }

    private static final class RecordingDelegate extends WSServletDelegate {
        private int destroyCalls;
        private int getCalls;
        private int postCalls;
        private int putCalls;
        private int deleteCalls;
        private int headCalls;

        private RecordingDelegate(ServletContext servletContext) {
            super(new ArrayList<>(), servletContext);
        }

        @Override
        public void destroy() {
            destroyCalls++;
        }

        @Override
        public void doHead(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
            headCalls++;
        }

        @Override
        public void doGet(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
            getCalls++;
        }

        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
            postCalls++;
        }

        @Override
        public void doPut(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
            putCalls++;
        }

        @Override
        public void doDelete(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
            deleteCalls++;
        }
    }
}
