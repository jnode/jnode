/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.test.fs.filesystem;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.naming.NameNotFoundException;

import org.jnode.driver.Device;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.test.fs.filesystem.config.FSTestConfig;
import org.jnode.test.support.AbstractTest;
import org.jnode.test.support.ContextManager;
import org.jnode.test.support.TestUtils;

/**
 * 
 * @author Fabien DUMINY
 */
public abstract class AbstractFSTest extends AbstractTest {
	public AbstractFSTest()
    {
        super(FSTestConfig.class);
    }

    /**
	 * 
	 */
	protected AbstractFSTest(String name)
	{
        super(FSTestConfig.class, name);
		this.fs = null;	
		this.device = null;
	}
	
	/**
	 * @return Returns the device.
	 */
	protected Device getDevice() {
		return device;
	}
	/**
	 * @return Returns the readOnly.
	 */
	final protected boolean isReadOnly() {
		return getFSTestConfig().isReadOnly();
	}

	/**
	 * @return Returns the fs.
	 */
	final protected FileSystem getFs() {
		return fs;
	}

	/**
	 * 
	 */
	public void setUp() throws Exception
	{
        super.setUp();
        
        device = getFSContext().getWorkDevice();
		fs = TestUtils.mountDevice(device, getFSTestConfig().getFsClass(), getFSTestConfig().isReadOnly());		
	}
	
	/**
	 * 
	 */
	public void tearDown() throws Exception
	{
		if(fs != null)
		{
			fs.close();
			fs = null;
		}
		super.tearDown();
	}

	/**
	 * 
	 * @param isRoot
	 * @return
	 */
	final protected String[] getEmptyDirNames(boolean isRoot)
	{
		return TestUtils.getEmptyDirNames(getFSTestConfig().getFsClass(), isRoot);
	}
    
    final protected FSTestConfig getFSTestConfig()
    {
        return (FSTestConfig) getTestConfig();
    }

    final protected FSContext getFSContext()
    {
        return (FSContext) ContextManager.getInstance().getContext();
    }
    
    protected void assertContainsOnly(String errorMessage, Iterator<? extends FSEntry> it, String[] requiredNames)
    {
        boolean ok = true;
        List<String> reqNames = (requiredNames == null) ? new ArrayList<String>() : new ArrayList<String>(Arrays.asList(requiredNames));
        List<String> names = TestUtils.getEntryNames(it);

        //log.debug("reqNames="+reqNames);
        //log.debug("names="+names);

        // remove required names from list
        for(int i = 0 ; i < reqNames.size(); i++)
        {
            if(!names.remove(reqNames.get(i)))
            {
                ok = false;
                break;
            }
        }

        if(ok)
        {
            // remaining names must be null
            for(int i = 0 ; i < names.size(); i++)
            {
                if(names.get(i) != null)
                {
                    ok = false;
                    break;
                }
            }
        }

        assertTrue(errorMessage+" (must contains only "+TestUtils.toString(reqNames)+") found: "+
                TestUtils.toString(names), ok);
    }
	
	/**
	 * 
	 * @throws NameNotFoundException
	 * @throws IOException
	 * @throws FileSystemException
	 */
	final protected void remountFS() throws NameNotFoundException, IOException, FileSystemException
	{
		remountFS(isReadOnly());
	}

	/**
	 * 
	 * @param readOnly
	 * @throws NameNotFoundException
	 * @throws IOException
	 * @throws FileSystemException
	 */
	final protected void remountFS(boolean readOnly) throws NameNotFoundException, IOException, FileSystemException
	{
		fs.close();
		this.fs = TestUtils.mountDevice(device, getFSTestConfig().getFsClass(), readOnly);
	}
	
	/**
	 * 
	 * @param fileName
	 * @param fileSizeInWords
	 * @return
	 * @throws Exception
	 * @throws IOException
	 * @throws FileSystemException
	 */
	final protected byte[] addTestFile(String fileName, int fileSizeInWords) throws Exception, IOException, FileSystemException
	{
		boolean oldReadOnly = isReadOnly();
		
		// remount FS in write mode, and write some data to our test file
		remountFS(false); // false = read/write mode
		
		FSDirectory rootDir = fs.getRootEntry().getDirectory();
		ByteBuffer data = ByteBuffer.wrap(TestUtils.getTestData(fileSizeInWords));
		FSFile file = rootDir.addFile(fileName).getFile();			
		file.write(0, data);
		file.flush();

		// remount FS in readOnly mode
		remountFS(oldReadOnly);
		
		return data.array();
	}
	    
	private FileSystem fs;	
	private Device device;
	
	//public static final int FILE_SIZE_IN_WORDS = 256 * 1024; // 512 Ko = 256 K Words
	public static final int FILE_SIZE_IN_WORDS = 128; // 512 Ko = 128 K Words
}
