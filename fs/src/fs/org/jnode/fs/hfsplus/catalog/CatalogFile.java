/*
 * $Id$
 *
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

package org.jnode.fs.hfsplus.catalog;

import org.jnode.fs.hfsplus.ExtendedFileInfo;
import org.jnode.fs.hfsplus.FileInfo;
import org.jnode.fs.hfsplus.HFSPlusBSDInfo;
import org.jnode.fs.hfsplus.HFSPlusForkData;
import org.jnode.fs.hfsplus.HFSUtils;
import org.jnode.fs.hfsplus.HfsPlusConstants;
import org.jnode.util.BigEndian;

/**
 * This class implements catalog file structure use in the catalog to hold
 * information about a file on the volume.
 * 
 * @author Fabien Lesire
 * 
 */
public class CatalogFile {

    public static final int CATALOG_FILE_SIZE = 248;
    /** catalog record type, always RECORD_TYPE_FILE */
    private int recordType;
    /** */
    private int flags;
    /** the catalog node id of the file */
    private CatalogNodeId fileId;
    /** The date and time the file was created */
    private int createDate;
    /**  */
    private int contentModDate;
    /** */
    private int attrModDate;
    /** */
    private int accessDate;
    /** */
    private int backupDate;
    /** */
    private HFSPlusBSDInfo permissions;
    /** */
    private FileInfo userInfo;
    /** */
    private ExtendedFileInfo finderInfo;
    /** */
    private int textEncoding;
    /** data fork location and size */
    private HFSPlusForkData datas;
    /** resource fork location and size */
    private HFSPlusForkData resources;

    /**
     * 
     * @param src
     */
    public CatalogFile(final byte[] src) {
        byte[] data = new byte[CATALOG_FILE_SIZE];
        System.arraycopy(src, 0, data, 0, 248);
        recordType = BigEndian.getInt16(data, 0);
        flags = BigEndian.getInt16(data, 2);
        fileId = new CatalogNodeId(data, 8);
        createDate = BigEndian.getInt32(data, 12);
        contentModDate = BigEndian.getInt32(data, 16);
        attrModDate = BigEndian.getInt32(data, 20);
        datas = new HFSPlusForkData(data, 88);
        resources = new HFSPlusForkData(data, 168);
    }

    /**
     * 
     * @param flags
     * @param fileId
     * @param createDate
     * @param contentModDate
     * @param attrModDate
     * @param datas
     * @param resources
     */
    public CatalogFile(int flags, CatalogNodeId fileId, HFSPlusForkData datas, HFSPlusForkData resources) {
        this.recordType = HfsPlusConstants.RECORD_TYPE_FILE;
        this.flags = flags;
        this.fileId = fileId;
        this.createDate = HFSUtils.getNow();
        this.contentModDate = HFSUtils.getNow();
        this.attrModDate = HFSUtils.getNow();
        this.datas = datas;
        this.resources = resources;
    }

    /**
     * 
     * @return
     */
    public byte[] getBytes() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public final String toString() {
        StringBuffer s = new StringBuffer();
        s.append("Record type:").append(recordType).append("\t");
        s.append("File ID  :").append(fileId.getId()).append("\n");
        s.append("Creation Date :").append(
                HFSUtils.printDate(createDate, "EEE MMM d HH:mm:ss yyyy")).append("\n");
        s.append("Content Mod Date  :").append(
                HFSUtils.printDate(contentModDate, "EEE MMM d HH:mm:ss yyyy")).append("\n");
        s.append("Attr Mod Date  :").append(
                HFSUtils.printDate(attrModDate, "EEE MMM d HH:mm:ss yyyy")).append("\n");
        return s.toString();
    }

    public int getRecordType() {
        return recordType;
    }

    public int getFlags() {
        return flags;
    }

    public CatalogNodeId getFileId() {
        return fileId;
    }

    public int getCreateDate() {
        return createDate;
    }

    public int getContentModDate() {
        return contentModDate;
    }

    public int getAttrModDate() {
        return attrModDate;
    }

    public HFSPlusForkData getDatas() {
        return datas;
    }

    public HFSPlusForkData getResources() {
        return resources;
    }

    public int getAccessDate() {
        return accessDate;
    }

    public int getBackupDate() {
        return backupDate;
    }

    public HFSPlusBSDInfo getPermissions() {
        return permissions;
    }

    public FileInfo getUserInfo() {
        return userInfo;
    }

    public ExtendedFileInfo getFinderInfo() {
        return finderInfo;
    }

    public int getTextEncoding() {
        return textEncoding;
    }

}
