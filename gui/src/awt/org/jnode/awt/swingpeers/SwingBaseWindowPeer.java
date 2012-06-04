/*
 * $Id$
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.awt.swingpeers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.InvocationEvent;
import java.awt.event.WindowEvent;
import java.awt.peer.WindowPeer;
import java.beans.PropertyVetoException;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import org.jnode.awt.JNodeToolkit;
import sun.awt.AppContext;
import sun.awt.SunToolkit;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
abstract class SwingBaseWindowPeer<awtT extends Window, swingPeerT extends SwingBaseWindow<awtT, swingPeerT>>
    extends SwingContainerPeer<awtT, swingPeerT> implements WindowPeer {

    private final WindowEventDispatcher eventDispatcher;

    public SwingBaseWindowPeer(SwingToolkit toolkit, awtT window,
                               swingPeerT jComponent) {
        super(toolkit, window, jComponent);
        jComponent.initialize(this);
        jComponent.getContentPane().setLayout(new SwingContainerLayout(targetComponent, this));
        jComponent.setLocation(targetComponent.getLocation());
        jComponent.setSize(targetComponent.getSize());
        this.eventDispatcher = new WindowEventDispatcher();
        jComponent.addInternalFrameListener(eventDispatcher);
        jComponent.validatePeerOnly();
        if (!window.isBackgroundSet()) {
            Color bg = peerComponent.getBackground();
            if (bg == null) bg = UIManager.getColor("window");
            if (bg == null) bg = UIManager.getColor("control");
            if (bg == null) bg = Color.GRAY;
            window.setBackground(bg);
        }
    }

    /**
     * Add this window to the desktop.
     */
    protected final void addToDesktop() {
        Runnable run = new Runnable() {
            public void run() {
                final JDesktopPane desktop = toolkit.getAwtContext().getDesktop();
                desktop.add(peerComponent);
                try {
                    peerComponent.setSelected(true);
                    desktop.getDesktopManager().activateFrame(peerComponent);
                    peerComponent.toFront();
                    desktop.doLayout();
                } catch (PropertyVetoException x) {
                    log.warn("", x);
                }
            }
        };

        final JDesktopPane desktop = toolkit.getAwtContext().getDesktop();
        AppContext ac = SunToolkit.targetToAppContext(desktop);
        if (ac != null) {
            EventQueue eq = (EventQueue) ac.get(AppContext.EVENT_QUEUE_KEY);
            if (eq != null) {
                eq.postEvent(new InvocationEvent(Toolkit.getDefaultToolkit(), run));
                return;
            }
        }
        //shouldn't get here
        throw new RuntimeException("Desktop event queue not found!");
    }

    /**
     * @see org.jnode.awt.swingpeers.ISwingContainerPeer#addAWTComponent(java.awt.Component,
     *      javax.swing.JComponent)
     */
    public final void addAWTComponent(Component awtComponent, JComponent peer) {
        peerComponent.getContentPane().add(peer);
    }

    public final Point getLocationOnScreen() {
        return peerComponent.getLocation();
    }

    /**
     * @see java.awt.peer.ComponentPeer#getGraphics()
     */
    public final Graphics getGraphics() {
        //TODO review this
        //return peerComponent.getGraphics();
        return super.getGraphics();
    }

    /**
     * @see java.awt.peer.ContainerPeer#getInsets()
     */
    public final Insets getInsets() {
        final Insets insets = new Insets(0, 0, 0, 0);
        Object obj = this;   //javac detects error for: this instanceof SwingWindowPeer 
        if (obj instanceof SwingWindowPeer) {
            return insets;
        } else {
            final Container contentPane = peerComponent.getContentPane();
            Component c = contentPane;
            while (c != peerComponent) {
                insets.left += c.getX();
                insets.top += c.getY();
                c = c.getParent();
            }
            final int dw = peerComponent.getWidth() - contentPane.getWidth();
            final int dh = peerComponent.getHeight() - contentPane.getHeight();
            insets.right = dw - insets.left;
            insets.bottom = dh - insets.top;
            return insets;
        }
    }

    /**
     * Sets the resizable flag of this window.
     *
     * @param resizeable
     */
    public final void setResizable(boolean resizeable) {
        peerComponent.setResizable(resizeable);
    }

    /**
     * Sets the title of this window
     *
     * @param title
     */
    public void setTitle(String title) {
        peerComponent.setTitle(title);
    }

    public final void dispose() {
        peerComponent.dispose();
        toolkit.onDisposeFrame(this);
    }

    public void toBack() {
        SwingToolkit.invokeNowOrLater(new Runnable() {
            public void run() {
                peerComponent.toBack();
            }
        });
    }

    public void toFront() {
        SwingToolkit.invokeNowOrLater(new Runnable() {
            public void run() {
                peerComponent.toFront();
                try {
                    peerComponent.setSelected(true);
                } catch (PropertyVetoException x) {
                    log.warn(x);
                }
            }
        });
    }

    /**
     * Fire a WindowEvent with a given id to the awtComponent.
     */
    private final void fireWindowEvent(int id) {
        JNodeToolkit.postToTarget(new WindowEvent(targetComponent, id), targetComponent);
    }

    public void updateAlwaysOnTop() {
        //TODO implement it
    }

    public boolean requestWindowFocus() {
        //TODO implement it
        return peerComponent.requestFocusInWindow();
    }

    public void updateIconImages() {
        //TODO implement it
    }


    public void updateMinimumSize() {
        //TODO implement it
    }


    public void setModalBlocked(Dialog blocker, boolean blocked) {
        //TODO implement it
    }

    public void updateFocusableWindowState() {
        //TODO implement it
    }

    public void setAlwaysOnTop(boolean alwaysOnTop) {
        //TODO implement it
    }

    private class WindowEventDispatcher implements InternalFrameListener {

        /**
         * @see javax.swing.event.InternalFrameListener#internalFrameActivated(javax.swing.event.InternalFrameEvent)
         */
        public void internalFrameActivated(InternalFrameEvent event) {
            fireWindowEvent(WindowEvent.WINDOW_GAINED_FOCUS);
        }

        /**
         * @see javax.swing.event.InternalFrameListener#internalFrameClosed(javax.swing.event.InternalFrameEvent)
         */
        public void internalFrameClosed(InternalFrameEvent event) {
            fireWindowEvent(WindowEvent.WINDOW_CLOSED);
        }

        /**
         * @see javax.swing.event.InternalFrameListener#internalFrameClosing(javax.swing.event.InternalFrameEvent)
         */
        public void internalFrameClosing(InternalFrameEvent event) {
            fireWindowEvent(WindowEvent.WINDOW_CLOSING);
        }

        /**
         * @see javax.swing.event.InternalFrameListener#internalFrameDeactivated(javax.swing.event.InternalFrameEvent)
         */
        public void internalFrameDeactivated(InternalFrameEvent event) {
            fireWindowEvent(WindowEvent.WINDOW_DEACTIVATED);
        }

        /**
         * @see javax.swing.event.InternalFrameListener#internalFrameDeiconified(javax.swing.event.InternalFrameEvent)
         */
        public void internalFrameDeiconified(InternalFrameEvent event) {
            fireWindowEvent(WindowEvent.WINDOW_DEICONIFIED);
        }

        /**
         * @see javax.swing.event.InternalFrameListener#internalFrameIconified(javax.swing.event.InternalFrameEvent)
         */
        public void internalFrameIconified(InternalFrameEvent event) {
            fireWindowEvent(WindowEvent.WINDOW_ICONIFIED);
        }

        /**
         * @see javax.swing.event.InternalFrameListener#internalFrameOpened(javax.swing.event.InternalFrameEvent)
         */
        public void internalFrameOpened(InternalFrameEvent event) {
            fireWindowEvent(WindowEvent.WINDOW_OPENED);
        }
    }
}
