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

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.SOAPBinding;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link SpringService}.
 */
public class SpringServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void setAssemblerRejectsUnsupportedType() {
        SpringService service = new SpringService();

        assertThrows(IllegalArgumentException.class, () -> service.setAssembler("invalid"));
    }

    @Test
    void setBeanDoesNotOverridePreviouslyConfiguredImplType() throws Exception {
        SpringService service = new SpringService();
        service.setImpl(String.class);

        service.setBean(new EchoWebService());

        assertEquals(String.class, readField(service, "implType"));
    }

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

    @Test
    void afterPropertiesSetRejectsUnknownMetadataResourceType() {
        SpringService service = new SpringService();
        service.setMetadata(List.of(1));

        assertThrows(IllegalArgumentException.class, service::afterPropertiesSet);
    }

    @Test
    void afterPropertiesSetRejectsInvalidStringPrimaryWsdlLocation() throws Exception {
        SpringService service = new SpringService();
        service.setImpl(EchoWebService.class);
        service.setPrimaryWsdl("not-a-url-or-resource");

        assertThrows(ServerRtException.class, service::afterPropertiesSet);
    }

    @Test
    void getObjectCreatesEndpointOnceAndAppliesHandlers() throws Exception {
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

    @Test
    void getObjectRejectsUsingBindingAndBindingIdTogether() throws Exception {
        SpringService service = new SpringService();
        service.setBean(new EchoWebService());
        service.setBinding(BindingImpl.create(BindingID.parse(SOAPBinding.SOAP11HTTP_BINDING)));
        service.setBindingID(SOAPBinding.SOAP12HTTP_BINDING);

        assertThrows(IllegalStateException.class, service::getObject);
    }

    @Test
    void getObjectRejectsUsingBindingAndFeaturesTogether() throws Exception {
        SpringService service = new SpringService();
        service.setBean(new EchoWebService());
        service.setBinding(BindingImpl.create(BindingID.parse(SOAPBinding.SOAP11HTTP_BINDING)));
        service.setFeatures(List.<WebServiceFeature> of(new AddressingFeature()));

        assertThrows(IllegalStateException.class, service::getObject);
    }

    private Object readField(Object target, String name) throws Exception {
        Field field = SpringService.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    @WebService
    public static class EchoWebService {
        @WebMethod
        public String echo(String value) {
            return value;
        }
    }

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
