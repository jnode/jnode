/*
 * $Id$
 */
package org.jnode.fs.ntfs;

import java.io.IOException;

import org.jnode.util.NumberUtils;

/**
 * MFT file record structure.
 * 
 * @author Chira
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
class FileRecord extends NTFSRecord {

    private FileNameAttribute fileNameAttribute;

    /**
     * Initialize this instance.
     * 
     * @param volume
     * @param buffer
     */
    public FileRecord(NTFSVolume volume, byte[] buffer, int offset) throws IOException {
        super(volume, buffer, offset);
        //  check for the magic numberb to see if we have a filerecord
        if (getMagic() != Magic.FILE) { throw new IOException(
                "Invalid magic found: " + getMagic()); }
    }

    /**
     * Gets the allocated size of the FILE record in bytes.
     * 
     * @return Returns the alocatedSize.
     */
    public long getAlocatedSize() {
        return getUInt32(0x1C);
    }

    /**
     * Gets the real size of the FILE record in bytes.
     * 
     * @return Returns the realSize.
     */
    public long getRealSize() {
        return getUInt32(0x18);
    }

    /**
     * Is this a directory.
     * 
     * @return
     */
    public boolean isDirectory() {
        return ((this.getFlags() & 0x02) != 0);
    }

    /**
     * Gets the hard link count.
     * 
     * @return Returns the hardLinkCount.
     */
    public int getHardLinkCount() {
        return getUInt16(0x12);
    }

    /**
     * Gets the byte offset to the first attribute in this mft record from the
     * start of the mft record.
     * 
     * @return Returns the firtAttributeOffset.
     */
    public int getFirtAttributeOffset() {
        return getUInt16(0x14);
    }

    /**
     * Gets the flags.
     * 
     * @return Returns the flags.
     */
    public int getFlags() {
        return getUInt16(0x16);
    }

    /**
     * Gets the Next Attribute Id.
     * 
     * @return Returns the nextAttributeID.
     */
    public int getNextAttributeID() {
        return getUInt16(0x28);
    }

    /**
     * Gets the number of times this mft record has been reused.
     * 
     * @return Returns the sequenceNumber.
     */
    public int getSequenceNumber() {
        return getUInt16(0x10);
    }

    /**
     * @return Returns the updateSequenceOffset.
     */
    public int getUpdateSequenceOffset() {
        return getUInt16(0x4);
    }

    /**
     * Gets the name of this file.
     * 
     * @return
     */
    public String getFileName() {
        final FileNameAttribute fnAttr = getFileNameAttribute();
        if (fnAttr != null) {
            return fnAttr.getFileName();
        } else {
            return null;
        }
    }

    /**
     * Gets the filename attribute of this filerecord.
     * 
     * @return
     */
    public FileNameAttribute getFileNameAttribute() {
        if (fileNameAttribute == null) {
            fileNameAttribute = (FileNameAttribute) getAttribute(NTFSAttribute.Types.FILE_NAME);
        }
        return fileNameAttribute;
    }

    /**
     * Gets an attribute in this filerecord with a given id.
     * 
     * @param attrTypeID
     * @return
     */
    public NTFSAttribute getAttribute(int attrTypeID) {
        log.debug("getAttribute(0x" + NumberUtils.hex(attrTypeID, 4) + ")");
        int offset = this.getFirtAttributeOffset();
        while (true) {
            final int type = getUInt32AsInt(offset + 0x00);
            if (type == 0xFFFFFFFF) {
                // The end of the attribute list
                break;
            } else if (type == attrTypeID) {
                return NTFSAttribute.getAttribute(this, offset);
            } else {
                // Skip and go to the next
                final int attrLength = getUInt32AsInt(offset + 0x04);
                offset += attrLength;
            }
        }

        if (attrTypeID != NTFSAttribute.Types.ATTRIBUTE_LIST) {
            if (getAttribute(NTFSAttribute.Types.ATTRIBUTE_LIST) != null) {
                log.info("Has $ATTRIBUTE_LIST attribute");
            }
        }

        log.info("getAttribute(0x" + NumberUtils.hex(attrTypeID, 4)
                + ") not found");
        return null;
    }

    public void readData(long fileOffset, byte[] dest, int off, int len)
            throws IOException {
        final NTFSAttribute data = this.getAttribute(NTFSAttribute.Types.DATA);
        if (data.isResident()) {
            final NTFSResidentAttribute resData = (NTFSResidentAttribute) data;
            final int attrLength = resData.getAttributeLength();
            if (attrLength < len) { throw new IOException(
                    "File data(" + attrLength
                            + "b) is not large enough to read:" + len + "b"); }
            resData.getData(resData.getAttributeOffset() + (int) fileOffset,
                    dest, off, len);
            return;
        }

        // caclulate start and end cluster

        final int clusterSize = getVolume().getClusterSize();
        final long startCluster = (fileOffset / clusterSize);
        final int nrClusters = (int) ((len + (fileOffset % clusterSize)) / clusterSize) + 1;
        final NTFSNonResidentAttribute nresData = (NTFSNonResidentAttribute) data;

        final byte[] tmp = new byte[ nrClusters * clusterSize];
        nresData.readVCN(startCluster, tmp, 0, nrClusters);
        System.arraycopy(tmp, (int) fileOffset % clusterSize, dest, off, len);
    }
}