/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyVetoException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;
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
import org.jnode.awt.swingpeers.ISwingPeer;
import org.jnode.awt.swingpeers.SwingToolkit;

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
        if (frame instanceof ISwingPeer) {
            ISwingPeer isp = ((ISwingPeer) frame);
            Component comp = isp.getAWTComponent();
            if (!(comp instanceof Frame) || ((Frame) comp).isUndecorated()) {
                comp.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentHidden(ComponentEvent e) {
                        selectNextFrame(frame.getDesktopPane());
                    }
                });
                return;
            }
        }

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
        selectNextFrame(SwingToolkit.getJNodeToolkit().getAwtContext().getDesktop());
    }

    private void selectNextFrame(JDesktopPane desktop) {
        JInternalFrame[] frames = desktop.getAllFrames();
        JInternalFrame sel = null;
        int z = Integer.MAX_VALUE;
        for (int i = 0; i < frames.length; i++) {
            JInternalFrame f = frames[i];
            if (!f.isIcon() && f.isVisible()) {
                int fz = desktop.getComponentZOrder(f);
                if (fz > -1 && fz < z) {
                    z = fz;
                    sel = f;
                }
            }
        }
        try {
            if (sel != null)
                sel.setSelected(true);
        } catch (PropertyVetoException x) {
            //ignore
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
                        if (frame.isIcon()) {
                            frame.setIcon(false);
                            frame.setSelected(true);
                        } else if (frame.isSelected()) {
                            frame.setSelected(false);
                            frame.setIcon(true);
                            selectNextFrame(frame.getDesktopPane());
                        } else {
                            frame.setSelected(true);
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
                    //empty
                }

                public void internalFrameClosing(InternalFrameEvent event) {
                    final JDesktopPane desktop = FrameWrapper.this.frame.getDesktopPane();
                    removeFrame(FrameWrapper.this.frame);
                    desktop.remove(FrameWrapper.this.frame);
                    selectNextFrame(desktop);
                    desktop.repaint();
                }

                public void internalFrameDeactivated(InternalFrameEvent event) {
                    FrameWrapper.this.setBackground(Color.LIGHT_GRAY);
                }

                public void internalFrameDeiconified(InternalFrameEvent event) {
                    repaint();
                }

                public void internalFrameIconified(InternalFrameEvent event) {
                    repaint();
                }

                public void internalFrameOpened(InternalFrameEvent event) {
                    frame.requestFocus();
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
                        selectNextFrame(frame.getDesktopPane());
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
            frame.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (JInternalFrame.TITLE_PROPERTY.equals(evt.getPropertyName())) {
                        setText(frame.getTitle());
                    }
                }
            });
        }

        public final JInternalFrame getFrame() {
            return frame;
        }
    }
}
