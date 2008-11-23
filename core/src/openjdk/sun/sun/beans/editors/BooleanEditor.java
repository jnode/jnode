/*
 * Copyright 2006-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.beans.editors;

/**
 * Property editor for a java builtin "boolean" type.
 */

import java.beans.*;

public class BooleanEditor extends PropertyEditorSupport {


    public String getJavaInitializationString() {
        Object value = getValue();
        return (value != null)
                ? value.toString()
                : "null";
    }

    public String getAsText() {
        Object value = getValue();
        return (value instanceof Boolean)
             ? getValidName((Boolean) value)
             : "null";
    }

    public void setAsText(String text) throws java.lang.IllegalArgumentException {
        if (text == null) {
            setValue(null);
        } else if (isValidName(true, text)) {
            setValue(Boolean.TRUE);
        } else if (isValidName(false, text)) {
            setValue(Boolean.FALSE);
	} else {
	    throw new java.lang.IllegalArgumentException(text);
	}
    }

    public String[] getTags() {
        return new String[] {getValidName(true), getValidName(false)};
    }

    // the following method should be localized (4890258)

    private String getValidName(boolean value) {
        return value ? "True" : "False";
    }

    private boolean isValidName(boolean value, String name) {
        return getValidName(value).equalsIgnoreCase(name);
    }
}
