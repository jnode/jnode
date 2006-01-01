/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 * You should have received a copy of the GNU General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.test.fs.filesystem.tests;

import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.ReadOnlyFileSystemException;
import org.jnode.fs.util.FSUtils;
import org.jnode.test.fs.filesystem.AbstractFSTest;
import org.jnode.test.support.TestUtils;

/**
 * 
 * @author Fabien DUMINY
 */
public class BasicFSTest extends AbstractFSTest {
    public BasicFSTest()
    {
        super();
    }

    public BasicFSTest(String name)
    {
        super(name);
    }
    
//	public void testGetRootEntry() throws Exception
//	{
//		FSEntry rootEntry = getFs().getRootEntry();
//		log.debug(FSUtils.toString(rootEntry, false));
//	}
//
//	public void testListRootEntries() throws Exception
//	{
//		FSEntryIterator iterator = getFs().getRootEntry().getDirectory().iterator();
//		TestUtils.listEntries(iterator);
//	}
	
	public void testAddDirectory() throws Exception
	{
		FSDirectory rootDir = getFs().getRootEntry().getDirectory();
		String dirName = "A new directory.text";
		
		log.debug("Root dir before testAddDirectory :");
		TestUtils.listEntries(rootDir.iterator());				
		if(isReadOnly())
		{
			try {
				rootDir.addDirectory(dirName);
				fail("addDirectory must fail in readOnly mode");
			} catch (ReadOnlyFileSystemException e) {
				// success
			}
            assertContainsOnly("must be empty", rootDir.iterator(), getEmptyDirNames(true));			
		}
		else
		{
			try {
				FSEntry entry = rootDir.addDirectory(dirName);
				// success
				log.debug("added directory entry="+FSUtils.toString(entry, true));
			} catch (ReadOnlyFileSystemException e) {
				fail("addDirectory must succeed in read/write mode");				
			}
            assertContainsOnly("must contain "+dirName, rootDir.iterator(), 
					TestUtils.append(getEmptyDirNames(true), new String[]{dirName}));			
			FSEntry gotEntry = rootDir.getEntry(dirName);
			assertNotNull("must contain the added directory", gotEntry);					
			assertEquals("returned bad entry", dirName, gotEntry.getName());
		}
		log.debug("Root dir after testAddDirectory :\n"+rootDir);
		TestUtils.listEntries(rootDir.iterator());
	}

//	public void testAddFile() throws Exception
//	{
//		FSDirectory rootDir = getFs().getRootEntry().getDirectory();
//		String fileName = "A new file.text";
//
//		log.debug("Root dir before testAddFile :");
//		TestUtils.listEntries(rootDir.iterator());				
//		if(isReadOnly())
//		{
//			try {
//				rootDir.addFile(fileName);
//				fail("addFile must fail in readOnly mode");
//			} catch (ReadOnlyFileSystemException e) {
//				// success
//			}
//            
//			assertContainsOnly("must be empty", rootDir.iterator(), getEmptyDirNames(true));
//		}
//		else
//		{
//			try {
//				FSEntry entry = rootDir.addFile(fileName);
//				// success
//				log.debug("added file entry="+FSUtils.toString(entry, true));				
//			} catch (ReadOnlyFileSystemException e) {
//				fail("addFile must succeed in read/write mode");				
//			}
//            assertContainsOnly("must contain "+fileName, rootDir.iterator(), 
//					TestUtils.append(getEmptyDirNames(true), new String[]{fileName}));
//			FSEntry gotEntry = rootDir.getEntry(fileName);
//			assertNotNull("must contain the added file", gotEntry);					
//			assertEquals("returned bad entry", fileName, gotEntry.getName());
//		}
//		log.debug("Root dir after testAddFile :\n"+rootDir);
//		TestUtils.listEntries(rootDir.iterator());	
//	}
//
//	public void testAddFileThenRemountFSAndGetFile() throws IOException, Exception
//	{
//		if(!isReadOnly())
//		{
//			String filename = "a file to test.text";
//			FSDirectory rootDir = getFs().getRootEntry().getDirectory();
//			FSEntry entry = rootDir.addFile(filename);
//			FSEntry gotEntry = rootDir.getEntry(filename);
//			assertNotNull("must contain the added file", gotEntry);					
//			assertEquals("returned bad entry", filename, gotEntry.getName());
//			
//			log.debug("entry before remount="+FSUtils.toString(entry, true));
//			remountFS();
//			
//			FSDirectory rootDir2 = getFs().getRootEntry().getDirectory();
//			TestUtils.listEntries(rootDir2.iterator());				
//			assertFalse("same ref (rootDir) after remount", rootDir==rootDir2);
//			FSEntry gotEntry2 = rootDir2.getEntry(filename);
//			assertFalse("same ref (gotEntry2) after remount", gotEntry==gotEntry2);
//			assertNotNull("must contain the added file", gotEntry2);					
//			assertEquals("returned bad entry", filename, gotEntry2.getName());
//			log.debug("entry after remount="+FSUtils.toString(gotEntry2, true));
//		}
//	}
//	
//	public void testRemoveThenRemountFSAndGetEntry() throws Exception
//	{
//		if(!isReadOnly())
//		{
//			String filename = "a file to test.text";
//			FSDirectory rootDir = getFs().getRootEntry().getDirectory();
//			/*FSEntry entry =*/ rootDir.addFile(filename);
//			FSEntry gotEntry = rootDir.getEntry(filename);
//			assertNotNull("must contain the added file", gotEntry);					
//			assertEquals("returned bad entry", filename, gotEntry.getName());
//			
//			rootDir.remove(filename);
//			assertNull("must not contain the removed file", rootDir.getEntry(filename));
//			
//			remountFS();
//			
//			FSDirectory rootDir2 = getFs().getRootEntry().getDirectory();
//			TestUtils.listEntries(rootDir2.iterator());				
//			assertFalse("same ref (rootDir) after remount", rootDir==rootDir2);
//			FSEntry gotEntry2 = rootDir2.getEntry(filename);
//			assertNull("must not contain the removed file", gotEntry2);					
//		}		
//	}
}
