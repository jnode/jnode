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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;


public class ConfigManager {
    private static final Logger log = Logger.getLogger(ConfigManager.class);

    static private ConfigManager instance;
    static private boolean log4jInitialized = false;

    private Map<Class, List<TestConfig>> configs;
    private Map<TestKey, Iterator<TestConfig>> iterators;

    static public ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }

        return instance;
    }

    public void addConfig(TestConfig config) {
        List<TestConfig> cfgs = configs.get(config.getClass());
        if (cfgs == null) {
            cfgs = new ArrayList<TestConfig>();
            configs.put(config.getClass(), cfgs);
        }

        cfgs.add(config);
    }

    public TestConfig getConfig(Class configClazz, Class clazz, String testName) {
        TestKey key = new TestKey(clazz, testName);

        synchronized (iterators) {
            Iterator<TestConfig> it = iterators.get(key);
            if (it == null) {
                List<TestConfig> cfgs = configs.get(configClazz);
                it = cfgs.iterator();
                iterators.put(key, it);
            }
            TestConfig cfg = it.next();
            log.info(key + " got config " + cfg);
            return cfg;
        }
    }

    private ConfigManager() {
        configs = new HashMap<Class, List<TestConfig>>();
        iterators = new HashMap<TestKey, Iterator<TestConfig>>();
        ContextManager.getInstance().init();
    }

    static private class TestKey {
        private Class clazz;
        private String testName;

        public TestKey(Class clazz, String testName) {
            this.clazz = clazz;
            this.testName = testName;
        }

        public boolean equals(Object o) {
            if ((o == null) || !(o instanceof TestKey)) {
                return false;
            }

            TestKey tk = (TestKey) o;
            return (tk.clazz == this.clazz) && tk.testName.equals(this.testName);
        }

        public int hashCode() {
            return testName.hashCode();
        }

        public String toString() {
            return clazz.getName() + "." + testName;
        }
    }
}
