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
 
package org.jnode.net.nfs.nfs2;

public class FileAttribute {

    public static final int NON_FILE = 0;

    public static final int FILE = 1;

    public static final int DIRECTORY = 2;

    public static final int BLOCK_SPECIAL_DEVICE = 3;

    public static final int CHARACTER_SPECIAL_DEVICE = 4;

    public static final int SYMBOLIC_LINK = 5;

    protected int type;

    protected int mode;

    protected int nlink;

    protected int uid;

    protected int gid;

    protected int size;

    protected int blocksize;

    protected int rdev;

    protected int blocks;

    protected int fsid;

    protected int fileId;

    protected Time lastAccessed;

    protected Time lastModified;

    protected Time lastStatusChanged;

    public void setType(int x) {
        this.type = x;
    }

    public int getType() {
        return this.type;
    }

    public void setMode(int x) {
        this.mode = x;
    }

    public int getMode() {
        return this.mode;
    }

    public void setNlink(int x) {
        this.nlink = x;
    }

    public int getNlink() {
        return this.nlink;
    }

    public void setUid(int x) {
        this.uid = x;
    }

    public int getUid() {
        return this.uid;
    }

    public void setGid(int x) {
        this.gid = x;
    }

    public int getGid() {
        return this.gid;
    }

    public void setSize(int x) {
        this.size = x;
    }

    public int getSize() {
        return this.size;
    }

    public void setBlocksize(int x) {
        this.blocksize = x;
    }

    public int getBlocksize() {
        return this.blocksize;
    }

    public void setRdev(int x) {
        this.rdev = x;
    }

    public int getRdev() {
        return this.rdev;
    }

    public void setBlocks(int x) {
        this.blocks = x;
    }

    public int getBlocks() {
        return this.blocks;
    }

    public void setFsid(int x) {
        this.fsid = x;
    }

    public int getFsid() {
        return this.fsid;
    }

    public void setFileId(int x) {
        this.fileId = x;
    }

    public int getFileId() {
        return this.fileId;
    }

    public void setLastAccessed(Time x) {
        this.lastAccessed = x;
    }

    public Time getLastAccessed() {
        return this.lastAccessed;
    }

    public void setLastModified(Time x) {
        this.lastModified = x;
    }

    public Time getLastModified() {
        return this.lastModified;
    }

    public void setLastStatusChanged(Time x) {
        this.lastStatusChanged = x;
    }

    public Time getLastStatusChanged() {
        return this.lastStatusChanged;
    }

    public FileAttribute() {
    }

}
