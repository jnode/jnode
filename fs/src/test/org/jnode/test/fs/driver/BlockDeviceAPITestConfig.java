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


import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.test.fs.filesystem.FSContext;
import org.jnode.test.support.ContextManager;
import org.jnode.test.support.TestConfig;

/**
 * 
 * @author Fabien DUMINY
 */
public class BlockDeviceAPITestConfig implements TestConfig {
    private Class contextClass;
    
    public BlockDeviceAPITestConfig(Class contextClass)
    {
        this.contextClass = contextClass;
    }
    
    /**
     * 
     * @return
     */
    final public BlockDeviceAPI getBlockDeviceAPI() 
    {
        return ((BlockDeviceAPIContext) ContextManager.getInstance().getContext()).getApi();
    }
    
    final public Class getContextClass()
    {
        return contextClass;
    }
    
	/**
	 * 
	 */
	public String toString()
	{
        if(ContextManager.getInstance().getContext() == null)
        {
            return getContextClass().getName()+"[NO_CONTEXT]";
        }
        
        BlockDeviceAPI api = getBlockDeviceAPI();         
		return (api == null) ? 
               getContextClass().getName()+"[NO_API]" :
               api.getClass().getName();
	}
}
