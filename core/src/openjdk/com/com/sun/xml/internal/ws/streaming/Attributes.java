/*
 * Portions Copyright 2006 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.xml.internal.ws.streaming;

import javax.xml.namespace.QName;

/**
 * <p> The Attributes interface is essentially a version of the
 * org.xml.sax.Attributes interface modified to use the JAX-WS QName class.</p>
 *
 * <p> Although namespace declarations can appear in the attribute list, the
 * actual values of the local name and URI properties are
 * implementation-specific. </p>
 *
 * <p> Applications that need to iterate through all the attributes can use the
 * {@link #isNamespaceDeclaration} method to identify namespace declarations
 * and skip them. </p>
 *
 * <p> Also, the URI property of an attribute will never be null. The value
 * "" (empty string) is used for the URI of non-qualified attributes. </p>
 *
 * @author WS Development Team
 */
public interface Attributes {

    /**
     * Return the number of attributes in the list.
     *
     */
    public int getLength();

    /**
     * Return true if the attribute at the given index is a namespace
     * declaration.
     *
     * <p> Implementations are encouraged to optimize this method by taking into
     * account their internal representations of attributes. </p>
     *
     */
    public boolean isNamespaceDeclaration(int index);

    /**
     * Look up an attribute's QName by index.
     *
     */
    public QName getName(int index);

    /**
     * Look up an attribute's URI by index.
     *
     */
    public String getURI(int index);

    /**
     * Look up an attribute's local name by index.
     * If attribute is a namespace declaration, result
     * is expected including "xmlns:".
     */
    public String getLocalName(int index);

    /**
     * Look up an attribute's prefix by index.
     *
     */
    public String getPrefix(int index);

    /**
     * Look up an attribute's value by index.
     *
     */
    public String getValue(int index);

    /**
     * Look up the index of an attribute by QName.
     *
     */
    public int getIndex(QName name);

    /**
     * Look up the index of an attribute by URI and local name.
     *
     */
    public int getIndex(String uri, String localName);

    /**
     * Look up the index of an attribute by local name.
     *
     */
    public int getIndex(String localName);

    /**
     * Look up the value of an attribute by QName.
     *
     */
    public String getValue(QName name);

    /**
     * Look up the value of an attribute by URI and local name.
     *
     */
    public String getValue(String uri, String localName);

    /**
     * Look up the value of an attribute by local name.
     *
     */
    public String getValue(String localName);
}
