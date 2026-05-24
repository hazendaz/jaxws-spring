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

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

class SpringBindingTest {

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

    private Object readField(SpringBinding binding, String name) throws Exception {
        Field field = SpringBinding.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(binding);
    }
}
