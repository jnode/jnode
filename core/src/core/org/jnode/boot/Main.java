/**
 * $Id$
 */
package org.jnode.boot;

import org.jnode.plugin.PluginManager;
import org.jnode.plugin.PluginRegistry;
import org.jnode.plugin.manager.DefaultPluginManager;
import org.jnode.system.BootLog;
import org.jnode.vm.PragmaUninterruptible;
import org.jnode.vm.Unsafe;
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
	public static int vmMain() 
	throws PragmaUninterruptible {
		//return 15;
		try {
			Unsafe.debug("Starting JNode\n");
			final long start = VmSystem.currentKernelMillis();

			//Unsafe.debug("VmSystem.initialize\n");
			VmSystem.initialize();

			//Unsafe.debug("Starting PluginManager");
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
		Unsafe.debug("System has finished");
		return 0;
	}
}
