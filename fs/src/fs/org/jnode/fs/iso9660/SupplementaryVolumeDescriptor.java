/*
 * $Id$
 */
package org.jnode.fs.iso9660;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.jnode.system.BootLog;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class SupplementaryVolumeDescriptor extends VolumeDescriptor {

    private final int flags;
    private final String encoding;
    private final boolean encodingKnown;

    private final String systemIdentifier;
    private final String volumeIdentifier;
    private final long spaceSize;
    private final String escapeSequences;
    private final EntryRecord rootDirectoryEntry;

    /**
     * @param volume
     * @param buffer
     */
    public SupplementaryVolumeDescriptor(ISO9660Volume volume, byte[] buffer) throws UnsupportedEncodingException {
        super(volume, buffer);
        this.flags = getUInt8(buffer, 8);
        this.escapeSequences = getDChars(buffer, 89, 121-89);
        
        String encoding;
        boolean encodingKnown;
        try {
            encoding = getEncoding(escapeSequences);
            encodingKnown = true;
        } catch (UnsupportedEncodingException ex) {
            encoding = DEFAULT_ENCODING;
            encodingKnown = false;          
            BootLog.warn("Unsupported encoding, escapeSequences: '" + escapeSequences + "'");
        }
        this.encoding = encoding;
        this.encodingKnown = encodingKnown;
        this.systemIdentifier = getAChars(buffer, 9, 41-9, encoding);
        this.volumeIdentifier = getDChars(buffer, 41, 73-41, encoding);
        this.spaceSize = getUInt32Both(buffer, 81);
        this.rootDirectoryEntry = new EntryRecord(volume, buffer, 157,
                encoding);
    }        
    
    public void dump(PrintStream out) {
        out.println("Supplementary Volume Descriptor");
        out.println("\tFlags             " + flags);
        //out.println("\tEscape sequences  " + escapeSequences);
        //out.println("\tEncoding          " + encoding);
        out.println("\tSystemIdentifier  " + systemIdentifier);
        out.println("\tVolumeIdentifier  " + volumeIdentifier);
        out.println("\tVolume Space Size " + spaceSize);
    }
    
    /**
     * @return Returns the escapeSequences.
     */
    public final String getEscapeSequences() {
        return this.escapeSequences;
    }
    /**
     * @return Returns the flags.
     */
    public final int getFlags() {
        return this.flags;
    }
    /**
     * @return Returns the spaceSize.
     */
    public final long getSpaceSize() {
        return this.spaceSize;
    }
    /**
     * @return Returns the systemIdentifier.
     */
    public final String getSystemIdentifier() {
        return this.systemIdentifier;
    }
    /**
     * @return Returns the volumeIdentifier.
     */
    public final String getVolumeIdentifier() {
        return this.volumeIdentifier;
    }
    
    /**
     * Gets a derived encoding name from the given escape sequences.
     * @param escapeSequences
     * @return
     */
    private String getEncoding(String escapeSequences) throws UnsupportedEncodingException {
        if (escapeSequences.equals("%/@")) {
            // UCS-2 level 1
            return "UTF-16BE";
        } else if (escapeSequences.equals("%/C")) {
            // UCS-2 level 2
            return "UTF-16BE";
        } else if (escapeSequences.equals("%/E")) {
            // UCS-2 level 3
            return "UTF-16BE";
        } else {
            // Unknown
            throw new UnsupportedEncodingException(escapeSequences);
        }
    }
    
    /**
     * Is the used encoding known to this system.
     * @return
     */
    public final boolean isEncodingKnown() {
        return encodingKnown;
    }
    
    /**
     * @return Returns the encoding.
     */
    public final String getEncoding() {
        return this.encoding;
    }
    
    /**
     * @return Returns the rootDirectoryEntry.
     */
    public final EntryRecord getRootDirectoryEntry() {
        return this.rootDirectoryEntry;
    }
}