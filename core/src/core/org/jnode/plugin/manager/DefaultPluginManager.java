/*
 * $Id$
 */
package org.jnode.plugin.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.naming.NamingException;

import org.jnode.naming.InitialNaming;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.plugin.PluginManager;
import org.jnode.plugin.PluginPrerequisite;
import org.jnode.plugin.PluginRegistry;
import org.jnode.plugin.model.PluginRegistryModel;
import org.jnode.system.BootLog;

/**
 * @author epr
 * @author Matt Paine.
 */
public class DefaultPluginManager extends PluginManager {

	/** The registry of plugins */
	private final PluginRegistry registry;

	/**
	 * Initialize a new instance. This will also bind this pluginmanager in the initial namespace.
	 * 
	 * @param registry
	 */
	public DefaultPluginManager(PluginRegistry registry) throws PluginException {
		this.registry = registry;
		try {
			InitialNaming.bind(NAME, this);
		} catch (NamingException ex) {
			throw new PluginException("Cannot register name", ex);
		}
	}

	/**
	 * Gets the plugin registry
	 */
	public PluginRegistry getRegistry() {
		return registry;
	}

	/**
	 * Start all plugins that can be started, but have not been started yet
	 * 
	 * @throws PluginException
	 */
	public void startPlugins() throws PluginException {
		// Resolve all plugins
		((PluginRegistryModel)registry).resolveDescriptors();
		
		// Set the context classloader
		Thread.currentThread().setContextClassLoader(registry.getPluginsClassLoader());

		// Start the plugins
		final String cmdLine = System.getProperty("jnode.cmdline", "");
		final boolean debug = (cmdLine.indexOf("debug") > 0);
		final List descrList = createPluginDescriptorList();
		final ArrayList errors = new ArrayList(descrList.size());
		for (Iterator i = descrList.iterator(); i.hasNext();) {
			final PluginDescriptor descr = (PluginDescriptor) i.next();
			try {
				//===== Removed to call the startPlugin() method
				//if (canStart(descr)) {
				//	Syslog.debug("Starting " + descr.getId());
				//	descr.getPlugin().start();
				//} else {
				//	Syslog.warn("Skipping start of " + descr.getId() + " due to depencies.");
				//}

				if (debug) {
					Thread.sleep(250);
				}

				startPlugin(descr);

			} catch (Throwable ex) {
				errors.add(new StartError(ex, descr.getId()));
			}
		}

		// Show all errors
		for (Iterator i = errors.iterator(); i.hasNext();) {
			final StartError error = (StartError) i.next();
			BootLog.error("Error starting plugin " + error.getPluginId(), error.getException());
			//break;
		}
	}

	/**
	 * Starts a single plugin.
	 * 
	 * @param d
	 *            The descriptor to start.
	 * @throws PluginException
	 *             if the plugin fails to start.
	 */
	public void startPlugin(PluginDescriptor d) throws PluginException {
		try {
			if (canStart(d)) {
				BootLog.debug("Starting " + d.getId());
				startSinglePlugin(d.getPlugin());
			} else {
				BootLog.warn("Skipping start of " + d.getId() + " due to to depencies.");
			}
		} catch (PluginException ex) {
			BootLog.error("Error starting " + d.getId());
			throw ex;
		} catch (Throwable ex) {
			BootLog.error("Error starting " + d.getId());
			throw new PluginException(ex);
		}
	}

	/**
	 * Stop all plugins that have been started
	 */
	public void stopPlugins() {
		try {
			final List descrList = createPluginDescriptorList();
			Collections.reverse(descrList);
			for (Iterator i = descrList.iterator(); i.hasNext();) {
				final PluginDescriptor descr = (PluginDescriptor) i.next();
				//descr.getPlugin().stop();
				try {
					stopPlugin(descr);
				} catch (PluginException ex) {
				}
			}
		} catch (PluginException ex) {
			BootLog.error("Cannot stop plugins", ex);
		}
	}

	/**
	 * Stops a single plugin and all plugins that depend on it.
	 * 
	 * @param d
	 *            The descriptor to stop.
	 * @throws PluginException
	 *             if the plugin fails to stop.
	 */
	public void stopPlugin(PluginDescriptor d) throws PluginException {
		final String id = d.getId();
		for (Iterator i = registry.getDescriptorIterator(); i.hasNext();) {
			final PluginDescriptor descr = (PluginDescriptor) i.next();
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
	private List createPluginDescriptorList() throws PluginException {

		// Get all descriptors into a hashmap (id, descriptor).
		final HashMap all = new HashMap();
		for (Iterator i = registry.getDescriptorIterator(); i.hasNext();) {
			final PluginDescriptor descr = (PluginDescriptor) i.next();
			all.put(descr.getId(), descr);
		}
		// Remove those plugin where some prerequisites do not exist
		for (Iterator i = all.values().iterator(); i.hasNext();) {
			final PluginDescriptor descr = (PluginDescriptor) i.next();
			if (!prerequisitesExist(descr, all)) {
				BootLog.info("Skipping plugin " + descr.getId());
				all.remove(descr.getId());
				i = all.values().iterator();
			}
		}

		// Now create a sorted list
		final ArrayList list = new ArrayList();
		final HashSet nameSet = new HashSet();

		while (all.size() > 0) {
			int additions = 0;
			for (Iterator i = all.values().iterator(); i.hasNext();) {
				final PluginDescriptor descr = (PluginDescriptor) i.next();
				if (canAdd(descr, nameSet)) {
					list.add(descr);
					nameSet.add(descr.getId());
					all.remove(descr.getId());
					additions++;
					i = all.values().iterator();
				}

			}
			if (additions == 0) {
				throw new PluginException("Cycle in plugin prerequisites remaining: " + all.keySet());
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
	private boolean canAdd(PluginDescriptor descr, HashSet nameSet) {
		//Syslog.debug("Testing " + descr.getId());
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
	 * Can the plugin of the given descriptor be started?
	 * 
	 * @param descr
	 */
	private boolean canStart(PluginDescriptor descr) throws PluginException {
		final PluginPrerequisite[] prereq = descr.getPrerequisites();
		for (int i = 0; i < prereq.length; i++) {
			final PluginPrerequisite pr = prereq[i];
			final PluginDescriptor prDescr = registry.getPluginDescriptor(pr.getPluginId());
			if (!prDescr.getPlugin().isActive()) {
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
}
