/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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
import org.jnode.fs.hfsplus.HfsPlusBSDInfo;
import org.jnode.fs.hfsplus.HfsPlusForkData;
import org.jnode.fs.hfsplus.HfsUtils;
import org.jnode.util.BigEndian;

/**
 * This class implements catalog file structure use in the catalog to hold
 * information about a file on the volume.
 *
 * @author Fabien Lesire
 */
public class CatalogFile {

    public static final int RECORD_TYPE_FILE = 0x0002;
    public static final int RECORD_TYPE_FILE_THREAD = 0x0004;
    public static final int FLAGS_HARDLINK_CHAIN = 0x0020;

    public static final int CATALOG_FILE_SIZE = 248;
    /**
     * catalog record type, always RECORD_TYPE_FILE
     */
    private int recordType;
    /** */
    private int flags;
    /**
     * the catalog node id of the file
     */
    private CatalogNodeId fileId;
    /**
     * The date and time the file was created
     */
    private long createDate;
    /**  */
    private long contentModDate;
    /** */
    private long attrModDate;
    /** */
    private long accessDate;
    /** */
    private long backupDate;
    /** */
    private HfsPlusBSDInfo permissions;
    /** */
    private FileInfo userInfo;
    /** */
    private ExtendedFileInfo finderInfo;
    /** */
    private int textEncoding;
    /**
     * data fork location and size
     */
    private HfsPlusForkData datas;
    /**
     * resource fork location and size
     */
    private HfsPlusForkData resources;

    /**
     * @param src
     */
    public CatalogFile(final byte[] src) {
        byte[] data = new byte[CATALOG_FILE_SIZE];
        System.arraycopy(src, 0, data, 0, 248);
        recordType = BigEndian.getInt16(data, 0);
        flags = BigEndian.getUInt16(data, 2);
        fileId = new CatalogNodeId(data, 8);
        createDate = BigEndian.getUInt32(data, 12);
        contentModDate = BigEndian.getUInt32(data, 16);
        attrModDate = BigEndian.getUInt32(data, 20);
        accessDate = BigEndian.getUInt32(data, 24);
        backupDate = BigEndian.getUInt32(data, 28);
        permissions = new HfsPlusBSDInfo(data, 32);
        userInfo = new FileInfo(data, 48);
        datas = new HfsPlusForkData(fileId, true, data, 88);
        resources = new HfsPlusForkData(fileId, false, data, 168);
    }

    /**
     * @param flags
     * @param fileId
     * @param datas
     * @param resources
     */
    public CatalogFile(int flags, CatalogNodeId fileId, HfsPlusForkData datas, HfsPlusForkData resources) {
        this.recordType = RECORD_TYPE_FILE;
        this.flags = flags;
        this.fileId = fileId;
        this.createDate = HfsUtils.getNow();
        this.contentModDate = HfsUtils.getNow();
        this.attrModDate = HfsUtils.getNow();
        this.datas = datas;
        this.resources = resources;
    }

    /**
     * @return a serious case of nothing much at all
     */
    public byte[] getBytes() {
        return null;
    }

    public final String toString() {
        StringBuffer s = new StringBuffer();
        s.append("Record type:").append(recordType).append("\t");
        s.append("File ID  :").append(fileId.getId()).append("\n");
        s.append("Creation Date :").append(
            HfsUtils.printDate(createDate, "EEE MMM d HH:mm:ss yyyy")).append("\n");
        s.append("Content Mod Date  :").append(
            HfsUtils.printDate(contentModDate, "EEE MMM d HH:mm:ss yyyy")).append("\n");
        s.append("Attr Mod Date  :").append(
            HfsUtils.printDate(attrModDate, "EEE MMM d HH:mm:ss yyyy")).append("\n");
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

    public long getCreateDate() {
        return HfsUtils.getDate(createDate & 0xffffffffL, false) * 1000L;
    }

    public long getContentModDate() {
        return HfsUtils.getDate(contentModDate & 0xffffffffL, false) * 1000L;
    }

    public long getAttrModDate() {
        return HfsUtils.getDate(attrModDate & 0xffffffffL, false) * 1000L;
    }

    public HfsPlusForkData getDatas() {
        return datas;
    }

    public HfsPlusForkData getResources() {
        return resources;
    }

    public long getAccessDate() {
        return HfsUtils.getDate(accessDate & 0xffffffffL, false) * 1000L;
    }

    public long getBackupDate() {
        return HfsUtils.getDate(backupDate & 0xffffffffL, false) * 1000L;
    }

    public HfsPlusBSDInfo getPermissions() {
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
