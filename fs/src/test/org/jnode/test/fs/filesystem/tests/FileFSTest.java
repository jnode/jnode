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
 
package org.jnode.test.fs.filesystem.tests;

import org.apache.log4j.Logger;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSFile;
import org.jnode.fs.ReadOnlyFileSystemException;
import org.jnode.fs.util.FSUtils;
import org.jnode.test.fs.filesystem.AbstractFSTest;
import org.jnode.test.support.TestUtils;

/**
 * 
 * @author Fabien DUMINY
 */
public class FileFSTest extends AbstractFSTest {
    public FileFSTest()
    {
        super();
    }
        
    public FileFSTest(String name)
    {
        super(name);
    }
            
	public void testWriteFileThenRemountFSAndRead() throws Exception
	{
		final String fileName = "RWTest";		
		
		FSDirectory rootDir = getFs().getRootEntry().getDirectory();
		FSFile file = null;
		byte[] data = TestUtils.getTestData(FILE_SIZE_IN_WORDS);
				
		if(isReadOnly())
		{
			try
			{
				file = rootDir.addFile(fileName).getFile();
				fail("addFile must throw ReadOnlyFileSystemException in readOnly mode");
			}
			catch(ReadOnlyFileSystemException rofse)
			{
				// success
			}
		}
		else
		{
			file = rootDir.addFile(fileName).getFile();			
			file.write(0, data, 0, data.length);
			file.flush();
			
			assertEquals("bad file.length after write", data.length, file.getLength());			
		}
				
		remountFS();

		if(!isReadOnly())
		{
			FSDirectory rootDir2 = getFs().getRootEntry().getDirectory();
			FSFile file2 = rootDir2.getEntry(fileName).getFile();
			assertNotNull("file not saved", file2);
			assertEquals("bad file.length after remount", data.length, file2.getLength());			
			
			byte[] data2 = new byte[data.length];
			log.debug(getFs().getClass().getName()+ ": buffer after alloc\n"+FSUtils.toString(data2, 0, 512));
			file2.read(0, data2, 0, data2.length);
			log.debug(getFs().getClass().getName()+ ": buffer after read\n"+FSUtils.toString(data2, 0, 512));
			assertTrue("read and written data are differents", TestUtils.equals(data, data2));
		}
	}
	
	public void testWriteFileInReadOnlyMode() throws Exception
	{
		if(isReadOnly())
		{
			final String fileName = "RWTest";

			byte[] data = addTestFile(fileName, FILE_SIZE_IN_WORDS);
			
			// re-get the entry to our test file
			FSDirectory rootDir2 = getFs().getRootEntry().getDirectory();
			FSFile file2 = rootDir2.getEntry(fileName).getFile();

			// In readOnly mode, writing to our file must fail
			try
			{
				file2.write(0, data, 0, data.length);				
				fail("write must throw ReadOnlyFileSystemException in readOnly mode");
			}
			catch(ReadOnlyFileSystemException rofse)
			{
				// success
			}
		}		
	}
	
	public void testSetLength() throws Exception
	{
		final int newSize = 128;
		final String fileName = "RWTest";
		
		if(isReadOnly())
		{
			byte[] data = addTestFile(fileName, FILE_SIZE_IN_WORDS);
			
			FSFile file = getFs().getRootEntry().getDirectory().getEntry(fileName).getFile();
			try
			{
				file.setLength(newSize);
				fail("setLength must throw ReadOnlyFileSystemException in readOnly mode");
			}
			catch(ReadOnlyFileSystemException rofse)
			{
				// success
			}
			assertEquals("setLength mustn't change size in readOnly mode", data.length, file.getLength());
		}
		else
		{
			/*byte[] data =*/ addTestFile(fileName, FILE_SIZE_IN_WORDS);
			
			FSFile file = getFs().getRootEntry().getDirectory().getEntry(fileName).getFile();
			file.setLength(newSize);
			assertEquals("setLength must change size", newSize, file.getLength());			
		}
	}
}
