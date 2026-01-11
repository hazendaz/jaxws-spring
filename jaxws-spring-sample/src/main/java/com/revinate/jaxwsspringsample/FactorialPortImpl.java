/*
 * This project is dual-licensed under the terms of the Common Development and Distribution License (CDDL) v1.0 and the GNU General Public License (GPL) v2.0 with Classpath Exception.
 *
 * You may choose either license.
 *
 * See LICENSE-CDDL-1.0 and LICENSE-GPL-2.0-CE for full license texts.
 */
package com.revinate.jaxwsspringsample;

import com.revinate.sample.service.FactorialFault;
import com.revinate.sample.service.FactorialPort;

import javax.jws.WebService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@WebService(endpointInterface = "com.revinate.sample.service.FactorialPort")
@Component
public class FactorialPortImpl implements FactorialPort {

    @Autowired
    private NumberService numberService;

    public int factorial(int number) throws FactorialFault {
        if (number < 0) {
            String message = "Number cannot be negative.";
            com.revinate.sample.datatype.FactorialFault fault = new com.revinate.sample.datatype.FactorialFault();
            fault.setMessage(message);
            fault.setFaultInfo("Number: " + number);
            throw new FactorialFault(message, fault);
        }

        return numberService.factorial(number);
    }
}
