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

package org.jnode.test.fs.driver.context;

import org.jmock.MockObjectTestCase;
import org.jnode.test.fs.driver.BlockDeviceAPIContext;
import org.jnode.test.support.Context;
import org.jnode.test.support.TestConfig;

public class IDEDiskPartitionDriverContext extends BlockDeviceAPIContext
{
    public IDEDiskPartitionDriverContext()
    {
        super("IDEDiskPartitionDriver");
    }
        
    public void init(TestConfig config, MockObjectTestCase testCase) throws Exception
    {
        super.init(config, testCase);
        
        // TODO Auto-generated method stub
        
    }
//  TODO: create context
    
//private IDEDiskPartitionDevice createDevice(TestCase testCase)
//  {
//      Bus bus = StubDeviceManager.INSTANCE.getSystemBus();
//      IDEDiskPartitionDevice device = new IDEDiskPartitionDevice(bus, "hdTest", );
//      return device;
//  }
  
//      BlockDeviceAPIContext parentCtx = IDE_DISK_DRIVER.create(testCase);
//      IDEDiskPartitionDriver api = new IDEDiskPartitionDriver();
//      IDEDiskPartitionDevice device = MockObjectFactory.createIDEDevice(testCase);
//      return new BlockDeviceAPIContext(parent, api, device)
//      {
//          protected void freeImpl()
//          {
//              
//          }                    
//      };
}
