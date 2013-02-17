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
 
package org.jnode.system.resource;

import org.vmmagic.unboxed.Address;

/**
 * Interface for searching data in a memory region.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface MemoryScanner {

    /**
     * Find a given 8-bit int (match) withing a given region.
     * This method searches at interface of stepSize large, only values at
     * the beginning of a step are matched.
     *
     * @param start    The address where to start searching.
     * @param size     The length of the region to search (in bytes)
     * @param match    The value to search for
     * @param stepSize The size of of step.
     * @return The address of the first match, or null if not found.
     */
    public Address findInt8(Address start, int size, int match, int stepSize);

    /**
     * Find a given 16-bit int (match) withing a given region.
     * This method searches at interface of stepSize large, only values at
     * the beginning of a step are matched.
     *
     * @param start    The address where to start searching.
     * @param size     The length of the region to search (in bytes)
     * @param match    The value to search for
     * @param stepSize The size of of step.
     * @return The address of the first match, or null if not found.
     */
    public Address findInt16(Address start, int size, int match, int stepSize);

    /**
     * Find a given 32-bit int (match) withing a given region.
     * This method searches at interface of stepSize large, only values at
     * the beginning of a step are matched.
     *
     * @param start    The address where to start searching.
     * @param size     The length of the region to search (in bytes)
     * @param match    The value to search for
     * @param stepSize The size of of step.
     * @return The address of the first match, or null if not found.
     */
    public Address findInt32(Address start, int size, int match, int stepSize);

    /**
     * Find a given 64-bit int (match) withing a given region.
     * This method searches at interface of stepSize large, only values at
     * the beginning of a step are matched.
     *
     * @param start    The address where to start searching.
     * @param size     The length of the region to search (in bytes)
     * @param match    The value to search for
     * @param stepSize The size of of step.
     * @return The address of the first match, or null if not found.
     */
    public Address findInt64(Address start, int size, long match, int stepSize);

    /**
     * Find a given 8-bit int array (match) withing a given region.
     * This method searches at interface of stepSize large, only values at
     * the beginning of a step are matched.
     *
     * @param start       The address where to start searching.
     * @param size        The length of the region to search (in bytes)
     * @param match       The value to search for
     * @param matchOffset
     * @param matchLength
     * @param stepSize    The size of of step.
     * @return The address of the first match, or null if not found.
     */
    public Address findInt8Array(Address start, int size, byte[] match, int matchOffset, int matchLength, int stepSize);

    /**
     * Find a given 16-bit int array (match) withing a given region.
     * This method searches at interface of stepSize large, only values at
     * the beginning of a step are matched.
     *
     * @param start       The address where to start searching.
     * @param size        The length of the region to search (in bytes)
     * @param match       The value to search for
     * @param matchOffset
     * @param matchLength
     * @param stepSize    The size of of step.
     * @return The address of the first match, or null if not found.
     */
    public Address findInt16Array(Address start, int size, char[] match, int matchOffset, int matchLength,
                                  int stepSize);

    /**
     * Find a given 16-bit int array (match) withing a given region.
     * This method searches at interface of stepSize large, only values at
     * the beginning of a step are matched.
     *
     * @param start       The address where to start searching.
     * @param size        The length of the region to search (in bytes)
     * @param match       The value to search for
     * @param matchOffset
     * @param matchLength
     * @param stepSize    The size of of step.
     * @return The address of the first match, or null if not found.
     */
    public Address findInt16Array(Address start, int size, short[] match, int matchOffset, int matchLength,
                                  int stepSize);

    /**
     * Find a given 32-bit int array (match) withing a given region.
     * This method searches at interface of stepSize large, only values at
     * the beginning of a step are matched.
     *
     * @param start       The address where to start searching.
     * @param size        The length of the region to search (in bytes)
     * @param match       The value to search for
     * @param matchOffset
     * @param matchLength
     * @param stepSize    The size of of step.
     * @return The address of the first match, or null if not found.
     */
    public Address findInt32Array(Address start, int size, int[] match, int matchOffset, int matchLength, int stepSize);

    /**
     * Find a given 64-bit int array (match) withing a given region.
     * This method searches at interface of stepSize large, only values at
     * the beginning of a step are matched.
     *
     * @param start       The address where to start searching.
     * @param size        The length of the region to search (in bytes)
     * @param match       The value to search for
     * @param matchOffset
     * @param matchLength
     * @param stepSize    The size of of step.
     * @return The address of the first match, or null if not found.
     */
    public Address findInt64Array(Address start, int size, long[] match, int matchOffset, int matchLength,
                                  int stepSize);
}
