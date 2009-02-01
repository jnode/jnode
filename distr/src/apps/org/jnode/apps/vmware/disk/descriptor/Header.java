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
 
package org.jnode.apps.vmware.disk.descriptor;

import org.jnode.apps.vmware.disk.IOUtils;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public class Header {
    /**
     * cid/content id when no parent
     */
    public static final long CID_NOPARENT = 0x0;

    private String version;
    private long contentID; // cid
    private long parentContentID; // parent cid (maybe be CID_NOPARENT)

    private CreateType createType;

    private String parentFileNameHint;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getContentID() {
        return contentID;
    }

    public void setContentID(long contentID) {
        this.contentID = contentID;
    }

    public long getParentContentID() {
        return parentContentID;
    }

    public void setParentContentID(long parentContentID) {
        this.parentContentID = parentContentID;
    }

    public CreateType getCreateType() {
        return createType;
    }

    public void setCreateType(CreateType createType) {
        this.createType = createType;
    }

    public String getParentFileNameHint() {
        return parentFileNameHint;
    }

    public void setParentFileNameHint(String parentFileNameHint) {
        this.parentFileNameHint = parentFileNameHint;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Header)) {
            return false;
        }

        Header h = (Header) obj;

        return this.version.equals(h.version) && (this.contentID == h.contentID) &&
                (this.parentContentID == h.parentContentID) &&
                this.createType.equals(h.createType) &&
                IOUtils.equals(this.parentFileNameHint, h.parentFileNameHint);
    }

    @Override
    public String toString() {
        return "Header[version:" + version + ",contentID:" + contentID + ",parentContentID:" +
                parentContentID + ",createType:" + createType + ",parentFileNameHint:" +
                parentFileNameHint + "]";
    }
}
