package org.jnode.apps.vmware.disk.handler;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.descriptor.Descriptor;
import org.jnode.apps.vmware.disk.descriptor.DiskDatabase;
import org.jnode.apps.vmware.disk.extent.Extent;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
abstract public class IOHandler {
	private static final Logger LOG = Logger.getLogger(IOHandler.class);
		
	public static final int SECTOR_SIZE = 512;
	
	final protected Descriptor descriptor;
	final private long nbSectors;
	
	final private Map<Extent, ExtentIO> extentIOCache = new HashMap<Extent, ExtentIO>(); 

	protected IOHandler(Descriptor descriptor) throws IOException {
		this.descriptor = descriptor;
		
		DiskDatabase ddb = descriptor.getDiskDatabase(); 
		nbSectors = ddb.getCylinders() * ddb.getHeads() * ddb.getSectors();
	}

	public void write(long sector, ByteBuffer data)
					throws IOException
	{ 
		int nbSectors = checkBounds(sector, data);
		writeImpl(sector, nbSectors, data);
	}

	public void read(long sector, ByteBuffer data)
					throws IOException
	{
		int nbSectors = checkBounds(sector, data);
		readImpl(sector, nbSectors, data);
	}

	public void flush() throws IOException
	{
		for(ExtentIO io : extentIOCache.values())
		{			
			io.flush();
		}
		extentIOCache.clear();
	}

	protected int checkBounds(long sector, ByteBuffer buffer) throws IOException {
		int nbSectors = buffer.remaining() / SECTOR_SIZE;
		if((buffer.remaining() % SECTOR_SIZE) != 0)
		{
			nbSectors++;
		}
		
		checkBounds(sector);
		checkBounds(sector + nbSectors - 1);
		
		return nbSectors;
	}
	
	protected void checkBounds(long sector) throws IOException
	{
		if(sector < 0)
		{
			throw new IOException("negative sector (actual:"+sector+")");
		}
		
		if(sector >= this.nbSectors)
		{
			throw new IOException("sector above limit("+this.nbSectors+") (actual:"+sector+")");
		}
	}

	protected ExtentIO getExtentIO(long sector) throws IOException 
	{
		Extent extent = getExtent(sector);
		return getExtentIO(extent);
	}

	final protected ExtentIO getExtentIO(Extent extent) throws IOException 
	{
		ExtentIO io = extentIOCache.get(extent);
		
		if(io == null)
		{
			RandomAccessFile raf = new RandomAccessFile(extent.getFile(), "rw");
			LOG.debug("length for file "+extent.getFileName()+" : "+raf.length());
			io = new ExtentIO(raf);
			extentIOCache.put(extent, io);
		}
		
		return io;
	}
	
	protected Extent getExtent(long sector) throws IOException 
	{
		Extent handler = null;
		for(Extent extent : descriptor.getExtents())
		{		
			LOG.debug(extent.getFileName()+": SizeInSectors="+extent.getSizeInSectors());
			
			if(sector < extent.getSizeInSectors())
			{
				handler = extent;
				break;
			}
			
			sector -= extent.getSizeInSectors();
		}
		return handler;
	}

	public void readImpl(long sector, int nbSectors, ByteBuffer dst)
			throws IOException {
				for(int i = 0 ; i < nbSectors ; i++, sector++)
				{
					getExtentIO(sector).read(sector, dst);
				}
			}

	public void writeImpl(long sector, int nbSectors, ByteBuffer src)
			throws IOException {
				for(int i = 0 ; i < nbSectors ; i++, sector++)
				{
					getExtentIO(sector).write(sector, src);
				}
			}
}
