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

package org.jnode.fs.hfsplus;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jnode.fs.FSFile;
import org.jnode.fs.hfsplus.catalog.CatalogFile;
import org.jnode.fs.hfsplus.extent.ExtentDescriptor;
import org.jnode.fs.hfsplus.tree.LeafRecord;

public class HFSPlusFile extends HFSPlusEntry implements FSFile {

    private CatalogFile file;

    public HFSPlusFile(HfsPlusFileSystem fs, HFSPlusDirectory parent, String name, LeafRecord record) {
        super(fs, parent, name, record);
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
