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
 
package org.jnode.partitions.ibm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.bus.ide.IDEConstants;
import org.jnode.partitions.PartitionTable;
import org.jnode.partitions.PartitionTableEntry;
import org.jnode.partitions.PartitionTableType;

import com.sun.org.apache.bcel.internal.generic.NEW;

/**
 * @author epr
 */
public class IBMPartitionTable implements PartitionTable {

    /** The type of partition table */
    private final IBMPartitionTableType tableType;
	/** The bootsector data */
	//private final byte[] bootSector;
	/** The partition entries */
	private final IBMPartitionTableEntry[] partitions;
	
	/** The device */
	private final Device drivedDevice;
	
	/** Extended partition */
	private final ArrayList<IBMPartitionTableEntry> extendedPartitions = new ArrayList<IBMPartitionTableEntry>();
	
	/** My logger */
	private static final Logger log = Logger.getLogger(IBMPartitionTable.class);
	
	/** The position of the extendedPartition in the table */ 
	private int extendedPartitionEntry = -1;
	
	/**
	 * Create a new instance
	 * @param bootSector
	 */
	public IBMPartitionTable(IBMPartitionTableType tableType, byte[] bootSector, Device device) {
		//this.bootSector = bootSector;
        this.tableType = tableType;
		this.partitions = new IBMPartitionTableEntry[4];
		this.drivedDevice = device;
		if(containsPartitionTable(bootSector)) {
			for (int partNr = 0; partNr < partitions.length ;  partNr++) {
				log.debug("try part "+ partNr);
				partitions[partNr] = new IBMPartitionTableEntry(this, bootSector, partNr);
				if(partitions[partNr].isExtended()) {
					extendedPartitionEntry = partNr;
					log.debug("Found Extended partitions");
					handleExtended(partitions[partNr]);
				}
			}
		}
	}
	
	/**
	 * Fill the extended Table
	 *  
	 */
	private void handleExtended(IBMPartitionTableEntry current) {
		
		final long startLBA  = current.getStartLba();
		final ByteBuffer sector = ByteBuffer.allocate(IDEConstants.SECTOR_SIZE);
		try {
			log.debug("Try to read the Extended Partition Table");
			BlockDeviceAPI api = drivedDevice.getAPI(BlockDeviceAPI.class);
			api.read(startLBA * IDEConstants.SECTOR_SIZE, sector);
		} catch (ApiNotFoundException e) {
			// I think we ca'nt get it 
			log.error("API Not Found Exception");
		} catch (IOException e) {
			// I think we ca'nt get it
			log.error("IOException");
		}
        
		IBMPartitionTableEntry entry = null;
		for(int i = 0; i < 4 ; i++) {
			entry = new IBMPartitionTableEntry(this, sector.array(), i);
			if(entry.isValid() && !entry.isEmpty()) {
				//corrct the offset
				
				//log.debug(entry.dump());
				if(entry.isExtended()) {
					entry.setStartLba(entry.getStartLba() + partitions[extendedPartitionEntry].getStartLba());
					//log.debug("going recurse :"+i);
					handleExtended(entry);
				}else {
					//log.debug("adding extended :"+i);
					entry.setStartLba(entry.getStartLba() + current.getStartLba());
					extendedPartitions.add(entry);
				}
			}else {
				//log.debug("Entry is not valid or empty:");
				//log.debug(entry.dump());
			}
		}
	}
	
	public boolean hasExtended() {
		return !extendedPartitions.isEmpty();
	}
	
	/**
	 * Does the given bootsector contain an IBM partition table?
	 * @param bootSector
	 */
	public static boolean containsPartitionTable(byte[] bootSector) {
		if ((bootSector[510] & 0xFF) != 0x55) {
			return false;
		}
		if ((bootSector[511] & 0xFF) != 0xAA) {
			return false;
		}
		return true;
	}
	
	/**
	 * Gets the number of partitions in this table.
	 */
	public int getLength() {
		return partitions.length; 
	}
	
	/**
	 * Gets a single entry.
	 * @param partNr
	 */
	public PartitionTableEntry getEntry(int partNr) {
		return partitions[partNr];
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
