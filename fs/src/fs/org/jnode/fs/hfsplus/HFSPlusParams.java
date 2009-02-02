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
 
package org.jnode.fs.hfsplus;

import org.jnode.fs.FileSystemException;

public class HFSPlusParams {

    public static final int MINIMAL_BLOCK_SIZE = 512;
    public static final int DEFAULT_BLOCK_SIZE = 4096;
    public static final int OPTIMAL_BLOCK_SIZE = 4096;
    public static final int DATA_CLUMP_FACTOR = 16;
    public static final int RESOURCE_CLUMP_FACTOR = 16;
    public static final int DEFAULT_JOURNAL_SIZE = 8 * 1024 * 1024;
    public static final int DEFAULT_CATALOG_NODE_SIZE = 8192;
    public static final int DEFAULT_EXTENT_NODE_SIZE = 4096;
    public static final int DEFAULT_ATTRIBUTE_NODE_SIZE = 4096;

    private long blockDeviceSize;

    private String volumeName;
    private int blockSize;
    private int resourceClumpBlocks;
    private int dataClumpBlocks;
    private int catalogClumpBlocks;
    private int extentClumpBlocks;
    private int attributeClumpBlocks;
    private int bitmapClumpBlocks;
    private boolean journaled;
    private int journalSize;

    private int resourceClumpSize;
    private int dataClumpSize;
    private int catalogClumpSize;
    private int catalogNodeSize;
    private int extentClumpSize;
    private int extentNodeSize;
    private int attributeClumpSize;
    private int attributeNodeSize;
    private int allocationClumpSize;

    /**
     * Default constructor.
     */
    public HFSPlusParams() {
        this.catalogNodeSize = DEFAULT_CATALOG_NODE_SIZE;
        this.extentNodeSize = DEFAULT_EXTENT_NODE_SIZE;
    }

    /**
     * 
     * @param blockDeviceSize
     * @param sectorSize
     * 
     * @throws FileSystemException
     * 
     */
    public void initializeDefaultsValues(long blockDeviceSize, long sectorSize) throws FileSystemException {
        long clumpSize = 0;
        this.blockDeviceSize = blockDeviceSize;
        if (resourceClumpBlocks == 0) {
            if (blockSize > DEFAULT_BLOCK_SIZE) {
                clumpSize = round(RESOURCE_CLUMP_FACTOR * DEFAULT_BLOCK_SIZE, blockSize);
            } else {
                clumpSize = RESOURCE_CLUMP_FACTOR * blockSize;
            }
        } else {
            clumpSize = clumpSizeCalculation(resourceClumpBlocks);
        }
        resourceClumpSize = (int) clumpSize;
        if (dataClumpBlocks == 0) {
            if (blockSize > DEFAULT_BLOCK_SIZE) {
                clumpSize = round(DATA_CLUMP_FACTOR * DEFAULT_BLOCK_SIZE, blockSize);
            } else {
                clumpSize = DATA_CLUMP_FACTOR * blockSize;
            }
        } else {
            clumpSize = clumpSizeCalculation(dataClumpBlocks);
        }

        if (blockSize < OPTIMAL_BLOCK_SIZE || blockDeviceSize < 0x40000000) {
            catalogNodeSize = 4096;
        }
        long sectorCount = blockDeviceSize / sectorSize;
        if (catalogClumpBlocks == 0) {
            clumpSize = getBTreeClumpSize(blockSize, catalogNodeSize, sectorCount, true);
        } else {
            clumpSize = clumpSizeCalculation(catalogClumpBlocks);
            if (clumpSize % catalogNodeSize != 0) {
                throw new FileSystemException("clump size is not a multiple of node size");
            }
        }
        catalogClumpSize = (int) clumpSize;
        if (extentClumpBlocks == 0) {
            clumpSize = getBTreeClumpSize(blockSize, extentNodeSize, sectorCount, false);
        } else {
            clumpSize = clumpSizeCalculation(extentClumpBlocks);
        }
        extentClumpSize = (int) clumpSize;

        if (attributeClumpBlocks == 0) {
            clumpSize = 0;
        } else {
            clumpSize = clumpSizeCalculation(attributeClumpBlocks);
            if (clumpSize % attributeNodeSize != 0) {
                throw new FileSystemException("clump size is not a multiple of attribute node size");
            }
        }
        attributeClumpSize = (int) clumpSize;

        long totalBlocks = this.getBlockCount();
        long minClumpSize = this.getBlockCount() >> 3;
        if ((totalBlocks & 7) == 0) {
            ++minClumpSize;
        }
        if (bitmapClumpBlocks == 0) {
            clumpSize = minClumpSize;
        } else {
            clumpSize = clumpSizeCalculation(bitmapClumpBlocks);
            if (clumpSize < minClumpSize) {
                throw new FileSystemException("bitmap clump size is too small.");
            }
        }
        allocationClumpSize = (int) clumpSize;

    }

    private int[] extentClumpTable = new int[] {4, 4, 4, 5, 5, 6, 7, 8, 9, 11, 14, 16, 20, 25, 32 };
    private int[] catalogClumpTable = new int[] {4, 6, 8, 11, 14, 19, 25, 34, 45, 60, 80, 107, 144, 192, 256 };

    /**
     * Get the file clump size for Extent and catalog B-Tree files.
     * 
     * @param blockSize
     * @param nodeSize
     * @param sectors
     * @param catalog If true, calculate catalog clump size. In the other case, calculate extent clump size.
     * 
     * @return
     */
    private long getBTreeClumpSize(int blockSize, int nodeSize, long sectors, boolean catalog) {
        int size = Math.max(blockSize, nodeSize);
        long clumpSize = 0;
        if (sectors < 0x200000) {
            clumpSize = (sectors << 2);
            if (clumpSize < (8 * nodeSize)) {
                clumpSize = (8 * nodeSize);
            }
        } else {
            sectors = sectors >> 22;
            for (int i = 0; sectors != 0 && (i < 14); ++i) {
                if (catalog) {
                    clumpSize = catalogClumpTable[i] * 1024 * 1024;
                } else {
                    clumpSize = extentClumpTable[i] * 1024 * 1024;
                }
                sectors = sectors >> 1;
            }
        }

        clumpSize /= size;
        clumpSize *= size;
        
        if (clumpSize == 0) {
            clumpSize = size;
        }
        
        return clumpSize;
    }

    /**
     * 
     * @param clumpBlocks
     * 
     * @return
     */
    private int clumpSizeCalculation(long clumpBlocks) throws FileSystemException {
        long clumpSize = clumpBlocks * blockSize;
        if ((clumpSize & 0XFFFFFFFF00000000L) == 0) {
            throw new FileSystemException("Too many blocks (" + clumpBlocks + ") for clump size (" + clumpSize + ").");
        }
        return (int) clumpSize;
    }

    private long round(long x, long y) {
        return (((x + y) - 1) / y * y);
    }

    public String getVolumeName() {
        return volumeName;
    }

    public void setVolumeName(String volumeName) {
        this.volumeName = volumeName;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public boolean isJournaled() {
        return journaled;
    }

    public void setJournaled(boolean journaled) {
        this.journaled = journaled;
    }

    public int getJournalSize() {
        return journalSize;
    }

    public void setJournalSize(int journalSize) {
        this.journalSize = journalSize;
    }

    public int getCatalogNodeSize() {

        return catalogNodeSize;
    }

    public long getBlockCount() {
        return blockDeviceSize / blockSize;
    }

    public int getCatalogClumpSize() {
        return catalogClumpSize;
    }

    public int getExtentClumpSize() {
        return extentClumpSize;
    }

    public int getResourceClumpSize() {
        return resourceClumpSize;
    }

    public int getDataClumpSize() {
        return dataClumpSize;
    }

    public int getAttributeClumpSize() {
        return attributeClumpSize;
    }

    public int getAttributeNodeSize() {
        return attributeNodeSize;
    }

    public void setAttributeClumpBlocks(int attributeClumpBlocks) {
        this.attributeClumpBlocks = attributeClumpBlocks;
    }

    public int getAllocationClumpSize() {
        return allocationClumpSize;
    }

    public void setBitmapClumpBlocks(int bitmapClumpBlocks) {
        this.bitmapClumpBlocks = bitmapClumpBlocks;
    }

    public int getExtentNodeSize() {
        return extentNodeSize;
    }
}
