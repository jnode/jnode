/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
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
