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

import org.apache.log4j.Logger;
import org.jmock.cglib.Mock;

public class MockUtils
{
    private static final Logger log = Logger.getLogger(MockUtils.class);    
    
    static public Object createMockObject(Class name)
    {
        return createMockObject(name, null);
    }
    
    static public Object createMockObject(Class name, MockInitializer initializer)
    {
        String shortName = getShortName(name);
        Mock mock = new Mock(name, shortName);
        log.info("created a Mock for "+shortName);
        
        if(initializer != null)
        {
            initializer.init(mock);
        }
                                
        return mock.proxy();
    }

    static public Object createMockObject(Class name, Class[] clsArgs, Object[] args)
    {
        return createMockObject(name, null, clsArgs, args);
    }
    
    static public Object createMockObject(Class name, MockInitializer initializer, Class[] clsArgs, Object[] args)
    {
        String shortName = getShortName(name);
        CGLibCoreMockExt cglibMock = new CGLibCoreMockExt(name, shortName);
        Mock mock = new Mock(cglibMock);
        log.info("created a Mock for "+shortName);
        
        if(initializer != null)
        {
            initializer.init(mock);
        }
                
        return cglibMock.createProxy(clsArgs, args);        
    }

    static public String getShortName(Class clazz)
    {
        String name = clazz.getName();
        int idx = name.lastIndexOf('.');
        return (idx >= 0) ? name.substring(idx+1) : name;
    }
        
    private MockUtils()
    {
    }
}
