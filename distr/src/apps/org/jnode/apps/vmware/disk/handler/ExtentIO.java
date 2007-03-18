package org.jnode.apps.vmware.disk.handler;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.handler.sparse.SparseExtentFactory;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
public class ExtentIO 
{
	private static final Logger LOG = Logger.getLogger(ExtentIO.class);
		
	private final RandomAccessFile raf;
	private final FileChannel channel;
	
	public ExtentIO(RandomAccessFile raf)
	{
		this.raf = raf;
		this.channel = raf.getChannel();		
	}
	
	public void read(long sector, ByteBuffer dst) throws IOException 
	{
		int oldLimit = dst.limit();
		dst.limit((int) (dst.position() + IOHandler.SECTOR_SIZE));
		
		channel.position(IOHandler.SECTOR_SIZE * sector);
		LOG.debug("channel pos before : "+channel.position());
		int read = channel.read(dst);
		LOG.debug("channel pos after : "+channel.position());
		LOG.debug("nb bytes read: "+read);
		
		dst.limit(oldLimit);
	}

	public void write(long sector, ByteBuffer src) throws IOException 
	{
		channel.position(IOHandler.SECTOR_SIZE * sector);
		channel.write(src);
	}
	
	public void flush() throws IOException 
	{
		raf.close();
		channel.close();
	}	
}