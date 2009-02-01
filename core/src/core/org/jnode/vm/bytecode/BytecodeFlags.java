/*
 * $Id$
 *
 * JNode.org
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
 
package org.jnode.vm.bytecode;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface BytecodeFlags {

    public static final byte F_START_OF_BASICBLOCK = 0x01;
    public static final byte F_START_OF_TRYBLOCK = 0x02;
    public static final byte F_START_OF_TRYBLOCKEND = 0x04;
    public static final byte F_START_OF_EXCEPTIONHANDLER = 0x08;
    public static final byte F_START_OF_INSTRUCTION = 0x10;
    public static final byte F_YIELDPOINT = 0x20;
}
