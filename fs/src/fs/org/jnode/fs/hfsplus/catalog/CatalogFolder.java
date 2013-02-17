/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
    
    public static final int CATALOG_FOLDER_SIZE = 88;

    private int recordType;
    private int valence;
    private CatalogNodeId folderId;
    private int createDate;
    private int contentModDate;
    private int attrModDate;
    private int accessDate;
    private int backupDate;
    private HfsPlusBSDInfo permissions;

    /**
     * 
     * @param src
     */
    public CatalogFolder(final byte[] src) {
        byte[] data = new byte[88];
        System.arraycopy(src, 0, data, 0, CATALOG_FOLDER_SIZE);
        recordType = BigEndian.getInt16(data, 0);
        valence = BigEndian.getInt32(data, 4);
        folderId = new CatalogNodeId(data, 8);
        createDate = BigEndian.getInt32(data, 12);
        contentModDate = BigEndian.getInt32(data, 16);
        attrModDate = BigEndian.getInt32(data, 20);
        accessDate = BigEndian.getInt32(data, 24);
        backupDate = BigEndian.getInt32(data, 28);
        permissions = new HfsPlusBSDInfo(data, 32);
    }

    /**
     * 
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
        BigEndian.setInt32(data, 4, valence);
        System.arraycopy(folderId.getBytes(), 0, data, 8, folderId.getBytes().length);
        BigEndian.setInt32(data, 12, createDate);
        BigEndian.setInt32(data, 16, contentModDate);
        BigEndian.setInt32(data, 20, attrModDate);
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

    public int getValence() {
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

    public void setValence(int valence) {
        this.valence = valence;
    }

    public void setFolderId(CatalogNodeId folderId) {
        this.folderId = folderId;
    }

    public void setCreateDate(long createDate) {
        this.createDate = (int) HfsUtils.getDate(createDate / 1000L, true);
    }

    public void setContentModDate(long contentModDate) {
        this.contentModDate = (int) HfsUtils.getDate(contentModDate / 1000L, true);
    }

    public void setAttrModDate(long attrModDate) {
        this.attrModDate = (int) HfsUtils.getDate(attrModDate / 1000L, true);
    }
    
    public void incrementValence(){
    	this.setValence(this.getValence() + 1);
    }
    
}
