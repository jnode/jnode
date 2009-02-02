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
 
package org.jnode.net.nfs.nfs2;

public class FileSystemAttribute {

    private long transferSize;

    private long blockSize;

    private long blockCount;

    private long freeBlockCount;

    private long availableBlockCount;

    public long getTransferSize() {
        return transferSize;
    }

    public void setTransferSize(long transferSize) {
        this.transferSize = transferSize;
    }

    public long getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(long blockSize) {
        this.blockSize = blockSize;
    }

    public long getBlockCount() {
        return blockCount;
    }

    public void setBlockCount(long blockCount) {
        this.blockCount = blockCount;
    }

    public long getFreeBlockCount() {
        return freeBlockCount;
    }

    public void setFreeBlockCount(long freeBlockCount) {
        this.freeBlockCount = freeBlockCount;
    }

    public long getAvailableBlockCount() {
        return availableBlockCount;
    }

    public void setAvailableBlockCount(long availableBlockCount) {
        this.availableBlockCount = availableBlockCount;
    }

    @Override
    public String toString() {

        StringBuffer buffer = new StringBuffer();
        buffer.append("FileSystemAttribute ");
        buffer.append("transferSize:");
        buffer.append(transferSize);
        buffer.append(";blockSize:");
        buffer.append(blockSize);
        buffer.append(";blockCount:");
        buffer.append(blockCount);
        buffer.append(";freeBlockCount:");
        buffer.append(freeBlockCount);
        return buffer.toString();
    }

}
