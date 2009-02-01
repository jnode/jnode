/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.fs.hfsplus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

import org.jnode.driver.Device;
import org.jnode.driver.block.FileDevice;
import org.jnode.emu.plugin.model.DummyConfigurationElement;
import org.jnode.emu.plugin.model.DummyExtension;
import org.jnode.emu.plugin.model.DummyExtensionPoint;
import org.jnode.emu.plugin.model.DummyPluginDescriptor;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.service.FileSystemService;
import org.jnode.fs.service.def.FileSystemPlugin;

public class HfsPlusFileSystemTest extends TestCase {
    private String TEST_IMAGE_FILENAME = "/home/flesire/kvm/hfs2.img";

    public void testCreate() {
        try {
            File file = new File(TEST_IMAGE_FILENAME);
            Device device = new FileDevice(file, "rw");
            DummyPluginDescriptor desc = new DummyPluginDescriptor(true);
            DummyExtensionPoint ep = new DummyExtensionPoint("types",
                    "org.jnode.fs.types", "types");
            desc.addExtensionPoint(ep);
            DummyExtension extension = new DummyExtension();
            DummyConfigurationElement element = new DummyConfigurationElement();
            element
                    .addAttribute("class", HfsPlusFileSystemType.class
                            .getName());
            extension.addElement(element);
            ep.addExtension(extension);

            FileSystemService fss = new FileSystemPlugin(desc);
            HfsPlusFileSystemType type = fss
                    .getFileSystemType(HfsPlusFileSystemType.ID);
            HfsPlusFileSystem fs = new HfsPlusFileSystem(device, false, type);
            HFSPlusParams params = new HFSPlusParams();
            params.setVolumeName("testdrive");
            params.setBlockSize(params.OPTIMAL_BLOCK_SIZE);
            params.setJournaled(false);
            params.setJournalSize(params.DEFAULT_JOURNAL_SIZE);
            fs.create(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testRead() {
        File file = new File(TEST_IMAGE_FILENAME);
        try {
            Device device = new FileDevice(file, "rw");
            HfsPlusFileSystem fs = new HfsPlusFileSystemType().create(device,
                    false);
            fs.read();
            fs.createRootEntry();
            FSDirectory root = fs.getRootEntry().getDirectory();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
    }

}
