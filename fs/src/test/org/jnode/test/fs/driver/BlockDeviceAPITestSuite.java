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
        BlockDeviceAPITestConfig cfg;
        
        addConfig(configs, RamDiskDriverContext.class);     
        addConfig(configs, ByteArrayDeviceContext.class);
               
        addConfig(configs, FloppyDriverContext.class);
        addConfig(configs, FileDeviceContext.class);        

        // with no partition
        cfg = addConfig(configs, IDEDiskDriverContext.class);
        
        // with one partition
        cfg = addConfig(configs, IDEDiskDriverContext.class);
        cfg.addPartition(new Partition(false, 0, cfg.getDeviceNbSectors()));
        
        // with two partitions
        cfg = addConfig(configs, IDEDiskDriverContext.class);
        int nbSectors1 = cfg.getDeviceNbSectors() / 2;
        int nbSectors2 = cfg.getDeviceNbSectors() - nbSectors1;
        cfg.addPartition(new Partition(false, 0, nbSectors1));
        cfg.addPartition(new Partition(false, nbSectors1, nbSectors2));

        // These tests are disabled because they will succeed but they should not !!!
        // (probably not tested, so it give no error for this bad config) :
//        // with two overlapping partitions
//        cfg = addConfig(configs, IDEDiskDriverContext.class);
//        int overlapSectors = 10;
//        nbSectors1 = cfg.getDeviceNbSectors() / 2;
//        nbSectors2 = cfg.getDeviceNbSectors() - nbSectors1;
//        cfg.addPartition(new Partition(false, 0, nbSectors1));
//        cfg.addPartition(new Partition(false, nbSectors1-overlapSectors, nbSectors2));
                
//        addConfig(configs, IDEDiskPartitionDriverContext.class);//TODO: develop stubs ?
//        
//        addConfig(configs, SCSICDROMDriverContext.class); //TODO: develop stubs ?
//        addConfig(configs, BlockAlignmentSupportContext.class);  //TODO: develop stubs ?
//        addConfig(configs, MappedBlockDeviceSupportContext.class);//TODO: develop stubs ? 
        
        return configs;
    }
    
    protected BlockDeviceAPITestConfig addConfig(List configs, Class contextClass)
    {
        BlockDeviceAPITestConfig cfg = new BlockDeviceAPITestConfig(contextClass);
        configs.add(cfg);            
        return cfg;
    }
    
    public Class[] getTestSuites()
    {
        return new Class[]
                  {
                        BlockDeviceAPITest.class, 
                  };
    }
}
