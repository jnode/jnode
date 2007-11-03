/*
 * $Id: SystemHelpPlugin.java 2224 2006-01-01 12:49:03Z epr $
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

package org.jnode.shell.bjorne;

import javax.naming.NamingException;

import org.jnode.naming.InitialNaming;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.shell.CommandInterpreter;
import org.jnode.shell.ShellManager;

/**
 * This plugin installs Bjorne shell functionality for use by the CommandShell.
 * 
 * @author crawley@jnode.org
 */
public class BjorneShellPlugin extends Plugin {

    private static final CommandInterpreter.Factory FACTORY = new CommandInterpreter.Factory() {
        public CommandInterpreter create() {
            return new BjorneInterpreter();
        }

        public String getName() {
            return "bjorne";
        }
    };

    /**
     * Initialize a new instance
     * 
     * @param descriptor
     */
    public BjorneShellPlugin(PluginDescriptor descriptor) {
        super(descriptor);
    }

    /**
     * Start this plugin
     */
    protected void startPlugin() throws PluginException {
        try {
            ShellManager mgr = (ShellManager) InitialNaming
                    .lookup(ShellManager.NAME);
            mgr.registerInterpreterFactory(FACTORY);
        } catch (NamingException ex) {
            throw new PluginException("Cannot find the shell manager", ex);
        }
    }

    /**
     * Stop this plugin
     */
    protected void stopPlugin() throws PluginException {
        try {
            ShellManager mgr = (ShellManager) InitialNaming
                    .lookup(ShellManager.NAME);
            mgr.unregisterInterpreterFactory(FACTORY);
        } catch (NamingException ex) {
            throw new PluginException("Cannot find the shell manager", ex);
        }
    }
}
