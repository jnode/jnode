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

package com.sun.xml.internal.ws.api.model.wsdl;

/**
 * Abstraction of wsdl:portType/wsdl:operation/wsdl:input
 *
 * @author Vivek Pandey
 */
public interface WSDLInput extends WSDLObject, WSDLExtensible {
    /**
     * Gives the wsdl:portType/wsdl:operation/wsdl:input@name
     */
    String getName();

    /**
     * Gives the WSDLMessage corresponding to wsdl:input@message
     * <p/>
     * This method should not be called before the entire WSDLModel is built. Basically after the WSDLModel is built
     * all the references are resolve in a post processing phase. IOW, the WSDL extensions should
     * not call this method.
     *
     * @return Always returns null when called from inside WSDL extensions.
     */
    WSDLMessage getMessage();

    /**
     * Gives the Action Message Addressing Property value for
     * {@link this} message.
     * <p/>
     * This method provides the correct value irrespective of
     * whether the Action is explicitly specified in the WSDL or
     * implicitly derived using the rules defined in WS-Addressing.
     *
     * @return Action
     */
    String getAction();
}
