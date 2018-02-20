/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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
 
package org.jnode.fs.ntfs.usnjrnl;

/**
 * The common parts of a USN journal record.
 *
 * @param <T> the type of MFT record.
 * @author Luke Quinane
 */
public interface UsnRecordCommonHeader<T> {

    /**
     * Gets the size of this entry.
     *
     * @return the size.
     */
    long getSize();

    /**
     * Gets the major version number.
     *
     * @return the major version number.
     */
    int getMajorVersion();

    /**
     * Gets the minor version number.
     *
     * @return the minor version number.
     */
    int getMinorVersion();

    /**
     * Gets the MFT reference.
     *
     * @return the MFT reference.
     */
    T getMftReference();

    /**
     * Gets the parent MFT reference.
     *
     * @return the parent MFT reference.
     */
    T getParentMtfReference();
}
