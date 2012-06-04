/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.shell.bjorne;

import org.jnode.shell.ShellFailureException;

class VariableSlot {
    private String value;
    private final String name;
    private boolean exported;
    private boolean readOnly;

    VariableSlot(String name, String value, boolean exported) {
        if (name == null) {
            throw new ShellFailureException("null name");
        }
        if (value == null) {
            throw new ShellFailureException("null value");
        }
        this.value = value;
        this.exported = exported;
        this.name = name;
    }

    VariableSlot(VariableSlot other) {
        this.value = other.value;
        this.exported = other.exported;
        this.name = other.name;
    }
    
    String getValue() {
        return value;
    }

    void setValue(String value) {
        this.value = value;
    }

    boolean isExported() {
        return exported;
    }

    void setExported(boolean exported) {
        this.exported = exported;
    }

    boolean isReadOnly() {
        return readOnly;
    }

    void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    String getName() {
        return name;
    }

}
