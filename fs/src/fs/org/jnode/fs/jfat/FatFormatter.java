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
 
package org.jnode.fs.jfat;
/*
 * $Id: FatFormatter.java  2007-07-26 +0100 (s,26 JULY 2007) Tanmoy Deb $
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

import static java.lang.Integer.toHexString;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;

import org.apache.log4j.Logger;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.util.LittleEndian;

/**
 * <p>
 * According to the FAT32 Documents.
 * <ul>
 * <li>FAT32 disk structure:</li>
 * <li>Sector 0 Boot Sector</li>
 * <li>Sector 1 FSInfo</li>
 * <li>Sector 2 More boot code</li>
 * <li>Sector 3 unused</li>
 * <li>Sector 4 unused</li>
 * <li>Sector 5 unused</li>
 * <li>Sector 6 Backup boot sector</li>
 * <li>Sector 7 Backup FSInfo sector</li>
 * <li>Sector 8 Backup 'more boot code</li>
 * <li>Reserved sectors up to the FAT at sector 32</li>
 * </ul>
 * 
 * @author tango
 * 
 */
public class FatFormatter {
    private static final Logger log = Logger.getLogger(FatFormatter.class);

    /** The Device Identifier for Floppy Device */
    public static final int FLOPPY_DESC = 0xf0;
    
    /** The Device Identifier for Hard Disk Device */
    public static final int HD_DESC = 0xf8;
    
    /** The Device Identifier for RAM Disk Device */
    public static final int RAMDISK_DESC = 0xfa;
    
    /** The Size of Fat. */
    private int FatSize;
    
    /** The Number of the Fat in system. */
    public final int NumOfFATs = 2;
    
    /** The boot sector backing up. */
    public final int BackupBootSector = 6;
    
    /** The number of reserved sectors. */
    public final int ReservedSectorCount = 32;
    
    /** The Tracks Per cylinder. */
    private static final int NB_HEADS = 255;
    
    /** The sector per track. */
    private static final int SECTOR_PER_TRACK = 63;
    
    /** The volume label. */
    private static final String VOL_LABEL = "NO NAME    ";
    
    /** The FAT version label. */
    private static final String FAT_LABEL = "FAT32   ";
    
    /** The Identifier of the Boot Sector */
    public final byte[] BS_Identifier = {(byte) 0x55, (byte) 0xAA};
    
    /** The First 3 Bytes of the BootSector */
    public final byte[] BS_jmpBoot = {(byte) 0xEB, (byte) 0x5A, (byte) 0x90};
    
    /**
     * This lead signature is used to validate that this is in fact an FSInfo
     * sector.
     */
    private final int FSI_LeadSig = 0x41615252;
    
    /**
     * The signature that is more localized in the sector to the location of the
     * fields that are used.
     */
    private final int FSI_StrucSig = 0x61417272;
    
    /**
     * Contains the last known free cluster count on the volume. If the value is
     * 0xFFFFFFFF, then the free count is unknown and must be computed.
     */
    private int FSI_FreeCount = 0xffffffff;
    
    /**
     * This is a hint for the FAT driver. It indicates the cluster number at
     * which the driver should start looking for free clusters.
     */
    private final int FSI_Nxt_Free = 3; // A confusion here but OK now for
                                        // setting the Info at 3rd sec
    /**
     * This trail signature is used to validate that this is in fact an FSInfo
     * sector
     */
    private final int FSI_TrailSig = 0xaa550000;
    
    /** The media ID at the reserved cluster one. */
    private final int ReservedSector_0 = 0x0ffffff8;
    
    /** The End Of Chain ID at the reserved sector second. */
    private final int ReservedSector_1 = 0xffffffff;
    
    /** The End of cluster chain of the root directory. */
    private final int ReservedSector_2 = 0x0fffffff;
    
    /** The array for the reserved sector. */
    private byte[] reservedSector;

    /**
     * The Hard Disk's formatting logic implementation by JFAT. This Version
     * only support to the Hard Disks.
     * 
     * @throws IOException
     */
    public static FatFormatter HDDFormatter(int sectorSize, int nbTotalSectors,
            ClusterSize clusterSize, int hiddenSectors, BlockDeviceAPI api) throws IOException {

        return new FatFormatter(HD_DESC, sectorSize, nbTotalSectors, SECTOR_PER_TRACK, NB_HEADS,
                clusterSize, hiddenSectors, api);
    }

    /**
     * The Constructor for the HDD devices in the JNode system.
     * 
     * @throws IOException
     * 
     */
    public FatFormatter(int mediumDescriptor, int sectorSize, int nbTotalSectors,
            int sectorsPerTrack, int nbHeads, ClusterSize ClusterSize, int hiddenSectors,
            BlockDeviceAPI api) throws IOException {

        FatFsInfo fsInfo = new FatFsInfo(sectorSize);
        BootSector bs = new BootSector(sectorSize);
        api.flush();
        int DiskSize = getDiskSize(nbTotalSectors, sectorSize);
        int SectorPerCluster = get_spc(ClusterSize, sectorSize);
        int UserAreaSize =
                getUserAreaSize(nbTotalSectors, ReservedSectorCount, NumOfFATs, ClusterSize);
        this.FatSize =
                getFATSizeSectors(nbTotalSectors, ReservedSectorCount, SectorPerCluster, NumOfFATs,
                        sectorSize);

        // fill out the boot sector and fs info
        bs.setBS_JmpBoot(this.BS_jmpBoot);
        bs.setBS_OemName("MSWIN4.1");
        bs.setBPB_BytesPerSector(sectorSize);
        bs.setBPB_SecPerCluster(SectorPerCluster); // look FATformat source
        bs.setBPB_RsvSecCount(this.ReservedSectorCount);
        bs.setBPB_NoFATs(NumOfFATs);
        bs.setBPB_RootEntCnt(0);
        bs.setBPB_TotSec16(0);
        bs.setBPB_MediumDescriptor(mediumDescriptor);
        bs.setBPB_FATSz16(0);
        bs.setBPB_SecPerTrk(SECTOR_PER_TRACK);
        bs.setBPB_NumHeads(NB_HEADS);
        bs.setBPB_HiddSec(hiddenSectors);
        bs.setBPB_TotSec32(nbTotalSectors);

        // For FAT16/32 only
        bs.setBPB_FATSz32(FatSize);
        bs.setBPB_ExtFlags(0);
        bs.setBPB_FSVer(0);
        bs.setBPB_RootClus(2);
        bs.setBPB_FSInfo(1);
        bs.setBPB_BkBootSec(BackupBootSector);

        bs.setBS_DrvNum(0x80);
        bs.setBS_Reserved1(0);
        bs.setBS_BootSig(0x29); // 0x29 if the next three bits are OK.

        int VolumeID = getDriveSerialNumber();
        bs.setBS_VolID(VolumeID);
        bs.setBS_VolLab(VOL_LABEL);
        bs.setBS_FilSysType(FAT_LABEL);
        bs.setBS_Identifier(BS_Identifier);

        // Keeping the FSInfos
        fsInfo.setFsInfo_LeadSig(FSI_LeadSig);
        fsInfo.setFsInfo_Reserved1();
        fsInfo.setFsInfo_StrucSig(FSI_StrucSig);
        FSI_FreeCount = (UserAreaSize / SectorPerCluster) - 1;
        fsInfo.setFsInfo_FreeCount(FSI_FreeCount);
        fsInfo.setFsInfo_NextFree(FSI_Nxt_Free);
        fsInfo.setReserve2();
        fsInfo.setFsInfo_TrailSig(FSI_TrailSig);

        /**
         * TODO:This portion need modofication for the Disk Size Handlings Not
         * So mandatory now .WIll look into it. Work out the Cluster Count
         */
        long FatNeeded = UserAreaSize / SectorPerCluster;
        /**
         * check for a cluster count of >2^28, since the upper 4 bits of the
         * cluster values in the FAT are reserved
         */
        if (FatNeeded > Math.pow(2, 28)) {
            log
                    .error("This drive has more than 2^28 clusters, try to specify a larger cluster size\n");
        }

        /**
         * Once zero_sectors has run, any data on the drive is basically
         * lost.... First zero out ReservedSect + FatSize * NumFats +
         * SectorsPerCluster
         * 
         */
        int SystemAreaSize = (ReservedSectorCount + (NumOfFATs * FatSize) + SectorPerCluster);
        log.info("Clearing out " + SystemAreaSize +
                " sectors for Reserved sectors, fats and root cluster...\n");

        /** Disk Freeing */
        try {
            setQuickSectorFree(SystemAreaSize, api);
        } catch (IOException e1) {
            log.info("Error ocured during Disk Free.");
        }
        /** calling the Format method */
        try {
            log.info("Initialising reserved sectors and FATs....");
            format(api, bs, fsInfo, SectorPerCluster, nbTotalSectors);
        } catch (IOException e) {
            log.error("Problems in Disk Formatting.");
        }

        /**
         * The mkjfat informations group
         */
        int DiskSizeGB = DiskSize / (1000 * 1000 * 1000);
        log.info("Size(Bytes): " + DiskSize + "\tSize(GB): " + DiskSizeGB + "\tSectors :" +
                nbTotalSectors);
        log.info("Volume ID is :" + toHexString(VolumeID >> 16) + ":" +
                toHexString(VolumeID & 0xffff));
        log.info(sectorSize + " Bytes Per Sector , Cluster Size  " + ClusterSize + " bytes.");
        log.info(ReservedSectorCount + " Reserved Sectors ," + (FatSize) + " Sectors Per FAT ," +
                NumOfFATs + " FATs.");
        log.info("Total Clusters :" + (UserAreaSize / SectorPerCluster));
        log.info("Free Clusters: " + FSI_FreeCount);

        // Flushing the DeviceAPI
        api.flush();
    }

    /**
     * Format the given device, according to my settings
     * 
     * @param api
     * @throws IOException
     */
    public void format(BlockDeviceAPI api, BootSector bs, FatFsInfo fsInfo, int sectorPerCluster,
            int nbTotalSectors) throws IOException {
        log.info("The Formatting...\n");
        /**
         * Now we should write the boot sector and fsinfo twice, once at 0 and
         * once at the backup boot sect position
         */
        for (int i = 0; i < 2; i++) {
            int SectorStart = (i == 0) ? 0 : (BackupBootSector * 512);
            bs.write(api, (long) SectorStart); // Write the BootSector
            fsInfo.write(api, (long) SectorStart + 512);
        }
        /** Write the first fat sector in the right places */
        for (int i = 0; i < NumOfFATs; i++) {
            int SectorStart = (ReservedSectorCount + (i * FatSize)) * 512;
            this.reservedSector = new byte[12];
            LittleEndian.setInt32(this.reservedSector, 0, ReservedSector_0);
            LittleEndian.setInt32(this.reservedSector, 4, ReservedSector_1);
            LittleEndian.setInt32(this.reservedSector, 8, ReservedSector_2);
            api.write(SectorStart, ByteBuffer.wrap(this.reservedSector));

        }

    }

    /**
     * The Method for Disk Free Operations.
     * 
     * @param systemAreaSize
     * @param api
     * @throws IOException
     */
    private void setQuickSectorFree(int systemAreaSize, BlockDeviceAPI api) throws IOException {
        byte[] reserveArray = new byte[512];
        for (int i = 0; i < systemAreaSize; i++) {
            api.write(i * 512, ByteBuffer.wrap(reserveArray));
        }
    }

    /**
     * This is for Quick Disk Free Operation TODO:Need to test with it.
     * 
     * @param TotalSectors
     * @param api
     * @throws IOException
     */
    private void setQuickDiskFree(int TotalSectors, BlockDeviceAPI api) throws IOException {
        // TODO:For Total Disk Formatting
    }

    /**
     * @param clusterSize
     * @param BytesPerSector
     * @return
     */
    private static int get_spc(ClusterSize clusterSize, int BytesPerSector) {
        return (clusterSize.getSize() / BytesPerSector);
    }

    /**
     * The method for getting the Serial Number of the Drive.
     */
    public int getDriveSerialNumber() {
        Date date = new Date();
        int year = FatUtils.getYear(date.getTime());
        int month = FatUtils.getMonth((int) date.getTime());
        int day = FatUtils.getDay((int) date.getTime());
        int hour = FatUtils.getHours((int) date.getTime());
        int minute = FatUtils.getMinutes((int) date.getTime());
        int second = FatUtils.getSeconds((int) date.getTime());
        int milliSecond = (int) FatUtils.getMilliSeconds(date.getTime());

        int low = day + (month << 8);
        int tmp = (milliSecond / 10) + (second << 8);
        low += tmp;

        int high = minute + (hour << 8);
        high += year;

        int serialNo = low + (high << 16);
        return serialNo;
    }

    /**
     * Get the FAT size sectors.
     */
    public int getFATSizeSectors(int TotalSectorNumber, int ReservedSecCnt, int SecPerClus,
            int NumFATs, int BytesPerSect) {
        long Numerator, Denominator;
        long FATElementSize = 4;
        long FATsz;

        Numerator = FATElementSize * (TotalSectorNumber - ReservedSecCnt);
        Denominator = (SecPerClus * BytesPerSect) + (FATElementSize * NumFATs);

        FATsz = Numerator / Denominator;
        // round up
        FATsz += 1;

        return ((int) FATsz);
    }

    /**
     * @param TotalSectors
     * @param ReservedSectorCount
     * @param NumFATs
     * @param FATSize
     * @return
     */
    private int getUserAreaSize(int TotalSectors, int ReservedSectorCount, int NumFATs,
            ClusterSize FATSize) {
        int UserAreaSize = TotalSectors - ReservedSectorCount - (NumFATs * FATSize.getSize());
        return (UserAreaSize);
    }

    /**
     * The Total Disk Size
     * @param TotalSectors
     * @param sectorSize
     * @return
     */
    private int getDiskSize(int TotalSectors, int sectorSize) {
        return (TotalSectors * sectorSize);
    }

}
