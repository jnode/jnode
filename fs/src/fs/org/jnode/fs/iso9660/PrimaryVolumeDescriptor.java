/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.fs.iso9660;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Wrapper for a Primary Volume Descriptor.
 * 
 * See ISO9660 section 8.4.
 * 
 * @author vali
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PrimaryVolumeDescriptor extends VolumeDescriptor {

    private final String systemIdentifier;

    private final String volumeIdentifier;

    private final String volumeSetIdentifier;

    private final long spaceSize;

    private final int LBSize;

    private final long pathTableSize;

    private final long locationOfTyp_L_PathTable;

    private final long locationOfOptionalTyp_L_PathTable;

    private final long locationOfTyp_M_PathTable;

    private final long locationOfOptionalTyp_M_PathTable;

    private final int volumeSetSize;

    private final EntryRecord rootDirectoryEntry;

    /**
     * Initialize this instance.
     * 
     * @param volume
     * @param buffer
     * @throws IOException
     */
    public PrimaryVolumeDescriptor(ISO9660Volume volume, byte[] buffer)
            throws IOException {
        super(volume, buffer);
        this.systemIdentifier = getAChars(buffer, 9, 41 - 9);
        this.volumeIdentifier = getDChars(buffer, 41, 73 - 41);
        this.volumeSetIdentifier = getDChars(buffer, 191, 319 - 191);
        this.spaceSize = getUInt32Both(buffer, 81);
        this.volumeSetSize = getUInt16Both(buffer, 121);

        this.LBSize = getUInt16Both(buffer, 129);
        // path table info
        this.pathTableSize = getUInt32Both(buffer, 133);
        this.locationOfTyp_L_PathTable = getUInt32LE(buffer, 141);
        this.locationOfOptionalTyp_L_PathTable = getUInt32LE(buffer, 145);
        this.locationOfTyp_M_PathTable = getUInt32BE(buffer, 149);
        this.locationOfOptionalTyp_M_PathTable = getUInt32BE(buffer, 153);

        this.rootDirectoryEntry = new EntryRecord(volume, buffer, 157,
                DEFAULT_ENCODING);
    }

    public void dump(PrintStream out) {
        out.println("Primary volume information: ");
        out.println("	- Standard Identifier: " + this.getStandardIdentifier());
        out.println("	- System Identifier: " + this.getSystemIdentifier());
        out.println("	- Volume Identifier: " + this.getVolumeIdentifier());
        out.println("	- Volume set Identifier: "
                + this.getVolumeSetIdentifier());
        out.println("	- Volume set size: " + this.getVolumeSetSize());
        out.println("	- Number of LBs: " + this.getSpaceSize());
        out.println("	- Size of LBs: " + this.getLBSize());
        out.println("	- PathTable size: " + this.getPathTableSize());
        out.println("		- Location of L PathTable : "
                + this.getLocationOfTyp_L_PathTable());
        out.println("		- Location of Optional L PathTable : "
                + this.getLocationOfOptionalTyp_L_PathTable());
        out.println("		- Location of M PathTable : "
                + this.getLocationOfTyp_M_PathTable());
        out.println("		- Location of Optional M PathTable : "
                + this.getLocationOfOptionalTyp_M_PathTable());
        out.println("	- Root directory entry: ");
        out.println("		- Size: "
                + this.getRootDirectoryEntry().getLengthOfDirectoryEntry());
        out.println("		- Extended attribute size: "
                + this.getRootDirectoryEntry().getLengthOfExtendedAttribute());
        out.println("		- Location of the extent: "
                + this.getRootDirectoryEntry().getLocationOfExtent());
        //out.println(" - Length of the file identifier: " +
        // this.getRootDirectoryEntry().getLengthOfFileIdentifier());
        out.println("		- is directory: "
                + this.getRootDirectoryEntry().isDirectory());
        out.println("		- File identifier: "
                + this.getRootDirectoryEntry().getFileIdentifier());
        out.println("		- Data Length: "
                + this.getRootDirectoryEntry().getDataLength());
        out.println("		- File unit size: "
                + this.getRootDirectoryEntry().getFileUnitSize());

    }

    /**
     * @return Returns the numberOfLB.
     */
    public long getSpaceSize() {
        return spaceSize;
    }

    /**
     * @return Returns the lBSize.
     */
    public int getLBSize() {
        return LBSize;
    }

    /**
     * @return Returns the patheTableSize.
     */
    public long getPathTableSize() {
        return pathTableSize;
    }

    /**
     * @return Returns the locationOfOptionalTyp_L_PathTable.
     */
    public long getLocationOfOptionalTyp_L_PathTable() {
        return locationOfOptionalTyp_L_PathTable;
    }

    /**
     * @return Returns the locationOfOptionalTyp_M_PathTable.
     */
    public long getLocationOfOptionalTyp_M_PathTable() {
        return locationOfOptionalTyp_M_PathTable;
    }

    /**
     * @return Returns the locationOfTyp_L_PathTable.
     */
    public long getLocationOfTyp_L_PathTable() {
        return locationOfTyp_L_PathTable;
    }

    /**
     * @return Returns the locationOfTyp_M_PathTable.
     */
    public long getLocationOfTyp_M_PathTable() {
        return locationOfTyp_M_PathTable;
    }

    /**
     * @return Returns the rootDirectoryEntry.
     */
    public EntryRecord getRootDirectoryEntry() {
        return rootDirectoryEntry;
    }

    /**
     * @return Returns the volumeSetSize.
     */
    public int getVolumeSetSize() {
        return volumeSetSize;
    }

    /**
     * @return Returns the volumeSetIdentifier.
     */
    public String getVolumeSetIdentifier() {
        return volumeSetIdentifier;
    }

    /**
     * @return Returns the systemIdentifier.
     */
    public String getSystemIdentifier() {
        return systemIdentifier;
    }

    /**
     * @return Returns the volumeIdentifier.
     */
    public String getVolumeIdentifier() {
        return volumeIdentifier;
    }
}
