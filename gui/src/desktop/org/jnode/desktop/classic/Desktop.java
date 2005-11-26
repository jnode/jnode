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

package org.jnode.desktop.classic;

import org.apache.log4j.Logger;
import org.jnode.awt.JNodeAwtContext;
import org.jnode.awt.JNodeToolkit;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.PluginClassLoader;
import org.jnode.vm.Vm;
import org.jnode.vm.VmSystem;

import javax.swing.DefaultDesktopManager;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.AWTError;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

/**
 * @author Levente S\u00e1ntha
 */
public class Desktop implements Runnable {

    final static Logger log = Logger.getLogger(Desktop.class);

    TaskBar taskBar;

    /**
     * @see Runnable#run()
     */
    public void run() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final ClassLoader cl = getClass().getClassLoader();
                final ExtensionPoint appsEP;
                if (cl instanceof PluginClassLoader) {
                    appsEP = ((PluginClassLoader) cl).getDeclaringPluginDescriptor().getExtensionPoint("apps");
                } else {
                    throw new AWTError("Need to be loaded using a plugin classloader");
                }
                Desktop.this.taskBar = new TaskBar(appsEP);


                final JNodeToolkit tk = JNodeToolkit.getJNodeToolkit();
                final JNodeAwtContext ctx = tk.getAwtContext();
                final JDesktopPane desktop = ctx.getDesktop();
                final Container awtRoot = ctx.getAwtRoot();

                taskBar.startButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        if (taskBar.startMenu.isShowing()) {
                            taskBar.startMenu.setVisible(false);
                        } else {
                            Point p = taskBar.startButton.getLocationOnScreen();
                            int h = taskBar.startMenu.getPreferredSize().height;
                            taskBar.startMenu.show(desktop, 0, p.y - h);
                        }
                    }
                });


                taskBar.quitMI.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        taskBar.startMenu.setVisible(false);
                        JNodeToolkit.stopGui();
                    }
                });

                taskBar.haltMI.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JNodeToolkit.stopGui();
                        VmSystem.halt(false);
                    }
                });

                taskBar.restartMI.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JNodeToolkit.stopGui();
                        VmSystem.halt(true);
                    }
                });


                taskBar.desktopColorMI.addActionListener(new ActionListener() {
                    private JFrame f;
                    private Color oldColor;
                    public void actionPerformed(ActionEvent e) {
                        if(f == null){
                            f = new JFrame("Desktop color");
                            final JColorChooser color_chooser = new JColorChooser();
                            f.add(color_chooser, BorderLayout.CENTER);
                            JButton ok = new JButton("OK");
                            ok.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent event) {
                                    desktop.setBackground(color_chooser.getColor());
                                    f.setVisible(false);
                                }
                            });
                            JButton apply = new JButton("Apply");
                            apply.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent event) {
                                    desktop.setBackground(color_chooser.getColor());
                                }
                            });
                            JButton cancel = new JButton("Cancel");
                            cancel.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent event) {
                                    desktop.setBackground(oldColor);
                                    f.setVisible(false);
                                }
                            });
                            JPanel buttons = new JPanel();
                            buttons.add(ok);
                            buttons.add(apply);
                            buttons.add(cancel);
                            f.add(buttons, BorderLayout.SOUTH);
                        }
                        oldColor = desktop.getBackground();
                        Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
                        int x = (ss.width - 500) / 2;
                        int y = (ss.height - 400) / 2;
                        f.setSize(500, 400);
                        f.setVisible(true);
                        f.setLocation(x, y);
                    }
                });


                awtRoot.removeAll();
                awtRoot.setLayout(null);
                awtRoot.add(desktop);
                final int h = awtRoot.getHeight();
                final int controlBarHeight = 36;
                final int w = awtRoot.getWidth();
                desktop.setBounds(0, 0, w, h - controlBarHeight);
                awtRoot.add(taskBar);
                taskBar.setBounds(0, h - controlBarHeight, w, controlBarHeight);

                awtRoot.invalidate();
                awtRoot.repaint();
                System.out.println("taskBar.bounds=" + taskBar.getBounds());
                System.out.println("desktop.bounds=" + desktop.getBounds());

                // Update desktopmanager
                desktop.setDesktopManager(new DesktopManagerImpl());
                desktop.addContainerListener(new DesktopContainerListener());

                // Set background info
                final int dx = 30;
                final int dy = dx;
                final JLabel welcomeLbl = new JLabel("Welcome to JNode");
                welcomeLbl.setForeground(Color.WHITE);
                welcomeLbl.setLocation(dx, dy);
                welcomeLbl.setFont(welcomeLbl.getFont().deriveFont(20.0f));
                welcomeLbl.setSize(welcomeLbl.getPreferredSize());
                desktop.add(welcomeLbl, (Integer) (JLayeredPane.DEFAULT_LAYER - 1));

                final JLabel versionLbl = new JLabel("version " + Vm.getVm().getVersion());
                versionLbl.setForeground(Color.WHITE);
                versionLbl.setFont(versionLbl.getFont().deriveFont(14.0f));
                versionLbl.setSize(versionLbl.getPreferredSize());
                versionLbl.setLocation(desktop.getWidth() - versionLbl.getWidth() - dy, desktop.getHeight() - versionLbl.getHeight() - dy);
                desktop.add(versionLbl, (Integer) (JLayeredPane.DEFAULT_LAYER - 1));

                // Update
                desktop.doLayout();
                desktop.repaint();
            }
        });
    }

    private class DesktopContainerListener implements ContainerListener {

        /**
         * @see java.awt.event.ContainerListener#componentAdded(java.awt.event.ContainerEvent)
         */
        public void componentAdded(ContainerEvent event) {
            final Component c = event.getChild();
            if (c instanceof JInternalFrame) {
                taskBar.windowBar.addFrame((JInternalFrame) c);
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
                taskBar.windowBar.removeFrame((JInternalFrame) c);
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
            //frame.setVisible(true);
            if (p != null) {
                p.setSelectedFrame(frame);
            }
        }

        /**
         * @see javax.swing.DesktopManager#iconifyFrame(javax.swing.JInternalFrame)
         */
        public void iconifyFrame(JInternalFrame frame) {
            final JDesktopPane p = frame.getDesktopPane();
            //frame.setVisible(false);
            if ((p != null) && (p.getSelectedFrame() == frame)) {
                p.setSelectedFrame(null);
            }
        }
    }
}
