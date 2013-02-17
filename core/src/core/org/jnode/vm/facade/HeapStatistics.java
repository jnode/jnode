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
 
package org.jnode.vm.facade;

import java.io.IOException;

/**
 * Heap statistical data collection.
 *
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */
public interface HeapStatistics {

    /**
     * Sets the minimum number of instances a class must have before
     * it is listed in toString.
     *
     * @param count
     */
    void setMinimumInstanceCount(int count);

    /**
     * Sets the minimum bytes of occupied memory by all instances of a class
     * before it is listed in toString.
     *
     * @param bytes
     */
    void setMinimumTotalSize(long bytes);

    /**
     * Sets the object filter. If the filter accept the object, 
     * then it will be added to statistics.
     * @param objectFilter
     * 
     */
    void setObjectFilter(ObjectFilter objectFilter);

    /**
     * Write the statistical data to an {@link Appendable}.
     * 
     * @param a
     * @throws IOException 
     */
    void writeTo(Appendable a) throws IOException;
}
