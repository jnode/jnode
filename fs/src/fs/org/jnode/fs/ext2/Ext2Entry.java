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

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.spi.AbstractFSEntry;

/**
 * @author Andras Nagy
 * 
 * In case of a directory, the data will be parsed to get the file-list by
 * Ext2Directory. In case of a regular file, no more processing is needed.
 * 
 * TODO: besides getFile() and getDirectory(), we will need getBlockDevice()
 * getCharacterDevice(), etc.
 */
public class Ext2Entry extends AbstractFSEntry {

    private final Logger log = Logger.getLogger(getClass());
    private INode iNode = null;
    private int type;

    public Ext2Entry(INode iNode, String name, int type, Ext2FileSystem fs, FSDirectory parent) {
        super(fs, null, parent, name, getFSEntryType(name, iNode));
        this.iNode = iNode;
        this.type = type;

        log.setLevel(Level.INFO);
        log.debug("Ext2Entry(iNode, name): name=" + name + (isDirectory() ? " is a directory " : "") +
                (isFile() ? " is a file " : ""));
    }

    public long getLastChanged() throws IOException {
        return iNode.getCtime();
    }

    public long getLastModified() throws IOException {
        return iNode.getMtime();
    }

    public long getLastAccessed() throws IOException {
        return iNode.getAtime();
    }

    public void setLastChanged(long lastChanged) throws IOException {
        iNode.setCtime(lastChanged);
    }

    public void setLastModified(long lastModified) throws IOException {
        iNode.setMtime(lastModified);
    }

    public void setLastAccessed(long lastAccessed) throws IOException {
        iNode.setAtime(lastAccessed);
    }

    /**
     * Returns the type.
     * 
     * @return int type. Valid types are Ext2Constants.EXT2_FT_*
     */
    public int getType() {
        return type;
    }

    INode getINode() {
        return iNode;
    }

    private static int getFSEntryType(String name, INode iNode) {
        int mode = iNode.getMode() & Ext2Constants.EXT2_S_IFMT;

        if ("/".equals(name))
            return AbstractFSEntry.ROOT_ENTRY;
        else if (mode == Ext2Constants.EXT2_S_IFDIR)
            return AbstractFSEntry.DIR_ENTRY;
        else if (mode == Ext2Constants.EXT2_S_IFREG || mode == Ext2Constants.EXT2_FT_SYMLINK)
            return AbstractFSEntry.FILE_ENTRY;
        else
            return AbstractFSEntry.OTHER_ENTRY;
    }
}
