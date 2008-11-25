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

package com.sun.xml.internal.txw2;

import javax.xml.namespace.QName;

/**
 * Pluggable datatype writer.
 *
 * @author Kohsuke Kawaguchi
 */
public interface DatatypeWriter<DT> {

    /**
     * Gets the Java class that this writer can write.
     *
     * @return
     *      must not be null. Must be the same value always.
     */
    Class<DT> getType();

    /**
     * Prints the given datatype object and appends that result
     * into the given buffer.
     *
     * @param dt
     *      the datatype object to be printed.
     * @param resolver
     *      allows the converter to declare additional namespace prefixes.
     */
    void print(DT dt, NamespaceResolver resolver, StringBuilder buf);


    static final DatatypeWriter<?>[] BUILDIN = new DatatypeWriter<?>[] {
        new DatatypeWriter<String>() {
            public Class<String> getType() {
                return String.class;
            }
            public void print(String s, NamespaceResolver resolver, StringBuilder buf) {
                buf.append(s);
            }
        },
        new DatatypeWriter<Integer>() {
            public Class<Integer> getType() {
                return Integer.class;
            }
            public void print(Integer i, NamespaceResolver resolver, StringBuilder buf) {
                buf.append(i);
            }
        },
        new DatatypeWriter<Float>() {
            public Class<Float> getType() {
                return Float.class;
            }
            public void print(Float f, NamespaceResolver resolver, StringBuilder buf) {
                buf.append(f);
            }
        },
        new DatatypeWriter<Double>() {
            public Class<Double> getType() {
                return Double.class;
            }
            public void print(Double d, NamespaceResolver resolver, StringBuilder buf) {
                buf.append(d);
            }
        },
        new DatatypeWriter<QName>() {
            public Class<QName> getType() {
                return QName.class;
            }
            public void print(QName qn, NamespaceResolver resolver, StringBuilder buf) {
                String p = resolver.getPrefix(qn.getNamespaceURI());
                if(p.length()!=0)
                    buf.append(p).append(':');
                buf.append(qn.getLocalPart());
            }
        }
    };
}
