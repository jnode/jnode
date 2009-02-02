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
 
package org.jnode.fs.fat;

import java.io.IOException;

import org.jnode.driver.block.BlockDeviceAPI;


/**
 * @author epr
 */
public class FatFormatter {
    public static final int MAX_DIRECTORY = 512;
    public static final int FLOPPY_DESC = 0xf0;
    public static final int HD_DESC = 0xf8;
    public static final int RAMDISK_DESC = 0xfa;

    private BootSector bs;
    private Fat fat;
    private FatDirectory rootDir;

    public static FatFormatter fat144FloppyFormatter(int reservedSectors, BootSector bs) {
        return new FatFormatter(FLOPPY_DESC, 512, 1, 2880, 18, 2, FatType.FAT32, 2, 0,
                reservedSectors, bs);
    }

    public static FatFormatter HDFormatter(int bps, int nbTotalSectors, int sectorsPerTrack,
            int nbHeads, FatType fatSize, int hiddenSectors, int reservedSectors, BootSector bs) {
        return new FatFormatter(HD_DESC, bps,
                calculateDefaultSectorsPerCluster(bps, nbTotalSectors), nbTotalSectors,
                sectorsPerTrack, nbHeads, fatSize, 2, hiddenSectors, reservedSectors, bs);
    }

    protected FatFormatter(int mediumDescriptor, int bps, int spc, int nbTotalSectors,
            int sectorsPerTrack, int nbHeads, FatType fatSize, int nbFats, int hiddenSectors,
            int reservedSectors, BootSector bs) {
        this.bs = bs;
        final float fatEntrySize = fatSize.getEntrySize();

        bs.setMediumDescriptor(mediumDescriptor);
        bs.setOemName("JNode1.0");
        bs.setBytesPerSector(bps);
        bs.setNrReservedSectors(reservedSectors);
        bs.setNrRootDirEntries(mediumDescriptor == FLOPPY_DESC ? 224
                : calculateDefaultRootDirectorySize(bps, nbTotalSectors));
        bs.setNrLogicalSectors(nbTotalSectors);
        bs.setSectorsPerFat((Math.round(nbTotalSectors / (spc * (bps / fatEntrySize))) + 1));
        bs.setSectorsPerCluster(spc);
        bs.setNrFats(2);
        bs.setSectorsPerTrack(sectorsPerTrack);
        bs.setNrHeads(nbHeads);
        bs.setNrHiddenSectors(hiddenSectors);

        fat = new Fat(fatSize, mediumDescriptor, bs.getSectorsPerFat(), bs.getBytesPerSector());
        fat.setMediumDescriptor(bs.getMediumDescriptor());

        rootDir = new FatLfnDirectory(null, bs.getNrRootDirEntries());
    }

    private static int calculateDefaultSectorsPerCluster(int bps, int nbTotalSectors) {
        // Apply the default cluster size from MS
        long sizeInMB = (nbTotalSectors * bps) / (1024 * 1024);

        int spc;

        if (sizeInMB < 32) {
            spc = 1;
        } else if (sizeInMB < 64) {
            spc = 2;
        } else if (sizeInMB < 128) {
            spc = 4;
        } else if (sizeInMB < 256) {
            spc = 8;
        } else if (sizeInMB < 1024) {
            spc = 32;
        } else if (sizeInMB < 2048) {
            spc = 64;
        } else if (sizeInMB < 4096) {
            spc = 128;
        } else if (sizeInMB < 8192) {
            spc = 256;
        } else if (sizeInMB < 16384) {
            spc = 512;
        } else
            throw new IllegalArgumentException("Disk too large to be formatted in FAT16");
        return spc;
    }

    private static int calculateDefaultRootDirectorySize(int bps, int nbTotalSectors) {
        int totalSize = bps * nbTotalSectors;
        // take a default 1/5 of the size for root max
        if (totalSize >= MAX_DIRECTORY * 5 * 32) { // ok take the max
            return MAX_DIRECTORY;
        } else {
            return totalSize / (5 * 32);
        }
    }

    /**
     * Set the label
     * 
     * @param label
     */
    public void setLabel(String label) throws IOException {
        rootDir.setLabel(label);
    }

    /**
     * Format the given device, according to my settings
     * 
     * @param api
     * @throws IOException
     */
    public void format(BlockDeviceAPI api) throws IOException {
        bs.write(api);
        for (int i = 0; i < bs.getNrFats(); i++) {
            fat.write(api, FatUtils.getFatOffset(bs, i));
        }
        rootDir.write(api, FatUtils.getRootDirOffset(bs));
        api.flush();
    }

    /**
     * Returns the bs.
     *
     * @return BootSector
     */
    public BootSector getBootSector() {
        return bs;
    }
}
