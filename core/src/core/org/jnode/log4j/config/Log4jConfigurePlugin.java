/*
 * $Id$
 */
package org.jnode.log4j.config;

import java.awt.event.KeyEvent;
import java.util.Enumeration;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jnode.driver.console.ConsoleException;
import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.x86.ScrollableShellConsole;
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
	private static final String DebugConsoleName = "DebugConsole";
	
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
		try {
		    // Create the appenders
			final ConsoleManager conMgr = (ConsoleManager)InitialNaming.lookup(ConsoleManager.NAME);
			final ScrollableShellConsole console = new ScrollableShellConsole(conMgr, DebugConsoleName);
			conMgr.registerConsole(console);
			
			console.setAcceleratorKeyCode(KeyEvent.VK_F7);
			final VirtualConsoleAppender debugApp = new VirtualConsoleAppender(console, new PatternLayout(LAYOUT));
			debugApp.setThreshold(Level.DEBUG);
			BootLog.setDebugOut(console.getOut());

    		final ConsoleAppender infoApp = new ConsoleAppender(new PatternLayout(LAYOUT));
    		infoApp.setThreshold(Level.INFO);

    		// Add the new appenders
    		root.addAppender(debugApp);
    		root.addAppender(infoApp);

    		// Remove the existing appenders.
			for (Enumeration enum = root.getAllAppenders(); enum.hasMoreElements(); ) {
			    final Appender appender = (Appender)enum.nextElement();
			    if ((appender != debugApp) && (appender != infoApp)) {
			        root.removeAppender(appender);
			    }
			}
		} catch (NameNotFoundException ex) {
			root.error("ConsoleManager not found", ex);
		} catch (ConsoleException e) {
			root.error("Log4J console can not be created", e);
		}
	}

	/**
	 * @see org.jnode.plugin.Plugin#stopPlugin()
	 */
	protected void stopPlugin() throws PluginException {
		LogManager.resetConfiguration();
	}

}
