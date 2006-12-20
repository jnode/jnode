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
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jnode.naming.InitialNaming;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.plugin.PluginManager;
import org.jnode.plugin.PluginReference;
import org.jnode.plugin.PluginRegistry;
import org.jnode.plugin.URLPluginLoader;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;
import org.jnode.shell.help.argument.OptionArgument;
import org.jnode.shell.help.argument.PluginArgument;
import org.jnode.shell.help.argument.URLArgument;
import org.jnode.vm.Vm;

/**
 * @author epr
 */
public class PluginCommand {

    static final OptionArgument ARG_LOADER = new OptionArgument("loader",
            "Loader management", 
            new OptionArgument.Option("addloader", "Add plugin loader"));

    static final OptionArgument ARG_ACTION = new OptionArgument("action",
            "action to do on the plugin", 
            new OptionArgument.Option("load", "Load the plugin"),
            new OptionArgument.Option("reload", "Reload the plugin"),
			new OptionArgument.Option("unload", "Unload the plugin")); 

    static final URLArgument ARG_URL = new URLArgument("url", "plugin location");

    static final PluginArgument ARG_ACTION_ID = new PluginArgument("plugin",
            "plugin identifier");

    static final PluginArgument ARG_LIST_ID = new PluginArgument("plugin",
            "plugin identifier");
    
    static final Argument ARG_VERSION = new Argument("version", "plugin version");

    static final Parameter PARAM_LOADER = new Parameter(ARG_LOADER);

    static final Parameter PARAM_ACTION = new Parameter(ARG_ACTION);

    static final Parameter PARAM_ACTION_ID = new Parameter(ARG_ACTION_ID);

    static final Parameter PARAM_LIST_ID = new Parameter(ARG_LIST_ID);

    static final Parameter PARAM_URL = new Parameter(ARG_URL);
    static final Parameter PARAM_VERSION = new Parameter(ARG_VERSION, Parameter.OPTIONAL);

    public static Help.Info HELP_INFO = new Help.Info(
            "plugin",
            new Syntax[] {
                    new Syntax("Print name and state of all loaded plugins"),
                    new Syntax("Plugin loader management", PARAM_LOADER, PARAM_URL),
                    new Syntax("Load/Unload the plugin", PARAM_ACTION,
                            PARAM_ACTION_ID, PARAM_VERSION),
                    new Syntax("Print name and state of plugin", PARAM_LIST_ID)});

    public static void main(final String[] args) throws Exception {
        AccessController.doPrivileged(new PrivilegedExceptionAction() {
            public Object run() throws Exception {
                new PluginCommand().execute(args, System.in, System.out, System.err);
                return null;
                }});
    }

    /**
     * Execute this command
     */
    public void execute(String[] args, InputStream in, PrintStream out,
            PrintStream err) throws Exception {

        final ParsedArguments cmdLine = HELP_INFO.parse(args);
        final PluginManager mgr = (PluginManager) InitialNaming
                .lookup(PluginManager.NAME);

        if (PARAM_LOADER.isSet(cmdLine)) {
            final String action = ARG_LOADER.getValue(cmdLine);
            if (action.equals("addloader")) {
                addPluginLoader(out, mgr, ARG_URL.getURL(cmdLine));
            } else {
                out.println("Unknown load action " + action);
            }
        } else if (PARAM_ACTION.isSet(cmdLine)) {
            final String action = ARG_ACTION.getValue(cmdLine);
            if (action.equals("load")) {
                final String version;
                if (PARAM_VERSION.isSet(cmdLine)) {
                    version = ARG_VERSION.getValue(cmdLine);
                } else {
                    version = Vm.getVm().getVersion();
                }
                loadPlugin(out, mgr, ARG_ACTION_ID.getValue(cmdLine), version);
            } else if (action.equals("reload")) {
                    final String version;
                    if (PARAM_VERSION.isSet(cmdLine)) {
                        version = ARG_VERSION.getValue(cmdLine);
                    } else {
                        version = Vm.getVm().getVersion();
                    }
                    final String id = ARG_ACTION_ID.getValue(cmdLine);
                    reloadPlugin(out, mgr, id, version);
            } else if (action.equals("unload")) {
            	unloadPlugin(out, mgr, ARG_ACTION_ID.getValue(cmdLine));
            } else {
                out.println("Unknown action " + action);
            }
        } else if (PARAM_LIST_ID.isSet(cmdLine)) {
            listPlugin(out, mgr, ARG_LIST_ID.getValue(cmdLine));
        } else {
            listPlugins(out, mgr);
        }
    }

    private void addPluginLoader(PrintStream out, PluginManager mgr, URL url)
            throws PluginException, MalformedURLException {
        final String ext = url.toExternalForm();
        if (!ext.endsWith("/")) {
            url = new URL(ext + "/");
        }
        out.println("Adding loader for " + url);
        mgr.getLoaderManager().addPluginLoader(new URLPluginLoader(url));
    }

    private void loadPlugin(PrintStream out, PluginManager mgr, String id, String version)
            throws PluginException {
        out.println("Loading " + id);
        mgr.getRegistry().loadPlugin(mgr.getLoaderManager(), id, version);
    }

    private void reloadPlugin(PrintStream out, PluginManager mgr, String id,
            String version) throws PluginException {
        out.println("Reloading " + id);
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
    }

    private void unloadPlugin(PrintStream out, PluginManager mgr, String id)
    throws PluginException {
    	mgr.getRegistry().unloadPlugin(id);
    	out.println("Unloaded " + id);
    }
    
    private void listPlugins(PrintStream out, PluginManager mgr)
            throws PluginException {
        final ArrayList<String> rows = new ArrayList<String>();
        for (PluginDescriptor descr : mgr.getRegistry()) {
            StringBuffer sb = new StringBuffer();
            sb.append(descr.getId());
            sb.append("; state ");
            sb.append((descr.getPlugin().isActive())?"active":"inactive");
            sb.append("; version ");
            sb.append(descr.getPlugin().getDescriptor().getVersion());
            rows.add(sb.toString());
        }
        Collections.sort(rows);
        for (String row : rows) {
            out.println(row);
        }
    }

    private void listPlugin(PrintStream out, PluginManager mgr, String id)
            throws PluginException {
        final PluginDescriptor descr = mgr.getRegistry()
                .getPluginDescriptor(id);
        if (descr != null) {           
            out.print(descr.getId());
            out.print("; state ");
            out.print((descr.getPlugin().isActive())?"active":"inactive");
            out.println("; version " + descr.getPlugin().getDescriptor().getVersion());
        } else {
            out.println("Plugin " + id + " not found");
        }
    }
}
