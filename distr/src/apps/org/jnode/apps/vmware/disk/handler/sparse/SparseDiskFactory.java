package org.jnode.apps.vmware.disk.handler.sparse;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.ExtentDeclaration;
import org.jnode.apps.vmware.disk.IOUtils;
import org.jnode.apps.vmware.disk.descriptor.*;
import org.jnode.apps.vmware.disk.extent.Access;
import org.jnode.apps.vmware.disk.extent.Extent;
import org.jnode.apps.vmware.disk.extent.ExtentType;
import org.jnode.apps.vmware.disk.tools.DiskFactory;
import org.jnode.driver.bus.ide.IDEConstants;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
public class SparseDiskFactory extends DiskFactory 
{
	private static final Logger LOG = Logger.getLogger(SparseDiskFactory.class);
		
	@Override
	protected File createDiskImpl(File directory, String name, long size) throws IOException 
	{		
		File mainFile = createMainFile(directory, name, size);
		return mainFile;
	}
	
	protected File createMainFile(File directory, String name, long size) throws IOException 
	{		
		File mainFile = new File(directory, createDiskFileName(name, 0));
		RandomAccessFile raf = new RandomAccessFile(mainFile, "rw");
		FileChannel channel = raf.getChannel(); 

		Descriptor descriptor = buildDescriptor(mainFile, size);
		SparseExtentHeader header = new SparseExtentHeader(); 	

		
		return mainFile;
	}
	
	protected Descriptor buildDescriptor(File mainFile, long size)
	{
		LOG.info("buildDescriptor: wanted size="+size);
		long sizeInSectors = size / IDEConstants.SECTOR_SIZE;
		if((size % IDEConstants.SECTOR_SIZE) != 0)
		{
			LOG.debug("buildDescriptor: adding 1 more sector to fit size");
			sizeInSectors ++;
		}
		
		// build DiskDatabase
		DiskDatabase ddb = new DiskDatabase();
		ddb.setAdapterType(AdapterType.ide);
		ddb.setSectors(64);
		ddb.setHeads(32);
		
		int cylinderCapacity = ddb.getSectors() * ddb.getHeads(); 
		int cylinders = (int) (sizeInSectors / cylinderCapacity);
		if((sizeInSectors % cylinderCapacity) != 0)
		{
			LOG.debug("buildDescriptor: adding 1 more cylinder to fit size");
			cylinders++;
		}
		ddb.setCylinders(cylinders);

		int nbSectors = ddb.getCylinders() * ddb.getHeads() * ddb.getSectors();
		LOG.info("buildDescriptor: allocated size="+(nbSectors * IDEConstants.SECTOR_SIZE));
		
		// build Header
		Header header = new Header();
		header.setVersion("1");
		header.setContentID(0);
		header.setParentContentID(Header.CID_NOPARENT);
		header.setCreateType(CreateType.monolithicSparse);
		header.setParentFileNameHint("");
		
		// build extents
		List<Extent> extents = new ArrayList<Extent>();
		SparseExtent mainExtent = createMainExtent(mainFile, nbSectors); 
		extents.add(mainExtent);
		
		Descriptor descriptor = new Descriptor(mainFile, header, extents, ddb);
		mainExtent.setDescriptor(descriptor);
		return descriptor;
	}
	
	private SparseExtent createMainExtent(File mainFile, int nbSectors) 
	{
		long offset = 0L; //TODO should be changed ?
		ExtentDeclaration extentDecl = IOUtils.createExtentDeclaration(mainFile, mainFile.getName(), Access.RW, nbSectors, ExtentType.SPARSE, offset);

		// create extent header
		SparseExtentHeader sparseHeader = new SparseExtentHeader();
		sparseHeader.setValidNewLineDetectionTest(true);
		sparseHeader.setRedundantGrainTableWillBeUsed(false);
		
		sparseHeader.setGrainSize(16);
		sparseHeader.setDescriptorOffset(0); //TODO set value
		sparseHeader.setDescriptorSize(0); //TODO set value
		sparseHeader.setRgdOffset(0); //TODO set value
		
		sparseHeader.setNumGTEsPerGT(512);
		sparseHeader.setGdOffset(0); //TODO set value
		sparseHeader.setOverHead(0); //TODO set value
		sparseHeader.setUncleanShutdown(false);
		
		
		int nbGrains = (int) (nbSectors / sparseHeader.getGrainSize());
		int modulo = (int) (nbSectors % sparseHeader.getGrainSize()); 
		if(modulo != 0)
		{
			nbGrains++;
			nbSectors += (sparseHeader.getGrainSize() - modulo);
		}
		sparseHeader.setCapacity(nbSectors);
		
		IOUtils.computeGrainTableCoverage(sparseHeader);
		
		// create allocation table
		int nbEntries = (int) (nbSectors / sparseHeader.getGrainTableCoverage());
		int[] entries = new int[nbEntries];
		Arrays.fill(entries, 0);		
		GrainDirectory gd = new GrainDirectory(entries); 
		GrainTable[] gTables = null;
		AllocationTable allocationTable = new AllocationTable(gd, gTables);
				
		SparseExtent mainExtent = new SparseExtent(null, extentDecl, sparseHeader, allocationTable, allocationTable);
		return mainExtent;
	}

	protected String createDiskFileName(String name, int index)
	{
		return name + "-" + index + ".vmdk";
	}
}
