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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.test.fs.filesystem;

import junit.extensions.jfunc.JFuncSuite;
import junit.extensions.jfunc.textui.JFuncRunner;
import org.jnode.test.fs.filesystem.config.FSTestConfig;
import org.jnode.test.fs.filesystem.tests.BasicFSTest;
import org.jnode.test.fs.filesystem.tests.ConcurrentAccessFSTest;
import org.jnode.test.fs.filesystem.tests.FileFSTest;
import org.jnode.test.fs.filesystem.tests.TreeFSTest;

public class FSTestSuite extends JFuncSuite {
    public static void main(String[] args) throws Throwable {
        JFuncRunner.run(FSTestSuite.suite());
        //JFuncRunner.main(new String[]{"-v", "--color", FSTestSuite.class.getName()});
        //JFuncRunner.main(new String[]{"-v", FSTestSuite.class.getName()});
    }

    public static JFuncSuite suite() throws Throwable {
        JFuncSuite suite = new JFuncSuite();

        for (FSTestConfig config : new FSConfigurations()) {
            BasicFSTest basicTest = (BasicFSTest) suite.getTestProxy(new BasicFSTest());
            basicTest.testAddDirectory(config);
            basicTest.testAddFile(config);
            basicTest.testAddFileThenRemountFSAndGetFile(config);
            basicTest.testGetRootEntry(config);
            basicTest.testListRootEntries(config);
            basicTest.testRemoveThenRemountFSAndGetEntry(config);

            FileFSTest fileTest = (FileFSTest) suite.getTestProxy(new FileFSTest());
            fileTest.testSetLength(config);
            fileTest.testWriteFileInReadOnlyMode(config);
            fileTest.testWriteFileThenRemountFSAndRead(config);

            TreeFSTest treeTest = (TreeFSTest) suite.getTestProxy(new TreeFSTest());
            treeTest.testFSTree(config);
            treeTest.testFSTreeWithRemountAndLongName(config);
            treeTest.testFSTreeWithRemountAndShortName(config);

            ConcurrentAccessFSTest threadTest = (ConcurrentAccessFSTest)
                suite.getTestProxy(new ConcurrentAccessFSTest());
            threadTest.testRead(config);
            threadTest.testWrite(config);
            threadTest.testReadWrite(config);
        }

        return suite;
    }
}
