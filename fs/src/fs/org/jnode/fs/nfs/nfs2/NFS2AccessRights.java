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
 
package org.jnode.fs.nfs.nfs2;

import java.io.IOException;
import java.security.Principal;

import org.jnode.fs.FSAccessRights;

public class NFS2AccessRights extends NFS2Object implements FSAccessRights {

    private NFS2Entry entry;

    public NFS2AccessRights(NFS2FileSystem fileSystem, NFS2Entry entry) {
        super(fileSystem);
        this.entry = entry;
    }

    public boolean canExecute() {

        return false;

    }

    public boolean canRead() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean canWrite() {
        // TODO Auto-generated method stub
        return false;
    }

    public Principal getOwner() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean setExecutable(boolean enable, boolean owneronly) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean setReadable(boolean enable, boolean owneronly) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean setWritable(boolean enable, boolean owneronly) {
        // TODO Auto-generated method stub
        return false;
    }

}
