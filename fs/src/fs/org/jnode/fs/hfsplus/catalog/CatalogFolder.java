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

import org.jnode.fs.hfsplus.HFSUtils;
import org.jnode.fs.hfsplus.HfsPlusConstants;
import org.jnode.util.BigEndian;

public class CatalogFolder {
    
    public static final int CATALOG_FOLDER_SIZE = 88;
    
    private byte[] data;

    public CatalogFolder(final byte[] src) {
        data = new byte[88];
        System.arraycopy(src, 0, data, 0, CATALOG_FOLDER_SIZE);
    }

    /**
     * Create a new catalog folder.
     * 
     * @param folderId
     * 
     */
    public CatalogFolder() {
        data = new byte[88];
        BigEndian.setInt16(data, 0, HfsPlusConstants.RECORD_TYPE_FOLDER);
    }

    public final int getRecordType() {
        return BigEndian.getInt16(data, 0);
    }

    public final void setValence(int valence) {
        BigEndian.setInt32(data, 4, valence);
    }

    public final int getValence() {
        return BigEndian.getInt32(data, 4);
    }

    public final CatalogNodeId getFolderId() {
        return new CatalogNodeId(data, 8);
    }

    public final void setFolderId(CatalogNodeId folderId) {
        System.arraycopy(folderId.getBytes(), 0, data, 8,
                folderId.getBytes().length);
    }

    public final int getCreateDate() {
        return BigEndian.getInt32(data, 12);
    }

    public void setCreateDate(int time) {
        BigEndian.setInt32(data, 12, time);
    }

    public final int getContentModDate() {
        return BigEndian.getInt32(data, 16);
    }

    public void setContentModDate(int time) {
        BigEndian.setInt32(data, 16, time);
    }

    public final int getAttrModDate() {
        return BigEndian.getInt32(data, 20);
    }

    public void setAttrModDate(int time) {
        BigEndian.setInt32(data, 20, time);
    }

    public byte[] getBytes() {
        return data;
    }

    public final String toString() {
        StringBuffer s = new StringBuffer();
        s.append("Record type: ").append(getRecordType()).append("\n");
        s.append("Valence: ").append(getValence()).append("\n");
        s.append("Folder ID: ").append(getFolderId().getId()).append("\n");
        s.append("Creation Date :").append(
                HFSUtils.printDate(getCreateDate(), "EEE MMM d HH:mm:ss yyyy"))
                .append("\n");
        s.append("Content Mod Date  :").append(
                HFSUtils.printDate(getContentModDate(),
                        "EEE MMM d HH:mm:ss yyyy")).append("\n");
        s.append("Attr Mod Date  :")
                .append(
                        HFSUtils.printDate(getAttrModDate(),
                                "EEE MMM d HH:mm:ss yyyy")).append("\n");
        return s.toString();
    }
}
