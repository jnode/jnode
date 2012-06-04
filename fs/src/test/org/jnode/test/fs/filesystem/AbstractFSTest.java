/*
 * $Id$
 *
 * Copyright (C) 2003-2012 JNode.org
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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.naming.NameNotFoundException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.emu.naming.BasicNameSpace;
import org.jnode.emu.plugin.model.DummyConfigurationElement;
import org.jnode.emu.plugin.model.DummyExtension;
import org.jnode.emu.plugin.model.DummyExtensionPoint;
import org.jnode.emu.plugin.model.DummyPluginDescriptor;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.service.FileSystemService;
import org.jnode.fs.service.def.FileSystemPlugin;
import org.jnode.naming.InitialNaming;
import org.jnode.test.fs.filesystem.config.FSTestConfig;
import org.jnode.test.fs.filesystem.config.FSType;
import org.jnode.test.support.TestUtils;
import org.jnode.util.OsUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Fabien DUMINY
 */
@RunWith(Parameterized.class)
public abstract class AbstractFSTest extends TestCase {
    protected final Logger log = Logger.getLogger(getClass());
    private static boolean setup = false;

    //public static final int FILE_SIZE_IN_WORDS = 256 * 1024; // 512 Ko = 256 K Words
    public static final int FILE_SIZE_IN_WORDS = 128; // 512 Ko = 128 K Words

    private FileSystem<?> fs;
    protected FSTestConfig config;
    private Device device;

    public AbstractFSTest(FSTestConfig config) {
        super();
        this.config = config;
    }

    @Parameters
    public static List<FSTestConfig[]> getData() {
        List<FSTestConfig[]> config = new ArrayList<FSTestConfig[]>();
        for (FSTestConfig cfg : new FSConfigurations()) {
            config.add(new FSTestConfig[] {cfg});
        }
        return config;
    }
    
    /**
     *
     */
    protected AbstractFSTest(String name) {
        super(name);
    }

    @Before
    public final void setUp() throws NameNotFoundException, FileSystemException, IOException,
        InstantiationException, IllegalAccessException, Exception {
        super.setUp();
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
        this.device = config.getDeviceParam().createDevice();
        this.fs = config.getFileSystem().format(this.device);
        this.fs = config.getFileSystem().mount(this.device);
    }

    @After
    public final void tearDown() throws Exception {
        // Some tests don't call setup(config), which means that config will be null when teardown is called.
        if (config != null) {
            config.getDeviceParam().tearDown(device);
        }
        super.tearDown();
    }

//  /**
//   * @return Returns the device.
//   */
//  protected Device getDevice() {
//      return device;
//  }
//  /**
//   * @return Returns the readOnly.
//   */
//  final protected boolean isReadOnly() {
//      return getFSTestConfig().isReadOnly();
//  }

    /**
     * @return Returns the fs.
     */
    protected final FileSystem<?> getFs() {
        return fs;
    }

    /**
     *
     */
//  public void setUp() throws Exception
//  {
//      super.setUp();
//        
//     device = getFSContext().getWorkDevice();
//      fs = TestUtils.mountDevice(device, getFSTestConfig().getFsClass(), getFSTestConfig().isReadOnly());
//  }

//  /**
//   *
//   */
//  public void tearDown() throws Exception
//  {
//      if(fs != null)
//      {
//          fs.close();
//          fs = null;
//      }
//      super.tearDown();
//  }

    /**
     * @param isRoot
     * @return
     */
    protected final String[] getEmptyDirNames(FSTestConfig config, boolean isRoot) {
        return config.getFileSystem().getType().getEmptyDirNames(isRoot);
    }

//    final protected FSTestConfig getFSTestConfig()
//    {
//        return (FSTestConfig) getTestConfig();
//    }
//
//    final protected FSContext getFSContext()
//    {
//        return (FSContext) ContextManager.getInstance().getContext();
//    }

//    protected void assertFalse(String message, boolean condition) {
//        assertTrue(message, !condition);
//    }

    protected void assertContainsOnly(String errorMessage, Iterator<? extends FSEntry> it, String[] requiredNames) {
        boolean ok = true;
        List<String> reqNames =
            (requiredNames == null) ? new ArrayList<String>() : new ArrayList<String>(Arrays.asList(requiredNames));
        List<String> names = TestUtils.getEntryNames(it);

        //log.debug("reqNames="+reqNames);
        //log.debug("names="+names);

        // remove required names from list
        for (int i = 0; i < reqNames.size(); i++) {
            if (!names.remove(reqNames.get(i))) {
                ok = false;
                break;
            }
        }

        if (ok) {
            // remaining names must be null
            for (int i = 0; i < names.size(); i++) {
                if (names.get(i) != null) {
                    ok = false;
                    break;
                }
            }
        }

        assertTrue(errorMessage + " (must contains only " + TestUtils.toString(reqNames) + ") found: " +
            TestUtils.toString(names), ok);
    }

    /**
     * @param readOnly
     * @throws NameNotFoundException
     * @throws IOException
     * @throws FileSystemException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    protected final void remountFS(FSTestConfig config, boolean readOnly)
        throws NameNotFoundException, IOException, FileSystemException, InstantiationException, IllegalAccessException {
        fs.close();
        fs = config.getFileSystem().getType().mount(fs.getDevice(), readOnly);
    }

    /**
     * @param fileName
     * @param fileSizeInWords
     * @return
     * @throws Exception
     * @throws IOException
     * @throws FileSystemException
     */
    protected final byte[] addTestFile(String fileName, int fileSizeInWords)
        throws Exception, IOException, FileSystemException {
        boolean oldReadOnly = config.isReadOnly();

        // remount FS in write mode, and write some data to our test file
        remountFS(config, false); // false = read/write mode

        FSDirectory rootDir = fs.getRootEntry().getDirectory();
        ByteBuffer data = ByteBuffer.wrap(TestUtils.getTestData(fileSizeInWords));
        FSFile file = rootDir.addFile(fileName).getFile();
        file.write(0, data);
        file.flush();

        // remount FS in readOnly mode
        remountFS(config, oldReadOnly);

        return data.array();
    }
}
