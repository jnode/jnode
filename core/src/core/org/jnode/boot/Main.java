/**
 * $Id$
 */
package org.jnode.boot;

import java.util.List;

import org.jnode.plugin.PluginManager;
import org.jnode.plugin.manager.DefaultPluginManager;
import org.jnode.plugin.model.PluginRegistryModel;
import org.jnode.system.BootLog;
import org.jnode.vm.PragmaLoadStatics;
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

	protected static PluginRegistryModel pluginRegistry;

	/**
	 * First java entry point after the assembler kernel has booted.
	 * 
	 * @return int
	 */
	public static int vmMain() throws PragmaUninterruptible, PragmaLoadStatics {
		//return 15;
		try {
			Unsafe.debug("Starting JNode\n");
			final long start = VmSystem.currentKernelMillis();

			Unsafe.debug("VmSystem.initialize\n");
			VmSystem.initialize();

			// Load the plugins from the initjar
			BootLog.info("Loading initjar plugins");
			final InitJarProcessor proc = new InitJarProcessor(VmSystem.getInitJar());
			List descriptors = proc.loadPlugins(pluginRegistry);

			BootLog.info("Starting PluginManager");
			final PluginManager piMgr = new DefaultPluginManager(pluginRegistry);
			piMgr.startSystemPlugins(descriptors);

			final ClassLoader loader = pluginRegistry.getPluginsClassLoader();
			final String mainClassName = proc.getMainClassName();
			final Runnable main;
			if (mainClassName != null) {
				final Class mainClass = loader.loadClass(mainClassName);
				main = (Runnable) mainClass.newInstance();
			} else {
				BootLog.warn("No Main-Class found");
				main = null;
			}
			final long end = VmSystem.currentKernelMillis();
			System.out.println("JNode initialization finished in " + (end - start) + "ms.");

			if (main != null) {
			    main.run();
			}

		} catch (Throwable ex) {
			BootLog.error("Error in bootstrap", ex);
			sleepForever();
			return -2;
		}
		Unsafe.debug("System has finished");
		return 0;
	}
	
	private static void sleepForever() {
	    while (true) {
	        try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException ex) {
                // Ignore
            }
	    }
	}
}
