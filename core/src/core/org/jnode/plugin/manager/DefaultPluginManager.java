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

package org.jnode.plugin.manager;

import gnu.java.security.action.GetPropertyAction;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.naming.NamingException;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.plugin.PluginLoaderManager;
import org.jnode.plugin.PluginManager;
import org.jnode.plugin.PluginPrerequisite;
import org.jnode.plugin.PluginRegistry;
import org.jnode.plugin.model.PluginRegistryModel;
import org.jnode.security.JNodePermission;
import org.jnode.system.BootLog;

/**
 * @author epr
 * @author Matt Paine.
 */
public final class DefaultPluginManager extends PluginManager {

    /**
     * The registry of plugins
     */
    private final PluginRegistry registry;
    /**
     * The loader manager
     */
    private final DefaultPluginLoaderManager loaderMgr;

    private static final int START_TIMEOUT = 10000;

    private static final JNodePermission START_SYSTEM_PLUGINS_PERM = new JNodePermission("startSystemPlugins");
    private static final JNodePermission STOP_PLUGINS_PERM = new JNodePermission("stopPlugins");

    /**
     * Initialize a new instance. This will also bind this pluginmanager in the
     * initial namespace.
     *
     * @param registry
     */
    public DefaultPluginManager(PluginRegistry registry) throws PluginException {
        this.loaderMgr = new DefaultPluginLoaderManager();
        this.registry = registry;
        try {
            InitialNaming.bind(NAME, this);
        } catch (NamingException ex) {
            throw new PluginException("Cannot register name", ex);
        }
    }

    /**
     * Gets the plugin loader manager.
     */
    public final PluginLoaderManager getLoaderManager() {
        return loaderMgr;
    }


    /**
     * Gets the plugin registry
     */
    public PluginRegistry getRegistry() {
        return registry;
    }

    /**
     * Start all system plugins and plugins with the auto-start flag on.
     *
     * @throws PluginException
     */
    public void startSystemPlugins(List descriptors) throws PluginException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(START_SYSTEM_PLUGINS_PERM);
        }

        // Resolve all plugins
        ((PluginRegistryModel) registry).resolveDescriptors();
        ((PluginRegistryModel) registry).resolveDescriptors(descriptors);

        // Set the context classloader
        Thread.currentThread().setContextClassLoader(
            registry.getPluginsClassLoader());

        // Start the plugins
        final String cmdLine = (String) AccessController.doPrivileged(new GetPropertyAction("jnode.cmdline", ""));
        final boolean debug = (cmdLine.indexOf("debug") > 0);
        final List<PluginDescriptor> descrList = createPluginDescriptorList();

        // Order list by priority
        Collections.sort(descrList, new PriorityComparator());

        // 2 loops, first start all system plugins,
        // then start all auto-start plugins
        for (int type = 0; type < 2; type++) {
            BootLog.info("Starting " + ((type == 0) ? "system" : "auto-start") + " plugins");
            for (PluginDescriptor descr : descrList) {
                try {
                    final boolean start;
                    if (type == 0) {
                        start = descr.isSystemPlugin();
                    } else {
                        start = (!descr.isSystemPlugin()) && descr.isAutoStart();
                    }
                    if (start) {
                        if (debug) {
                            Thread.sleep(250);
                        }

                        startSinglePlugin(descr.getPlugin());
                    }
                } catch (Throwable ex) {
                    BootLog.error("Cannot start " + descr.getId(), ex);
                    if (debug) {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ex1) {
                            // Ignore
                        }
                    }
                }
            }
        }

        // Wait a while until all plugins have finished their startup process
        if (!isStartPluginsFinished()) {
            BootLog.info("Waiting for plugins to finished their startprocess");
            final long start = System.currentTimeMillis();
            long now = start;
            int loop = 0;
            while (!isStartPluginsFinished() && (now - start < START_TIMEOUT)) {
                try {
                    if (++loop == 10) {
                        loop = 0;
                    }
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    // Ignore
                }
                now = System.currentTimeMillis();
            }
            System.out.println();
            if (now >= START_TIMEOUT) {
                // List all non-finished plugins
                listUnfinishedPlugins();
            }
        }
    }

    /**
     * Stop all plugins that have been started
     */
    public final void stopPlugins() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(STOP_PLUGINS_PERM);
        }

        try {
            final List<PluginDescriptor> descrList = createPluginDescriptorList();
            Collections.reverse(descrList);
            for (PluginDescriptor descr : descrList) {
                //descr.getPlugin().stop();
                try {
                    stopPlugin(descr);
                } catch (PluginException ex) {
                    //empty
                }
            }
            BootLog.info("Stopped all plugins");
        } catch (PluginException ex) {
            BootLog.error("Cannot stop plugins", ex);
        }
    }

    /**
     * Stops a single plugin and all plugins that depend on it.
     *
     * @param d The descriptor to stop.
     * @throws PluginException if the plugin fails to stop.
     */
    public final void stopPlugin(PluginDescriptor d) throws PluginException {
        final String id = d.getId();
        //BootLog.info("__Stopping " + id);
        for (PluginDescriptor descr : registry) {
            if (descr.depends(id)) {
                stopPlugin(descr);
            }
        }
        stopSinglePlugin(d.getPlugin());
    }

    /**
     * Create a list on plugin descriptors in the right order for startPlugins.
     *
     * @return List&lt;PluginDescriptor&gt;
     */
    private List<PluginDescriptor> createPluginDescriptorList() throws PluginException {

        // Get all descriptors into a hashmap (id, descriptor).
        final HashMap<String, PluginDescriptor> all = new HashMap<String, PluginDescriptor>();
        final HashSet<String> systemSet = new HashSet<String>();
        for (PluginDescriptor descr : registry) {
            all.put(descr.getId(), descr);
            if (descr.isSystemPlugin()) {
                systemSet.add(descr.getId());
            }
        }
        // Remove those plugin where some prerequisites do not exist
        for (Iterator<PluginDescriptor> i = all.values().iterator(); i.hasNext();) {
            final PluginDescriptor descr = (PluginDescriptor) i.next();
            if (!prerequisitesExist(descr, all)) {
                BootLog.info("Skipping plugin " + descr.getId());
                all.remove(descr.getId());
                systemSet.remove(descr.getId());
                i = all.values().iterator();
            }
        }

        // Now create a sorted list
        final ArrayList<PluginDescriptor> list = new ArrayList<PluginDescriptor>();
        final HashSet<String> nameSet = new HashSet<String>();

        while (all.size() > 0) {
            int additions = 0;
            for (Iterator<PluginDescriptor> i = all.values().iterator(); i.hasNext();) {
                final PluginDescriptor descr = (PluginDescriptor) i.next();
                if (canAdd(descr, nameSet, systemSet)) {
                    list.add(descr);
                    nameSet.add(descr.getId());
                    all.remove(descr.getId());
                    systemSet.remove(descr.getId());
                    additions++;
                    i = all.values().iterator();
                }

            }
            if (additions == 0) {
                throw new PluginException(
                    "Cycle in plugin prerequisites remaining: " + all.keySet());
            }
        }

        return list;
    }

    /**
     * Can the given descriptor be added to a startPlugin ordered list?
     *
     * @param descr
     * @param nameSet
     */
    private boolean canAdd(PluginDescriptor descr, HashSet<String> nameSet,
                           HashSet<String> systemSet) {
        //Syslog.debug("Testing " + descr.getId());
        if (!descr.isSystemPlugin()) {
            if (!systemSet.isEmpty()) {
                return false;
            }
        }
        final PluginPrerequisite[] prereq = descr.getPrerequisites();
        for (int i = 0; i < prereq.length; i++) {
            final PluginPrerequisite pr = prereq[i];
            if (!nameSet.contains(pr.getPluginId())) {
                //Syslog.debug("Not in set: " + pr.getPluginId());
                return false;
            }
        }
        return true;
    }

    /**
     * Do all prerequisite plugins exists?
     *
     * @param descr
     * @param all
     */
    private boolean prerequisitesExist(PluginDescriptor descr, HashMap all) {
        final PluginPrerequisite[] prereq = descr.getPrerequisites();
        for (int i = 0; i < prereq.length; i++) {
            final PluginPrerequisite pr = prereq[i];
            if (!all.containsKey(pr.getPluginId())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Is the isStartFinished property of all started plugins true.
     *
     * @return
     */
    private boolean isStartPluginsFinished() {
        for (PluginDescriptor descr : registry) {
            try {
                final Plugin pi = descr.getPlugin();
                if (pi.isActive()) {
                    if (!pi.isStartFinished()) {
                        return false;
                    }
                }
            } catch (PluginException ex) {
                // Ignore
            }
        }
        return true;
    }

    /**
     * List of started but unfinished plugins.
     */
    private void listUnfinishedPlugins() {
        for (PluginDescriptor descr : registry) {
            try {
                final Plugin pi = descr.getPlugin();
                if (pi.isActive()) {
                    if (!pi.isStartFinished()) {
                        BootLog.error("Plugin " + descr.getId()
                            + " has not yet finished");
                    }
                }
            } catch (PluginException ex) {
                // Ignore
            }
        }
    }

    static class StartError {

        private final Throwable exception;

        private final String pluginId;

        /**
         * @param exception
         * @param pluginId
         */
        public StartError(final Throwable exception, final String pluginId) {
            super();
            this.exception = exception;
            this.pluginId = pluginId;
        }

        /**
         * @return Returns the exception.
         */
        public final Throwable getException() {
            return this.exception;
        }

        /**
         * @return Returns the pluginId.
         */
        public final String getPluginId() {
            return this.pluginId;
        }
    }

    private static class PriorityComparator implements Comparator<PluginDescriptor> {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(PluginDescriptor o1, PluginDescriptor o2) {
            int p1 = o1.getPriority();
            int p2 = o2.getPriority();
            if (p1 > p2) {
                return -1;
            }
            if (p1 == p2) {
                return 0;
            }
            return 1;
        }

    }
}
