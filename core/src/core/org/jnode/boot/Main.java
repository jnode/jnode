/**
 * $Id$
 */
package org.jnode.boot;

import org.jnode.driver.console.Screen;
import org.jnode.plugin.PluginManager;
import org.jnode.plugin.PluginRegistry;
import org.jnode.plugin.manager.DefaultPluginManager;
import org.jnode.system.BootLog;
import org.jnode.vm.VmSystem;

/**
 * First class that is executed when JNode boots.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Main {

	public static final String MAIN_METHOD_NAME = "vmMain";
	public static final String MAIN_METHOD_SIGNATURE = "()I";
	public static final String REGISTRY_FIELD_NAME = "pluginRegistry";

	protected static PluginRegistry pluginRegistry;

	/**
	 * First java entry point after the assembler kernel has booted.
	 * 
	 * @return int
	 */
	public static int vmMain() {
		try {
			final long start = VmSystem.currentKernelMillis();

			VmSystem.initialize();
			Screen.debug("Starting JNode\n");

			final PluginManager piMgr = new DefaultPluginManager(pluginRegistry);
			piMgr.startPlugins();

			final long end = VmSystem.currentKernelMillis();
			System.out.println("JNode initialization finished in " + (end - start) + "ms.");

			Class shellClass = Class.forName("org.jnode.shell.CommandShell");
			Runnable shell = (Runnable) shellClass.newInstance();
			shell.run();

		} catch (Throwable ex) {
			BootLog.error("Error in bootstrap", ex);
			return -2;
		}
		Screen.debug("System has finished");
		return 0;
	}
}
