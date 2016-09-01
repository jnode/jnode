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

import org.jnode.fs.hfsplus.HfsPlusBSDInfo;
import org.jnode.fs.hfsplus.HfsUtils;
import org.jnode.util.BigEndian;

public class CatalogFolder {
    /* Types */
    public static final int RECORD_TYPE_FOLDER = 0x0001;
    public static final int RECORD_TYPE_FOLDER_THREAD = 0x0003;

    /**
     * The folder type set on hardlinks - 'fdrp'.
     */
    public static final int HARDLINK_FOLDER_TYPE = 0x66647270;

    /**
     * The creator set on hardlinks - 'MACS'.
     */
    public static final int HARDLINK_CREATOR = 0x4d414353;

    public static final int CATALOG_FOLDER_SIZE = 88;

    private int recordType;
    private int flags;
    private long valence;
    private CatalogNodeId folderId;
    private long createDate;
    private long contentModDate;
    private long attrModDate;
    private long accessDate;
    private long backupDate;
    private HfsPlusBSDInfo permissions;

    /**
     * @param src
     */
    public CatalogFolder(final byte[] src) {
        byte[] data = new byte[88];
        System.arraycopy(src, 0, data, 0, CATALOG_FOLDER_SIZE);
        recordType = BigEndian.getInt16(data, 0);
        flags = BigEndian.getUInt16(data, 2);
        valence = BigEndian.getUInt32(data, 4);
        folderId = new CatalogNodeId(data, 8);
        createDate = BigEndian.getUInt32(data, 12);
        contentModDate = BigEndian.getUInt32(data, 16);
        attrModDate = BigEndian.getUInt32(data, 20);
        accessDate = BigEndian.getUInt32(data, 24);
        backupDate = BigEndian.getUInt32(data, 28);
        permissions = new HfsPlusBSDInfo(data, 32);
    }

    /**
     * @param valence
     * @param folderID
     */
    public CatalogFolder(int valence, CatalogNodeId folderID) {
        this.recordType = RECORD_TYPE_FOLDER;
        this.valence = valence;
        this.folderId = folderID;
        this.createDate = HfsUtils.getNow();
        this.contentModDate = HfsUtils.getNow();
        this.attrModDate = HfsUtils.getNow();
    }

    /**
     * Return bytes representation of the catalog folder.
     *
     * @return byte array representation.
     */
    public byte[] getBytes() {
        byte[] data = new byte[88];
        BigEndian.setInt16(data, 0, recordType);
        BigEndian.setInt32(data, 4, (int) valence);
        System.arraycopy(folderId.getBytes(), 0, data, 8, folderId.getBytes().length);
        BigEndian.setInt32(data, 12, (int) createDate);
        BigEndian.setInt32(data, 16, (int) contentModDate);
        BigEndian.setInt32(data, 20, (int) attrModDate);
        return data;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("Record type: ").append(recordType).append("\n");
        s.append("Valence: ").append(valence).append("\n");
        s.append("Folder ID: ").append(folderId.getId()).append("\n");
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

    public long getValence() {
        return valence;
    }

    public CatalogNodeId getFolderId() {
        return folderId;
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

    public long getAccessDate() {
        return HfsUtils.getDate(accessDate & 0xffffffffL, false) * 1000L;
    }

    public long getBackupDate() {
        return HfsUtils.getDate(backupDate & 0xffffffffL, false) * 1000L;
    }

    public HfsPlusBSDInfo getPermissions() {
        return permissions;
    }

    public void setRecordType(int recordType) {
        this.recordType = recordType;
    }

    public void setValence(long valence) {
        this.valence = valence;
    }

    public void setFolderId(CatalogNodeId folderId) {
        this.folderId = folderId;
    }

    public void setCreateDate(long createDate) {
        this.createDate = HfsUtils.getDate(createDate / 1000L, true);
    }

    public void setContentModDate(long contentModDate) {
        this.contentModDate = HfsUtils.getDate(contentModDate / 1000L, true);
    }

    public void setAttrModDate(long attrModDate) {
        this.attrModDate = HfsUtils.getDate(attrModDate / 1000L, true);
    }

    public void incrementValence() {
        this.setValence(this.getValence() + 1);
    }

}
