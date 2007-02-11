/*
 *
 */
package org.jnode.fs.jfat;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;
import org.jnode.partitions.PartitionTableEntry;
import org.jnode.partitions.ibm.IBMPartitionTableEntry;
import org.jnode.partitions.ibm.IBMPartitionTypes;


/**
 * @author gvt
 */
public class FatFileSystemType implements FileSystemType {
    private static final Logger log =
        Logger.getLogger ( FatFileSystemType.class );
    
    public static final String NAME="JFAT";

    
    public String getName() {
	return NAME;
    }


    public boolean supports ( PartitionTableEntry pte,
			      byte[] firstSector,
			      FSBlockDeviceAPI devApi ) {
	if ( pte != null ) {
	    if ( !pte.isValid() )
		return false;

	    if ( ! ( pte instanceof IBMPartitionTableEntry ) )
		return false;

	    final IBMPartitionTableEntry ipte =
	    				(IBMPartitionTableEntry)pte;

		final IBMPartitionTypes type = ipte.getSystemIndicator();
		if((type == IBMPartitionTypes.PARTTYPE_WIN95_FAT32) ||
		   (type == IBMPartitionTypes.PARTTYPE_WIN95_FAT32_LBA) )
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	return false;
    }


    public FileSystem create ( Device device, boolean readOnly )
	throws FileSystemException {
	FatFileSystem fs = new FatFileSystem ( device, readOnly );

	return fs;
    }


    public FileSystem format ( Device device, Object specificOptions )
	throws FileSystemException {
	throw new FileSystemException ( "not implemented ... yet" );
    }
}
