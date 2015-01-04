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

import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.driver.bus.ide.IDEConstants;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.Formatter;
import org.jnode.fs.service.FileSystemService;
import org.jnode.partitions.PartitionTableEntry;
import org.jnode.partitions.ibm.IBMPartitionTableEntry;
import org.jnode.naming.InitialNaming;
import javax.naming.NameNotFoundException;


/**
 * @author gvt
 * @author Tango
 */
public class FatFileSystemFormatter extends Formatter<FatFileSystem> {
    private static final Logger log = Logger.getLogger(FatFileSystemFormatter.class);

    private ClusterSize clusterSize;

    public FatFileSystemFormatter(ClusterSize clusterSize) {
        super(new FatFileSystemType());
        this.clusterSize = clusterSize;
    }

    public FatFileSystem format(Device device) throws FileSystemException {

        try {
            long numberOfSectors;
            long offset;

            FSBlockDeviceAPI api = (FSBlockDeviceAPI) device.getAPI(BlockDeviceAPI.class);
            int sectorSize = api.getSectorSize();

            if (sectorSize != IDEConstants.SECTOR_SIZE) {
                log.error("This mkjfat1.0 support only the Hard Disk.Sector Size must " +
                        IDEConstants.SECTOR_SIZE + " bytes.\n");
            }

            PartitionTableEntry entry = api.getPartitionTableEntry();

            // if we can deduce partitiontable/fat dependencies do it otherwise
            // guess it.
            if (entry != null && entry instanceof IBMPartitionTableEntry) {
                numberOfSectors = ((IBMPartitionTableEntry) entry).getNrSectors();
                offset = ((IBMPartitionTableEntry) entry).getStartLba();
            } else {
                numberOfSectors = api.getLength() / sectorSize;
                offset = 0;
            }
            
            /*
             * Check the Disks Availability. low end limit - 65536 sectors I
             * suspect that most FAT32 implementations would mount this volume
             * just fine, but the spec says that we shouldn't do this, so we won't.
             */
            if (numberOfSectors < 65536) {
                log.error("This drive is too small for FAT32 - there must be at least 64K clusters\n");
            }
            
            /*
             * This is a more fundamental limitation on FAT32 - the total sector
             * count in the root dir 32bit. With a bit of creativity, FAT32
             * could be extended to handle at least 2^28 clusters There would
             * need to be an extra field in the FSInfo sector, and the old
             * sector count could be set to 0xffffffff. This is non standard
             * though, the Windows FAT driver FASTFAT.SYS won't understand this.
             * Perhaps a future version of FAT32 and FASTFAT will handle this.
             */
            if (numberOfSectors >= (Math.pow(2, 32) - 1)) {
                log.error("This drive is too big for FAT32 - max 2TB supported :" + numberOfSectors);
            }

            // The FAT32 Formatter class constructor
            FatFormatter.HDDFormatter(sectorSize, (int) numberOfSectors, clusterSize, (int) offset, api);

            final FileSystemService fSS = InitialNaming.lookup(FileSystemService.NAME);
            FatFileSystemType type = fSS.getFileSystemType(FatFileSystemType.ID);
            return type.create(device, false); // not readOnly !
        } catch (IOException ioe) {
            throw new FileSystemException("Formating problem", ioe);
        } catch (ApiNotFoundException e) {
            throw new FileSystemException("Formating problem", e);
        } catch (NameNotFoundException e) {
            throw new FileSystemException("Formating problem", e);
        }
    }

}
