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
 
package org.jnode.fs.iso9660;

import java.io.PrintStream;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class VolumeDescriptor extends Descriptor {

    private final ISO9660Volume volume;

    private final int type;

    private final String standardIdentifier;

    /**
     * Initialize this instance.
     * 
     * @param volume
     * @param buffer
     */
    public VolumeDescriptor(ISO9660Volume volume, byte[] buffer) {
        this.volume = volume;
        this.type = getUInt8(buffer, 1);
        this.standardIdentifier = getDChars(buffer, 2, 5);
    }

    /**
     * @return Returns the standardIdentifier.
     */
    public final String getStandardIdentifier() {
        return this.standardIdentifier;
    }

    /**
     * @return Returns the type.
     */
    public final int getType() {
        return this.type;
    }

    /**
     * Gets the type of volume descriptor from the buffer.
     * 
     * @param buffer
     * @return
     */
    public static final int getType(byte[] buffer) {
        return getUInt8(buffer, 1);
    }

    /**
     * @return Returns the volume.
     */
    public final ISO9660Volume getVolume() {
        return this.volume;
    }

    public abstract void dump(PrintStream out);
}
