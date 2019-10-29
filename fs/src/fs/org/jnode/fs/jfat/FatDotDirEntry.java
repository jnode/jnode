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
 
package org.jnode.fs.jfat;

import java.io.IOException;


/**
 * @author gvt
 */
public class FatDotDirEntry extends FatShortDirEntry {

    private static final byte dot = '.';

    public FatDotDirEntry(FatFileSystem fs, FatMarshal entry, int index) {
        super(fs, entry, index);
        if (!isDirectory())
            throw new UnsupportedOperationException();
    }

    public FatDotDirEntry(FatFileSystem fs, boolean dotDot, FatShortDirEntry parent,
            int startCluster) throws IOException {
        super(fs);
        init(parent, startCluster);
        if (dotDot) {
            lName[1] = dot;
        }
        encodeName();
    }

    private void init(FatShortDirEntry parent, int startCluster) throws IOException {
        setNameCase(new FatCase());
        setAttr(new FatAttr());
        setDirectory();
        setCreated(parent.getCreated());
        clearName();
        lName[0] = dot;
        setLastAccessed(parent.getLastAccessed());
        setLastModified(parent.getLastModified());
        setStartCluster(startCluster);
        setLength(0);
    }
}
