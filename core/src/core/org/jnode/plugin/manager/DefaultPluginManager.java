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
import org.jnode.system.BootLog;

/**
 * @author epr
 * @author Matt Paine.
 */
public class DefaultPluginManager implements PluginManager {

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

				startPlugin(descr);

			} catch (Throwable ex) {
				errors.add(ex);
			}
		}

		// Show all errors
		for (Iterator i = errors.iterator(); i.hasNext();) {
			final Throwable ex = (Throwable) i.next();
			BootLog.error("Error starting plugins", ex);
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
		if (canStart(d)) {
			BootLog.debug("Starting " + d.getId());
			d.getPlugin().start();
		} else {
			BootLog.warn("Skipping start of " + d.getId() + " due to to depencies.");
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
	 * Stops a single plugin.
	 * 
	 * @param d
	 *            The descriptor to stop.
	 * @throws PluginException
	 *             if the plugin fails to stop.
	 */
	public void stopPlugin(PluginDescriptor d) throws PluginException {
		d.getPlugin().stop();
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
				throw new PluginException("Cycle in plugin prerequisites remaining: " + all.keySet() + ", nameSet: " + nameSet);
			}
		}

		return list;
	}

	/**
	 * Can the given descriptor be added to a startPlugin ordered list?
	 * 
	 * @param descr
	 * @param names
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

}
