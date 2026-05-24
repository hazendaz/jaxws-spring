/*
 * This project is dual-licensed under the terms of the Common Development and Distribution License (CDDL) v1.0 and the GNU General Public License (GPL) v2.0 with Classpath Exception.
 *
 * You may choose either license.
 *
 * See LICENSE-CDDL-1.0 and LICENSE-GPL-2.0-CE for full license texts.
 */
package com.revinate.jaxwsspringsample;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.revinate.sample.service.FactorialFault;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

class FactorialPortImplTest {

    @Test
    void factorialDelegatesToNumberServiceForValidInput() throws Exception {
        FactorialPortImpl port = new FactorialPortImpl();
        injectNumberService(port, new NumberService());

        int result = port.factorial(5);

        assertEquals(120, result);
    }

    @Test
    void factorialThrowsFaultForNegativeInput() throws Exception {
        FactorialPortImpl port = new FactorialPortImpl();
        injectNumberService(port, new NumberService());

        FactorialFault fault = assertThrows(FactorialFault.class, () -> port.factorial(-2));

        assertEquals("Number cannot be negative.", fault.getMessage());
        assertNotNull(fault.getFaultInfo());
        assertEquals("Number: -2", fault.getFaultInfo().getFaultInfo());
    }

    private void injectNumberService(FactorialPortImpl port, NumberService service) throws Exception {
        Field field = FactorialPortImpl.class.getDeclaredField("numberService");
        field.setAccessible(true);
        field.set(port, service);
    }
}
