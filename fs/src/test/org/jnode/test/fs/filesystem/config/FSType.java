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
 
package org.jnode.test.fs.filesystem.config;

import java.io.IOException;

import org.apache.tools.ant.types.EnumeratedAttribute;
import org.jnode.driver.Device;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.ext2.Ext2FileSystem;
import org.jnode.fs.ext2.Ext2FileSystemType;
import org.jnode.fs.fat.Fat;
import org.jnode.fs.fat.FatFileSystem;
import org.jnode.fs.fat.FatFileSystemType;
import org.jnode.fs.iso9660.ISO9660FileSystem;
import org.jnode.fs.iso9660.ISO9660FileSystemType;
import org.jnode.fs.ntfs.NTFSFileSystem;
import org.jnode.fs.ntfs.NTFSFileSystemType;
import org.jnode.test.support.TestUtils;

/**
 * 
 * @author Fabien DUMINY
 */
public class FSType {
    public static final FSType EXT2    = new FSType("ext2", Ext2FileSystem.class, Ext2FileSystemType.class);
    public static final FSType FAT     = new FSType("fat", FatFileSystem.class, FatFileSystemType.class);
    public static final FSType NTFS    = new FSType("ntfs", NTFSFileSystem.class, NTFSFileSystemType.class);
    public static final FSType ISO9660 = new FSType("iso9660", ISO9660FileSystem.class, ISO9660FileSystemType.class);
    
    private FSType(String name, Class fsClass, Class fsTypeClass)
    {
        this.name = name;
        this.fsClass = fsClass;
        this.fsTypeClass = fsTypeClass;        
    }
    
	public void format(Device device, String options) throws FileSystemException, IOException
	{
		TestUtils.formatDevice(device, fsClass, convertOptions(options));		
	}
	
	protected Object convertOptions(String options)
	{
        if(equals(EXT2))
        {
            return Integer.valueOf(options);                    
        }
        else if(equals(FAT))
        {
            int opt = Integer.parseInt(options);
            if((opt != Fat.FAT12) && (opt != Fat.FAT16) && (opt != Fat.FAT32))
                throw new IllegalArgumentException("bad FAT formatOptions");
            
            return new Integer(opt);            
        }
        else
		{		
            return options;
		}		
	}
	
	/**
	 * 
	 */
	public String toString()
	{
		return name;
	}
    
    /**
     * 
     * @return
     */
    public Class getFsTypeClass()
    {
    	return fsTypeClass;
    }

    /**
     * 
     * @return
     */
    public Class getFsClass()
    {
    	return fsClass;
    }

    private String name;
    private Class fsClass;
    private Class fsTypeClass;        
}
