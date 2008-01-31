package org.jnode.fs.hfsplus;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jnode.driver.Device;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.BlockDeviceFileSystemType;
import org.jnode.fs.FileSystemException;
import org.jnode.partitions.PartitionTableEntry;
import org.jnode.partitions.ibm.IBMPartitionTableEntry;
import org.jnode.partitions.ibm.IBMPartitionTypes;
import org.jnode.util.BigEndian;

public class HfsPlusFileSystemType implements BlockDeviceFileSystemType<HfsPlusFileSystem> {
	public static final Class<HfsPlusFileSystemType> ID = HfsPlusFileSystemType.class;
	public HfsPlusFileSystem create(Device device, boolean readOnly)
			throws FileSystemException {
		HfsPlusFileSystem fs = new HfsPlusFileSystem(device, readOnly);
		fs.read();
		return fs;
	}

	public String getName() {
		return "HFS+";
	}

	public boolean supports(PartitionTableEntry pte, byte[] firstSector,
			FSBlockDeviceAPI devApi) {
		if(pte!=null) {
			if (pte instanceof IBMPartitionTableEntry)
			    if (((IBMPartitionTableEntry)pte).getSystemIndicator() != IBMPartitionTypes.PARTTYPE_LINUXNATIVE)
				return false;
		}
		//need to check the magic
		ByteBuffer magic = ByteBuffer.allocate(2);
		try{
		    devApi.read(1024, magic);
		} catch(IOException e) {
		    return false;
		}
		int magicNumber = BigEndian.getInt16(magic.array(), 0);
		return (magicNumber == HfsPlusConstants.HFSPLUS_SUPER_MAGIC);
	}

}
