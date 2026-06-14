/*
 * This project is dual-licensed under the terms of the Common Development and Distribution License (CDDL) v1.0 and the GNU General Public License (GPL) v2.0 with Classpath Exception.
 *
 * You may choose either license.
 *
 * See LICENSE-CDDL-1.0 and LICENSE-GPL-2.0-CE for full license texts.
 */
package com.revinate.jaxwsspringsample;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link NumberService}.
 */
public class NumberServiceTest {

    @Test
    void fibonacciComputesExpectedValues() {
        NumberService service = new NumberService();

        assertEquals(0, service.fibonacci(0));
        assertEquals(1, service.fibonacci(1));
        assertEquals(8, service.fibonacci(6));
    }

    @Test
    void factorialComputesExpectedValues() {
        NumberService service = new NumberService();

        assertEquals(1, service.factorial(0));
        assertEquals(1, service.factorial(1));
        assertEquals(120, service.factorial(5));
    }
}
