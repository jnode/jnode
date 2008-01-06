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
 
package org.jnode.awt;

import org.apache.log4j.Logger;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Levente S\u00e1ntha
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
        if(JNodeToolkit.isGuiActive()){
            ((JNodeToolkit)JNodeToolkit.getDefaultToolkit()).joinGUI();
            JNodeToolkit.waitUntilStopped();
        } else {
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
}
