/*
 * $Id$
 *
 * JNode.org
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
 
package org.jnode.fs.ext2;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.jnode.fs.FileSystemException;

/**
 * A single directory record, i.e. the inode number and name of an entry in a
 * directory
 * 
 * @author Andras Nagy
 */
public class Ext2DirectoryRecord {
    private final Logger log = Logger.getLogger(getClass());
    /*
     * private int iNodeNr; private int recLen; private short nameLen; private
     * short type; private StringBuffer name;
     */
    private int offset;
    private byte[] data;
    private long fileOffset;
    private Ext2FileSystem fs;

    /**
     * @param data: the data that makes up the directory block
     * @param offset: the offset where the current DirectoryRecord begins within
     *            the block
     * @param fileOffset: the offset from the beginning of the directory file
     */
    public Ext2DirectoryRecord(Ext2FileSystem fs, byte[] data, int offset, int fileOffset) {
        this.fs = fs;
        this.data = data;
        this.offset = offset;
        this.fileOffset = fileOffset;

        // make a copy of the data
        synchronized (data) {
            byte[] newData = new byte[getRecLen()];
            System.arraycopy(data, offset, newData, 0, getRecLen());
            this.data = newData;
            setOffset(0);
        }
    }

    /**
     * Create a new Ext2DirectoryRecord from scratch (it can be retrieved with
     * getData())
     * 
     * @param iNodeNr
     * @param type
     * @param name
     */
    public Ext2DirectoryRecord(Ext2FileSystem fs, long iNodeNr, int type, String name) {
        this.offset = 0;
        this.fs = fs;
        data = new byte[8 + name.length()];
        setName(name);
        setINodeNr(iNodeNr);
        setType(type);
        setNameLen(name.length());
        int newLength = name.length() + 8;
        // record length is padded to n*4 bytes
        if (newLength % 4 != 0)
            newLength += 4 - newLength % 4;
        setRecLen(newLength);
    }

    public byte[] getData() {
        return data;
    }

    public int getOffset() {
        return offset;
    }

    private void setOffset(int offset) {
        this.offset = offset;
    }

    public long getFileOffset() {
        return fileOffset;
    }

    private void setFileOffset(long fileOffset) {
        this.fileOffset = fileOffset;
    }

    /**
     * Returns the fileType.
     * 
     * @return short
     */
    public synchronized int getType() {
        return Ext2Utils.get8(data, offset + 7);
    }

    private synchronized void setType(int type) {
        if (!fs.hasIncompatFeature(Ext2Constants.EXT2_FEATURE_INCOMPAT_FILETYPE))
            return;
        Ext2Utils.set8(data, offset + 7, type);
    }

    /**
     * Returns the iNodeNr.
     * 
     * @return long
     */
    public synchronized int getINodeNr() {
        return (int) Ext2Utils.get32(data, offset);
    }

    private synchronized void setINodeNr(long nr) {
        Ext2Utils.set32(data, offset, nr);
    }

    /**
     * Returns the name.
     * 
     * @return StringBuffer
     */
    public synchronized String getName() {
        StringBuffer name = new StringBuffer();
        if (getINodeNr() != 0) {
            // TODO: character conversion??
            for (int i = 0; i < getNameLen(); i++)
                name.append((char) Ext2Utils.get8(data, offset + 8 + i));
            log.debug("Ext2DirectoryRecord(): iNode=" + getINodeNr() + ", name=" + name);
        }
        return name.toString();
    }

    private synchronized void setName(String name) {
        for (int i = 0; i < name.length(); i++)
            Ext2Utils.set8(data, offset + 8 + i, name.charAt(i));
    }

    /**
     * Returns the recLen.
     * 
     * @return int
     */
    public synchronized int getRecLen() {
        return Ext2Utils.get16(data, offset + 4);
    }

    private synchronized void setRecLen(int len) {
        Ext2Utils.set16(data, offset + 4, len);
    }

    private synchronized int getNameLen() {
        return Ext2Utils.get8(data, offset + 6);
    }

    private synchronized void setNameLen(int len) {
        Ext2Utils.set8(data, offset + 6, len);
    }

    /**
     * The last directory record's length is set such that it extends until the
     * end of the block. This method truncates it when a new record is added to
     * the directory
     */
    protected synchronized void truncateRecord() {
        int newLength = getNameLen() + 8;
        // record length is padded to n*4 bytes
        if (newLength % 4 != 0)
            newLength += 4 - newLength % 4;
        setRecLen(newLength);
        log.debug("truncateRecord(): newLength: " + newLength);
    }

    /**
     * The last directory record's length is set such that it extends until the
     * end of the block. This method extends the directory record to this
     * length. The directoryRecord's <code>fileOffset</code> will be set to
     * <code>beginning</code>.
     * 
     * @param beginning: the offset where the record begins
     * @param end: the offset where the record should end (usually the size a
     *            filesystem block)
     */
    protected synchronized void expandRecord(long beginning, long end) throws FileSystemException {
        log.debug("expandRecord(" + beginning + ", " + end + ")");
        if (beginning + getNameLen() + 8 < end) {
            // the record fits in the block
            setRecLen((int) (end - beginning));
            // pad the end of the record with zeroes
            byte[] newData = new byte[getRecLen()];
            Arrays.fill(newData, 0, getRecLen(), (byte) 0);
            System.arraycopy(data, getOffset(), newData, 0, getNameLen() + 8);
            setOffset(0);
            setFileOffset(beginning);
            data = newData;
        } else {
            throw new FileSystemException("The directory record does not fit into the block!");
        }
        log.debug("expandRecord(): newLength: " + getRecLen());
    }
}
