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

package com.sun.tools.internal.ws.api.wsdl;

import org.w3c.dom.Element;
import org.xml.sax.Locator;

/**
 * Provides WSDL parsing context. It should be used by the WSDL extension handlers to register their namespaces so that
 * it can be latter used by other extensions to resolve the namespaces.
 *
 * @author Vivek Pandey
 */
public interface TWSDLParserContext {

    /**
     * Pushes the parsing context
     */
    void push();

    /**
     * pops the parsing context
     */
    void pop();

    /**
     * Gives the namespace URI for a given prefix
     *
     * @param prefix non-null prefix
     * @return null of the prefix is not found
     */
    String getNamespaceURI(String prefix);

    /**
     * Gives the prefixes in the current context
     */
    Iterable<String> getPrefixes();

    /**
     * Gives default namespace
     *
     * @return null if there is no default namespace declaration found
     */
    String getDefaultNamespaceURI();

    /**
     * Registers naemespace declarations of a given {@link Element} found in the WSDL
     *
     * @param e {@link Element} whose namespace declarations need to be registered
     */
    void registerNamespaces(Element e);

    /**
     * gives the location information for the given Element.
     */
    Locator getLocation(Element e);

}
