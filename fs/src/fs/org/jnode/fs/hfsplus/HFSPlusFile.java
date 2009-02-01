/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.fs.hfsplus;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jnode.fs.hfsplus.catalog.CatalogFile;
import org.jnode.fs.hfsplus.extent.ExtentDescriptor;
import org.jnode.fs.hfsplus.tree.LeafRecord;
import org.jnode.fs.spi.AbstractFSFile;

public class HFSPlusFile extends AbstractFSFile {
    private LeafRecord record;
    private CatalogFile file;

    public HFSPlusFile(final HFSPlusEntry e) {
        super((HfsPlusFileSystem) e.getFileSystem());
        this.record = e.getRecord();
        this.file = new CatalogFile(record.getData());
    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public final long getLength() {
        return file.getDataFork().getTotalSize();
    }

    @Override
    public final void read(final long fileOffset, final ByteBuffer dest) throws IOException {
        HfsPlusFileSystem fs = (HfsPlusFileSystem) getFileSystem();
        for (ExtentDescriptor d : file.getDataFork().getExtents()) {
            if (d.getStartBlock() != 0 && d.getBlockCount() != 0) {
                long firstOffset = d.getStartBlock() * fs.getVolumeHeader().getBlockSize();
                fs.getApi().read(firstOffset, dest);
            }
        }
    }

    @Override
    public void write(final long fileOffset, final ByteBuffer src) throws IOException {
        // TODO Auto-generated method stub

    }

    public void setLength(final long length) throws IOException {
        // TODO Auto-generated method stub

    }

}
