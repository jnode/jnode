/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
public class StackLocation<T> extends Location<T> {
    private int displacement;

    /**
     * @param displacement
     */
    public StackLocation(int displacement) {
        super("local" + displacement);
        this.displacement = displacement;
    }

    public StackLocation() {
        this(0);
    }

    public void setDisplacement(int displacement) {
        this.displacement = displacement;
    }

    /**
     *
     */
    public int getDisplacement() {
        //TODO: remove the 8 here, hack for C compatibility while testing
        return displacement + 8;
    }
}
