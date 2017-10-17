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
 
package org.jnode.fs.ext2;

import java.io.IOException;
import org.apache.log4j.Logger;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntryLastAccessed;
import org.jnode.fs.FSEntryLastChanged;
import org.jnode.fs.spi.AbstractFSEntry;

/**
 * @author Andras Nagy
 *         <p/>
 *         In case of a directory, the data will be parsed to get the file-list
 *         by Ext2Directory. In case of a regular file, no more processing is
 *         needed.
 *         <p/>
 *         TODO: besides getFile() and getDirectory(), we will need
 *         getBlockDevice() getCharacterDevice(), etc.
 */
public class Ext2Entry extends AbstractFSEntry implements FSEntryLastChanged, FSEntryLastAccessed {

    private static final Logger log = Logger.getLogger(Ext2Entry.class);
    private INode iNode = null;
    private long directoryRecordId;
    private int type;

    public Ext2Entry(INode iNode, long directoryRecordId, String name, int type, Ext2FileSystem fs,
                     FSDirectory parent) {
        super(fs, null, parent, name, getFSEntryType(name, iNode));
        this.iNode = iNode;
        this.directoryRecordId = directoryRecordId;
        this.type = type;

        log.debug("Ext2Entry(iNode, name): name=" + name +
            (isDirectory() ? " is a directory " : "") + (isFile() ? " is a file " : ""));
    }

    public long getLastChanged() throws IOException {
        return iNode.getCtime() * 1000;
    }

    @Override
    public String getId() {
        return Long.toString(iNode.getINodeNr()) + '-' + Long.toString(directoryRecordId);
    }

    public long getLastModified() throws IOException {
        return iNode.getMtime() * 1000;
    }

    public long getLastAccessed() throws IOException {
        return iNode.getAtime() * 1000;
    }

    public void setLastChanged(long lastChanged) throws IOException {
        iNode.setCtime(lastChanged / 1000);
    }

    public void setLastModified(long lastModified) throws IOException {
        iNode.setMtime(lastModified / 1000);
    }

    public void setLastAccessed(long lastAccessed) throws IOException {
        iNode.setAtime(lastAccessed / 1000);
    }

    /**
     * Returns the type.
     *
     * @return int type. Valid types are Ext2Constants.EXT2_FT_*
     */
    public int getType() {
        return type;
    }

    public INode getINode() {
        return iNode;
    }

    private static int getFSEntryType(String name, INode iNode) {
        int mode = iNode.getMode() & Ext2Constants.EXT2_S_IFMT;

        if ("/".equals(name))
            return AbstractFSEntry.ROOT_ENTRY;
        else if (mode == Ext2Constants.EXT2_S_IFDIR)
            return AbstractFSEntry.DIR_ENTRY;
        else if (mode == Ext2Constants.EXT2_S_IFREG || mode == Ext2Constants.EXT2_S_IFLNK ||
            mode == Ext2Constants.EXT2_S_IFIFO || mode == Ext2Constants.EXT2_S_IFCHR ||
            mode == Ext2Constants.EXT2_S_IFBLK)
            return AbstractFSEntry.FILE_ENTRY;
        else
            return AbstractFSEntry.OTHER_ENTRY;
    }
}
