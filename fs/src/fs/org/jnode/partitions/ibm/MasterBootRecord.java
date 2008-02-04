package org.jnode.partitions.ibm;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.bus.ide.IDEConstants;
import org.jnode.fs.fat.BootSector;

public class MasterBootRecord {
	private static final int PARTITION_TABLE_OFFSET = 0x1be;
	private static final int PARTITION_TABLE_END_OFFSET = PARTITION_TABLE_OFFSET + 64;

	private final ByteBuffer mbr;
	private boolean dirty;
	private final IBMPartitionTableEntry[] partitions;

	public MasterBootRecord() {
		mbr = ByteBuffer.allocate(IDEConstants.SECTOR_SIZE);
        dirty = false;
        partitions = new IBMPartitionTableEntry[4];
	}

	public MasterBootRecord(byte[] buffer) throws IOException
	{
		mbr = ByteBuffer.wrap(buffer);
        dirty = false;
        partitions = new IBMPartitionTableEntry[4];
	}

	public MasterBootRecord(BlockDeviceAPI devApi) throws IOException
	{
		this();
		read(devApi);
	}

	final public boolean containsPartitionTable() {
		return IBMPartitionTable.containsPartitionTable(mbr.array());
	}

	final public void copyPartitionTableFrom(MasterBootRecord srcMbr) {
		srcMbr.mbr.position(PARTITION_TABLE_OFFSET).limit(PARTITION_TABLE_END_OFFSET);
		mbr.position(PARTITION_TABLE_OFFSET);
		mbr.put(srcMbr.mbr);
	}

	/**
	 * Write the BPB to the MBR to its Correct Position.
	 * @param bpb
	 */
	final public void setBPB(byte[] bpb) {
	    System.arraycopy (bpb ,0, mbr.array(), 0x3, bpb.length);
	}

	/**
	 * Write the contents of this bootsector to the given device.
	 *
	 * @param device
	 */
	final public synchronized void write(BlockDeviceAPI devApi) throws IOException {
    	devApi.write(0, mbr);
        devApi.flush();

		dirty = false;
	}

	/**
	 * Read the contents of this bootsector from the given device.
	 *
	 * @param device
	 */
	final public synchronized void read(BlockDeviceAPI api) throws IOException {
        api.read(0, mbr);

		dirty = false;
	}

	/**
	 * TODO remove the temporary workaround : internal array shouldn't be exposed
	 * @return
	 */
	public byte[] array() {
		return mbr.array();
	}
}
