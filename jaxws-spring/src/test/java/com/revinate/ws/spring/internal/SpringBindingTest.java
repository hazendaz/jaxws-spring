/*
 * This project is dual-licensed under the terms of the Common Development and Distribution License (CDDL) v1.0 and the GNU General Public License (GPL) v2.0 with Classpath Exception.
 *
 * You may choose either license.
 *
 * See LICENSE-CDDL-1.0 and LICENSE-GPL-2.0-CE for full license texts.
 */
package com.revinate.ws.spring.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.revinate.ws.spring.SpringService;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.http.servlet.ServletAdapter;
import com.sun.xml.ws.transport.http.servlet.ServletAdapterList;

import java.lang.reflect.Field;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.servlet.ServletContext;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SpringBinding}.
 */
public class SpringBindingTest {

    @Test
    void settersStoreConfiguredValues() throws Exception {
        SpringBinding binding = new SpringBinding();
        binding.setBeanName("sample");
        binding.setUrl("/service/sample");
        binding.setService(null);

        assertEquals("sample", readField(binding, "beanName"));
        assertEquals("/service/sample", readField(binding, "urlPattern"));
        assertNull(readField(binding, "endpoint"));
    }

    @Test
    void createUsesBeanNameWhenConfigured() throws Exception {
        SpringBinding binding = new SpringBinding();
        binding.setBeanName("sample-name");
        binding.setUrl("/service/sample");
        binding.setService(createEndpoint());

        CapturingServletAdapterList owner = new CapturingServletAdapterList();
        binding.create(owner);

        assertEquals("sample-name", owner.adapterName);
        assertEquals("/service/sample", owner.urlPattern);
    }

    @Test
    void createFallsBackToUrlPatternWhenBeanNameMissing() throws Exception {
        SpringBinding binding = new SpringBinding();
        binding.setUrl("/service/fallback");
        binding.setService(createEndpoint());

        CapturingServletAdapterList owner = new CapturingServletAdapterList();
        binding.create(owner);

        assertEquals("/service/fallback", owner.adapterName);
        assertEquals("/service/fallback", owner.urlPattern);
    }

    private WSEndpoint<?> createEndpoint() throws Exception {
        SpringService service = new SpringService();
        service.setBean(new TestWebService());
        return service.getObject();
    }

    private Object readField(SpringBinding binding, String name) throws Exception {
        Field field = SpringBinding.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(binding);
    }

    @WebService
    public static class TestWebService {
        @WebMethod
        public String ping() {
            return "pong";
        }
    }

    private static final class CapturingServletAdapterList extends ServletAdapterList {
        private String adapterName;
        private String urlPattern;

        private CapturingServletAdapterList() {
            super((ServletContext) null);
        }

        @Override
        protected ServletAdapter createHttpAdapter(String name, String urlPattern, WSEndpoint<?> endpoint) {
            this.adapterName = name;
            this.urlPattern = urlPattern;
            return super.createHttpAdapter(name, urlPattern, endpoint);
        }
    }
}
