/*
 * $Id$
 */
package org.jnode.fs.iso9660;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.jnode.util.LittleEndian;

/**
 * @author Chira
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class EntryRecord {

    private final int entryLength;
    private final int extAttributeLength;
    private final long extentLocation;
    private final int dataLength;
    private final int fileUnitSize;
    private final int flags;
    private final int interleaveSize;
    private final String identifier;
    
    private final ISO9660Volume volume;
    private final String encoding;

    /**
     * Initialize this instance.
     * @param volume
     * @param buff
     * @param offset
     */
    public EntryRecord(ISO9660Volume volume, byte[] buff, int offset, String encoding) {
        this.volume = volume;
        this.encoding = encoding;
        this.entryLength = LittleEndian.getUInt8(buff, offset+0);
        this.extAttributeLength = LittleEndian.getUInt8(buff, offset+1);
        this.extentLocation = LittleEndian.getUInt32(buff, offset+2);
        this.dataLength = (int)LittleEndian.getUInt32(buff, offset+10);
        this.fileUnitSize = LittleEndian.getUInt8(buff, offset+26);
        this.interleaveSize = LittleEndian.getUInt8(buff, offset+27);
       	this.flags = LittleEndian.getUInt8(buff, offset + 25);
       	// This must be after flags, because of isDirectory.
       	this.identifier = getFileIdentifier(buff, offset, isDirectory(), encoding);
    }

    public void readFileData(long offset, byte[] buffer, int bufferOffset,
            int size) throws IOException {
        volume.readFromLBN(this.getLocationOfExtent(), offset, buffer,
                bufferOffset, size);
    }

    public byte[] getExtentData() throws IOException {
        byte[] buffer = new byte[ this.getDataLength()];
        volume.readFromLBN(this.getLocationOfExtent(), 0, buffer, 0, this
                .getDataLength());
        return buffer;
    }

    public int getLengthOfDirectoryEntry() {
        return entryLength;
    }

    public int getLengthOfExtendedAttribute() {
        return extAttributeLength;
    }

    public long getLocationOfExtent() {
        return extentLocation;
    }

    public int getDataLength() {
        return dataLength;
    }

    public final boolean isDirectory() {
        return (flags & 0x03) != 0;
    }

    public final boolean isLastEntry() {
        return (flags & 0x40) == 0;
    }

    public final int getFlags() {
        return flags;
    }

    public final int getFileUnitSize() {
        return fileUnitSize;
    }

    public final int getInterleaveSize() {
        return interleaveSize;
    }

    public String getFileIdentifier() {
        return identifier;
    }

    /**
     * @return Returns the volume.
     */
    public final ISO9660Volume getVolume() {
        return volume;
    }

    private final String getFileIdentifier(byte[] buff, int offset, boolean isDir, String encoding) {
        final int fidLength = LittleEndian.getUInt8(buff, offset+32);
        if (isDir) {
            final int buff33 = LittleEndian.getUInt8(buff, offset+33);
            if ((fidLength == 1) && (buff33 == 0x00)) {
                return ".";
            } else if ((fidLength == 1) && (buff33 == 0x01)) {
                return "..";
            }
        }
        try {
            return new String(buff, offset+33, fidLength, encoding);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * @return Returns the encoding.
     */
    public final String getEncoding() {
        return this.encoding;
    }
}