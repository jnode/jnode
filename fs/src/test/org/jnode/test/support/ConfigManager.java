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

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.PluginException;
import org.jnode.test.fs.driver.stubs.StubDeviceManager;
import org.jnode.test.fs.driver.stubs.StubNameSpace;
import org.jnode.test.fs.filesystem.config.OsType;


public class ConfigManager
{   
    private static final Logger log = Logger.getLogger(ConfigManager.class);
    
    static private ConfigManager instance; 
    static private boolean log4jInitialized = false; 
    
    private Map configs; 
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
        List cfgs = (List) configs.get(config.getClass());
        if(cfgs == null)
        {
            cfgs = new ArrayList();
            configs.put(config.getClass(), cfgs);
        }
        
        cfgs.add(config);        
    }
    
    public TestConfig getConfig(Class configClazz, Class clazz, String testName)
    {
        TestKey key = new TestKey(clazz, testName);
        Iterator it = (Iterator) iterators.get(key);
        if(it == null)
        {
            List cfgs = (List) configs.get(configClazz);
            it = cfgs.iterator();
            iterators.put(key, it);
        }
        
        TestConfig cfg = (TestConfig) it.next();
        log.info(key+" got config "+cfg);
        return cfg;
    }
    
    private ConfigManager()
    {        
        configs = new HashMap(); 
        iterators = new HashMap();
        ContextManager.getInstance().init();
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
        
        public String toString()
        {
            return clazz.getName()+"."+testName; 
        }
    }
}
