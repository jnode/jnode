/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.test.support;

import java.util.List;

import junit.framework.TestSuite;

import org.apache.log4j.Logger;

abstract public class AbstractTestSuite extends TestSuite
{
    public AbstractTestSuite()
    {
        ContextManager.getInstance().init();     
        init();
    }
    
    /**
     * Add a TestSuite containing TestSuites 
     * (for each couple (config, TestSuite class))
     */
    final public void init()
    {
        List<TestConfig> configs = getConfigs();
        Class[] testSuites = getTestSuites();
        log.info(configs.size()+" configs, "+
                 testSuites.length+" TestSuites"); 
        
        ConfigManager cfgManager = ConfigManager.getInstance();
        
        for(TestConfig cfg : configs)
        {
            cfgManager.addConfig(cfg);
            
            for(Class cls : testSuites)
            {
                addTestSuite(cls);
            }
        }
    }
    
    /**
     * 
     * @return a list of TestConfig(s)
     */
    abstract public List<TestConfig> getConfigs();
    
    /**
     * 
     * @return an array of TestSuite classes
     */
    abstract public Class[] getTestSuites();
    
    protected final Logger log = Logger.getLogger(getClass().getName());       
}
