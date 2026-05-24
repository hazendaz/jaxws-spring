/*
 * This project is dual-licensed under the terms of the Common Development and Distribution License (CDDL) v1.0 and the GNU General Public License (GPL) v2.0 with Classpath Exception.
 *
 * You may choose either license.
 *
 * See LICENSE-CDDL-1.0 and LICENSE-GPL-2.0-CE for full license texts.
 */
package com.revinate.ws.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SDDocumentCollectorTest {

    @TempDir
    Path tempDir;

    @Test
    void collectDocsReturnsWsdlAndXsdFromDirectory() throws Exception {
        Path docsDir = Files.createDirectory(tempDir.resolve("sample-docs"));
        Files.writeString(docsDir.resolve("service.wsdl"), "wsdl");
        Files.writeString(docsDir.resolve("schema.xsd"), "xsd");
        Files.writeString(docsDir.resolve("ignore.txt"), "text");
        Path nestedDir = Files.createDirectory(docsDir.resolve("nested"));
        Files.writeString(nestedDir.resolve("nested.wsdl"), "wsdl");

        ClassLoader cl = new FixedResourceClassLoader("sample-docs", docsDir.toUri().toURL());

        Map<URL, Object> docs = SDDocumentCollector.collectDocs("sample-docs", cl);

        assertEquals(3, docs.size());
        Set<String> fileNames = docs.keySet().stream().map(this::extractFileName).collect(Collectors.toSet());
        assertTrue(fileNames.contains("service.wsdl"));
        assertTrue(fileNames.contains("schema.xsd"));
        assertTrue(fileNames.contains("nested.wsdl"));
    }

    @Test
    void collectDocsReturnsEmptyMapWhenResourceIsMissing() {
        ClassLoader cl = new FixedResourceClassLoader("other", null);

        Map<URL, Object> docs = SDDocumentCollector.collectDocs("sample-docs", cl);

        assertTrue(docs.isEmpty());
    }

    private String extractFileName(URL url) {
        String path = url.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private static final class FixedResourceClassLoader extends ClassLoader {
        private final String resourceName;
        private final URL resourceUrl;

        private FixedResourceClassLoader(String resourceName, URL resourceUrl) {
            this.resourceName = resourceName;
            this.resourceUrl = resourceUrl;
        }

        @Override
        public URL getResource(String name) {
            if (this.resourceName.equals(name)) {
                return this.resourceUrl;
            }
            return null;
        }
    }
}
