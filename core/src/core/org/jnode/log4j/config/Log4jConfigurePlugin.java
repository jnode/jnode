/*
 * $Id$
 */
package org.jnode.log4j.config;

import javax.naming.NameNotFoundException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jnode.driver.console.Console;
import org.jnode.driver.console.ConsoleManager;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.system.BootLog;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Log4jConfigurePlugin extends Plugin {

	public static final String LAYOUT = "%-5p [%c{1}]: %m%n";
	
	/**
	 * @param descriptor
	 */
	public Log4jConfigurePlugin(PluginDescriptor descriptor) {
		super(descriptor);
	}

	/**
	 * @see org.jnode.plugin.Plugin#startPlugin()
	 */
	protected void startPlugin() throws PluginException {
		final Logger root = Logger.getRootLogger();
		final ConsoleAppender infoApp = new ConsoleAppender(new PatternLayout(LAYOUT));
		infoApp.setThreshold(Level.INFO);
		root.addAppender(infoApp);
		try {
			final ConsoleManager conMgr = (ConsoleManager)InitialNaming.lookup(ConsoleManager.NAME);
			final Console console2 = conMgr.getConsole(1);
			final VirtualConsoleAppender debugApp = new VirtualConsoleAppender(console2, new PatternLayout(LAYOUT));
			debugApp.setThreshold(Level.DEBUG);
			root.addAppender(debugApp);
			BootLog.setDebugOut(console2.getOut());
		} catch (NameNotFoundException ex) {
			root.error("ConsoleManager not found", ex);
		}
	}

	/**
	 * @see org.jnode.plugin.Plugin#stopPlugin()
	 */
	protected void stopPlugin() throws PluginException {
		LogManager.resetConfiguration();
	}

}
