/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.fs.jfat;

import org.jnode.util.BinaryScaleFactor;


public enum ClusterSize {
    _1Kb(1), _2Kb(2), _4Kb(4), _8Kb(8), _16Kb(16), _32Kb(32), _64Kb(64);

    private final int size;

    private ClusterSize(int sizeInKb) {
        size = (int) (sizeInKb * BinaryScaleFactor.K.getMultiplier()); //Converted into KB
    }

    public final int getSize() {
        return size;
    }
}
