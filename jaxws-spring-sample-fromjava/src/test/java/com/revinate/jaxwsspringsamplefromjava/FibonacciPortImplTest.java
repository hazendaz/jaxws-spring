/*
 * This project is dual-licensed under the terms of the Common Development and Distribution License (CDDL) v1.0 and the GNU General Public License (GPL) v2.0 with Classpath Exception.
 *
 * You may choose either license.
 *
 * See LICENSE-CDDL-1.0 and LICENSE-GPL-2.0-CE for full license texts.
 */
package com.revinate.jaxwsspringsamplefromjava;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link FibonacciPortImpl}.
 */
public class FibonacciPortImplTest {

    @Test
    void fibonacciDelegatesToNumberServiceForValidIndex() throws Exception {
        FibonacciPortImpl port = new FibonacciPortImpl();
        injectNumberService(port, new NumberService());

        int result = port.fibonacci(5);

        assertEquals(5, result);
    }

    @Test
    void fibonacciThrowsExceptionForNegativeIndex() throws Exception {
        FibonacciPortImpl port = new FibonacciPortImpl();
        injectNumberService(port, new NumberService());

        FibonacciException exception = assertThrows(FibonacciException.class, () -> port.fibonacci(-1));

        assertEquals("Index cannot be negative.", exception.getMessage());
        assertEquals("Index: -1", exception.getDetail());
    }

    private void injectNumberService(FibonacciPortImpl port, NumberService service) throws Exception {
        Field field = FibonacciPortImpl.class.getDeclaredField("numberService");
        field.setAccessible(true);
        field.set(port, service);
    }
}
