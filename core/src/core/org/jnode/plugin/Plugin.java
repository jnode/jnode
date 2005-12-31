/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.plugin;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.prefs.Preferences;

import org.jnode.plugin.model.PluginDescriptorModel;
import org.jnode.system.BootLog;


/**
 * Abstract plugin class.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class Plugin {

	/** The descriptor of this plugin */
	private final PluginDescriptor descriptor;
	/** Has this plugin been started? */
	private boolean started;
    /** Preferences root for plugins */
    private static transient Preferences pluginPrefs;
	
	/**
	 * Initialize a new instance
	 * 
	 * @param descriptor
	 */
	public Plugin(PluginDescriptor descriptor) {
		this.descriptor = descriptor;
		this.started = false;
		if (descriptor == null) {
			throw new IllegalArgumentException("descriptor cannot be null");
		}
	}

	/**
	 * Gets the descriptor of this plugin
	 * 
	 * @return The descriptor
	 */
	public final PluginDescriptor getDescriptor() {
		return descriptor;
	}

    /**
     * Gets the configuration data of this plugin.
     * @return The persistent configuration data.
     */
    public final Preferences getPreferences() {
        if (pluginPrefs == null) {
            final Preferences root;
            root = (Preferences) AccessController
                    .doPrivileged(new PrivilegedAction() {
                        public Object run() {
                            return Preferences.systemRoot();
                        }
                    });
            pluginPrefs = root.node("plugins");
        }
        return pluginPrefs.node(getDescriptor().getId());
    }
    
	/**
	 * Start this plugin
	 * To invoke this method, a JNodePermission("startPlugin") is required. 
	 * @throws PluginException
	 */
	public final void start() throws PluginException {
	    final SecurityManager sm = System.getSecurityManager();
	    if (sm != null) {
	        sm.checkPermission(PluginSecurityConstants.START_PERM);
	    }
		if (!started) {
		    if (descriptor.hasCustomPluginClass()) {
		        BootLog.debug("__Starting " + descriptor.getId());
		    }
            started = true;
		    startPlugin();
		    ((PluginDescriptorModel)descriptor).firePluginStarted();
		}
	}

	/**
	 * Stop this plugin.
	 * To invoke this method, a JNodePermission("stopPlugin") is required. 
	 * 
	 * @throws PluginException
	 */
	public final void stop() throws PluginException {
	    final SecurityManager sm = System.getSecurityManager();
	    if (sm != null) {
	        sm.checkPermission(PluginSecurityConstants.STOP_PERM);
	    }
		if (started) {
            started = false;
		    ((PluginDescriptorModel)descriptor).firePluginStop();
			stopPlugin();
		}
	}

	/**
	 * Is this plugin active. A plugin if active between a call to start and stop.
	 * 
	 * @see #start()
	 * @see #stop()
	 * @return boolean
	 */
	public final boolean isActive() {
		return started;
	}

	/**
	 * Has this plugin finished its startup work.
	 * Most plugins do their start work in the {@link #startPlugin()} method.
	 * However, some plugins create thread there to do some work in the background.
	 * These plugins should overwrite this method and return true when the startup
	 * process is fully finished.
	 * 
	 * @return True if this plugins has fully finished its startup process, false otherwise.
	 */
	public boolean isStartFinished() {
	    return started;
	}
	
	/**
	 * Actually start this plugin.
	 * 
	 * @throws PluginException
	 */
	protected abstract void startPlugin() throws PluginException;

	/**
	 * Actually start this plugin.
	 * 
	 * @throws PluginException
	 */
	protected abstract void stopPlugin() throws PluginException;
}
