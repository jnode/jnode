package org.jnode.test.fs.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jnode.fs.fat.Fat;
import org.jnode.test.fs.filesystem.config.DeviceParam;
import org.jnode.test.fs.filesystem.config.FS;
import org.jnode.test.fs.filesystem.config.FSAccessMode;
import org.jnode.test.fs.filesystem.config.FSTestConfig;
import org.jnode.test.fs.filesystem.config.FSType;
import org.jnode.test.fs.filesystem.config.FileParam;
import org.jnode.test.fs.filesystem.config.OsType;

public class FSConfigurations implements Iterable<FSTestConfig> {
    public static final boolean DO_FORMAT = true;
    public static final boolean DO_NOT_FORMAT = false;

	private List<FSTestConfig> configs = new ArrayList<FSTestConfig>();
	
	public Iterator<FSTestConfig> iterator() {
		return configs.iterator();
	}
	
    public FSConfigurations()
    {
        String tempDir = System.getProperty("java.io.tmpdir");
		final String diskFileName = tempDir + File.separatorChar + "diskimg.WRK";
		
        configs.addAll(createFileConfigs(OsType.OTHER_OS, FSType.EXT2, 
                FSAccessMode.BOTH, Integer.valueOf(1), DO_FORMAT, diskFileName, "1M"));

        configs.addAll(createFileConfigs(OsType.OTHER_OS, FSType.EXT2, 
                FSAccessMode.BOTH, Integer.valueOf(4), DO_FORMAT, diskFileName, "1M"));
		
//        configs.addAll(createFileConfigs(OsType.OTHER_OS, FSType.NTFS, 
//                FSAccessMode.BOTH, "", DO_FORMAT, diskFileName, "1M"));

        configs.addAll(createFileConfigs(OsType.OTHER_OS, FSType.FAT, 
                FSAccessMode.BOTH, Fat.FAT12, DO_FORMAT, diskFileName, "1M"));
		
        configs.addAll(createFileConfigs(OsType.OTHER_OS, FSType.FAT, 
                FSAccessMode.BOTH, Fat.FAT16, DO_FORMAT, diskFileName, "1M"));

        configs.addAll(createFileConfigs(OsType.OTHER_OS, FSType.FAT, 
                FSAccessMode.BOTH, Fat.FAT32, DO_FORMAT, diskFileName, "1M"));
        		
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
    }
    
    private List<FSTestConfig> createFileConfigs(OsType osType, FSType fsType, 
            FSAccessMode accessMode, Object options, boolean format, String fileSize, String fileName)
    {
        FileParam fp = new FileParam(fileSize, fileName);
        return createConfigs(osType, fsType, 
                accessMode, options, format, fp);        
    }
    
    private List<FSTestConfig> createConfigs(OsType osType, FSType fsType, 
            FSAccessMode accessMode, Object options, boolean format, DeviceParam device)
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
