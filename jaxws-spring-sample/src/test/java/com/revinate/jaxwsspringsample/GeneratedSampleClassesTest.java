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
import static org.junit.jupiter.api.Assertions.assertSame;

import com.revinate.sample.datatype.Factorial;
import com.revinate.sample.datatype.FactorialResponse;
import com.revinate.sample.datatype.Fibonacci;
import com.revinate.sample.datatype.FibonacciResponse;
import com.revinate.sample.datatype.ObjectFactory;
import com.revinate.sample.service.FactorialFault;
import com.revinate.sample.service.SampleService;

import java.net.URL;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.Test;

/**
 * Coverage tests for generated SOAP classes.
 */
public class GeneratedSampleClassesTest {

    @Test
    void datatypeAccessorsAndFactoryMethodsWork() {
        ObjectFactory factory = new ObjectFactory();

        Fibonacci fibonacci = factory.createFibonacci();
        fibonacci.setIndex(8);
        assertEquals(8, fibonacci.getIndex());

        FibonacciResponse fibonacciResponse = factory.createFibonacciResponse();
        fibonacciResponse.setReturn(21);
        assertEquals(21, fibonacciResponse.getReturn());

        com.revinate.sample.datatype.FibonacciFault fibonacciFault = factory.createFibonacciFault();
        fibonacciFault.setFaultInfo("bad-index");
        fibonacciFault.setMessage("invalid");
        assertEquals("bad-index", fibonacciFault.getFaultInfo());
        assertEquals("invalid", fibonacciFault.getMessage());

        Factorial factorial = factory.createFactorial();
        factorial.setNumber(6);
        assertEquals(6, factorial.getNumber());

        FactorialResponse factorialResponse = factory.createFactorialResponse();
        factorialResponse.setReturn(720);
        assertEquals(720, factorialResponse.getReturn());

        com.revinate.sample.datatype.FactorialFault factorialFault = factory.createFactorialFault();
        factorialFault.setFaultInfo("bad-number");
        factorialFault.setMessage("invalid");
        assertEquals("bad-number", factorialFault.getFaultInfo());
        assertEquals("invalid", factorialFault.getMessage());
    }

    @Test
    void serviceFaultWrappersExposeFaultBeans() {
        com.revinate.sample.datatype.FibonacciFault fibonacciFaultInfo = new com.revinate.sample.datatype.FibonacciFault();
        Throwable cause = new IllegalArgumentException("cause");

        com.revinate.sample.service.FibonacciFault fibonacciFault = new com.revinate.sample.service.FibonacciFault(
                "fibonacci-error", fibonacciFaultInfo, cause);

        assertEquals("fibonacci-error", fibonacciFault.getMessage());
        assertSame(cause, fibonacciFault.getCause());
        assertSame(fibonacciFaultInfo, fibonacciFault.getFaultInfo());

        com.revinate.sample.datatype.FactorialFault factorialFaultInfo = new com.revinate.sample.datatype.FactorialFault();
        FactorialFault factorialFault = new FactorialFault("factorial-error", factorialFaultInfo, cause);

        assertEquals("factorial-error", factorialFault.getMessage());
        assertSame(cause, factorialFault.getCause());
        assertSame(factorialFaultInfo, factorialFault.getFaultInfo());
    }

    @Test
    void sampleServiceConstructorsAndPortAccessorsAreUsable() throws Exception {
        SampleService defaultService = new SampleService();
        assertNotNull(defaultService);
        assertNotNull(defaultService.getFibonacciPort());
        assertNotNull(defaultService.getFactorialPort());

        URL wsdl = getClass().getClassLoader().getResource("sample/wsdl/SampleService.wsdl");
        assertNotNull(wsdl);
        QName qName = new QName("http://www.revinate.com/sample", "SampleService");

        SampleService explicitService = new SampleService(wsdl, qName);
        assertNotNull(explicitService);
        assertNotNull(explicitService.getFibonacciPort());
        assertNotNull(explicitService.getFactorialPort());
    }
}
