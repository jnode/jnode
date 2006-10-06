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
 
package org.jnode.desktop.classic;

import java.awt.AWTError;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Enumeration;

import javax.swing.DefaultDesktopManager;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.jnode.awt.JNodeAwtContext;
import org.jnode.awt.JNodeToolkit;
import org.jnode.awt.swingpeers.DesktopFrame;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.PluginClassLoader;
import org.jnode.vm.VmSystem;

/**
 * @author Levente S\u00e1ntha
 */
public class Desktop implements Runnable {

    final static Logger log = Logger.getLogger(Desktop.class);

    TaskBar taskBar;
    JPopupMenu desktopMenu;
    JDesktopPane desktopPane;
    //Due to this reference to DesktopFrame desktop plugin needs swingpeers
    //todo abstract out this dependency in the future
    DesktopFrame desktopFrame;

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
                Desktop.this.taskBar = new TaskBar(Desktop.this, appsEP);


                final JNodeToolkit tk = JNodeToolkit.getJNodeToolkit();
                final JNodeAwtContext ctx = tk.getAwtContext();
                desktopFrame = (DesktopFrame) ctx;
                desktopPane = ctx.getDesktop();
                final Container awtRoot = ctx.getAwtRoot();

                if(ctx instanceof JFrame){
                    ((JFrame) ctx).addWindowListener(new WindowAdapter() {
                        public void windowClosed(WindowEvent e) {
                            taskBar.clock.stop();
                        }
                    });
                }

                taskBar.startButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        if (taskBar.startMenu.isShowing()) {
                            taskBar.startMenu.setVisible(false);
                        } else {
                            Point p = taskBar.startButton.getLocationOnScreen();
                            int h = taskBar.startMenu.getPreferredSize().height;
                            taskBar.startMenu.show(desktopPane, 0, p.y - h);
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
                    private JFrame frame;
                    private JColorChooser colorChooser;
                    private Color oldColor;

                    public void actionPerformed(ActionEvent e) {
                        //if (frame == null) {
                            frame = new JFrame("Desktop color");
                            colorChooser = new JColorChooser();
                            frame.add(colorChooser, BorderLayout.CENTER);
                            JButton ok = new JButton("OK");
                            ok.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent event) {
                                    desktopPane.setBackground(colorChooser.getColor());
                                    frame.setVisible(false);
                                    frame.dispose();
                                }
                            });
                            JButton apply = new JButton("Apply");
                            apply.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent event) {
                                    desktopPane.setBackground(colorChooser.getColor());
                                }
                            });
                            JButton cancel = new JButton("Cancel");
                            cancel.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent event) {
                                    desktopPane.setBackground(oldColor);
                                    frame.setVisible(false);
                                    frame.dispose();
                                }
                            });
                            JPanel buttons = new JPanel();
                            buttons.add(ok);
                            buttons.add(apply);
                            buttons.add(cancel);
                            frame.add(buttons, BorderLayout.SOUTH);
                        //}

                        oldColor = desktopPane.getBackground();
                        colorChooser.setColor(oldColor);
                        Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
                        int x = (ss.width - 500) / 2;
                        int y = (ss.height - 400) / 2;
                        frame.setSize(500, 400);
                        frame.setVisible(true);
                        frame.setLocation(x, y);
                    }
                };
                taskBar.desktopColorMI.addActionListener(desktopColorAction);


                awtRoot.removeAll();
                awtRoot.setLayout(new BorderLayout());
                final int controlBarHeight = 36;
                final int w = awtRoot.getWidth();
                taskBar.setPreferredSize(new Dimension(w, controlBarHeight));
                awtRoot.add(taskBar, BorderLayout.SOUTH);


                //desktopFrame.setBackgroundImage(loadImage());

                awtRoot.add(desktopPane, BorderLayout.CENTER);


                awtRoot.invalidate();
                awtRoot.repaint();

                // Update desktopmanager
                desktopPane.setDesktopManager(new DesktopManagerImpl());
                desktopPane.addContainerListener(new DesktopContainerListener());

                desktopMenu = new JPopupMenu("Desktop settings");
                JMenuItem desktopColor = new JMenuItem("Desktop color");
                desktopColor.addActionListener(desktopColorAction);
                desktopMenu.add(desktopColor);
                desktopMenu.add(taskBar.changeResMI1);
                desktopMenu.add(taskBar.changeResMI2);
                desktopMenu.add(taskBar.changeResMI3);
                desktopMenu.add(taskBar.changeResMI4);
                desktopPane.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent event) {
                        if(event.getButton() == MouseEvent.BUTTON2){
                            if (desktopMenu .isShowing()) {
                                desktopMenu .setVisible(false);
                            } else {
                                desktopMenu.show(desktopPane, event.getX(), event.getY());
                            }
                        }
                    }
                });

                // Update
                desktopPane.doLayout();
                desktopPane.repaint();
            }
        });
    }

    private class DesktopContainerListener implements ContainerListener {

        /**
         * @see java.awt.event.ContainerListener#componentAdded(java.awt.event.ContainerEvent)
         */
        public void componentAdded(ContainerEvent event) {
            final Component c = event.getChild();
            if (c instanceof JInternalFrame && !JNodeToolkit.getJNodeToolkit().isWindow(c)) {
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
    }

    void enableBackgroundImage(boolean b){
        if(b) desktopFrame.setBackgroundImage(loadImage());
        else desktopFrame.setBackgroundImage(null);
        desktopPane.repaint();
    }

    static BufferedImage loadImage()
    {
        try {
            InputStream  in = new URL("plugin:org.jnode.desktop!/background.png").openStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[2048];
            int len = 0;
            while((len = in.read(buf)) > 0)
                out.write(buf, 0, len);
            ByteArrayInputStream bin = new ByteArrayInputStream(out.toByteArray());
            BufferedImage img = ImageIO.read(bin);
            BufferedImage ret = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
            img.copyData(ret.getRaster());
            return img;
        } catch (Throwable ex) {
            log.error("Error loading desktop background.", ex);
        }

        return null;
    }
}
