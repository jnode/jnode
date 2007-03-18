package org.jnode.apps.vmware.disk.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
public class Utils {
	private static final Logger LOG = Logger.getLogger(Utils.class);
	
	private static final String TEMP_DIR = "VMWareDisk";
	
	public static File createTempDir() throws IOException
	{
        String tmpDir = System.getProperty("java.io.tmpdir");        
        File dir = new File(tmpDir, TEMP_DIR);
        if(!dir.exists())
        {
        	if(!dir.mkdir())
        	{
        		throw new IOException("can't create directory "+dir);
        	}
        }
        else
        {
        	clearTempDir(false);
        }
        
        return dir;
	}

	public static void clearTempDir(boolean deleteDir) throws IOException
	{
        String tmpDir = System.getProperty("java.io.tmpdir");        
        File dir = new File(tmpDir, TEMP_DIR);
        if(dir.exists())
        {
            for(File tmpFile : dir.listFiles())
            {
            	LOG.debug("deleting file "+tmpFile);
            	tmpFile.delete();
            }                	
        }
        
        if(deleteDir)
        {
        	dir.delete();
        }        
	}

	public static File copyDisk(final File mainFile) throws IOException
	{
		final String name = mainFile.getName();
		final int idx = name.lastIndexOf('.'); 
		final String beginName = name.substring(0, idx);
		final String endName = name.substring(idx);
		
		final File parentDir = mainFile.getParentFile();
		File[] files = parentDir.listFiles(new FilenameFilter()
				{
					public boolean accept(File dir, String name) {
						boolean ok = name.startsWith(beginName) &&
							   name.endsWith(endName);
						return ok;
					}
				});

		File tmpDir = createTempDir();
        
        File mainFileCopy = null;
		for(File file : files)
		{
			File f = copyFile(file, tmpDir);
			if(file.getName().equals(mainFile.getName()))
			{
				mainFileCopy = f;
			}
		}
		
		return mainFileCopy;
	}
	
	public static File copyFile(File file, File dir) throws IOException
	{
		LOG.debug("copying file "+file.getName()+" to "+dir.getName());
		FileInputStream fis = null;
		FileOutputStream fos = null;
		File outFile = null;
		
		try
		{
			fis = new FileInputStream(file);
			FileChannel inCh = fis.getChannel();
			
			outFile = new File(dir, file.getName());
			fos = new FileOutputStream(outFile);
			FileChannel outCh = fos.getChannel();
			
			outCh.transferFrom(inCh, 0, inCh.size());
			
			return outFile;
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
			throw ioe;
		}
		finally
		{
			try {
				if(fos != null)
				{
					fos.close();
				}
			}
			finally
			{
				if(fis != null)
				{
					fis.close();
				}
			}
		}
	}
}
