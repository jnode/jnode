/*
 * $Id$
 */
package org.jnode.fs.iso9660;

import java.io.IOException;

import org.jnode.util.BigEndian;
import org.jnode.util.LittleEndian;

/**
 * @author vali
 *  
 */
public class VolumeDescriptor {

    public static int VolumeSetTerminator_TYPE = 255;

    public static int PrimaryVolumeDescriptor_TYPE = 1;

    public static int SupplementaryVolumeDescriptor_TYPE = 2;

    private final int type;

    private final String standardIdentifier;

    private final String systemIdentifier;

    private final String volumeIdentifier;

    private final String volumeSetIdentifier;

    private final long numberOfLB;

    private final int LBSize;

    private final int pathTableSize;

    private final int locationOfTyp_L_PathTable;

    private final int locationOfOptionalTyp_L_PathTable;

    private final int locationOfTyp_M_PathTable;

    private final int locationOfOptionalTyp_M_PathTable;

    private final int volumeSetSize;

    private final EntryRecord rootDirectoryEntry;

    private final ISO9660Volume volume;

    public VolumeDescriptor(ISO9660Volume volume, byte[] buff)
            throws IOException {
        this.volume = volume;
        this.type = LittleEndian.getUInt8(buff, 0);
        this.standardIdentifier = new String(buff, 1, 5);
        this.systemIdentifier = new String(buff, 8, 31);
        this.volumeIdentifier = new String(buff, 40, 31);
        this.volumeSetIdentifier = new String(buff, 190, 127);
        this.numberOfLB = LittleEndian.getUInt32(buff, 80);
        this.volumeSetSize = LittleEndian.getUInt16(buff, 120);

        this.LBSize = LittleEndian.getUInt16(buff, 128);
        // path table info
        this.pathTableSize = (int) LittleEndian.getUInt32(buff, 132);
        this.locationOfTyp_L_PathTable = (int) LittleEndian
                .getUInt32(buff, 140);
        this.locationOfOptionalTyp_L_PathTable = (int) LittleEndian.getUInt32(
                buff, 144);
        this.locationOfTyp_M_PathTable = (int) BigEndian.getUInt32(buff, 148);
        this.locationOfOptionalTyp_M_PathTable = (int) BigEndian.getUInt32(
                buff, 152);

        this.rootDirectoryEntry = new EntryRecord(volume, buff, 156, "US-ASCII");
    }

    public void printOut() {
        System.out.println("Primary volume information: ");
        System.out.println("	- Standard Identifier: "
                + this.getStandardIdentifier());
        System.out.println("	- System Identifier: "
                + this.getSystemIdentifier());
        System.out.println("	- Volume Identifier: "
                + this.getVolumeIdentifier());
        System.out.println("	- Volume set Identifier: "
                + this.getVolumeSetIdentifier());
        System.out.println("	- Volume set size: " + this.getVolumeSetSize());
        System.out.println("	- Number of LBs: " + this.getNumberOfLB());
        System.out.println("	- Size of LBs: " + this.getLBSize());
        System.out.println("	- PathTable size: " + this.getPathTableSize());
        System.out.println("		- Location of L PathTable : "
                + this.getLocationOfTyp_L_PathTable());
        System.out.println("		- Location of Optional L PathTable : "
                + this.getLocationOfOptionalTyp_L_PathTable());
        System.out.println("		- Location of M PathTable : "
                + this.getLocationOfTyp_M_PathTable());
        System.out.println("		- Location of Optional M PathTable : "
                + this.getLocationOfOptionalTyp_M_PathTable());
        System.out.println("	- Root directory entry: ");
        System.out.println("		- Size: "
                + this.getRootDirectoryEntry().getLengthOfDirectoryEntry());
        System.out.println("		- Extended attribute size: "
                + this.getRootDirectoryEntry().getLengthOfExtendedAttribute());
        System.out.println("		- Location of the extent: "
                + this.getRootDirectoryEntry().getLocationOfExtent());
        //System.out.println(" - Length of the file identifier: " +
        // this.getRootDirectoryEntry().getLengthOfFileIdentifier());
        System.out.println("		- is directory: "
                + this.getRootDirectoryEntry().isDirectory());
        System.out.println("		- File identifier: "
                + this.getRootDirectoryEntry().getFileIdentifier());
        System.out.println("		- Data Length: "
                + this.getRootDirectoryEntry().getDataLength());
        System.out.println("		- File unit size: "
                + this.getRootDirectoryEntry().getFileUnitSize());

    }

    /**
     * @return Returns the numberOfLB.
     */
    public long getNumberOfLB() {
        return numberOfLB;
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
    public int getPathTableSize() {
        return pathTableSize;
    }

    /**
     * @return Returns the locationOfOptionalTyp_L_PathTable.
     */
    public int getLocationOfOptionalTyp_L_PathTable() {
        return locationOfOptionalTyp_L_PathTable;
    }

    /**
     * @return Returns the locationOfOptionalTyp_M_PathTable.
     */
    public int getLocationOfOptionalTyp_M_PathTable() {
        return locationOfOptionalTyp_M_PathTable;
    }

    /**
     * @return Returns the locationOfTyp_L_PathTable.
     */
    public int getLocationOfTyp_L_PathTable() {
        return locationOfTyp_L_PathTable;
    }

    /**
     * @return Returns the locationOfTyp_M_PathTable.
     */
    public int getLocationOfTyp_M_PathTable() {
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
     * @return Returns the volume.
     */
    public ISO9660Volume getVolume() {
        return volume;
    }

    /**
     * @return Returns the volumeSetIdentifier.
     */
    public String getVolumeSetIdentifier() {
        return volumeSetIdentifier;
    }

    /**
     * @return Returns the standardIdentifier.
     */
    public String getStandardIdentifier() {
        return standardIdentifier;
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

    /**
     * @return Returns the type.
     */
    public int getType() {
        return type;
    }
}