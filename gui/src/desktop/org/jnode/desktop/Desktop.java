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

import java.awt.AWTError;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

import javax.swing.DefaultDesktopManager;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.jnode.awt.JNodeAwtContext;
import org.jnode.awt.JNodeToolkit;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.PluginClassLoader;
import org.jnode.vm.VmSystem;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Desktop implements Runnable {

	final static Logger log = Logger.getLogger(Desktop.class);

	ControlBar controlBar;

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		final ClassLoader cl = getClass().getClassLoader();
		final ExtensionPoint appsEP;
		if (cl instanceof PluginClassLoader) {
			appsEP = ((PluginClassLoader)cl).getDeclaringPluginDescriptor().getExtensionPoint("apps");
		} else {
			throw new AWTError("Need to be loaded using a plugin classloader");
		}
		this.controlBar = new ControlBar(appsEP);
		
		final JNodeToolkit tk = JNodeToolkit.getJNodeToolkit();
		final JNodeAwtContext ctx = tk.getAwtContext();
		final JDesktopPane desktop = ctx.getDesktop();
		final Container awtRoot = ctx.getAwtRoot();

		final JButton haltCmd = new JButton("Halt2");
		haltCmd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JNodeToolkit.stopGui();
			}
		});
		controlBar.getApplicationBar().add(haltCmd);

		final JButton rebootCmd = new JButton("Reboot");
		rebootCmd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JNodeToolkit.stopGui();
				VmSystem.halt(true);
			}
		});
		controlBar.getApplicationBar().add(rebootCmd);

		awtRoot.removeAll();
		awtRoot.setLayout(null);
		awtRoot.add(desktop);
		final int w = awtRoot.getWidth();
		final int controlBarWidth = w / 8;
		final int h = awtRoot.getHeight();
		desktop.setBounds(0, 0, w - controlBarWidth, h);
		awtRoot.add(controlBar);
		controlBar.setBounds(w - controlBarWidth, 0, controlBarWidth, h);
		
		awtRoot.invalidate();

		// Update desktopmanager
		desktop.setDesktopManager(new DesktopManagerImpl());
		desktop.addContainerListener(new DesktopContainerListener());

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Frame test = new Frame("Test");
                test.setSize(100, 200);
                test.show();                
            }
        });

		log.debug("dm=" + desktop.getDesktopManager());
		// TODO Auto-generated method stub

	}

	private class DesktopContainerListener implements ContainerListener {

		/**
		 * @see java.awt.event.ContainerListener#componentAdded(java.awt.event.ContainerEvent)
		 */
		public void componentAdded(ContainerEvent event) {
			final Component c = event.getChild();
			if (c instanceof JInternalFrame) {
				controlBar.getWindowBar().addFrame((JInternalFrame) c);
			} else {
			    log.info("componentAdded: " + c.getClass().getName());         
            }
		}

		/**
		 * @see java.awt.event.ContainerListener#componentRemoved(java.awt.event.ContainerEvent)
		 */
		public void componentRemoved(ContainerEvent event) {
			final Component c = event.getChild();
			if (c instanceof JInternalFrame) {
				controlBar.getWindowBar().removeFrame((JInternalFrame) c);
            } else {
                log.info("componentRemoved: " + c.getClass().getName());         
			}
		}
	}

	private class DesktopManagerImpl extends DefaultDesktopManager {

		/**
		 * @see javax.swing.DesktopManager#deiconifyFrame(javax.swing.JInternalFrame)
		 */
		public void deiconifyFrame(JInternalFrame frame) {
			final JDesktopPane p = frame.getDesktopPane();
			frame.setVisible(true);
			if (p != null) {
				p.setSelectedFrame(frame);
			}
		}

		/**
		 * @see javax.swing.DesktopManager#iconifyFrame(javax.swing.JInternalFrame)
		 */
		public void iconifyFrame(JInternalFrame frame) {
			final JDesktopPane p = frame.getDesktopPane();
			frame.setVisible(false);
			if ((p != null) && (p.getSelectedFrame() == frame)) {
				p.setSelectedFrame(null);
			}
		}

		/**
		 * @see javax.swing.DesktopManager#activateFrame(javax.swing.JInternalFrame)
		 */
		public void activateFrame(JInternalFrame frame) {
			super.activateFrame(frame);
			controlBar.getWindowBar().setActiveFrame(frame);
		}

		/**
		 * @see javax.swing.DesktopManager#deactivateFrame(javax.swing.JInternalFrame)
		 */
		public void deactivateFrame(JInternalFrame frame) {
			super.deactivateFrame(frame);
		}
	}
}
