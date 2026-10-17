/*
 * This project is dual-licensed under the terms of the Common Development and Distribution License (CDDL) v1.0 and the GNU General Public License (GPL) v2.0 with Classpath Exception.
 *
 * You may choose either license.
 *
 * See LICENSE-CDDL-1.0 and LICENSE-GPL-2.0-CE for full license texts.
 */
package com.revinate.ws.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.server.ServerRtException;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.xml.ws.WebServiceFeature;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import jakarta.xml.ws.soap.AddressingFeature;
import jakarta.xml.ws.soap.SOAPBinding;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link SpringService}.
 */
public class SpringServiceTest {

    /** The temp dir. */
    @TempDir
    Path tempDir;

    /**
     * Sets the assembler rejects unsupported type.
     */
    @Test
    void setAssemblerRejectsUnsupportedType() {
        SpringService service = new SpringService();

        assertThrows(IllegalArgumentException.class, () -> service.setAssembler("invalid"));
    }

    /**
     * Sets the bean does not override previously configured impl type.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void setBeanDoesNotOverridePreviouslyConfiguredImplType() throws Exception {
        SpringService service = new SpringService();
        service.setImpl(String.class);

        service.setBean(new EchoWebService());

        assertEquals(String.class, readField(service, "implType"));
    }

    /**
     * After properties set converts primary wsdl and metadata from urls.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void afterPropertiesSetConvertsPrimaryWsdlAndMetadataFromUrls() throws Exception {
        Path primaryWsdl = Files.writeString(tempDir.resolve("primary.wsdl"), "wsdl");
        Path metadataXsd = Files.writeString(tempDir.resolve("meta.xsd"), "xsd");
        SpringService service = new SpringService();
        service.setPrimaryWsdl(primaryWsdl.toUri().toURL());
        service.setMetadata(List.of(metadataXsd.toUri().toURL()));

        service.afterPropertiesSet();

        assertNotNull(readField(service, "primaryWsdl"));
        Collection<?> metadata = (Collection<?>) readField(service, "metadata");
        assertNotNull(metadata);
        assertEquals(1, metadata.size());
    }

    /**
     * After properties set converts string resources using absolute urls.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void afterPropertiesSetConvertsStringResourcesUsingAbsoluteUrls() throws Exception {
        SpringService service = new SpringService();
        service.setImpl(EchoWebService.class);
        service.setPrimaryWsdl("https://example.com/service.wsdl");
        service.setMetadata(List.of("https://example.com/schema.xsd"));

        service.afterPropertiesSet();

        assertNotNull(readField(service, "primaryWsdl"));
        Collection<?> metadata = (Collection<?>) readField(service, "metadata");
        assertNotNull(metadata);
        assertEquals(1, metadata.size());
    }

    /**
     * After properties set rejects unknown metadata resource type.
     */
    @Test
    void afterPropertiesSetRejectsUnknownMetadataResourceType() {
        SpringService service = new SpringService();
        service.setMetadata(List.of(1));

        assertThrows(IllegalArgumentException.class, service::afterPropertiesSet);
    }

    /**
     * After properties set rejects invalid string primary wsdl location.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void afterPropertiesSetRejectsInvalidStringPrimaryWsdlLocation() throws Exception {
        SpringService service = new SpringService();
        service.setImpl(EchoWebService.class);
        service.setPrimaryWsdl("not-a-url-or-resource");

        assertThrows(ServerRtException.class, service::afterPropertiesSet);
    }

    /**
     * Gets the object creates endpoint once and applies handlers.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void objectCreatesEndpointOnceAndAppliesHandlers() throws Exception {
        SpringService service = new SpringService();
        service.setBean(new EchoWebService());
        service.setHandlers(List.of(new NoOpHandler()));

        WSEndpoint<?> first = service.getObject();
        WSEndpoint<?> second = service.getObject();

        assertNotNull(first);
        assertSame(first, second);

        WSBinding binding = (WSBinding) readField(service, "binding");
        assertEquals(1, binding.getHandlerChain().size());
    }

    /**
     * Gets the object rejects using binding and binding id together.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void objectRejectsUsingBindingAndBindingIdTogether() throws Exception {
        SpringService service = new SpringService();
        service.setBean(new EchoWebService());
        service.setBinding(BindingImpl.create(BindingID.parse(SOAPBinding.SOAP11HTTP_BINDING)));
        service.setBindingID(SOAPBinding.SOAP12HTTP_BINDING);

        assertThrows(IllegalStateException.class, service::getObject);
    }

    /**
     * Gets the object rejects using binding and features together.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void objectRejectsUsingBindingAndFeaturesTogether() throws Exception {
        SpringService service = new SpringService();
        service.setBean(new EchoWebService());
        service.setBinding(BindingImpl.create(BindingID.parse(SOAPBinding.SOAP11HTTP_BINDING)));
        service.setFeatures(List.<WebServiceFeature> of(new AddressingFeature()));

        assertThrows(IllegalStateException.class, service::getObject);
    }

    /**
     * Read field.
     *
     * @param target
     *            the target
     * @param name
     *            the name
     *
     * @return the object
     *
     * @throws Exception
     *             the exception
     */
    private Object readField(Object target, String name) throws Exception {
        Field field = SpringService.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    /**
     * The Class EchoWebService.
     */
    @WebService
    public static class EchoWebService {

        /**
         * Echo.
         *
         * @param value
         *            the value
         *
         * @return the string
         */
        @WebMethod
        public String echo(String value) {
            return value;
        }
    }

    /**
     * The Class NoOpHandler.
     */
    private static final class NoOpHandler implements SOAPHandler<SOAPMessageContext> {
        @Override
        public boolean handleMessage(SOAPMessageContext context) {
            return true;
        }

        @Override
        public boolean handleFault(SOAPMessageContext context) {
            return true;
        }

        @Override
        public void close(MessageContext context) {
            // no-op
        }

        @Override
        public Set<QName> getHeaders() {
            return Set.of();
        }
    }
}
