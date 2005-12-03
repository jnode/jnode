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
import org.jnode.vm.VmSystem;

import javax.swing.DefaultDesktopManager;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.security.AccessController;

import gnu.java.security.action.SetPropertyAction;

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


                ActionListener desktopColorAction = new ActionListener() {
                    private JFrame f;
                    private Color oldColor;

                    public void actionPerformed(ActionEvent e) {
                        if (f == null) {
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
                };
                taskBar.desktopColorMI.addActionListener(desktopColorAction);
                class ChangeScreenResolution implements ActionListener, Runnable {
                    private String resolution;

                    public ChangeScreenResolution(String resolution) {
                        this.resolution = resolution;
                    }

                    public void run() {
                        ((JNodeToolkit) Toolkit.getDefaultToolkit()).changeScreenSize(resolution);
                        AccessController.doPrivileged(new SetPropertyAction("jnode.awt.screensize", resolution));
                    }

                    public void actionPerformed(ActionEvent event) {
                        SwingUtilities.invokeLater(this);
                    }
                }
                taskBar.changeResMI1.addActionListener(new ChangeScreenResolution("640x480/32"));
                taskBar.changeResMI2.addActionListener(new ChangeScreenResolution("800x600/32"));
                taskBar.changeResMI3.addActionListener(new ChangeScreenResolution("1024x768/32"));
                taskBar.changeResMI4.addActionListener(new ChangeScreenResolution("1280x1024/32"));


                awtRoot.removeAll();
                awtRoot.setLayout(new BorderLayout());
                final int controlBarHeight = 36;
                final int w = awtRoot.getWidth();
                taskBar.setPreferredSize(new Dimension(w, controlBarHeight));
                awtRoot.add(desktop, BorderLayout.CENTER);
                awtRoot.add(taskBar, BorderLayout.SOUTH);

                awtRoot.invalidate();
                awtRoot.repaint();

                // Update desktopmanager
                desktop.setDesktopManager(new DesktopManagerImpl());
                desktop.addContainerListener(new DesktopContainerListener());

                final JPopupMenu desktopMenu = new JPopupMenu("Desktop settings");
                JMenuItem desktopColor = new JMenuItem("Desktop color");
                desktopColor.addActionListener(desktopColorAction);
                desktopMenu.add(desktopColor);
                desktopMenu.add(taskBar.changeResMI1);
                desktopMenu.add(taskBar.changeResMI2);
                desktopMenu.add(taskBar.changeResMI3);
                desktopMenu.add(taskBar.changeResMI4);
                desktop.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent event) {
                        if(event.getButton() == MouseEvent.BUTTON2){
                            if (desktopMenu .isShowing()) {
                                desktopMenu .setVisible(false);
                            } else {
                                desktopMenu.show(desktop, event.getX(), event.getY());
                            }
                        }
                    }
                });

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
