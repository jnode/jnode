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

import java.util.List;

import junit.framework.TestSuite;

import org.apache.log4j.Logger;

abstract public class AbstractTestSuite extends TestSuite
{
    public AbstractTestSuite()
    {
        ConfigManager.initLog4j();        
        init();
    }
    
    /**
     * Add a TestSuite containing TestSuites 
     * (for each couple (config, TestSuite class))
     */
    final public void init()
    {
        try
        {
        List configs = getConfigs();
        Class[] testSuites = getTestSuites();
        log.info(configs.size()+" configs, "+
                 testSuites.length+" TestSuites"); 
        
        int nbConfigs = configs.size();
        TestConfig cfg;
        ConfigManager cfgManager = ConfigManager.getInstance();
        
        for(int i = 0 ; i < nbConfigs ; i++)
        {
            cfg = (TestConfig) configs.get(i);
            cfgManager.addConfig(cfg);
            
            for(int j = 0 ; j < testSuites.length ; j++)
            {
                addTestSuite(testSuites[j]);
            }
        }
    }
    catch(OutOfMemoryError oome)
    {
        oome.printStackTrace();
        System.err.println("freeMemory:"+Runtime.getRuntime().freeMemory());
        System.err.println("maxMemory:"+Runtime.getRuntime().maxMemory());
        System.err.println("totalMemory:"+Runtime.getRuntime().totalMemory());
    }        
    }
    
    /**
     * 
     * @return a list of TestConfig(s)
     */
    abstract public List getConfigs();
    
    /**
     * 
     * @return an array of TestSuite classes
     */
    abstract public Class[] getTestSuites();
    
    protected final Logger log = Logger.getLogger(getClass().getName());       
}
