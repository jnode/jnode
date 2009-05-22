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
 
package org.jnode.command.system;

import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.naming.NameNotFoundException;

import org.jnode.naming.InitialNaming;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.plugin.PluginManager;
import org.jnode.plugin.PluginReference;
import org.jnode.plugin.PluginRegistry;
import org.jnode.plugin.URLPluginLoader;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.PluginArgument;
import org.jnode.shell.syntax.StringArgument;
import org.jnode.shell.syntax.SyntaxMultiplicityException;
import org.jnode.shell.syntax.URLArgument;
import org.jnode.vm.Vm;

/**
 * @author epr
 */
public class PluginCommand extends AbstractCommand {

    private static final String help_load = "Load a plugin";
    private static final String help_reload = "Reload a plugin";
    private static final String help_unload = "Unload a plugin";
    private static final String help_url = "Loader location";
    private static final String help_plugin = "Plugin identifier";
    private static final String help_version = "Plugin version";
    private static final String help_super = "List and manage plugins and plugin loaders";
    private static final String fmt_add_loader = "Added plugin loader for %s%n";
    private static final String fmt_load = "Loaded plugin %s version %s%n";
    private static final String fmt_reload = "Reloaded plugin %s version %s%n";
    private static final String fmt_unload = "Unloaded plugin %s%n";
    @SuppressWarnings("unused")
    private static final String str_state = "state";
    private static final String str_active = "active";
    private static final String str_inactive = "inactive";
    @SuppressWarnings("unused")
    private static final String str_version = "version";
    private static final String fmt_list = "%s; state %s; version %s";
    private static final String fmt_no_plugin = "Plugin %s not found%n";
    
    private final FlagArgument argLoad;
    private final FlagArgument argReload;
    private final FlagArgument argUnload;
    private final URLArgument argLoaderUrl;
    private final PluginArgument argPluginID;
    private final StringArgument argVersion;

    private PrintWriter out;
    private PluginManager mgr;
    

    public PluginCommand() {
        super(help_super);
        argLoad      = new FlagArgument("load", Argument.OPTIONAL, help_load);
        argReload    = new FlagArgument("reload", Argument.OPTIONAL, help_reload);
        argUnload    = new FlagArgument("unload", Argument.OPTIONAL, help_unload);
        argLoaderUrl = new URLArgument("loader", Argument.OPTIONAL, help_url);
        argPluginID  = new PluginArgument("plugin", Argument.OPTIONAL, help_plugin);
        argVersion   = new StringArgument("version", Argument.OPTIONAL, help_version);
        registerArguments(argPluginID, argLoad, argReload, argUnload,
                argLoaderUrl, argVersion);
    }

    public static void main(final String[] args) throws Exception {
        new PluginCommand().execute(args);
    }

    /**
     * Execute this command
     */
    public void execute() throws Exception {
        this.out = getOutput().getPrintWriter();
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                public Object run() throws Exception {
                    doRun();
                    return null;
                }
            });
        } catch (PrivilegedActionException ex) {
            throw ex.getException();
        }
    }

    private void doRun() 
        throws NameNotFoundException, SyntaxMultiplicityException, PluginException, MalformedURLException {
        mgr = InitialNaming.lookup(PluginManager.NAME);
        final String version = argVersion.isSet() ? argVersion.getValue() : Vm.getVm().getVersion();
        final String pluginId = argPluginID.getValue();
        if (argLoaderUrl.isSet()) {
            addPluginLoader(argLoaderUrl.getValue());
        } else if (argLoad.isSet()) {
            loadPlugin(pluginId, version);
        } else if (argReload.isSet()) {
            reloadPlugin(pluginId, version);
        } else if (argUnload.isSet()) {
            unloadPlugin(pluginId);
        } else if (pluginId != null) {
            listPlugin(pluginId);
        } else {
            listPlugins();
        }
    }

    private void addPluginLoader(URL url) throws PluginException, MalformedURLException {
        final String ext = url.toExternalForm();
        if (!ext.endsWith("/")) {
            url = new URL(ext + "/");
        }
        mgr.getLoaderManager().addPluginLoader(new URLPluginLoader(url));
        out.format(fmt_add_loader, url);
    }

    private void loadPlugin(String id, String version) throws PluginException {
        mgr.getRegistry().loadPlugin(mgr.getLoaderManager(), id, version);
        out.format(fmt_load, id, version);
    }
    
    private void reloadPlugin(String id, String version) throws PluginException {
        final PluginRegistry reg = mgr.getRegistry();
        final List<PluginReference> refs = reg.unloadPlugin(id);
        for (PluginReference ref : refs) {
            if (reg.getPluginDescriptor(ref.getId()) == null) {
                reg.loadPlugin(mgr.getLoaderManager(), ref.getId(), ref.getVersion());
            }
        }
        if (reg.getPluginDescriptor(id) == null) {
            reg.loadPlugin(mgr.getLoaderManager(), id, version);
        }
        out.format(fmt_reload, id, version);
    }

    private void unloadPlugin(String id) throws PluginException {
        mgr.getRegistry().unloadPlugin(id);
        out.format(fmt_unload, id);
    }
    
    private void listPlugins() throws PluginException {
        final ArrayList<String> rows = new ArrayList<String>();
        for (PluginDescriptor descr : mgr.getRegistry()) {
            String row = String.format(fmt_list,
                descr.getId(),
                descr.getPlugin().isActive() ? str_active : str_inactive,
                descr.getPlugin().getDescriptor().getVersion()
            );
            rows.add(row);
        }
        Collections.sort(rows);
        for (String row : rows) {
            out.println(row);
        }
    }

    private void listPlugin(String id) throws PluginException {
        final PluginDescriptor descr = mgr.getRegistry().getPluginDescriptor(id);
        if (descr != null) {
            out.format(fmt_list,
                descr.getId(),
                descr.getPlugin().isActive() ? str_active : str_inactive,
                descr.getPlugin().getDescriptor().getVersion()
            );
            out.println();
        } else {
            out.format(fmt_no_plugin, id);
        }
    }
}
