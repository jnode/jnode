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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.naming.NameNotFoundException;
import junit.extensions.jfunc.JFuncTestCase;
import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.test.fs.filesystem.config.FSTestConfig;
import org.jnode.test.support.TestUtils;

/**
 * @author Fabien DUMINY
 */
public abstract class AbstractFSTest extends JFuncTestCase {
    protected final Logger log = Logger.getLogger(getClass());

    //public static final int FILE_SIZE_IN_WORDS = 256 * 1024; // 512 Ko = 256 K Words
    public static final int FILE_SIZE_IN_WORDS = 128; // 512 Ko = 128 K Words

    private FileSystem fs;
    private FSTestConfig config;
    private Device device;

    public AbstractFSTest() {
        super();
    }

    /**
     *
     */
    protected AbstractFSTest(String name) {
        super(name);
    }

    protected final void setUp(FSTestConfig config) throws NameNotFoundException, FileSystemException, IOException,
        InstantiationException, IllegalAccessException, Exception {
        super.setUp();

        this.config = config;
        this.device = config.getDeviceParam().createDevice();
        this.fs = config.getFileSystem().mount(this.device);
    }

    public final void tearDown() throws Exception {
        config.getDeviceParam().tearDown(device);
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
    protected final FileSystem getFs() {
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

    protected void assertFalse(String message, boolean condition) {
        assertTrue(message, !condition);
    }

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
