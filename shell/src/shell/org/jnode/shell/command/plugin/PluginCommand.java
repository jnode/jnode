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
 
package org.jnode.shell.command.plugin;

import java.io.InputStream;
import java.io.PrintStream;
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
import org.jnode.shell.CommandLine;
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

    private final FlagArgument FLAG_LOAD = 
        new FlagArgument("load", Argument.OPTIONAL, "Load the plugin");

    private final FlagArgument FLAG_RELOAD = 
        new FlagArgument("reload", Argument.OPTIONAL, "Reload the plugin");

    private final FlagArgument FLAG_UNLOAD = 
        new FlagArgument("unload", Argument.OPTIONAL, "Unload the plugin");

    private final URLArgument ARG_LOADER_URL = 
        new URLArgument("loader", Argument.OPTIONAL, "loader location");

    private final PluginArgument ARG_PLUGIN_ID =
        new PluginArgument("plugin", Argument.OPTIONAL, "plugin identifier");

    private final StringArgument ARG_VERSION =
        new StringArgument("version", Argument.OPTIONAL, "plugin version");

    private PrintStream out;
    private PluginManager mgr;
    

    public PluginCommand() {
        super("List and manage plugins and plugin loaders");
        registerArguments(ARG_PLUGIN_ID, FLAG_LOAD, FLAG_RELOAD, FLAG_UNLOAD,
                ARG_LOADER_URL, ARG_VERSION);
    }

    public static void main(final String[] args) throws Exception {
        new PluginCommand().execute(args);
    }

    /**
     * Execute this command
     */
    public void execute(CommandLine commandLine, InputStream in, PrintStream out,
            PrintStream err) throws Exception {
        this.out = out;
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
        mgr = (PluginManager) InitialNaming.lookup(PluginManager.NAME);
        final String version = ARG_VERSION.isSet() ? ARG_VERSION.getValue() : Vm.getVm().getVersion();
        final String pluginId = ARG_PLUGIN_ID.getValue();
        if (ARG_LOADER_URL.isSet()) {
            addPluginLoader(ARG_LOADER_URL.getValue());
        } else if (FLAG_LOAD.isSet()) {
            loadPlugin(pluginId, version);
        } else if (FLAG_RELOAD.isSet()) {
            reloadPlugin(pluginId, version);
        } else if (FLAG_UNLOAD.isSet()) {
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
        out.println("Added plugin loader for " + url);
    }

    private void loadPlugin(String id, String version) throws PluginException {
        mgr.getRegistry().loadPlugin(mgr.getLoaderManager(), id, version);
        out.println("Loaded plugin " + id + " version " + version);
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
        out.println("Reloaded plugin " + id + " version " + version);
    }

    private void unloadPlugin(String id) throws PluginException {
        mgr.getRegistry().unloadPlugin(id);
        out.println("Unloaded plugin " + id);
    }

    private void listPlugins() throws PluginException {
        final ArrayList<String> rows = new ArrayList<String>();
        for (PluginDescriptor descr : mgr.getRegistry()) {
            StringBuilder sb = new StringBuilder();
            sb.append(descr.getId());
            sb.append("; state ");
            sb.append((descr.getPlugin().isActive()) ? "active" : "inactive");
            sb.append("; version ");
            sb.append(descr.getPlugin().getDescriptor().getVersion());
            rows.add(sb.toString());
        }
        Collections.sort(rows);
        for (String row : rows) {
            out.println(row);
        }
    }

    private void listPlugin(String id) throws PluginException {
        final PluginDescriptor descr = mgr.getRegistry().getPluginDescriptor(id);
        if (descr != null) {           
            out.print(descr.getId());
            out.print("; state ");
            out.print((descr.getPlugin().isActive()) ? "active" : "inactive");
            out.println("; version " + descr.getPlugin().getDescriptor().getVersion());
        } else {
            out.println("Plugin " + id + " not found");
        }
    }
}
