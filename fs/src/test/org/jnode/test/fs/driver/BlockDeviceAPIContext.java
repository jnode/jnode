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

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jmock.MockObjectTestCase;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.DeviceException;
import org.jnode.driver.DeviceFinder;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.block.ramdisk.RamDiskDevice;
import org.jnode.test.fs.driver.context.RamDiskDriverContext;
import org.jnode.test.fs.driver.stubs.StubDeviceManager;
import org.jnode.test.support.Context;
import org.jnode.test.support.TestConfig;
import org.jnode.test.support.TestUtils;

abstract public class BlockDeviceAPIContext extends Context
{
    final public static String DEVICE_SIZE_STR = "1M"; // may use multipliers (K, M, G)
    final public static long DEVICE_SIZE = TestUtils.getSize(DEVICE_SIZE_STR); 
    
    final public static String SLOW_DEVICE_SIZE_STR = "1K"; // may use multipliers (K, M, G)
    final public static long SLOW_DEVICE_SIZE = TestUtils.getSize(SLOW_DEVICE_SIZE_STR);
    
    protected static final Logger log = Logger.getLogger(BlockDeviceAPIContext.class);                   
    
    private BlockDeviceAPI api;
    private BlockDeviceAPIContext parentContext;

    public BlockDeviceAPIContext()
    {        
    }
        
    protected void init(BlockDeviceAPIContext parentContext, BlockDeviceAPI api, Device device)
    {
        this.api = api;
        this.parentContext = parentContext;

        log.info("api="+api+ " device="+device);
        if((device != null) && (api instanceof Driver))
        {
            Driver driver = (Driver) api;
            try
            {                    
                boolean registered = false;
                
                try
                {
                    StubDeviceManager.INSTANCE.getDevice(device.getId());
                    registered = true;
                }
                catch (DeviceNotFoundException e)
                {
                    registered = false;
                }
                
                device.setDriver(driver);
                
                if(!registered)
                {
                    StubDeviceManager.INSTANCE.register(device);
                }                    
            }
            catch (DriverException e)
            {
                log.error("Error while starting device "+driver.getDevice(), e);
            }
            catch (DeviceAlreadyRegisteredException e)
            {
                log.error("Error while starting device "+driver.getDevice(), e);
            }                
        }                        
        
        log.info(api.getClass().getName()+" initialized");
    }

    public void destroy()
    {
        try
        {
            if(api != null)
            {
                api.flush();
            }
        }
        catch (IOException e)
        {
            log.error("can't flush "+api.getClass().getName(), e);
        }
        
        if(api instanceof Driver)
        {
            Driver driver = (Driver) api;
            Device device = driver.getDevice();
            
            if(device != null)
            {
                try
                {
                    StubDeviceManager.INSTANCE.stop(device);
                    StubDeviceManager.INSTANCE.unregister(device);                        
                }
                catch (DriverException e)
                {
                    log.error("Error while stopping device "+driver.getDevice(), e);
                }
                catch (DeviceNotFoundException e)
                {
                    log.error("Error while stopping device "+driver.getDevice(), e);
                }
            }
        }
        
        if(parentContext != null)
        {
            parentContext.destroy();
        }
        
        unregisterDevices();
        
        try
        {
            destroyImpl();
        }
        catch (Exception e)
        {
            log.error("Error while freeing BlockDeviceAPIContext", e);
        }
    }
    
    final public BlockDeviceAPI getApi()
    {
        return api;
    }            
    
    protected Driver findDriver(DeviceFinder finder, String devName)
    {
        try
        {
            finder.findDevices(StubDeviceManager.INSTANCE, StubDeviceManager.INSTANCE.getSystemBus());
        }
        catch (DeviceAlreadyRegisteredException e)
        {
            log.warn(e);
        }
        catch (DeviceException e)
        {
            log.error(e);
        }
        
        try
        {
            Device dev = StubDeviceManager.INSTANCE.getDevice(devName);
            log.debug("dev="+dev);
            log.debug("driver="+(dev==null ? "null" : ""+dev.getDriver()));
            return dev.getDriver();
        }
        catch (DeviceNotFoundException e)
        {
            log.fatal("can't find "+devName, e);
            return null;
        }
    }
        
    protected void destroyImpl() throws Exception
    {        
    }
    
    private void unregisterDevices()
    {
        //Collection devs = StubDeviceManager.INSTANCE.getDevicesByAPI(RemovableDeviceAPI.class);
        Collection devs = StubDeviceManager.INSTANCE.getDevices();
        for(Iterator it = devs.iterator() ; it.hasNext() ; )
        {
            Device device = (Device) it.next();
            try
            {
                StubDeviceManager.INSTANCE.unregister(device);
            }
            catch (DriverException e)
            {
                log.error("can't unregister "+device.getClass().getName(), e);
            }
        }            
    }
    
    protected BlockDeviceAPIContext createParentBlockDeviceAPI()
    {
        return new RamDiskDriverContext();
    }

    protected Device createParentDevice(long size)
    {
        //StubDevice dev = new StubDevice(size);
        //return dev;
        return new RamDiskDevice(StubDeviceManager.INSTANCE.getSystemBus() , "ParentDev", (int) DEVICE_SIZE);
    }    
}
