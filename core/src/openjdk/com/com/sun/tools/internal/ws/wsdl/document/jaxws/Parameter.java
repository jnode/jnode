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
package com.sun.tools.internal.ws.wsdl.document.jaxws;

import javax.xml.namespace.QName;

/**
 * @author Vivek Pandey
 *
 * class representing jaxws:parameter
 *
 */
public class Parameter {
    private String part;
    private QName element;
    private String name;
    private String messageName;

    /**
     * @param part
     * @param element
     * @param name
     */
    public Parameter(String msgName, String part, QName element, String name) {
        this.part = part;
        this.element = element;
        this.name = name;
        this.messageName = msgName;
    }

    public String getMessageName() {
        return messageName;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    /**
     * @return Returns the element.
     */
    public QName getElement() {
        return element;
    }

    /**
     * @param element The element to set.
     */
    public void setElement(QName element) {
        this.element = element;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the part.
     */
    public String getPart() {
        return part;
    }

    /**
     * @param part The part to set.
     */
    public void setPart(String part) {
        this.part = part;
    }
}
