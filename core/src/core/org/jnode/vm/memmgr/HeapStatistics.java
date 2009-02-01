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
 
package org.jnode.vm.memmgr;

import org.jnode.vm.VmSystemObject;

/**
 * Heap statistical data collection.
 *
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */
public abstract class HeapStatistics extends VmSystemObject {

    /**
     * Sets the minimum number of instances a class must have before
     * it is listed in toString.
     *
     * @param count
     */
    public abstract void setMinimumInstanceCount(int count);

    /**
     * Sets the minimum bytes of occupied memory by all instances of a class
     * before it is listed in toString.
     *
     * @param bytes
     */
    public abstract void setMinimumTotalSize(long bytes);

    /**
     * Convert the statistical data to a string.
     *
     * @see java.lang.Object#toString()
     */
    public abstract String toString();
}
