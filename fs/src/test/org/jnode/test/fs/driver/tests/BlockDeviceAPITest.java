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

package org.jnode.test.fs.driver.tests;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.test.fs.driver.BlockDeviceAPITestConfig;
import org.jnode.test.support.AbstractTest;

public class BlockDeviceAPITest extends AbstractTest
{
    public BlockDeviceAPITest()
    {
        super(BlockDeviceAPITestConfig.class);
    }

    public BlockDeviceAPITest(String name)
    {
        super(BlockDeviceAPITestConfig.class, name);
    }
        
    public void setUp() throws Exception
    {
        super.setUp();
        
        //put specific setUp here        
    }
    
    /**
     * 
     */
    public void tearDown() throws Exception
    {
        //put specific tearDown here
        
        super.tearDown();
    }    
    
    public BlockDeviceAPI getBlockDeviceAPI()
    {
        return ((BlockDeviceAPITestConfig) getTestConfig()).getBlockDeviceAPI();
    }
    
    public void testFlush() throws IOException
    {
        getBlockDeviceAPI().flush();
    }
    
    public void testGetLength() throws IOException
    {
        long length = getBlockDeviceAPI().getLength();
        assertTrue("length must be > 0 (actual:"+length+")", length>0);
    }
    
    public void testRead() throws IOException
    {
        ByteBuffer bb = ByteBuffer.allocate(512);
        
        int offset = 0;
        int toRead;
        BlockDeviceAPI api = getBlockDeviceAPI();
        long apiLength = api.getLength();
        
        log.debug("apiLength="+apiLength);
        
        while(offset < apiLength)
        {            
            toRead = Math.min(bb.remaining(), (int) (apiLength-offset));
            log.debug("reading "+toRead+" bytes at offset "+offset);
            
            api.read(offset, bb.array(), 0, toRead);
            bb.clear();
            
            offset += toRead;
        }
    }
    
    public void testWrite() throws IOException
    {
        ByteBuffer bb = ByteBuffer.allocate(512);
        
        int offset = 0;
        int toWrite;
        BlockDeviceAPI api = getBlockDeviceAPI();
        
        while(offset < api.getLength())
        {            
            toWrite = Math.min(bb.remaining(), (int) (api.getLength()-offset));
            
            api.write(offset, bb.array(), 0, toWrite);
            bb.clear();
            
            offset += toWrite;
        }
    }
}
