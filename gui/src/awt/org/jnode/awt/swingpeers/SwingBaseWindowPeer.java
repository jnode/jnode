/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.peer.WindowPeer;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

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
    }

    /**
     * Add this window to the desktop.
     */
    protected final void addToDesktop() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final JDesktopPane desktop = toolkit.getAwtContext().getDesktop();
                desktop.add(peerComponent);
                desktop.setSelectedFrame(peerComponent);
                peerComponent.toFront();
                desktop.doLayout();
            }
        });
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
        return peerComponent.getGraphics();
    }

    /**
     * @see java.awt.peer.ContainerPeer#getInsets()
     */
    public final Insets getInsets() {
        final Container contentPane = peerComponent.getContentPane();
        // if ((contentPane.getWidth() == 0) || (contentPane.getHeight() == 0))
        // {
        // peerComponent.doLayout();
        // peerComponent.getRootPane().doLayout();
        // }
        final int cpWidth = contentPane.getWidth();
        final int cpHeight = contentPane.getHeight();
        final Insets insets;
        if ((cpWidth > 0) && (cpHeight > 0)) {
            insets = new Insets(0, 0, 0, 0);
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
        } else {
            insets = peerComponent.getInsets();
        }
        return insets;
    }

    /**
     * Sets the resizable flag of this window.
     * @param resizeable
     */
    public final void setResizable(boolean resizeable) {
        peerComponent.setResizable(resizeable);
    }

    /**
     * Sets the title of this window
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
            }
        });
    }

    /**
     * Fire a WindowEvent with a given id to the awtComponent.
     */
    private final void fireWindowEvent(int id) {
        getToolkitImpl().postEvent(new WindowEvent(targetComponent, id));
    }

    public void updateAlwaysOnTop() {
        //TODO implement it
    }

    public boolean requestWindowFocus() {
        //TODO implement it
        return false;
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
