/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
    public PrimaryVolumeDescriptor(ISO9660Volume volume, byte[] buffer) throws IOException {
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

        this.rootDirectoryEntry = new EntryRecord(volume, buffer, 157, DEFAULT_ENCODING);
    }

    public void dump(PrintStream out) {
        out.println("Primary volume information: ");
        out.println("\t- Standard Identifier: " + this.getStandardIdentifier());
        out.println("\t- System Identifier: " + this.getSystemIdentifier());
        out.println("\t- Volume Identifier: " + this.getVolumeIdentifier());
        out.println("\t- Volume set Identifier: " + this.getVolumeSetIdentifier());
        out.println("\t- Volume set size: " + this.getVolumeSetSize());
        out.println("\t- Number of LBs: " + this.getSpaceSize());
        out.println("\t- Size of LBs: " + this.getLBSize());
        out.println("\t- PathTable size: " + this.getPathTableSize());
        out.println("\t\t- Location of L PathTable : " + this.getLocationOfTyp_L_PathTable());
        out.println("\t\t- Location of Optional L PathTable : " +
                this.getLocationOfOptionalTyp_L_PathTable());
        out.println("\t\t- Location of M PathTable : " + this.getLocationOfTyp_M_PathTable());
        out.println("\t\t- Location of Optional M PathTable : " +
                this.getLocationOfOptionalTyp_M_PathTable());
        out.println("\t- Root directory entry: ");
        out.println("\t\t- Size: " + this.getRootDirectoryEntry().getLengthOfDirectoryEntry());
        out.println("\t\t- Extended attribute size: " +
                this.getRootDirectoryEntry().getLengthOfExtendedAttribute());
        out.println("\t\t- Location of the extent: " +
                this.getRootDirectoryEntry().getLocationOfExtent());
        //out.println(" - Length of the file identifier: " +
        // this.getRootDirectoryEntry().getLengthOfFileIdentifier());
        out.println("\t\t- is directory: " + this.getRootDirectoryEntry().isDirectory());
        out.println("\t\t- File identifier: " + this.getRootDirectoryEntry().getFileIdentifier());
        out.println("\t\t- Data Length: " + this.getRootDirectoryEntry().getDataLength());
        out.println("\t\t- File unit size: " + this.getRootDirectoryEntry().getFileUnitSize());

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
