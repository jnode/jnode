/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.vm.compiler.ir;

/**
 * @author Madhu Siddalingaiah
 */
public class StackVariable<T> extends Variable<T> {
    /**
     * @param type
     * @param index
     */
    public StackVariable(int type, int index) {
        super(type, index);
    }

    /**
     * @param variable
     */
    public StackVariable(StackVariable<T> variable) {
        this(variable.getType(), variable.getIndex());
    }

    public String toString() {
        return "s" + getIndex() + '_' + getSSAValue();
    }

    public Object clone() {
        return new StackVariable<T>(this);
    }
}
