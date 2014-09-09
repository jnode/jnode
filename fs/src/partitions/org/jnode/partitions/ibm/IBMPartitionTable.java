/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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

package org.jnode.partitions.ibm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.bus.ide.IDEConstants;
import org.jnode.partitions.PartitionTable;
import org.jnode.partitions.PartitionTableType;
import org.jnode.util.BigEndian;
import org.jnode.util.LittleEndian;

/**
 * @author epr
 */
public class IBMPartitionTable implements PartitionTable<IBMPartitionTableEntry> {
    public static final int TABLE_SIZE = 4;

    /**
     * The set of known filesystem markers.
     */
    private static final Set<String> FILESYSTEM_OEM_NAMES = new HashSet<String>();

    static {
        // FAT OEM names
        FILESYSTEM_OEM_NAMES.add("MSDOS5.0");
        FILESYSTEM_OEM_NAMES.add("MSWIN4.1");
        FILESYSTEM_OEM_NAMES.add("IBM  3.3");
        FILESYSTEM_OEM_NAMES.add("IBM  7.1");
        FILESYSTEM_OEM_NAMES.add("mkdosfs\u0000");
        FILESYSTEM_OEM_NAMES.add("FreeDOS ");

        // NTFS
        FILESYSTEM_OEM_NAMES.add("NTFS    ");
    }

    /**
     * The type of partition table
     */
    private final IBMPartitionTableType tableType;

    /**
     * The partition entries
     */
    private final IBMPartitionTableEntry[] partitions;

    /**
     * The device
     */
    private final Device driveDevice;

    /**
     * Extended partition
     */
    private final ArrayList<IBMPartitionTableEntry> extendedPartitions =
        new ArrayList<IBMPartitionTableEntry>();

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(IBMPartitionTable.class);

    /**
     * The position of the extendedPartition in the table
     */
    private int extendedPartitionEntry = -1;

    /**
     * Create a new instance
     *
     * @param bootSector
     */
    public IBMPartitionTable(IBMPartitionTableType tableType, byte[] bootSector, Device device) {
        // this.bootSector = bootSector;
        this.tableType = tableType;
        this.driveDevice = device;
        if (containsPartitionTable(bootSector)) {
            this.partitions = new IBMPartitionTableEntry[TABLE_SIZE];
            for (int partNr = 0; partNr < partitions.length; partNr++) {
                log.debug("try part " + partNr);
                partitions[partNr] = new IBMPartitionTableEntry(this, bootSector, partNr);
                if (partitions[partNr].isExtended()) {
                    extendedPartitionEntry = partNr;
                    log.debug("Found Extended partitions");
                    handleExtended(partitions[partNr]);
                }
            }
        } else {
            partitions = null;
        }
    }

    /**
     * Fill the extended Table
     */
    private void handleExtended(IBMPartitionTableEntry current) {

        final long startLBA = current.getStartLba();
        final ByteBuffer sector = ByteBuffer.allocate(IDEConstants.SECTOR_SIZE);
        try {
            log.debug("Try to read the Extended Partition Table");
            BlockDeviceAPI api = driveDevice.getAPI(BlockDeviceAPI.class);
            api.read(startLBA * IDEConstants.SECTOR_SIZE, sector);
        } catch (ApiNotFoundException e) {
            // I think we can't get it
            log.error("API Not Found Exception");
        } catch (IOException e) {
            // I think we can't get it
            log.error("IOException");
        }

        IBMPartitionTableEntry entry;
        for (int i = 0; i < TABLE_SIZE; i++) {
            entry = new IBMPartitionTableEntry(this, sector.array(), i);
            if (entry.isValid() && !entry.isEmpty()) {
                // correct the offset
                if (entry.isExtended()) {
                    entry.setStartLba(entry.getStartLba() +
                        partitions[extendedPartitionEntry].getStartLba());
                    handleExtended(entry);
                } else {
                    entry.setStartLba(entry.getStartLba() + current.getStartLba());
                    extendedPartitions.add(entry);
                }
            }
        }
    }

    public boolean hasExtended() {
        return !extendedPartitions.isEmpty();
    }

    /**
     * Does the given boot sector contain an IBM partition table?
     *
     * @param bootSector the data to check.
     * @return {@code true} if the data contains an IBM partition table, {@code false} otherwise.
     */
    public static boolean containsPartitionTable(byte[] bootSector) {
        if (bootSector.length < 0x200) {
            // Not enough data for detection
            return false;
        }

        if (LittleEndian.getUInt16(bootSector, 510) != 0xaa55) {
            log.debug("No aa55 magic");
            return false;
        }

        if (LittleEndian.getUInt16(bootSector, 428) == 0x5678) {
            // Matches the AAP MBR extra signature, probably an valid partition table
            log.debug("Has AAP MBR extra signature");
            return true;
        }

        if (LittleEndian.getUInt16(bootSector, 380) == 0xa55a) {
            // Matches the AST/NEC MBR extra signature, probably an valid partition table
            log.debug("Has AST/NEC MBR extra signature");
            return true;
        }

        if (LittleEndian.getUInt16(bootSector, 252) == 0x55aa) {
            // Matches the Disk Manager MBR extra signature, probably an valid partition table
        	log.debug("Has Disk Manager MBR extra signature");
            return true;
        }

        if (LittleEndian.getUInt32(bootSector, 2) == 0x4c57454e) {
            // Matches the NEWLDR MBR extra signature, probably an valid partition table
            log.debug("Has NEWLDR MBR extra signature");
            return true;
        }

        if (LittleEndian.getUInt32(bootSector, 6) == 0x4f4c494c) {
            // Matches the LILO signature, probably an valid partition table
        	log.debug("Has LILO signature");
            return true;
        }

        if (BigEndian.getUInt32(bootSector, 0) == 0x33ffbe00 && BigEndian.getUInt32(bootSector, 4) == 0x028ed7bc) {
            // Matches HP boot code. It is not possible to match the strings here because they are localised. E.g:
            //   "\r\nMissing operating system\r\n\u0000\r\nMaster Boot Record Error\r\n\u0000\r\nPress a key.\r\n\u0000"
            //   "\r\nManglende operativ system\r\n\u0000\r\nFeil i hovedoppstartsposten\r\n\u0000\r\nTrykk en tast"

            log.debug("Has HP boot code signature");
            return true;
        }

        String bootSectorAsString = new String(bootSector, 0, 512, Charset.forName("US-ASCII"));

        if (bootSectorAsString.contains("Invalid partition table\u001eError loading operating system\u0018Missing operating system")) {
            // Matches DOS 2.0 partition boot code error message signature
            // see:
            //     http://thestarman.narod.ru/asm/mbr/200MBR.htm
            log.debug("Has DOS 2.0 code error string signature");
            return true;
        }

        if (bootSectorAsString.contains("Invalid partition table\u0000Error loading operating system\u0000Missing operating system")) {
            // Matches Microsoft partition boot code error message signature
            // see:
            //     http://thestarman.pcministry.com/asm/mbr/VistaMBR.htm
            //     http://thestarman.narod.ru/asm/mbr/Win2kmbr.htm
            //     http://thestarman.narod.ru/asm/mbr/95BMEMBR.htm
            //     http://thestarman.narod.ru/asm/mbr/STDMBR.htm
            log.debug("Has Microsoft code error string signature");
            return true;
        }

        if (LittleEndian.getUInt32(bootSector, 296) == 0xC3F961D6L) {
            // Matches Microsoft Windows 2000 partition boot code. Starting from Windows 2000 the boot code error
            // messages are localised, so the check above won't match them.
            //
            // see:
            //     http://thestarman.narod.ru/asm/mbr/Win2kmbr.htm
            log.debug("Has w2k boot code signature");
            return true;
        }

        if (bootSectorAsString.contains("Read\u0000Boot\u0000 error\r\n\u0000")) {
            // Matches BSD partition boot code error message signature
        	log.debug("Has BSD code error string signature");
            return true;
        }

        if (bootSectorAsString.contains("GRUB \u0000Geom\u0000Hard Disk\u0000Read\u0000 Error")) {
            // Matches GRUB string signature
        	log.debug("Has GRUB string signature");
            return true;
        }

        if (bootSectorAsString.contains("\u0000Multiple active partitions.\r\n")) {
            // Matches SYSLINUX string signature
        	log.debug("Has SYSLINUX string signature");
            return true;
        }

        if (bootSectorAsString.contains("MAKEBOOT")) {
            // Matches MAKEBOOT string extra signature
        	log.debug("Has MAKEBOOT string signature");
            return true;
        }

        if (bootSectorAsString.contains("MBR \u0010\u0000")) {
            // Matches MBR string extra signature
        	log.debug("Has MBR string signature");
            return true;
        }

        if (LittleEndian.getUInt32(bootSector, 241) == 0x41504354) {
            // Matches TCPA signature. Seen at offsets:
            //  * 0xF1 - Windows Vista
            //  * 0x18E - Windows PE
            // see http://thestarman.pcministry.com/asm/mbr/VistaMBR.htm
        	log.debug("Has TCPA extra signature");
            return true;
        }

        String bsdNameTabString = new String(bootSector, 416, 16, Charset.forName("US-ASCII"));

        if (bsdNameTabString.contains("Linu\ufffd") || bsdNameTabString.contains("FreeBS\ufffd")) {
            // Matches BSD nametab entries signature
        	log.debug("Has BSD nametab entries");
            return true;
        }

        // Rule out the Linux kernel binary
        if (bootSector.length > 520) {
            String linuxKernelHeaderString = new String(bootSector, 514, 4, Charset.forName("US-ASCII"));

            if ("HdrS".equals(linuxKernelHeaderString)) {
                // Matches Linux kernel header signature
                log.debug("Has Linux kernel header signature");
                return false;
            }
        }

        // Check if this looks like a filesystem instead of a partition table
        String oemName = new String(bootSector, 3, 8, Charset.forName("US-ASCII"));
        if (FILESYSTEM_OEM_NAMES.contains(oemName)) {
            log.debug("Looks like a file system instead of a partition table.");
            return false;
        }

        if (LittleEndian.getUInt32(bootSector, 0xc) == 0x504E0000) {
            // Matches the 'NP' signature
            log.debug("Matches the 'NP' signature");
            return true;
        }

        // Nothing matched, fall back to validating any specified partition entries
        log.debug("Checking partitions");
        List<IBMPartitionTableEntry> entries = new ArrayList<IBMPartitionTableEntry>();
        for (int partitionNumber = 0; partitionNumber < TABLE_SIZE; partitionNumber++) {
            IBMPartitionTableEntry partition = new IBMPartitionTableEntry(null, bootSector, partitionNumber);

            if (partition.isValid()) {
                entries.add(partition);
            }
        }

        // Check that none of the entries are overlapping with each other
        for (int partitionNumber = 0; partitionNumber < entries.size(); partitionNumber++) {
            IBMPartitionTableEntry partition = entries.get(partitionNumber);

            for (int i = 0; i < entries.size(); i++) {
                if (i != partitionNumber) {
                    IBMPartitionTableEntry otherPartition = entries.get(i);

                    if (partition.getStartLba() <= otherPartition.getStartLba() + otherPartition.getNrSectors() - 1 &&
                        otherPartition.getStartLba() <= partition.getStartLba() + partition.getNrSectors() - 1) {
                        log.error("Parition table entries overlap: " + partition + " " + otherPartition);
                        return false;
                    }
                }
            }
        }

        // Finally, check if there is at least one entry that seems valid
        return !entries.isEmpty();
    }

    public Iterator<IBMPartitionTableEntry> iterator() {
        return new Iterator<IBMPartitionTableEntry>() {
            private int index = 0;
            private final int last = (partitions == null) ? 0 : partitions.length;

            public boolean hasNext() {
                return index < last;
            }

            public IBMPartitionTableEntry next() {
                return partitions[index++];
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * @return Returns the extendedPartitions.
     */
    public List<IBMPartitionTableEntry> getExtendedPartitions() {
        return extendedPartitions;
    }

    /**
     * @see org.jnode.partitions.PartitionTable#getType()
     */
    public PartitionTableType getType() {
        return tableType;
    }
}
