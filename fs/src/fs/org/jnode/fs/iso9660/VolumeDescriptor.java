/*
 * $Id$
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