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

import java.io.IOException;

import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.util.FSUtils;
import org.jnode.test.fs.filesystem.AbstractFSTest;

/**
 * 
 * @author Fabien DUMINY
 */
public class TreeFSTest extends AbstractFSTest {
    public TreeFSTest()
    {
        super();
    }
            
    public TreeFSTest(String name)
    {
        super(name);
    }
                
	public void testFSTree() throws IOException, Exception
	{
		if(!isReadOnly())
		{
			FSDirectory rootDir = getFs().getRootEntry().getDirectory();
			FSEntry dir1 = rootDir.addDirectory("dir1");
			assertNotNull("dir1 not added", rootDir.getEntry("dir1"));
			
			/*FSEntry dir11=*/ dir1.getDirectory().addDirectory("dir1.1");
			assertNotNull("dir11 not added", dir1.getDirectory().getEntry("dir1.1"));
						
			FSDirectory gotRootDir = getFs().getRootEntry().getDirectory();
			//assertNotNull("rootDir not saved", gotRootDir);
			assertTrue("same ref (gotRootDir) after remount", gotRootDir==rootDir);
			
			FSEntry gotDir1 = gotRootDir.getEntry("dir1");
			//assertNotNull("dir1 not saved", gotDir1);
			assertTrue("same ref (gotDir1) after remount", gotDir1==dir1);
			assertEquals("returned bad entry",dir1.getName(), gotDir1.getName());			
		}
	}

	public void testFSTreeWithRemountAndShortName() throws Exception
	{
		doTestFSTreeWithRemount("dir1");
	}
	
	public void testFSTreeWithRemountAndLongName() throws Exception
	{
		doTestFSTreeWithRemount("This is a Long FileName.extension");
	}

	private void doTestFSTreeWithRemount(String fileName) throws Exception
	{
		if(!isReadOnly())
		{
			FSDirectory rootDir = getFs().getRootEntry().getDirectory();
			log.debug("### testFSTreeWithRemount: rootDir=\n" + FSUtils.toString(rootDir, true));
			
			FSEntry dir1 = rootDir.addDirectory(fileName);
			assertNotNull("'"+fileName+"' not added", rootDir.getEntry(fileName));
			
			log.debug("### testFSTreeWithRemount: before remountFS");
			remountFS();
			log.debug("### testFSTreeWithRemount: after remountFS");
			
			FSDirectory gotRootDir = getFs().getRootEntry().getDirectory();
			assertNotNull("rootDir not saved", gotRootDir);
			assertFalse("same ref (gotRootDir) after remount", gotRootDir==rootDir);
			log.debug("### testFSTreeWithRemount: gotRootDir=\n" + FSUtils.toString(gotRootDir, true));
			
			FSEntry gotDir1 = gotRootDir.getEntry(fileName);
			log.debug("### testFSTreeWithRemount: after gotRootDir.getEntry");
			assertNotNull("'"+fileName+"' not saved", gotDir1);
			assertFalse("same ref (gotDir1) after remount", gotDir1==dir1);
			assertEquals("returned bad entry",dir1.getName(), gotDir1.getName());
		}
	}
}
