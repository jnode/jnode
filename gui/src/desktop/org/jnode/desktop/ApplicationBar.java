/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 
package org.jnode.desktop;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.jnode.plugin.ConfigurationElement;
import org.jnode.plugin.Extension;
import org.jnode.plugin.ExtensionPoint;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ApplicationBar extends JPanel {

	final static Logger log = Logger.getLogger(ApplicationBar.class);

	private final ExtensionPoint ep;

	private final static Class[] mainTypes = { String[].class };

	public ApplicationBar(ExtensionPoint ep) {
		this.ep = ep;
		
		reloadApps();
	}

	final void startApp(final String name, final String className) {
		try {
			final Runnable runner = new Runnable() {
				public void run() {
					try {
						final ClassLoader cl = Thread.currentThread().getContextClassLoader();
						final Class<?> cls = cl.loadClass(className);
						final Method main = cls.getMethod("main", mainTypes);
						final Object[] args = { new String[0] };
						main.invoke(null, args);					
					} catch (SecurityException ex) {
						log.error("Security exception in starting class " + className, ex);
					} catch (ClassNotFoundException ex) {
						log.error("Cannot find class " + className);
					} catch (NoSuchMethodException ex) {
						log.error("Cannot find main method in " + className);
					} catch (IllegalAccessException ex) {
						log.error("Cannot access main method in " + className);
					} catch (InvocationTargetException ex) {
						log.error("Error in " + className, ex.getTargetException());
					}
				}
			};
			final Thread t = new Thread(runner);
			t.start();
		} catch (SecurityException ex) {
			log.error("Security exception in starting class " + className, ex);
		}
	}

	private void reloadApps() {
		removeAll();
		final Extension[] exts = ep.getExtensions();
		final int cnt = exts.length;
		log.info("Found " + cnt + " extensions");
		for (int i = 0; i < cnt; i++) {
			final Extension ext = exts[i];

			final ConfigurationElement[] elems = ext.getConfigurationElements();
			log.info("Found " + elems.length + "ce's");
			for (int k = 0; k < elems.length; k++) {
				final ConfigurationElement ce = elems[k];
				if (ce.getName().equals("application")) {
					final String name = ce.getAttribute("name");
					final String className = ce.getAttribute("class");
					if ((name != null) && (className != null)) {
						log.info("Adding app " + name);
						final JButton b = new JButton(name);
						b.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								startApp(name, className);
							}
						});
						add(b);
					}
				}
			}
		}
	}
}
