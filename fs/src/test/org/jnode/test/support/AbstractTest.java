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

import junit.framework.TestCase;

import org.apache.log4j.Logger;

/**
 * 
 * @author Fabien DUMINY
 */
public abstract class AbstractTest extends TestCase {
	public AbstractTest(Class configClazz)
    {
        super();
        ConfigManager.initLog4j();
        this.configClazz = configClazz;
    }

    /**
	 * 
	 */
	protected AbstractTest(Class configClazz, String name)
	{
        super(name);
        ConfigManager.initLog4j();
        this.configClazz = configClazz;
	}
	
	/**
	 * 
	 */
	public void setUp() throws Exception
	{
        System.gc();
		log.info("BEGIN "+getTestName()); // marker for log4j traces
        
        TestConfig tc = getTestConfig();
        if(tc == null)
        {
            log.warn("NO CONFIGURATION");
        }
        else
        {
            tc.setUp();
        }
        System.gc();
	}
	
	/**
	 * 
	 */
	public void tearDown() throws Exception
	{
        System.gc();
        
        if(testConfig != null)
        {
            testConfig.tearDown();
        }
        
        log.info("END "+getTestName()); // marker for log4j traces
        System.gc();        
	}

    final protected TestConfig getTestConfig()
	{
        if(testConfig == null)
        {
            testConfig = ConfigManager.getInstance().getConfig(configClazz, getClass(), getName());
        }
        
		return testConfig;
	}
    
    final public void runTest() throws Throwable
    {
        try
        {
            super.runTest();
        }
        catch(Throwable t)
        {
            // trace the config and the tests that fail
            // to know the context of the error
            log.error("FAILED  \n"+
                      "config:"+testConfig, t);
            throw t;
        }
    }
    
    final protected String getTestName()
    {
        return getClass().getName()+"."+getName(); // className.methodName
    }
    
    private TestConfig testConfig;
    private Class configClazz;
	    
	protected final Logger log = Logger.getLogger(getTestName());	
}
