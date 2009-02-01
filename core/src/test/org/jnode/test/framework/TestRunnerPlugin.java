/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
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
 
package org.jnode.test.framework;

import org.apache.log4j.Logger;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.system.BootLog;

/**
 * @author Fabien DUMINY (fduminy at jnode.org)
 */
public class TestRunnerPlugin extends Plugin {

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(TestRunnerPlugin.class);

    private final TestManager manager;

    /**
     * Create a new instance
     */
    public TestRunnerPlugin(PluginDescriptor descriptor) {
        super(descriptor);
        manager = new TestManager(descriptor.getExtensionPoint("tests"));
        BootLog.debug("TestRunnerPlugin created : classloader=" + descriptor.getPluginClassLoader());
    }

    /**
     * Start this plugin
     */
    protected void startPlugin() throws PluginException {
        // nothing to do
    }

    /**
     * Stop this plugin
     */
    protected void stopPlugin() {
        // nothing to do
    }
}
