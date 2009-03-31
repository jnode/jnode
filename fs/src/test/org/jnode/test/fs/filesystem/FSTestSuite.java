/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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

import javax.naming.NamingException;

import junit.extensions.jfunc.JFuncSuite;
import junit.extensions.jfunc.textui.JFuncRunner;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jnode.emu.naming.BasicNameSpace;
import org.jnode.emu.plugin.model.DummyConfigurationElement;
import org.jnode.emu.plugin.model.DummyExtension;
import org.jnode.emu.plugin.model.DummyExtensionPoint;
import org.jnode.emu.plugin.model.DummyPluginDescriptor;
import org.jnode.fs.service.FileSystemService;
import org.jnode.fs.service.def.FileSystemPlugin;
import org.jnode.naming.InitialNaming;
import org.jnode.test.fs.filesystem.config.FSTestConfig;
import org.jnode.test.fs.filesystem.config.FSType;
import org.jnode.test.fs.filesystem.tests.BasicFSTest;
import org.jnode.test.fs.filesystem.tests.ConcurrentAccessFSTest;
import org.jnode.test.fs.filesystem.tests.FileFSTest;
import org.jnode.test.fs.filesystem.tests.TreeFSTest;
import org.jnode.util.OsUtils;

/**
 * This class runs a suite of functional tests on the JNode file system
 * implementation.  The tests are designed to be run from the JNode development
 * sandbox.  The JNode core project needs to be on the bootclasspath to avoid 
 * classloader security problems.
 * 
 * @author Fabien DUMINY
 * @author crawley@jnode.org
 */
public class FSTestSuite extends JFuncSuite {
    private static boolean setup = false;
    public static void main(String[] args) throws Throwable {
        setUp();

        JFuncRunner.run(FSTestSuite.suite());
        //JFuncRunner.main(new String[]{"-v", "--color", FSTestSuite.class.getName()});
        //JFuncRunner.main(new String[]{"-v", FSTestSuite.class.getName()});
    }

    private static void setUp() throws NamingException {
        if (!setup && !OsUtils.isJNode()) {
            // We are not running in JNode, emulate a JNode environment.

            InitialNaming.setNameSpace(new BasicNameSpace());

            // Build a plugin descriptor that is sufficient for the FileSystemPlugin to
            // configure file system types for testing.
            DummyPluginDescriptor desc = new DummyPluginDescriptor(true);
            DummyExtensionPoint ep = new DummyExtensionPoint("types", "org.jnode.fs.types", "types");
            desc.addExtensionPoint(ep);
            for (FSType fsType : FSType.values()) {
                DummyExtension extension = new DummyExtension();
                DummyConfigurationElement element = new DummyConfigurationElement();
                element.addAttribute("class", fsType.getFsTypeClass().getName());
                extension.addElement(element);
                ep.addExtension(extension);
            }

            FileSystemService fss = new FileSystemPlugin(desc);
            InitialNaming.bind(FileSystemService.class, fss);
        }
        setup = true;
    }

    public static TestSuite suite() throws Throwable {
        setUp();
        TestSuite allTestsSuite = new TestSuite("All FS Tests");
                
        for (FSTestConfig config : new FSConfigurations()) {
            TestSuite tests = new TestSuite();
            tests.setName(config.getFileSystem().toString());
            addTest(tests, new BasicFSTest(config), "testAddDirectory");
            addTest(tests, new BasicFSTest(config), "testAddFile");
            addTest(tests, new BasicFSTest(config), "testAddFileThenRemountFSAndGetFile");
            addTest(tests, new BasicFSTest(config), "testGetRootEntry");
            addTest(tests, new BasicFSTest(config), "testListRootEntries");
            addTest(tests, new BasicFSTest(config), "testRemoveThenRemountFSAndGetEntry");

            addTest(tests, new FileFSTest(config), "testSetLength");
            addTest(tests, new FileFSTest(config), "testWriteFileInReadOnlyMode");
            addTest(tests, new FileFSTest(config), "testWriteFileThenRemountFSAndRead");
            
            addTest(tests, new TreeFSTest(config), "testFSTree");
            addTest(tests, new TreeFSTest(config), "testFSTreeWithRemountAndLongName");
            addTest(tests, new TreeFSTest(config), "testFSTreeWithRemountAndShortName");

            addTest(tests, new ConcurrentAccessFSTest(config), "testRead");
            addTest(tests, new ConcurrentAccessFSTest(config), "testWrite");
            addTest(tests, new ConcurrentAccessFSTest(config), "testReadWrite");

            allTestsSuite.addTest(tests);
        }
        
        return allTestsSuite;
    }
    
    private static void addTest(TestSuite suite, TestCase test, String name) {
        test.setName(name);
        suite.addTest(test);
    }
}
