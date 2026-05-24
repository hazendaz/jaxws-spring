/*
 * This project is dual-licensed under the terms of the Common Development and Distribution License (CDDL) v1.0 and the GNU General Public License (GPL) v2.0 with Classpath Exception.
 *
 * You may choose either license.
 *
 * See LICENSE-CDDL-1.0 and LICENSE-GPL-2.0-CE for full license texts.
 */
package com.revinate.ws.spring;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.pipe.ClientTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.ServerTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubelineAssembler;
import com.sun.xml.ws.api.pipe.TubelineAssemblerFactory;
import com.sun.xml.ws.api.server.Container;

import jakarta.servlet.ServletContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests internal SPI behavior of SpringService container wrapper logic.
 */
public class SpringServiceContainerWrapperTest {

    @Test
    void getSPIReturnsConfiguredTubelineAssemblerFactory() throws Exception {
        SpringService service = new SpringService();
        TubelineAssemblerFactory factory = new TubelineAssemblerFactory() {
            @Override
            public TubelineAssembler doCreate(BindingID bindingId) {
                return null;
            }
        };
        service.setAssembler(factory);

        Object wrapper = createContainerWrapper(service);
        Object result = getSPI(wrapper, TubelineAssemblerFactory.class);

        assertSame(factory, result);
    }

    @Test
    void getSPIWrapsConfiguredTubelineAssemblerAsFactory() throws Exception {
        SpringService service = new SpringService();
        TubelineAssembler assembler = new TubelineAssembler() {
            @Override
            public Tube createClient(ClientTubeAssemblerContext context) {
                return null;
            }

            @Override
            public Tube createServer(ServerTubeAssemblerContext context) {
                return null;
            }
        };
        service.setAssembler(assembler);

        Object wrapper = createContainerWrapper(service);
        TubelineAssemblerFactory result = (TubelineAssemblerFactory) getSPI(wrapper, TubelineAssemblerFactory.class);

        assertNotNull(result);
        assertSame(assembler, result.doCreate(BindingID.parse("http://schemas.xmlsoap.org/wsdl/soap/http")));
    }

    @Test
    void getSPIReturnsServletContextAndDelegatesToConfiguredContainer() throws Exception {
        SpringService service = new SpringService();

        ServletContext servletContext = proxy(ServletContext.class);
        service.setServletContext(servletContext);

        Container delegateContainer = new Container() {
            @Override
            public <T> T getSPI(Class<T> spiType) {
                if (spiType == List.class) {
                    return spiType.cast(List.of("value"));
                }
                return null;
            }
        };
        service.setContainer(delegateContainer);

        Object wrapper = createContainerWrapper(service);

        assertSame(servletContext, getSPI(wrapper, ServletContext.class));
        assertEqualsListWithValue((List<?>) getSPI(wrapper, List.class));
    }

    @Test
    void getSPIReturnsDefaultModuleAndNullForUnknownType() throws Exception {
        SpringService service = new SpringService();
        Object wrapper = createContainerWrapper(service);

        com.sun.xml.ws.api.server.Module module = (com.sun.xml.ws.api.server.Module) getSPI(wrapper,
                com.sun.xml.ws.api.server.Module.class);

        assertNotNull(module);
        assertNotNull(module.getBoundEndpoints());
        assertNull(getSPI(wrapper, String.class));
    }

    private Object createContainerWrapper(SpringService service) throws Exception {
        Class<?> wrapperClass = Class.forName("com.revinate.ws.spring.SpringService$ContainerWrapper");
        Constructor<?> constructor = wrapperClass.getDeclaredConstructor(SpringService.class);
        constructor.setAccessible(true);
        return constructor.newInstance(service);
    }

    private Object getSPI(Object wrapper, Class<?> spiType) throws Exception {
        Method method = wrapper.getClass().getMethod("getSPI", Class.class);
        return method.invoke(wrapper, spiType);
    }

    private static <T> T proxy(Class<T> type) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, (p, m, a) -> {
            Class<?> returnType = m.getReturnType();
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

    private void assertEqualsListWithValue(List<?> list) {
        org.junit.jupiter.api.Assertions.assertEquals(List.of("value"), list);
    }
}
