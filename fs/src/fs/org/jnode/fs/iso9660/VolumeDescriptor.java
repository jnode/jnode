/*
 * $Id$
 */
package org.jnode.fs.iso9660;

import org.jnode.util.LittleEndian;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class VolumeDescriptor {

    public static interface Type {

        public static final int TERMINATOR = 255;

        public static final int BOOTRECORD = 0;
        
        public static final int PRIMARY_DESCRIPTOR = 1;

        public static final int SUPPLEMENTARY_DESCRIPTOR = 2;
        
        public static final int PARTITION_DESCRIPTOR = 3;
    }

    private final ISO9660Volume volume;
    private final int type;
    private final String standardIdentifier;

    /**
     * Initialize this instance.
     * @param volume
     * @param buffer
     */
    public VolumeDescriptor(ISO9660Volume volume, byte[] buffer) {
        this.volume = volume;
        this.type = LittleEndian.getUInt8(buffer, 0);
        this.standardIdentifier = new String(buffer, 1, 5).trim();
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
     * @param buffer
     * @return
     */
    public static final int getType(byte[] buffer) {
        return LittleEndian.getUInt8(buffer, 0);        
    }
    
    /**
     * @return Returns the volume.
     */
    public final ISO9660Volume getVolume() {
        return this.volume;
    }
}
