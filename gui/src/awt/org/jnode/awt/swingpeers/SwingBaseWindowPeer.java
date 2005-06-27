/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.peer.WindowPeer;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
abstract class SwingBaseWindowPeer<awtT extends Window, peerT extends JInternalFrame>
        extends SwingContainerPeer<awtT, peerT> implements WindowPeer {

    public SwingBaseWindowPeer(SwingToolkit toolkit, awtT window,
            peerT jComponent) {
        super(toolkit, window, jComponent);
        jComponent.addInternalFrameListener(new WindowEventDispatcher());
    }

    public final void dispose() {
        jComponent.dispose();
        toolkit.onDisposeFrame(this);
    }

    public void toBack() {
        SwingToolkit.invokeNowOrLater(new Runnable() {
            public void run() {
                jComponent.toBack();
            }
        });
    }

    public void toFront() {
        SwingToolkit.invokeNowOrLater(new Runnable() {
            public void run() {
                jComponent.toFront();
            }
        });
    }

    /**
     * Fire a WindowEvent with a given id to the awtComponent.
     */
    private final void fireWindowEvent(int id) {
        getToolkitImpl().postEvent(new WindowEvent(component, id));
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
