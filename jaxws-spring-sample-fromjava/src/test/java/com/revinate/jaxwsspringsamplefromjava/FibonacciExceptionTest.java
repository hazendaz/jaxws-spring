/*
 * This project is dual-licensed under the terms of the Common Development and Distribution License (CDDL) v1.0 and the GNU General Public License (GPL) v2.0 with Classpath Exception.
 *
 * You may choose either license.
 *
 * See LICENSE-CDDL-1.0 and LICENSE-GPL-2.0-CE for full license texts.
 */
package com.revinate.jaxwsspringsamplefromjava;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FibonacciExceptionTest {

    @Test
    void constructorSetsMessageAndDetail() {
        FibonacciException exception = new FibonacciException("message", "detail");

        assertEquals("message", exception.getMessage());
        assertEquals("detail", exception.getDetail());
    }
}
