/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.log4j.config;

import java.awt.event.KeyEvent;
import java.io.PrintStream;
import java.util.Enumeration;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.TextConsole;
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
		try {
		    // Create the appenders
			final ConsoleManager conMgr = InitialNaming.lookup(ConsoleManager.NAME);
			final TextConsole console = 
				(TextConsole)conMgr.createConsole(
						"Log4j", 
						(ConsoleManager.CreateOptions.TEXT | 
								ConsoleManager.CreateOptions.SCROLLABLE | 
								ConsoleManager.CreateOptions.NO_SYSTEM_OUT_ERR |
								ConsoleManager.CreateOptions.NO_LINE_EDITTING));
			conMgr.registerConsole(console);
			
			console.setAcceleratorKeyCode(KeyEvent.VK_F7);
			final VirtualConsoleAppender debugApp = new VirtualConsoleAppender(console, new PatternLayout(LAYOUT));
			debugApp.setThreshold(Level.DEBUG);
			BootLog.setDebugOut(new PrintStream(console.getOut()));

    		final ConsoleAppender infoApp = new ConsoleAppender(new PatternLayout(LAYOUT));
    		infoApp.setThreshold(Level.INFO);

    		// Add the new appenders
    		root.addAppender(debugApp);
    		root.addAppender(infoApp);

    		// Remove the existing appenders.
			for (Enumeration<?> appEnum = root.getAllAppenders(); appEnum.hasMoreElements(); ) {
			    final Appender appender = (Appender)appEnum.nextElement();
			    if ((appender != debugApp) && (appender != infoApp)) {
			        root.removeAppender(appender);
			    }
			}
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
