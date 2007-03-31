package org.jnode.apps.vmware.disk.descriptor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.ExtentDeclaration;
import org.jnode.apps.vmware.disk.IOUtils;
import org.jnode.apps.vmware.disk.IOUtils.KeyValue;
import org.jnode.apps.vmware.disk.extent.Access;
import org.jnode.apps.vmware.disk.extent.Extent;
import org.jnode.apps.vmware.disk.extent.ExtentType;
import org.jnode.apps.vmware.disk.handler.ExtentFactory;
import org.jnode.apps.vmware.disk.handler.FileDescriptor;
import org.jnode.apps.vmware.disk.handler.UnsupportedFormatException;
import org.jnode.apps.vmware.disk.handler.sparse.SparseExtentHeader;
import org.jnode.util.ByteBufferInputStream;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
abstract public class DescriptorRW {
	private static final Logger LOG = Logger.getLogger(DescriptorRW.class);
		
	private static final String VERSION     = "version";
	private static final String CID         = "CID";
	private static final String PARENT_CID  = "parentCID";
	private static final String CREATE_TYPE = "createType";
	
	private static final String DDB          = "ddb";
	private static final String ADAPTER_TYPE = "ddb.adapterType";
	private static final String SECTORS      = "ddb.geometry.sectors";
	private static final String HEADS        = "ddb.geometry.heads";
	private static final String CYLINDERS    = "ddb.geometry.cylinders";
	
	public Descriptor read(File file, int firstSector, int nbSectors) 
				throws IOException, UnsupportedFormatException
	{		
		RandomAccessFile raf = null;
		BufferedReader br = null;

		try {
			raf = new RandomAccessFile(file, "r");
			ByteBuffer bb = IOUtils.getSectorsByteBuffer(raf, firstSector, nbSectors);
			
			Reader r = new InputStreamReader(new ByteBufferInputStream(bb));
			br = new BufferedReader(r);
			
			Header header = readHeader(br);
			
			List<String> extentDecls = new ArrayList<String>();
			String lastLine = readExtents(br, extentDecls);
			DiskDatabase diskDatabase = readDiskDatabase(br, lastLine);
			
			List<Extent> extents = new ArrayList<Extent>(extentDecls.size());
			ExtentDeclaration mainExtentDecl = null;
			for(String decl : extentDecls)
			{
				ExtentDeclaration extentDecl = readExtentDeclaration(decl, file);
				if(extentDecl.isMainExtent())
				{
					mainExtentDecl = extentDecl;
				}
				else
				{
					FileDescriptor fileDescriptor = IOUtils.readFileDescriptor(
							extentDecl.getExtentFile());
		
					Extent extent = createExtent(fileDescriptor, extentDecl);
					extents.add(extent);
				}
			}
			
			Descriptor desc = new Descriptor(file, header, extents, diskDatabase);
			
			Extent mainExtent = createMainExtent(desc, mainExtentDecl);
			extents.add(0, mainExtent);
			
			return desc;
		}
		finally
		{
			if(br != null)
			{
				br.close();
			}
			
			if(raf != null)
			{
				raf.close();
			}
		}
	}
	
	protected DiskDatabase readDiskDatabase(BufferedReader br, String lastLine) throws IOException 
	{
		DiskDatabase ddb = new DiskDatabase();
		
		Map<String, String> values = IOUtils.readValuesMap(lastLine, br, true,
					ADAPTER_TYPE, SECTORS, HEADS, CYLINDERS);
		
		String value = values.get(ADAPTER_TYPE);
		ddb.setAdapterType(AdapterType.valueOf(value));
		
		value = values.get(SECTORS);
		ddb.setSectors(Integer.valueOf(value));
		
		value = values.get(HEADS);
		ddb.setHeads(Integer.valueOf(value));
		
		value = values.get(CYLINDERS);
		ddb.setCylinders(Integer.valueOf(value));
		
		return ddb;
	}

	protected String readExtents(BufferedReader br,
								 List<String> extentDecls) 
					throws IOException, UnsupportedFormatException 
	{		
		String line;
		
		while( ((line = IOUtils.readLine(br)) != null) &&
			   !line.startsWith(DDB))
		{
			extentDecls.add(line);
		}
		
		return line;
	}

	protected ExtentDeclaration readExtentDeclaration(String line, File mainFile)
	{
		StringTokenizer st = new StringTokenizer(line, " ", false);
		
		final Access access = Access.valueOf(st.nextToken());
		final long sizeInSectors = Long.valueOf(st.nextToken());
		final ExtentType extentType = ExtentType.valueOf(st.nextToken());
		
		final String fileName = IOUtils.removeQuotes(st.nextToken());
		
		long offset = 0L;
		if(st.hasMoreTokens())
		{
			offset = Long.valueOf(st.nextToken());
		}
					
		final File extentFile = IOUtils.getExtentFile(mainFile, fileName);
		final boolean isMainExtent = extentFile.getName().equals(mainFile.getName());
		return new ExtentDeclaration(access, sizeInSectors, extentType, fileName, extentFile, offset, isMainExtent);
	}
	
	protected Header readHeader(BufferedReader reader) throws IOException, UnsupportedFormatException
	{
		Header header = new Header();

		LOG.debug("trying to read VERSION");
		KeyValue keyValue = IOUtils.readValue(reader, null, VERSION, false);
		if(!"1".equals(keyValue.getValue()))
		{
			throw new UnsupportedFormatException("expected version 1 (found:"+keyValue.getValue()+")");
		}
		header.setVersion(keyValue.getValue());
		
		keyValue = IOUtils.readValue(reader, keyValue, CID, false);
		header.setContentID(Long.valueOf(keyValue.getValue(), 16));
		
		keyValue = IOUtils.readValue(reader, keyValue, PARENT_CID, false);
		header.setParentContentID(Long.parseLong(keyValue.getValue(), 16));
		
		keyValue = IOUtils.readValue(reader, keyValue, CREATE_TYPE, true);
		header.setCreateType(CreateType.valueOf(keyValue.getValue()));
		
		return header;
	}

	abstract protected Extent createMainExtent(Descriptor desc,
			ExtentDeclaration mainExtentDecl) throws IOException, UnsupportedFormatException;

	abstract protected Extent createExtent(FileDescriptor fileDescriptor, ExtentDeclaration extentDecl) 
				throws IOException, UnsupportedFormatException;
	
}
