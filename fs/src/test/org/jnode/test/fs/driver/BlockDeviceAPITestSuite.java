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

package org.jnode.test.fs.driver;

import java.util.ArrayList;
import java.util.List;

import org.jnode.test.fs.driver.context.BlockAlignmentSupportContext;
import org.jnode.test.fs.driver.context.ByteArrayDeviceContext;
import org.jnode.test.fs.driver.context.FileDeviceContext;
import org.jnode.test.fs.driver.context.FloppyDriverContext;
import org.jnode.test.fs.driver.context.IDEDiskDriverContext;
import org.jnode.test.fs.driver.context.IDEDiskPartitionDriverContext;
import org.jnode.test.fs.driver.context.MappedBlockDeviceSupportContext;
import org.jnode.test.fs.driver.context.RamDiskDriverContext;
import org.jnode.test.fs.driver.context.SCSICDROMDriverContext;
import org.jnode.test.fs.driver.tests.BlockDeviceAPITest;
import org.jnode.test.support.AbstractTestSuite;

public class BlockDeviceAPITestSuite extends AbstractTestSuite
{
    public List getConfigs()
    {
        List configs = new ArrayList();
        
        addConfig(configs, RamDiskDriverContext.class);     
        addConfig(configs, ByteArrayDeviceContext.class);
        addConfig(configs, FloppyDriverContext.class);
        addConfig(configs, FileDeviceContext.class);        
        addConfig(configs, IDEDiskDriverContext.class);
        
//        addConfig(configs, IDEDiskPartitionDriverContext.class);//TODO: develop stubs ?
//        
//        addConfig(configs, SCSICDROMDriverContext.class); //TODO: develop stubs ?
//        addConfig(configs, BlockAlignmentSupportContext.class);  //TODO: develop stubs ?
//        addConfig(configs, MappedBlockDeviceSupportContext.class);//TODO: develop stubs ? 
        
        return configs;
    }
    
    protected void addConfig(List configs, Class contextClass)
    {
        BlockDeviceAPITestConfig cfg = new BlockDeviceAPITestConfig(contextClass);
        configs.add(cfg);            
    }
    
    public Class[] getTestSuites()
    {
        return new Class[]
                  {
                        BlockDeviceAPITest.class, 
                  };
    }
}
