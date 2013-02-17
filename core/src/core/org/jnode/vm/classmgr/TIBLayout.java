/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.vm.classmgr;

/**
 * Constants defining indexes in the TIB array.
 *
 * @author epr
 */
public interface TIBLayout {

    // TIB array indexes

    /**
     * Index of VmType entry.
     * Type: VmType.
     */
    public static final int VMTYPE_INDEX = 0;

    /**
     * Index of IMT entry.
     * Type: Object[]
     */
    public static final int IMT_INDEX = 1;

    /**
     * Index of IMT collisions array entry.
     * Type: boolean[]
     */
    public static final int IMTCOLLISIONS_INDEX = 2;

    /**
     * Index of compiled IMT table.
     * Type: Object (architecture dependent)
     */
    public static final int COMPILED_IMT_INDEX = 3;

    /**
     * Index of the Superclasses array entry.
     * Type: VmType[]
     */
    public static final int SUPERCLASSES_INDEX = 4;

    // Other constants

    /**
     * Minimum length (in elements) of a TIB
     */
    public static final int MIN_TIB_LENGTH = 5;

    /**
     * Index of the first virtual method in the TIB
     */
    public static final int FIRST_METHOD_INDEX = MIN_TIB_LENGTH;

}
