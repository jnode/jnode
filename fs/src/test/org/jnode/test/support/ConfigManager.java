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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.PropertyConfigurator;
import org.jnode.test.fs.AllFSTest;
import org.jnode.test.fs.unit.config.OsType;
import org.jnode.test.fs.unit.config.FSTestConfig;


public class ConfigManager
{   
    static private ConfigManager instance; 
    static private boolean log4jInitialized = false; 
    
    private List configs; 
    private Map iterators; 
    
    static public ConfigManager getInstance()
    {
        if(instance == null)
        {
            instance = new ConfigManager();
        }
        
        return instance;
    }
    
    public void addConfig(TestConfig config)
    {
        configs.add(config);
    }
    
    public TestConfig getConfig(Class clazz, String testName)
    {
        TestKey key = new TestKey(clazz, testName);
        Iterator it = (Iterator) iterators.get(key);
        if(it == null)
        {
            it = configs.iterator();
            iterators.put(key, it);
        }
        
        TestConfig cfg = (TestConfig) it.next();
        System.out.print(": "+cfg);        
        return cfg;
    }
    
    public static void initLog4j()
    {
        if(!log4jInitialized && OsType.OTHER_OS.isCurrentOS())
        {
            // configure Log4j only is outside of JNode
            // (because JNode has its own config for Log4j)
            //String configLog4j = "/"+getClass().getName().replace('.', '/')+"/log4jForTests.properties";
            String configLog4j = "log4jForTests.properties";
            URL url = ConfigManager.class.getResource(configLog4j);
            if(url == null)
            {
                System.err.println("can't find resource "+configLog4j);
            }
            else
            {
                PropertyConfigurator.configure(url);
            }
            
            log4jInitialized = true;            
        }        
    }
    
    private ConfigManager()
    {        
        configs = new ArrayList(); 
        iterators = new HashMap();
        initLog4j();
    }        
    
    static private class TestKey
    {
        private Class clazz;
        private String testName;
        
        public TestKey(Class clazz, String testName)
        {
            this.clazz = clazz;
            this.testName = testName;            
        }

        public boolean equals(Object o)
        {
            if((o == null) || !(o instanceof TestKey))
            {
                return false;
            }
            
            TestKey tk = (TestKey) o;
            return (tk.clazz == this.clazz) && tk.testName.equals(this.testName); 
        }
        
        public int hashCode()
        {
            return testName.hashCode();
        }
    }
}
