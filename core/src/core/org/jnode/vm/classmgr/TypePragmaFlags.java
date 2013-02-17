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
 * Variable pragma flags for methods.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface TypePragmaFlags {

    /**
     * Method of this type will not get any yieldpoints
     */
    public static final char UNINTERRUPTIBLE = 0x0001;

    /**
     * Static variables of this type will be shared between isolates
     */
    public static final char SHAREDSTATICS = 0x0002;

    /**
     * Fields of this type must not be re-ordered
     */
    public static final char NO_FIELD_ALIGNMENT = 0x0004;

    /**
     * Methods of this type are allowed to use magic code.
     */
    public static final char MAGIC_PERMISSION = 0x0008;

    /**
     * All flags that are inherited from the super class
     */
    static final char INHERITABLE_FLAGS_MASK = UNINTERRUPTIBLE;
}
