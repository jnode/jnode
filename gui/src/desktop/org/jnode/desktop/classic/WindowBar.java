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

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DesktopManager;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import org.apache.log4j.Logger;
import org.jnode.awt.JNodeAwtContext;
import org.jnode.awt.JNodeToolkit;

/**
 * @author Levente S\u00e1ntha
 */
public class WindowBar extends JPanel {
    private static final Logger log = Logger.getLogger(WindowBar.class);
    private final Map<JInternalFrame, FrameWrapper> wrappers;

    public WindowBar() {
        FlowLayout layout = new FlowLayout(FlowLayout.LEFT, 1, 1);
        setLayout(layout);
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        wrappers = new HashMap<JInternalFrame, FrameWrapper>();
    }

    public void addFrame(final JInternalFrame frame) {
        log.debug("addFrame " + frame.getTitle());
        final FrameWrapper wrapper = new FrameWrapper(frame);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                wrappers.put(frame, wrapper);
                add(wrapper);
                revalidate();
                repaint();
            }
        });
    }

    public void removeFrame(final JInternalFrame frame) {
        log.debug("removeFrame " + frame.getTitle());
        final FrameWrapper wrapper = wrappers.get(frame);
        if (wrapper != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    wrappers.remove(frame);
                    remove(wrapper);
                    revalidate();
                    repaint();
                }
            });
        }
    }

    private class FrameWrapper extends JButton {
        private final JInternalFrame frame;

        /**
         * @param iFrame the wrapped frame
         */
        public FrameWrapper(JInternalFrame iFrame) {
            this.frame = iFrame;
            this.setText(iFrame.getTitle());
            this.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    try {
                        DesktopManager desktopManager = frame.getDesktopPane().getDesktopManager();
                        if (frame.isIcon()) {
                            frame.setIcon(false);
                            desktopManager.deiconifyFrame(frame);
                            frame.setSelected(true);
                            desktopManager.activateFrame(frame);
                        } else if (frame.isSelected()) {
                            frame.setSelected(false);
                            desktopManager.deactivateFrame(frame);
                            frame.setIcon(true);
                            desktopManager.iconifyFrame(frame);
                        } else {
                            frame.setSelected(true);
                            desktopManager.activateFrame(frame);
                        }
                    } catch (PropertyVetoException ex) {
                        log.warn("", ex);
                    }
                }
            });
            this.frame.addInternalFrameListener(new InternalFrameListener() {
                public void internalFrameActivated(InternalFrameEvent event) {
                    FrameWrapper.this.setBackground(Color.WHITE);
                }

                public void internalFrameClosed(InternalFrameEvent event) {
                    remove(FrameWrapper.this);
                    removeFrame(FrameWrapper.this.frame);
                    final JNodeToolkit tk = JNodeToolkit.getJNodeToolkit();
                    final JNodeAwtContext ctx = tk.getAwtContext();
                    final JDesktopPane desktop = ctx.getDesktop();
                    desktop.remove(FrameWrapper.this.frame);
                    revalidate();
                    repaint();
                }

                public void internalFrameClosing(InternalFrameEvent event) {
                    remove(FrameWrapper.this);
                    removeFrame(FrameWrapper.this.frame);
                    final JNodeToolkit tk = JNodeToolkit.getJNodeToolkit();
                    final JNodeAwtContext ctx = tk.getAwtContext();
                    final JDesktopPane desktop = ctx.getDesktop();
                    JDesktopPane desk = frame.getDesktopPane();
                    desktop.remove(FrameWrapper.this.frame);
                    desk.repaint();
                }

                public void internalFrameDeactivated(InternalFrameEvent event) {
                    FrameWrapper.this.setBackground(Color.LIGHT_GRAY);
                }

                public void internalFrameDeiconified(InternalFrameEvent event) {
                    repaint();
                    frame.getDesktopPane().repaint();
                }

                public void internalFrameIconified(InternalFrameEvent event) {
                    repaint();
                    frame.getDesktopPane().repaint();
                }

                public void internalFrameOpened(InternalFrameEvent event) {
                    frame.requestFocus();
                    frame.repaint();
                    frame.getDesktopPane().repaint();
                }
            });
            final JPopupMenu frameActions = new JPopupMenu();
            JMenuItem minimize = new JMenuItem("Minimize");
            frameActions.add(minimize);
            minimize.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    try {
                        if (frame.isMaximum())
                            frame.setMaximum(false);
                        frame.setIcon(true);
                        frame.getDesktopPane().getDesktopManager().iconifyFrame(frame);
                    } catch (PropertyVetoException e) {
                        //ignore
                    }
                }
            });
            JMenuItem maximize = new JMenuItem("Maximize");
            frameActions.add(maximize);
            maximize.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    try {
                        if (frame.isIcon())
                            frame.setIcon(false);
                        frame.setMaximum(true);
                        frame.getDesktopPane().getDesktopManager().maximizeFrame(frame);
                    } catch (PropertyVetoException e) {
                        //ignore
                    }
                }
            });

            JMenuItem restore = new JMenuItem("Restore");
            frameActions.add(restore);
            restore.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    try {
                        if (frame.isIcon())
                            frame.setIcon(false);
                        if (frame.isMaximum())
                            frame.setMaximum(false);
                        frame.setSelected(true);
                        frame.getDesktopPane().getDesktopManager().activateFrame(frame);
                    } catch (PropertyVetoException e) {
                        //ignore
                    }
                }
            });

            JMenuItem close = new JMenuItem("Close");
            frameActions.add(close);
            close.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    try {
                        frame.setClosed(true);
                        frame.getDesktopPane().getDesktopManager().closeFrame(frame);
                    } catch (PropertyVetoException e) {
                        //ignore
                    }
                }
            });

            this.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent event) {
                    if (event.getButton() == MouseEvent.BUTTON2) {
                        if (frameActions.isShowing()) {
                            frameActions.setVisible(false);
                        } else {
                            Point p = FrameWrapper.this.getLocationOnScreen();
                            int h = frameActions.getPreferredSize().height;
                            frameActions.show(frame.getDesktopPane(), p.x, p.y - h);
                        }
                    }
                }
            });
        }

        public final JInternalFrame getFrame() {
            return frame;
        }
    }
}
