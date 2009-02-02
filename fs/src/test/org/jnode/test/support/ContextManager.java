/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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

import java.net.URL;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jmock.MockObjectTestCase;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.block.floppy.support.FloppyDeviceFactory;
import org.jnode.driver.bus.ide.IDEDeviceFactory;
import org.jnode.naming.InitialNaming;
import org.jnode.naming.NameSpace;
import org.jnode.plugin.PluginException;
import org.jnode.test.fs.driver.factories.MockFloppyDeviceFactory;
import org.jnode.test.fs.driver.factories.MockIDEDeviceFactory;
import org.jnode.test.fs.driver.stubs.StubDeviceManager;
import org.jnode.test.fs.driver.stubs.StubNameSpace;
import org.jnode.test.fs.filesystem.config.OsType;


public class ContextManager {
    private static final Logger log = Logger.getLogger(ContextManager.class);
    private static ContextManager instance;

    private boolean initialized = false;
    private Context context;

    public static ContextManager getInstance() {
        if (instance == null) {
            instance = new ContextManager();
        }

        return instance;
    }

    public void init() {
        if (!initialized) {
            try {
                initLog4j();
                initNaming();
            } catch (PluginException e) {
                log.fatal("error in initNaming", e);
            }

            initialized = true;
        }
    }

    protected void initLog4j() {
        if (OsType.OTHER_OS.isCurrentOS()) {
            // configure Log4j only if outside of JNode
            // (because JNode has its own config for Log4j)

            // name must be of max 8 characters !!!
            // but extension can be larger that 3 characters !!!!!
            // (probably only under windows)
            String configLog4j = "log4jCfg.properties";

            URL url = ContextManager.class.getResource(configLog4j);
            if (url == null) {
                System.err.println("can't find resource " + configLog4j);
            } else {
                PropertyConfigurator.configure(url);
            }
        }
    }

    protected void initNaming() throws PluginException {
        if (OsType.OTHER_OS.isCurrentOS()) {
            NameSpace namespace = new StubNameSpace();
            InitialNaming.setNameSpace(namespace);
            populateNameSpace(namespace);

            //StubDeviceManager.INSTANCE.start();
        }
    }

    protected void populateNameSpace(NameSpace namespace) {
        try {
            namespace.bind(FloppyDeviceFactory.NAME, new MockFloppyDeviceFactory());
            namespace.bind(IDEDeviceFactory.NAME, new MockIDEDeviceFactory());
            namespace.bind(DeviceManager.NAME, StubDeviceManager.INSTANCE);

//            CMOSService cmos = new CMOSService()
//            {
//                public int getRegister(int regnr)
//                {
//                    switch(regnr)
//                    {
//                    case CMOSConstants.CMOS_FLOPPY_DRIVES: return 0x11;
//                    default: return 0;
//                    }
//                }
//                
//            };
//            namespace.bind(CMOSService.NAME, cmos);                        
        } catch (NameAlreadyBoundException e) {
            log.fatal("can't register stub services", e);
        } catch (NamingException e) {
            log.fatal("can't register stub services", e);
        }
    }

    private ContextManager() {
        initLog4j();
    }

    public Context getContext() {
        log.debug("getContext: " + context);
        return context;
    }

    public void setContext(Class contextClass, TestConfig config, MockObjectTestCase testCase) throws Exception {
        // first remove previous context
        clearContext();

        // create a new context from the test config
        context = (Context) contextClass.newInstance();
        context.init(config, testCase);
        log.debug("setContext: " + context);
    }

    public void clearContext() throws Exception {
        if (context != null) {
            context.destroy();
            context = null;
            log.debug("clearContext");
        }
    }
}
