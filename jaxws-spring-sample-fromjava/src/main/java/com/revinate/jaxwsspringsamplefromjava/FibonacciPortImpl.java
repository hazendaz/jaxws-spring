/*
 * This project is dual-licensed under the terms of the Common Development and Distribution License (CDDL) v1.0 and the GNU General Public License (GPL) v2.0 with Classpath Exception.
 *
 * You may choose either license.
 *
 * See LICENSE-CDDL-1.0 and LICENSE-GPL-2.0-CE for full license texts.
 */
package com.revinate.jaxwsspringsamplefromjava;

import javax.jws.WebService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class FibonacciPortImpl.
 */
@WebService
@Component
public class FibonacciPortImpl {

    /** The number service. */
    @Autowired
    private NumberService numberService;

    /**
     * Fibonacci.
     *
     * @param index
     *            the index
     *
     * @return the int
     *
     * @throws FibonacciException
     *             the fibonacci exception
     */
    public int fibonacci(int index) throws FibonacciException {
        if (index < 0) {
            throw new FibonacciException("Index cannot be negative.", "Index: " + index);
        }

        return numberService.fibonacci(index);
    }
}
