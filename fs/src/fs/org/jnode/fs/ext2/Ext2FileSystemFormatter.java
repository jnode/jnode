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
 
package org.jnode.fs.ext2;

import org.jnode.driver.Device;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.Formatter;
import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;
import javax.naming.NameNotFoundException;

/**
 * @author Andras Nagy
 */
public class Ext2FileSystemFormatter extends Formatter<Ext2FileSystem> {
    private BlockSize blockSize;

    /**
     *
     * @param blockSizeKb size of blocks in KB
     */
    public Ext2FileSystemFormatter(BlockSize blockSize) {
        super(new Ext2FileSystemType());
        this.blockSize = blockSize;
    }

    /*
     * (non-Javadoc)
     * @see org.jnode.fs.Formatter#format(org.jnode.driver.Device)
     */
    public synchronized Ext2FileSystem format(Device device) throws FileSystemException {
        try {
            FileSystemService fSS = InitialNaming.lookup(FileSystemService.NAME);
            Ext2FileSystemType type = fSS.getFileSystemType(Ext2FileSystemType.ID);
            Ext2FileSystem fs = new Ext2FileSystem(device, false, type);
            fs.create(blockSize);
            return fs;
        } catch (NameNotFoundException e) {
            throw new FileSystemException(e);
        }
    }
}
