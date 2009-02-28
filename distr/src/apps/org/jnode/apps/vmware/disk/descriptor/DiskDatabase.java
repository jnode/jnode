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
 
package org.jnode.apps.vmware.disk.descriptor;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare).
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public class DiskDatabase {
    private AdapterType adapterType;
    private int sectors;
    private int heads;
    private int cylinders;

    /**
     * 
     * @return
     */
    public AdapterType getAdapterType() {
        return adapterType;
    }

    /**
     * 
     * @param adapterType
     */
    public void setAdapterType(AdapterType adapterType) {
        this.adapterType = adapterType;
    }

    /**
     * 
     * @return
     */
    public int getSectors() {
        return sectors;
    }

    /**
     * 
     * @param sectors
     */
    public void setSectors(int sectors) {
        this.sectors = sectors;
    }

    /**
     * 
     * @return
     */
    public int getHeads() {
        return heads;
    }

    /**
     * 
     * @param heads
     */
    public void setHeads(int heads) {
        this.heads = heads;
    }

    /**
     * 
     * @return
     */
    public int getCylinders() {
        return cylinders;
    }

    /**
     * 
     * @param cylinders
     */
    public void setCylinders(int cylinders) {
        this.cylinders = cylinders;
    }

    /**
     * 
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DiskDatabase)) {
            return false;
        }

        DiskDatabase d = (DiskDatabase) obj;

        return (this.adapterType == d.adapterType) && (this.sectors == d.sectors) &&
                (this.heads == d.heads) && (this.cylinders == d.cylinders);
    }

    /**
     * 
     */
    @Override
    public String toString() {
        return "DiskDatabase[adapterType:" + adapterType + ",sectors:" + sectors + ",heads:" +
                heads + ",cylinders:" + cylinders + "]";
    }
}
