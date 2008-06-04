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

import org.apache.log4j.Logger;
import org.jmock.cglib.MockObjectTestCase;

/**
 * @author Fabien DUMINY
 */
public abstract class AbstractTest extends /*TestCase*/ MockObjectTestCase {
    private TestConfig testConfig;
    private Class configClazz;

    protected final Logger log;

    public AbstractTest(Class configClazz) {
        super();
        init(configClazz, null);
        log = Logger.getLogger(getTestName());
    }

    /**
     *
     */
    protected AbstractTest(Class configClazz, String name) {
        super();
        init(configClazz, name);
        log = Logger.getLogger(getTestName());
    }

    protected void init(Class configClazz, String name) {
        setName(name);
        ContextManager.getInstance().init();
        this.configClazz = configClazz;
    }

    /**
     *
     */
    public void setUp() throws Exception {
        log.info("BEGIN " + getTestName()); // marker for log4j traces

        TestConfig tc = getTestConfig();
        if (tc == null) {
            log.warn("NO CONFIGURATION");
            ContextManager.getInstance().clearContext();
        } else {
            Class contextClass = tc.getContextClass();
            if (!Context.class.isAssignableFrom(contextClass))
                throw new IllegalArgumentException(
                    "contextClass(" + contextClass.getName() + ") must implements Context");

            // create a new context from the test config
            // and apply it
            ContextManager.getInstance().setContext(contextClass, tc, this);
        }
    }

    /**
     *
     */
    public void tearDown() throws Exception {
        String testName = getTestName(); // must be called before clearContext 
        ContextManager.getInstance().clearContext();

        log.info("END " + testName); // marker for log4j traces
    }

    protected final TestConfig getTestConfig() {
        if (testConfig == null) {
            testConfig = ConfigManager.getInstance().getConfig(configClazz, getClass(), getName());
        }

        return testConfig;
    }

    public final void runTest() throws Throwable {
        try {
            super.runTest();
        } catch (Throwable t) {
            // trace the config and the tests that fail
            // to know the context of the error
            log.error("FAILED  \n" +
                "config:" + testConfig, t);
            throw t;
        }
    }

//    final public String getName()
//    {
//        String name = super.getName();
//        if(testConfig != null)
//        {
//            name += "[" + testConfig.getName() + "]";
//        }
//        return name;
//    }

    public final void setName(String name) {
        if (testConfig != null) {
            name += "[" + testConfig.getName() + "]";
        }
        super.setName(name);
    }

    protected final String getTestName() {
        return getClass().getName() + "." + getName(); // className.methodName
    }
}
