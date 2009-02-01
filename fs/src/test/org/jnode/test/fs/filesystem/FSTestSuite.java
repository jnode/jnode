/*
 * $Id$
 *
 * JNode.org
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

import junit.extensions.jfunc.JFuncSuite;
import junit.extensions.jfunc.textui.JFuncRunner;

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
    public static void main(String[] args) throws Throwable {
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
