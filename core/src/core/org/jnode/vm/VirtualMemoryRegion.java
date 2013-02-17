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
 
package org.jnode.vm;

public final class VirtualMemoryRegion {

    /**
     * Total space that can contain objects
     */
    public static final int HEAP = 0x1020301;
    /**
     * Space available to the memory manager
     */
    public static final int AVAILABLE = 0x1020302;
    /**
     * Space available to devices
     */
    public static final int DEVICE = 0x1020303;
    /**
     * Space the contains the bootimage
     */
    public static final int BOOTIMAGE = 0x1020304;
    /**
     * Space the contains the initial jar
     */
    public static final int INITJAR = 0x1020305;
    /**
     * Space that contains ACPI tables
     */
    public static final int ACPI = 0x1020306;
}
