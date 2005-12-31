/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jnode.test.fs.filesystem.config.DeviceParam;
import org.jnode.test.fs.filesystem.config.FS;
import org.jnode.test.fs.filesystem.config.FSAccessMode;
import org.jnode.test.fs.filesystem.config.FSTestConfig;
import org.jnode.test.fs.filesystem.config.FSType;
import org.jnode.test.fs.filesystem.config.FileParam;
import org.jnode.test.fs.filesystem.config.OsType;
import org.jnode.test.fs.filesystem.tests.BasicFSTest;
import org.jnode.test.fs.filesystem.tests.ConcurrentAccessFSTest;
import org.jnode.test.fs.filesystem.tests.FileFSTest;
import org.jnode.test.fs.filesystem.tests.TreeFSTest;
import org.jnode.test.support.AbstractTestSuite;
import org.jnode.test.support.TestConfig;

public class FSTestSuite extends AbstractTestSuite
{
    public List<TestConfig> getConfigs()
    {
        List<TestConfig> configs = new ArrayList<TestConfig>();
        String tempDir = System.getProperty("java.io.tmpdir");
		final String diskFileName = tempDir + File.separatorChar + "diskimg.WRK";
		
        configs.addAll(createFileConfigs(OsType.OTHER_OS, FSType.EXT2, 
                FSAccessMode.BOTH, "1", DO_FORMAT, diskFileName, "1M"));

        configs.addAll(createFileConfigs(OsType.OTHER_OS, FSType.EXT2, 
                FSAccessMode.BOTH, "4", DO_FORMAT, diskFileName, "1M"));
		
//        configs.addAll(createFileConfigs(OsType.OTHER_OS, FSType.NTFS, 
//                FSAccessMode.BOTH, "", DO_FORMAT, diskFileName, "1M"));

        configs.addAll(createFileConfigs(OsType.OTHER_OS, FSType.FAT, 
                FSAccessMode.BOTH, "12", DO_FORMAT, diskFileName, "1M"));
		
        configs.addAll(createFileConfigs(OsType.OTHER_OS, FSType.FAT, 
                FSAccessMode.BOTH, "16", DO_FORMAT, diskFileName, "1M"));

        configs.addAll(createFileConfigs(OsType.OTHER_OS, FSType.FAT, 
                FSAccessMode.BOTH, "32", DO_FORMAT, diskFileName, "1M"));
        		
//        configs.addAll(createFileConfigs(OsType.OTHER_OS, FSType.ISO9660, 
//                FSAccessMode.BOTH, "", DO_FORMAT, diskFileName, "1M"));
		
/*        
        configs.addAll(createConfigs(OsType.JNODE_OS, FSType.EXT2, 
                FSAccessMode.BOTH, "1", DO_FORMAT));
        //<workDevice name="hdb5" />
    
        configs.addAll(createConfigs(OsType.JNODE_OS, FSType.EXT2, 
                FSAccessMode.BOTH, null, DO_NOT_FORMAT));
        //<workRamdisk size="1M" />
*/
        
        return configs;
    }
    
    public Class[] getTestSuites()
    {
        return new Class[]
                  {
                          BasicFSTest.class, 
                          FileFSTest.class, 
                          TreeFSTest.class, 
                          ConcurrentAccessFSTest.class
                  };
    }
    
    public static final boolean DO_FORMAT = true;
    public static final boolean DO_NOT_FORMAT = false;

    static private List<FSTestConfig> createFileConfigs(OsType osType, FSType fsType, 
            FSAccessMode accessMode, String options, boolean format, String fileSize, String fileName)
    {
        FileParam fp = new FileParam(fileSize, fileName);
        return createConfigs(osType, fsType, 
                accessMode, options, format, fp);        
    }
    
    static private List<FSTestConfig> createConfigs(OsType osType, FSType fsType, 
            FSAccessMode accessMode, String options, boolean format, DeviceParam device)
    {
        List<FSTestConfig> configs = new ArrayList<FSTestConfig>();
        
        if(osType.isCurrentOS())
        {
            if(accessMode.doReadOnlyTests())
            {                   
                // true=readOnly mode 
                FS fs = new FS(fsType, true, options, format);
                
                FSTestConfig cfg = new FSTestConfig(osType, fs, device);
                configs.add(cfg);
            }
            
            if(accessMode.doReadWriteTests())
            {
                // false=readWrite mode 
                FS fs = new FS(fsType, false, options, format);
                
                FSTestConfig cfg = new FSTestConfig(osType, fs, device);
                configs.add(cfg);
            }
        }
        
        return configs;        
    }    
}
