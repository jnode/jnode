/*
 * $Id$
 */
package org.jnode.fs.iso9660;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class SupplementaryVolumeDescriptor extends VolumeDescriptor {
    
    /**
     * @param volume
     * @param buffer
     */
    public SupplementaryVolumeDescriptor(ISO9660Volume volume, byte[] buffer) {
        super(volume, buffer);
    }
}
