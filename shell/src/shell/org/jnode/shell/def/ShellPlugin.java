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
 
package org.jnode.shell.def;

import javax.naming.NamingException;

import org.jnode.naming.InitialNaming;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.shell.ShellManager;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.alias.def.DefaultAliasManager;
import org.jnode.shell.syntax.DefaultSyntaxManager;
import org.jnode.shell.syntax.SyntaxManager;

/**
 * Service used to create and bind the system alias manager.
 * @author epr
 */
public class ShellPlugin extends Plugin {

    /**
     * Initialize a new instance
     * @param descriptor
     */
    public ShellPlugin(PluginDescriptor descriptor) {
        super(descriptor);
    }

    /**
     * Start this plugin
     */
    protected void startPlugin() throws PluginException {
        try {
            final ShellManager shellMgr = new DefaultShellManager();
            final AliasManager aliasMgr = 
                new DefaultAliasManager(getDescriptor().getExtensionPoint("aliases"));
            final SyntaxManager syntaxMgr = 
                new DefaultSyntaxManager(getDescriptor().getExtensionPoint("syntaxes"));
            InitialNaming.bind(AliasManager.NAME, aliasMgr);
            InitialNaming.bind(ShellManager.NAME, shellMgr);
            InitialNaming.bind(SyntaxManager.NAME, syntaxMgr);
        } catch (NamingException ex) {
            throw new PluginException("Cannot bind shell component", ex);
        }
    }

    /**
     * Stop this plugin
     */
    protected void stopPlugin() throws PluginException {
        InitialNaming.unbind(ShellManager.NAME);
        InitialNaming.unbind(AliasManager.NAME);
        InitialNaming.unbind(SyntaxManager.NAME);
    }
}
