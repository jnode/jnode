/*
 * $Id$
 */
package org.jnode.awt;

import org.apache.log4j.Logger;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class StartAwt implements Runnable {

	private static final Logger log = Logger.getLogger(StartAwt.class);
	
	public static void main(String[] args) {
		new StartAwt().run();
	}
	
	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		JNodeToolkit.startGui();
		try {
			final String desktopClassName = System.getProperty("jnode.desktop");
			if (desktopClassName != null) {
				final Class desktopClass = Thread.currentThread().getContextClassLoader().loadClass(desktopClassName);
				final Object desktop = desktopClass.newInstance();
				if (desktop instanceof Runnable) {
					final Thread t = new Thread((Runnable)desktop);
					t.start();
				}
			}
		} catch (ClassNotFoundException ex) {
			log.error("Cannot find desktop class", ex);
		} catch (InstantiationException ex) {
			log.error("Cannot instantiate desktop class", ex);
		} catch (IllegalAccessException ex) {
			log.error("Cannot access desktop class", ex);
		} finally {
			JNodeToolkit.waitUntilStopped();
		}
	}
}
