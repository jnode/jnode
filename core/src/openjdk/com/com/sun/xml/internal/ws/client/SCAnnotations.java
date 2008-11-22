/*
 * Copyright 2005-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.xml.internal.ws.client;

import com.sun.xml.internal.ws.util.JAXWSUtils;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;

/**
 * Represents parsed {@link WebServiceClient} and {@link WebEndpoint}
 * annotations on a {@link Service}-derived class.
 *
 * @author Kohsuke Kawaguchi
 */
final class SCAnnotations {
    SCAnnotations(final Class<?> sc) {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                WebServiceClient wsc =sc.getAnnotation(WebServiceClient.class);
                if(wsc==null)
                    throw new WebServiceException("Service Interface Annotations required, exiting...");

                String name = wsc.name();
                String tns = wsc.targetNamespace();
                serviceQName = new QName(tns, name);
                try {
                    wsdlLocation = JAXWSUtils.getFileOrURL(wsc.wsdlLocation());
                } catch (IOException e) {
                    // TODO: report a reasonable error message
                    throw new WebServiceException(e);
                }

                for (Method method : sc.getDeclaredMethods()) {
                    WebEndpoint webEndpoint = method.getAnnotation(WebEndpoint.class);
                    if (webEndpoint != null) {
                        String endpointName = webEndpoint.name();
                        QName portQName = new QName(tns, endpointName);
                        portQNames.add(portQName);
                    }
                    Class<?> seiClazz = method.getReturnType();
                    if (seiClazz!=void.class) {
                        classes.add(seiClazz);
                    }
                }

                return null;
            }
        });
    }

    QName serviceQName;
    final ArrayList<QName> portQNames = new ArrayList<QName>();
    final ArrayList<Class> classes = new ArrayList<Class>();
    URL wsdlLocation;
}
