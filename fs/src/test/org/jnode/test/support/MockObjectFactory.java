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

package org.jnode.test.support;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;

import junit.framework.TestCase;

import org.jmock.cglib.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.stub.ReturnStub;
import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.ide.DefaultIDEControllerDriver;
import org.jnode.driver.ide.IDEBus;
import org.jnode.driver.ide.IDEConstants;
import org.jnode.driver.ide.IDEDevice;
import org.jnode.driver.ide.IDEDriveDescriptor;
import org.jnode.driver.ide.IDEDriverUtils;
import org.jnode.naming.InitialNaming;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;

public class MockObjectFactory
{
    public static IDEDevice createIDEDevice(Device parentDev, TestCase testCase,
                        final boolean supp48bitsAddr, final long deviceSize) 
                throws IllegalArgumentException, DriverException, ResourceNotFreeException
    {
        if((deviceSize % IDEConstants.SECTOR_SIZE) != 0)
        {
            throw new IllegalArgumentException("deviceSize("+deviceSize+
                    ") must be a multiple of SECTOR_SIZE("+IDEConstants.SECTOR_SIZE+")");
        }
        
        boolean master = true;
        boolean primary = true;
        IDEBus ideBus;
        try {
            ideBus = IDEDriverUtils.getIDEDeviceFactory().createIDEBus(parentDev, primary);
        } catch (NamingException ex) {
            throw new DriverException(ex);
        }
        
        // must have length 256 (see IDEDriveDescriptor)
        int[] data = new int[256];            
        Boolean atapi = Boolean.valueOf(true);
        
        final MockObjectTestCase mockTestCase = (MockObjectTestCase) testCase;
        MockInitializer initializer = new MockInitializer()
        {

            public void init(Mock mockDesc)
            {
                Boolean bSupp48bitsAddr = Boolean.valueOf(supp48bitsAddr); 
                mockDesc.expects(mockTestCase.atLeastOnce()).
                        method("supports48bitAddressing").
                        withNoArguments().will(new ReturnStub(bSupp48bitsAddr));
                
                long nbSectors =  deviceSize / IDEConstants.SECTOR_SIZE;
                Long lNbSectors = new Long(nbSectors);
                String methodName = supp48bitsAddr ? 
                            "getSectorsIn48bitAddressing" :
                            "getSectorsIn28bitAddressing";                
                mockDesc.expects(mockTestCase.atLeastOnce()).
                    method(methodName).
                    withNoArguments().will(new ReturnStub(lNbSectors));
                
            }                    
        };
        
        Class[] clsArgs = {int[].class, boolean.class};
        Object[] args = {data, atapi};
        IDEDriveDescriptor desc = (IDEDriveDescriptor) 
                MockUtils.createMockObject(IDEDriveDescriptor.class, 
                                           initializer, clsArgs, args);
        
        DefaultIDEControllerDriver ctrlDriver = new DefaultIDEControllerDriver();
        IDEDevice device = new IDEDevice(ideBus, primary, master, "hdTest", desc, ctrlDriver);
        
        return device;
    }
    
    public static void createResourceManager(TestCase testCase) throws NameAlreadyBoundException, NamingException
    {
        final MockObjectTestCase mockTestCase = (MockObjectTestCase) testCase;
        MockInitializer initializer = new MockInitializer()
        {

            public void init(Mock mockResMgr)
            {
                mockResMgr.expects(mockTestCase.atLeastOnce()).method("claimIOResource").withAnyArguments();
                mockResMgr.expects(mockTestCase.atLeastOnce()).method("claimIRQ").withAnyArguments();
            }                    
        };
        ResourceManager resMgr = (ResourceManager) MockUtils.createMockObject(ResourceManager.class, initializer);
        InitialNaming.bind(ResourceManager.NAME, resMgr);            
    }                
}
