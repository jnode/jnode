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
 
package org.jnode.boot;

import java.util.List;

import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginManager;
import org.jnode.plugin.manager.DefaultPluginManager;
import org.jnode.plugin.model.PluginRegistryModel;
import org.jnode.system.BootLog;
import org.jnode.vm.Unsafe;
import org.jnode.vm.VmSystem;
import org.jnode.vm.annotation.LoadStatics;
import org.jnode.vm.annotation.SharedStatics;
import org.vmmagic.pragma.UninterruptiblePragma;

/**
 * First class that is executed when JNode boots.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@SharedStatics
public final class Main {

	public static final String MAIN_METHOD_NAME = "vmMain";
	public static final String MAIN_METHOD_SIGNATURE = "()I";
	public static final String REGISTRY_FIELD_NAME = "pluginRegistry";

	protected static PluginRegistryModel pluginRegistry;

	/**
	 * First java entry point after the assembler kernel has booted.
	 * 
	 * @return int
	 */
    @LoadStatics
	public static int vmMain() throws UninterruptiblePragma {
		//return 15;
		try {
			Unsafe.debug("Starting JNode\n");
			final long start = VmSystem.currentKernelMillis();

			Unsafe.debug("VmSystem.initialize\n");
			VmSystem.initialize();

			// Load the plugins from the initjar
			BootLog.info("Loading initjar plugins");
			final InitJarProcessor proc = new InitJarProcessor(VmSystem.getInitJar());
			List<PluginDescriptor> descriptors = proc.loadPlugins(pluginRegistry);

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
			ex.printStackTrace();
			sleepForever();
			return -2;
		}
		Unsafe.debug("System has finished");
		return VmSystem.getExitCode();
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
