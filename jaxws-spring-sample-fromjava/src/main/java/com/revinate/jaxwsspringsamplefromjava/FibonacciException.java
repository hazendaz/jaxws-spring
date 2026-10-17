/*
 * This project is dual-licensed under the terms of the Common Development and Distribution License (CDDL) v1.0 and the GNU General Public License (GPL) v2.0 with Classpath Exception.
 *
 * You may choose either license.
 *
 * See LICENSE-CDDL-1.0 and LICENSE-GPL-2.0-CE for full license texts.
 */
package com.revinate.jaxwsspringsamplefromjava;

/**
 * The Class FibonacciException.
 */
public class FibonacciException extends Exception {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The detail. */
    private final String detail;

    /**
     * Instantiates a new fibonacci exception.
     *
     * @param message
     *            the message
     * @param detail
     *            the detail
     */
    public FibonacciException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    /**
     * Gets the detail.
     *
     * @return the detail
     */
    public String getDetail() {
        return detail;
    }
}
