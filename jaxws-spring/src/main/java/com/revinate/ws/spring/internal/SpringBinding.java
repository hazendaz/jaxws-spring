/*
 * This project is dual-licensed under the terms of the Common Development and Distribution License (CDDL) v1.0 and the GNU General Public License (GPL) v2.0 with Classpath Exception.
 *
 * You may choose either license.
 *
 * See LICENSE-CDDL-1.0 and LICENSE-GPL-2.0-CE for full license texts.
 */
package com.revinate.ws.spring.internal;

import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.http.servlet.ServletAdapterList;

import org.springframework.beans.factory.BeanNameAware;

/**
 * Represents the association between the service and URL.
 *
 * @author Kohsuke Kawaguchi
 */
public class SpringBinding implements BeanNameAware {

    private String beanName;
    private String urlPattern;
    private WSEndpoint<?> endpoint;

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    public void create(ServletAdapterList owner) {
        String name = beanName;
        if (name == null) {
            name = urlPattern;
        }
        owner.createAdapter(name, urlPattern, endpoint);
    }

    /**
     * URL pattern to which this service is bound.
     */
    public void setUrl(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    /**
     * The service to be bound to the specified URL.
     */
    public void setService(WSEndpoint<?> endpoint) {
        this.endpoint = endpoint;
    }
}
