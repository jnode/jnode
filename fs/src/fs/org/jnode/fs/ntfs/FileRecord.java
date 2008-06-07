/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 
package org.jnode.fs.ntfs;

import java.io.IOException;
import java.util.List;

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
        //  check for the magic number to see if we have a filerecord
        if (getMagic() != Magic.FILE) {
            throw new IOException("Invalid magic found: " + getMagic());
        }
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
     * XXX: Returning an iterator of multiple might be better.
     *
     * @param attrTypeID the type ID of the attribute we're looking for.
     * @return the attribute.
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
            final AttributeListAttribute attributeList =
                    (AttributeListAttribute) getAttribute(NTFSAttribute.Types.ATTRIBUTE_LIST);
            if (attributeList != null) {
                log.info("Has $ATTRIBUTE_LIST attribute");

                try {
                    final List<AttributeListEntry> entries = attributeList.getEntries(attrTypeID);
                    if (!entries.isEmpty()) {
                        log.debug("Found entries via $ATTRIBUTE_LIST: " + entries);
                        MasterFileTable mft = getVolume().getMFT();
                        NTFSAttribute attribute = null;
                        for (AttributeListEntry entry : entries) {
                            // XXX: This is a little crappy as we should already know the exact offset
                            //      of this attribute.  This just makes the best of the API we already
                            //      use everywhere else.
                            NTFSAttribute attr =
                                    mft.getRecord(entry.getFileReferenceNumber()).getAttribute(
                                            attrTypeID);

                            if (attribute == null) {
                                // First attribute encountered.
                                attribute = attr;
                                if (!(attr instanceof NTFSNonResidentAttribute)) {
                                    log
                                            .info("Don't know how to glue together resident attributes, "
                                                    + "returning the first one alone");
                                    break;
                                }
                            } else {
                                // Subsequent attribute.
                                if (attr instanceof NTFSNonResidentAttribute) {
                                    log.debug("Appending data runs onto parent attribute");
                                    ((NTFSNonResidentAttribute) attribute)
                                            .appendDataRuns(((NTFSNonResidentAttribute) attr)
                                                    .getDataRuns());
                                } else {
                                    log.info("Don't know how to glue a resident attribute onto "
                                            + "a non-resident one, skipping this attribute.");
                                }
                            }
                        }

                        return attribute;
                    }
                } catch (IOException e) {
                    log.error("IO error getting locating attribute list entry", e);
                }
            }
        }

        log.info("getAttribute(0x" + NumberUtils.hex(attrTypeID, 4) + ") not found");
        return null;
    }

    public void readData(long fileOffset, byte[] dest, int off, int len) throws IOException {
        final NTFSAttribute data = this.getAttribute(NTFSAttribute.Types.DATA);
        if (data.isResident()) {
            final NTFSResidentAttribute resData = (NTFSResidentAttribute) data;
            final int attrLength = resData.getAttributeLength();
            if (attrLength < len) {
                throw new IOException("File data(" + attrLength +
                        "b) is not large enough to read:" + len + "b");
            }
            resData.getData(resData.getAttributeOffset() + (int) fileOffset, dest, off, len);
            return;
        }

        // calculate start and end cluster

        final int clusterSize = getVolume().getClusterSize();
        final long startCluster = (fileOffset / clusterSize);
        final int nrClusters = (int) ((len + (fileOffset % clusterSize)) / clusterSize) + 1;
        final NTFSNonResidentAttribute nresData = (NTFSNonResidentAttribute) data;

        final byte[] tmp = new byte[nrClusters * clusterSize];
        nresData.readVCN(startCluster, tmp, 0, nrClusters);
        System.arraycopy(tmp, (int) fileOffset % clusterSize, dest, off, len);
    }
}
