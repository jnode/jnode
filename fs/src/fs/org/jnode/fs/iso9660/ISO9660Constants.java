/*
 * $Id$
 */
package org.jnode.fs.iso9660;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface ISO9660Constants {
    
    public static final char SEPARATOR1 = '.';
    public static final char SEPARATOR2 = ';';
    
    public static final String DEFAULT_ENCODING = "US-ASCII";
    
    public static interface VolumeDescriptorType {

        public static final int TERMINATOR = 255;

        public static final int BOOTRECORD = 0;

        public static final int PRIMARY_DESCRIPTOR = 1;

        public static final int SUPPLEMENTARY_DESCRIPTOR = 2;

        public static final int PARTITION_DESCRIPTOR = 3;
    }

}
