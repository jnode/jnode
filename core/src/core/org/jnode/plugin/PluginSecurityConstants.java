/*
 * $Id$
 */
package org.jnode.plugin;
import org.jnode.security.JNodePermission;
/**
 * @author epr
 */
public interface PluginSecurityConstants {
	
	/** Permission required to start a plugin */
	static final JNodePermission START_PERM = new JNodePermission("startPlugin");
	/** Permission required to stop a plugin */
	static final JNodePermission STOP_PERM = new JNodePermission("stopPlugin");

	/** Permission required to load a plugin */
	static final JNodePermission LOAD_PERM = new JNodePermission("loadPlugin");
	/** Permission required to unload a plugin */
	static final JNodePermission UNLOAD_PERM = new JNodePermission("unloadPlugin");
}