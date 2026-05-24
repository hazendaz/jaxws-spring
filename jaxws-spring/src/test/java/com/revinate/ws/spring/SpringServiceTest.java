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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

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

        service.setBean(new StringBuilder("value"));

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
    void afterPropertiesSetRejectsUnknownMetadataResourceType() {
        SpringService service = new SpringService();
        service.setMetadata(List.of(1));

        assertThrows(IllegalArgumentException.class, service::afterPropertiesSet);
    }

    private Object readField(Object target, String name) throws Exception {
        Field field = SpringService.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }
}
