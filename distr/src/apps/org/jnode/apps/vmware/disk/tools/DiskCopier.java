package org.jnode.apps.vmware.disk.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.test.Utils;

public class DiskCopier {
	private static final Logger LOG = Logger.getLogger(Utils.class);
	
	public static File copyDisk(final File mainFile, final File toDirectory) 
							throws IOException
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

        File mainFileCopy = null;
		for(File file : files)
		{
			File f = copyFile(file, toDirectory);
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
