/*
 * This project is dual-licensed under the terms of the Common Development and Distribution License (CDDL) v1.0 and the GNU General Public License (GPL) v2.0 with Classpath Exception.
 *
 * You may choose either license.
 *
 * See LICENSE-CDDL-1.0 and LICENSE-GPL-2.0-CE for full license texts.
 */
package com.revinate.jaxwsspringsample;

import com.revinate.sample.service.FibonacciFault;
import com.revinate.sample.service.FibonacciPort;

import javax.jws.WebService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@WebService(endpointInterface = "com.revinate.sample.service.FibonacciPort")
@Component
public class FibonacciPortImpl implements FibonacciPort {

    @Autowired
    private NumberService numberService;

    public int fibonacci(int index) throws FibonacciFault {
        if (index < 0) {
            String message = "Index cannot be negative.";
            com.revinate.sample.datatype.FibonacciFault fault = new com.revinate.sample.datatype.FibonacciFault();
            fault.setMessage(message);
            fault.setFaultInfo("Index: " + index);
            throw new FibonacciFault(message, fault);
        }

        return numberService.fibonacci(index);
    }
}
