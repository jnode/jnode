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
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.partitions.ibm.IBMPartitionTable;
import org.jnode.util.LittleEndian;
import org.jnode.util.NumberUtils;

/**
 * @author gvt
 * @author Tango
 */
public class BootSector {
    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(BootSector.class);

    private static final int IFAT12 = 12;
    private static final int IFAT16 = 16;
    private static final int IFAT32 = 32;

    private static final long SZFAT12 = 4085;
    private static final long SZFAT16 = 65525;

    private static final String SFAT12 = "FAT12";
    private static final String SFAT16 = "FAT16";
    private static final String SFAT32 = "FAT32";

    private byte[] sector;
    private int type;
    private boolean dirty;

    /*
     * Common fields, common offsets
     */
    private byte[] BS_jmpBoot;
    private String BS_OEMName;
    private int BPB_BytsPerSec;
    private int BPB_SecPerClus;
    private int BPB_RsvdSecCnt;
    private int BPB_NumFATs;
    private int BPB_RootEntCnt;
    private int BPB_TotSec16;
    private int BPB_Media;
    private int BPB_FATSz16;
    private int BPB_SecPerTrk;
    private int BPB_NumHeads;
    private long BPB_HiddSec;
    private long BPB_TotSec32;

    /*
     * FAT32 only fields;
     */
    private long BPB_FATSz32;
    private int BPB_ExtFlags;
    private int BPB_FSVer;
    private long BPB_RootClus;
    private int BPB_FSInfo;
    private int BPB_BkBootSec;
    private byte[] BPB_Reserved;

    /*
     * Common fields, different offsets
     */
    private int BS_DrvNum;
    private int BS_Reserved1;
    private int BS_BootSig;
    private long BS_VolID;
    private String BS_VolLab;
    private String BS_FilSysType;

    /*
     * computed fields
     */
    private long RootDirSectors;
    private long FATSz;
    private long FirstDataSector;
    private long TotSec;
    private long DataSec;
    private long CountOfClusters;

    public BootSector(int size) {
        sector = new byte[size];
        dirty = false;
    }

    public BootSector(byte[] sector) {
        this.sector = new byte[sector.length];
        System.arraycopy(sector, 0, this.sector, 0, sector.length);
        decode();
        dirty = false;
    }

    public boolean isaValidBootSector() {
        return IBMPartitionTable.containsPartitionTable(sector);
    }

    public synchronized void read(BlockDeviceAPI device) throws IOException {
        device.read(0, ByteBuffer.wrap(sector));
        decode();
        dirty = false;
    }

    public synchronized void write(BlockDeviceAPI device, long offset) throws IOException {
        //encode();//TODO: Notice here once (Changed Now)
        device.write(offset, ByteBuffer.wrap(sector));

        dirty = false;
    }

    private void compute() {
        RootDirSectors = ((BPB_RootEntCnt * 32) + (BPB_BytsPerSec - 1)) / BPB_BytsPerSec;

        if (BPB_FATSz16 != 0)
            FATSz = BPB_FATSz16;
        else
            FATSz = BPB_FATSz32;

        if (isFat32()) {
            FirstDataSector = BPB_RsvdSecCnt + (BPB_NumFATs * FATSz) + RootDirSectors;
        } else {
            FirstDataSector = BPB_RsvdSecCnt + (BPB_NumFATs * FATSz);
        }

        if (BPB_TotSec16 != 0)
            TotSec = BPB_TotSec16;
        else
            TotSec = BPB_TotSec32;

        DataSec = TotSec - (BPB_RsvdSecCnt + (BPB_NumFATs * FATSz) + RootDirSectors);

        CountOfClusters = DataSec / BPB_SecPerClus;

        if (CountOfClusters < SZFAT12)
            type = IFAT12;
        else if (CountOfClusters < SZFAT16)
            type = IFAT16;
        else
            type = IFAT32;
    }

    public void decode() {
        BS_jmpBoot = getBytes(0, 3);
        BS_OEMName = getString(3, 8);
        BPB_BytsPerSec = get16(11);
        BPB_SecPerClus = get8(13);
        BPB_RsvdSecCnt = get16(14);
        BPB_NumFATs = get8(16);
        BPB_RootEntCnt = get16(17);
        BPB_TotSec16 = get16(19);
        BPB_Media = get8(21);
        BPB_FATSz16 = get16(22);
        BPB_SecPerTrk = get16(24);
        BPB_NumHeads = get16(26);
        BPB_HiddSec = get32(28);
        BPB_TotSec32 = get32(32);

        if (BPB_FATSz16 == 0)
            BPB_FATSz32 = get32(36);

        compute();

        if (!isFat32()) {
            BS_DrvNum = get8(36);
            BS_Reserved1 = get8(37);
            BS_BootSig = get8(38);
            BS_VolID = get32(39);
            BS_VolLab = getString(43, 11);
            BS_FilSysType = getString(54, 8);
        } else {
            BPB_ExtFlags = get16(40);
            BPB_FSVer = get16(42);
            BPB_RootClus = get32(44);
            BPB_FSInfo = get16(48);
            BPB_BkBootSec = get16(50);
            BPB_Reserved = getBytes(52, 12);

            BS_DrvNum = get8(64);
            BS_Reserved1 = get8(65);
            BS_BootSig = get8(66);
            BS_VolID = get32(67);
            BS_VolLab = getString(71, 11);
            BS_FilSysType = getString(82, 8);
        }
    }

    @SuppressWarnings("unused")
    public void encode() {
        setBytes(0, 3, BS_jmpBoot);
        setString(3, 8, BS_OEMName);
        set16(11, BPB_BytsPerSec);
        set8(13, BPB_SecPerClus);
        set16(14, BPB_RsvdSecCnt);
        set8(16, BPB_NumFATs);
        set16(17, BPB_RootEntCnt);
        set16(19, BPB_TotSec16);
        set8(21, BPB_Media);
        set16(22, BPB_FATSz16);
        set16(24, BPB_SecPerTrk);
        set16(26, BPB_NumHeads);
        set32(28, BPB_HiddSec);
        set32(32, BPB_TotSec32);

        if (!isFat32()) {
            set8(36, BS_DrvNum);
            set8(37, BS_Reserved1);
            set8(38, BS_BootSig);
            set32(39, BS_VolID);
            setString(43, 11, BS_VolLab);
            setString(54, 8, BS_FilSysType);
        } else {
            set32(36, BPB_FATSz32);
            set16(40, BPB_ExtFlags);
            set16(42, BPB_FSVer);
            set32(44, BPB_RootClus);
            set16(48, BPB_FSInfo);
            set16(50, BPB_BkBootSec);
            setBytes(52, 12, BPB_Reserved);

            set8(64, BS_DrvNum);
            set8(65, BS_Reserved1);
            set8(66, BS_BootSig);
            set32(67, BS_VolID);
            setString(71, 11, BS_VolLab);
            setString(82, 8, BS_FilSysType);
        }
    }

    protected int get8(int offset) {
        return LittleEndian.getUInt8(sector, offset);
    }

    protected void set8(int offset, int value) {
        LittleEndian.setInt8(sector, offset, value);
        dirty = true;
    }

    protected int get16(int offset) {
        return LittleEndian.getUInt16(sector, offset);
    }

    protected void set16(int offset, int value) {
        LittleEndian.setInt16(sector, offset, value);
        dirty = true;
    }

    protected long get32(int offset) {
        return LittleEndian.getUInt32(sector, offset);
    }

    protected void set32(int offset, long value) {
        LittleEndian.setInt32(sector, offset, (int) value);
        dirty = true;
    }

    protected String getString(int offset, int len) {
        StringBuilder b = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            int v = sector[offset + i];
            b.append((char) v);
        }
        return b.toString();
    }

    protected void setString(int offset, int len, String value) {
        for (int i = 0; i < len; i++) {
            char ch;
            if (i < value.length())
                ch = value.charAt(i);
            else
                ch = (char) 0;
            LittleEndian.setInt8(sector, offset + i, ch);
        }
        dirty = true;
    }

    protected byte[] getBytes(int offset, int len) {
        byte[] v = new byte[len];

        System.arraycopy(sector, offset, v, 0, len);

        return v;
    }

    protected void setBytes(int offset, int len, byte[] value) {
        System.arraycopy(value, 0, sector, offset, len);
        dirty = true;
    }

    public String fatType() {
        switch (type) {
            case IFAT12:
                return SFAT12;
            case IFAT16:
                return SFAT16;
            case IFAT32:
                return SFAT32;
            default:
                return "";
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean isFat12() {
        if (type == IFAT12)
            return true;
        else
            return false;
    }

    public boolean isFat16() {
        if (type == IFAT16)
            return true;
        else
            return false;
    }

    public boolean isFat32() {
        if (type == IFAT32)
            return true;
        else
            return false;
    }

    /**
     * @return the FAT size / type
     */
    public int fatSize() {
        return type;
    }

    /**
     *
     * @return BPB_Media
     */
    public int getMediumDescriptor() {
        return BPB_Media;
    }

    public long getSectorsPerFat() {
        return FATSz;
    }

    public int getBytesPerSector() {
        return BPB_BytsPerSec;
    }

    public int getClusterSize() {
        return BPB_SecPerClus * BPB_BytsPerSec;
    }

    public int getSectorsPerCluster() {
        return BPB_SecPerClus;
    }

    public int getNrReservedSectors() {
        return BPB_RsvdSecCnt;
    }

    public int getNrFats() {
        return BPB_NumFATs;
    }

    public long getRootDirectoryStartCluster() {
        return BPB_RootClus;
    }

    public long getCountOfClusters() {
        return CountOfClusters;
    }

    public long getFirstDataSector() {
        return FirstDataSector;
    }

    public long getNrRootDirEntries() {
        return BPB_RootEntCnt;
    }

    public String getVolumeLabel() {
        return BS_VolLab;
    }

    public long getVolumeId() {
        return BS_VolID;
    }

    public String getOEMName() {
        return BS_OEMName;
    }

    /**
     * The Setting methods are writing here.
     *
     */
    public void setBS_JmpBoot(byte[] BS_jmpBoot) {
        setBytes(0, 3, BS_jmpBoot);
    }

    public void setBS_OemName(String BS_OEMName) {
        setString(3, 8, BS_OEMName);
    }

    public void setBPB_BytesPerSector(int BPB_BytsPerSec) {
        set16(11, BPB_BytsPerSec);
    }

    public void setBPB_SecPerCluster(int BPB_SecPerClus) {
        set8(13, BPB_SecPerClus);
    }

    public void setBPB_RsvSecCount(int BPB_RsvdSecCnt) {
        set16(14, BPB_RsvdSecCnt);
    }

    public void setBPB_NoFATs(int BPB_NumFATs) {
        set8(16, BPB_NumFATs);
    }

    public void setBPB_RootEntCnt(int BPB_RootEntCnt) {
        set16(17, BPB_RootEntCnt);
    }

    public void setBPB_TotSec16(int BPB_TotSec16) {
        set16(19, BPB_TotSec16);
    }

    public void setBPB_MediumDescriptor(int BPB_Media) {
        set8(21, BPB_Media);
    }

    public void setBPB_FATSz16(int BPB_FATSz16) {
        set16(22, BPB_FATSz16);
    }

    public void setBPB_SecPerTrk(int BPB_SecPerTrk) {
        set16(24, BPB_SecPerTrk);
    }

    public void setBPB_NumHeads(int BPB_NumHeads) {
        set16(26, BPB_NumHeads);
    }

    public void setBPB_HiddSec(long BPB_HiddSec) {
        set32(28, BPB_HiddSec);
    }

    public void setBPB_TotSec32(long BPB_TotSec32) {
        set32(32, BPB_TotSec32);
    }

    public void setBPB_FATSz32(long BPB_FATSz32) {
        set32(36, BPB_FATSz32);
    }

    public void setBPB_ExtFlags(int BPB_ExtFlags) {
        set16(40, BPB_ExtFlags);
    }

    public void setBPB_FSVer(int BPB_FSVer) {
        set16(42, BPB_FSVer);
    }

    public void setBPB_RootClus(long BPB_RootClus) {
        set32(44, BPB_RootClus);
    }

    public void setBPB_FSInfo(int BPB_FSInfo) {
        set16(48, BPB_FSInfo);
    }

    public void setBPB_BkBootSec(int BPB_BkBootSec) {
        set16(50, BPB_BkBootSec);
    }

    public void setBPB_Reserved(byte[] BPB_Reserved) {
        setBytes(52, 12, BPB_Reserved);
    }

    public void setBS_DrvNum(int BS_DrvNum) {
        set8(64, BS_DrvNum);
    }

    public void setBS_Reserved1(int BS_Reserved1) {
        set8(65, BS_Reserved1);
    }

    public void setBS_BootSig(int BS_BootSig) {
        set8(66, BS_BootSig);
    }

    public void setBS_VolID(long BS_VolID) {
        set32(67, BS_VolID);
    }

    public void setBS_VolLab(String BS_VolLab) {
        setString(71, 11, BS_VolLab);
    }

    public void setBS_FilSysType(String BS_FilSysType) {
        setString(82, 8, BS_FilSysType);
    }

    public void setBS_Identifier(byte[] ident) {
        setBytes(510, 2, ident);
    }

    public String toString() {
        StrWriter out = new StrWriter();

        out.println("***********************  BootSector *************************");
        out.println("fatType\t\t" + fatType());
        out.println("isDirty\t\t" + isDirty());
        out.println("*************************************************************");
        out.println("BS_jmpBoot\t" + NumberUtils.hex(BS_jmpBoot, 0, 3));
        out.println("BS_OEMName\t" + BS_OEMName);
        out.println("BPB_BytsPerSec\t" + BPB_BytsPerSec);
        out.println("BPB_SecPerClus\t" + BPB_SecPerClus);
        out.println("BPB_RsvdSecCnt\t" + BPB_RsvdSecCnt);
        out.println("BPB_NumFATs\t" + BPB_NumFATs);
        out.println("BPB_RootEntCnt\t" + BPB_RootEntCnt);
        out.println("BPB_TotSec16\t" + BPB_TotSec16);
        out.println("BPB_Media\t" + NumberUtils.hex(BPB_Media, 2));
        out.println("BPB_FATSz16\t" + BPB_FATSz16);
        out.println("BPB_SecPerTrk\t" + BPB_SecPerTrk);
        out.println("BPB_NumHeads\t" + BPB_NumHeads);
        out.println("BPB_HiddSec\t" + BPB_HiddSec);
        out.println("BPB_TotSec32\t" + BPB_TotSec32);
        out.println();

        if (isFat32()) {
            out.println("BPB_FATSz32\t" + BPB_FATSz32);
            out.println("BPB_ExtFlags\t" + NumberUtils.hex(BPB_ExtFlags, 2));
            out.println("BPB_FSVer\t" + NumberUtils.hex(BPB_FSVer, 2));
            out.println("BPB_RootClus\t" + BPB_RootClus);
            out.println("BPB_FSInfo\t" + BPB_FSInfo);
            out.println("BPB_BkBootSec\t" + BPB_BkBootSec);
            out.println("BPB_Reserved\t" + NumberUtils.hex(BPB_Reserved, 0, 12));
            out.println();
        }

        out.println("BS_DrvNum\t" + NumberUtils.hex(BS_DrvNum, 2));
        out.println("BS_Reserved1\t" + NumberUtils.hex(BS_Reserved1, 2));
        out.println("BS_BootSig\t" + NumberUtils.hex(BS_BootSig, 2));
        out.println("BS_VolID\t" + NumberUtils.hex(BS_VolID, 8));
        out.println("BS_VolLab\t" + BS_VolLab);
        out.println("BS_FilSysType\t" + BS_FilSysType);
        out.println();
        out.println("RootDirSectors\t" + RootDirSectors);
        out.println("FATSz\t\t" + FATSz);
        out.println("FirstDataSector\t" + FirstDataSector);
        out.println("TotSec\t\t" + TotSec);
        out.println("DataSec\t\t" + DataSec);
        out.println("CountOfClusters\t" + CountOfClusters);
        out.print("*************************************************************");

        return out.toString();
    }
}
