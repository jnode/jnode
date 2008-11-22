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

package com.sun.xml.internal.ws.fault;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.namespace.QName;

/**
 * <pre>
 *  &lt;env:Code>
 *       &lt;env:Value>env:Sender&lt;/env:Value>
 *       &lt;env:Subcode>
 *           &lt;env:Value>m:MessageTimeout1&lt;/env:Value>
 *           &lt;env:Subcode>
 *               &lt;env:Value>m:MessageTimeout2&lt;/env:Value>
 *           &lt;/env:Subcode>
 *       &lt;/env:Subcode>
 *  &lt;/env:Code>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CodeType", namespace = "http://www.w3.org/2003/05/soap-envelope", propOrder = {
    "Value",
    "Subcode"
})
class CodeType {
    @XmlTransient
    private static final String ns="http://www.w3.org/2003/05/soap-envelope";

    /**
     * mandatory, minOccurs=1
     */
    @XmlElement(namespace = ns)
    private QName Value;

    /**
     * optional, minOcccurs=0, maxOccurs="1"
     */
    @XmlElement(namespace = ns)
    private SubcodeType Subcode;

    CodeType(QName value) {
        Value = value;
    }

    CodeType() {
    }

    QName getValue(){
        return Value;
    }

    SubcodeType getSubcode(){
        return Subcode;
    }

    void setSubcode(SubcodeType subcode) {
        Subcode = subcode;
    }
}
