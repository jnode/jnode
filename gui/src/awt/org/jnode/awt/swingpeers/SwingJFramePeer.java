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

package org.jnode.awt.swingpeers;

import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.JComponent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.InternalFrameEvent;
import java.awt.Window;
import java.awt.VMAwtAPI;
import java.awt.AWTEvent;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Component;
import java.awt.MenuBar;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.awt.peer.WindowPeer;
import java.awt.peer.FramePeer;

/**
 * @author Levente S\u00e1ntha
 */
abstract class SwingJBaseWindow<awtT extends Window, swingPeerT extends SwingJBaseWindow<awtT, swingPeerT>>
        extends JFrame implements ISwingPeer<awtT> {

    /** The AWT component this is a peer for */
    protected final awtT target;

    /** The swing peer implementation */
    private SwingJBaseWindowPeer<awtT, swingPeerT> swingPeer;

    /**
     * Initialize this instance.
     *
     * @param target
     */
    public SwingJBaseWindow(awtT target) {
        this.target = target;
    }

    /**
     * @see javax.swing.JInternalFrame#reshape(int, int, int, int)
     */
    public void reshape(int x, int y, int width, int height) {
        VMAwtAPI.setBoundsCallback(target, x, y, width, height);
        VMAwtAPI.invalidateTree(target);
        super.reshape(x, y, width, height);
        //validate();   <-- stack overflow
    }

    /**
     * @see org.jnode.awt.swingpeers.ISwingPeer#getAWTComponent()
     */
    public final awtT getAWTComponent() {
        return target;
    }

    /**
     * Pass an event onto the AWT component.
     *
     * @see java.awt.Component#processEvent(java.awt.AWTEvent)
     */
    protected final void processEvent(AWTEvent event) {
        target.dispatchEvent(SwingToolkit.convertEvent(event,
                target));
    }

    /**
     * Process an event within this swingpeer
     *
     * @param event
     */
    public final void processAWTEvent(AWTEvent event) {
        super.processEvent(event);
    }

    /**
     * @see org.jnode.awt.swingpeers.ISwingPeer#validatePeerOnly()
     */
    public final void validatePeerOnly() {
        super.validate();
        doLayout();
        getRootPane().doLayout();
    }

    /**
     * @see javax.swing.JInternalFrame#createRootPane()
     */
    protected final JRootPane createRootPane() {
        return new RootPane();
    }

    /**
     * Gets the peer implementation.
     */
    final SwingJBaseWindowPeer<awtT, swingPeerT> getSwingPeer() {
        return swingPeer;
    }

    final void initialize(SwingJBaseWindowPeer<awtT, swingPeerT> swingPeer) {
        this.swingPeer = swingPeer;
        ((ContentPane) getContentPane()).initialize(target,
                swingPeer);
    }

    /**
     * @see java.awt.Component#invalidate()
     */
    public final void invalidate() {
        super.invalidate();
        if (target != null) {
            target.invalidate();
        }
    }

    /**
     * @see java.awt.Component#validate()
     */
    public final void validate() {
        super.validate();
        if (target != null) {
            target.validate();
        }
    }

    private final class ContentPane extends JComponent {

        private awtT target;

        private SwingJBaseWindowPeer swingPeer;

        public void initialize(awtT target, SwingJBaseWindowPeer<awtT, swingPeerT> swingPeer) {
            this.target = target;
            this.swingPeer = swingPeer;
            target.invalidate();
        }

        /**
         * @see javax.swing.JComponent#paintChildren(java.awt.Graphics)
         */
        protected void paintChildren(Graphics g) {
            super.paintChildren(g);
            final Insets insets = swingPeer.getInsets();
            SwingToolkit.paintLightWeightChildren(target, g, insets.left,
                    insets.top);
        }

        @SuppressWarnings("deprecation")
        public void reshape(int x, int y, int width, int height) {
            super.reshape(x, y, width, height);
            if (!swingPeer.isReshapeInProgress) {
                Point p = target.isShowing() ? target.getLocationOnScreen()
                        : new Point();
                // Point p = awtFrame.getLocationOnScreen();
                Insets ins = swingPeer.getInsets();
                target.reshape(p.x + x, p.y, width + ins.left + ins.right,
                        height + ins.bottom + ins.top);
            }
        }
    }

    private final class RootPane extends JRootPane {

        /**
         * @see javax.swing.JRootPane#createContentPane()
         */
        protected Container createContentPane() {
            return new ContentPane();
        }
    }
}


final class SwingJFrame extends SwingJBaseWindow<Frame, SwingJFrame> {

    public SwingJFrame(Frame awtFrame) {
        super(awtFrame);
    }

}

abstract class SwingJBaseWindowPeer<awtT extends Window, swingPeerT extends SwingJBaseWindow<awtT, swingPeerT>>
        extends SwingContainerPeer<awtT, swingPeerT> implements WindowPeer {

    private final WindowEventDispatcher eventDispatcher;

    public SwingJBaseWindowPeer(SwingToolkit toolkit, awtT window,
            swingPeerT jComponent) {
        super(toolkit, window, jComponent);
        jComponent.initialize(this);
        jComponent.getContentPane().setLayout(new SwingContainerLayout(target, this));
        jComponent.setLocation(target.getLocation());
        jComponent.setSize(target.getSize());
        this.eventDispatcher = new WindowEventDispatcher();
        //jComponent.addInternalFrameListener(eventDispatcher);
    }

    /**
     * Add this window to the desktop.
     */
    /*
    protected final void addToDesktop() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final JDesktopPane desktop = toolkit.getAwtContext().getDesktop();
                desktop.add(jComponent);
                desktop.setSelectedFrame(jComponent);
                jComponent.toFront();
                desktop.doLayout();
            }
        });
    }
    */

    /**
     * @see org.jnode.awt.swingpeers.ISwingContainerPeer#addAWTComponent(java.awt.Component,
     *      javax.swing.JComponent)
     */
    public final void addAWTComponent(Component awtComponent, JComponent peer) {
        jComponent.getContentPane().add(peer);
    }

    public final Point getLocationOnScreen() {
        return jComponent.getLocation();
    }

    /**
     * @see java.awt.peer.ComponentPeer#getGraphics()
     */
    public final Graphics getGraphics() {
        return jComponent.getGraphics();
    }

    /**
     * @see java.awt.peer.ContainerPeer#getInsets()
     */
    public final Insets getInsets() {
        final Container contentPane = jComponent.getContentPane();
        // if ((contentPane.getWidth() == 0) || (contentPane.getHeight() == 0))
        // {
        // jComponent.doLayout();
        // jComponent.getRootPane().doLayout();
        // }
        final int cpWidth = contentPane.getWidth();
        final int cpHeight = contentPane.getHeight();
        final Insets insets;
        if ((cpWidth > 0) && (cpHeight > 0)) {
            insets = new Insets(0, 0, 0, 0);
            Component c = contentPane;
            while (c != jComponent) {
                insets.left += c.getX();
                insets.top += c.getY();
                c = c.getParent();
            }
            final int dw = jComponent.getWidth() - contentPane.getWidth();
            final int dh = jComponent.getHeight() - contentPane.getHeight();
            insets.right = dw - insets.left;
            insets.bottom = dh - insets.top;
        } else {
            insets = jComponent.getInsets();
        }
        return insets;
    }

    /**
     * Sets the resizable flag of this window.
     * @param resizeable
     */
    public final void setResizable(boolean resizeable) {
        jComponent.setResizable(resizeable);
    }

    /**
     * Sets the title of this window
     * @param title
     */
    public void setTitle(String title) {
        jComponent.setTitle(title);
    }

    public final void dispose() {
        jComponent.dispose();
        //toolkit.onDisposeFrame(this);
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
        getToolkitImpl().postEvent(new WindowEvent(target, id));
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


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Levente S\u00e1ntha
 */
final class SwingJFramePeer extends SwingJBaseWindowPeer<Frame, SwingJFrame>
        implements FramePeer, ISwingContainerPeer {

    /**
     * Initialize this instance.
     */
    public SwingJFramePeer(SwingToolkit toolkit, Frame target) {
        super(toolkit, target, new SwingJFrame(target));
        setResizable(target.isResizable());
        //jComponent.setIconifiable(true);
        //jComponent.setMaximizable(true);
        //jComponent.setClosable(true);
        //try {
          //  jComponent.setIcon(target.getState() == Frame.ICONIFIED);
        //} catch (PropertyVetoException x) {
        //}
        setState(target.getState());
        jComponent.setTitle(target.getTitle());
        // frame.setIconImage(awtFrame.getIconImage());
        MenuBar mb = target.getMenuBar();
        if (mb != null) {
            setMenuBar(mb);
        }

        //addToDesktop();
    }

    /**
     * @see java.awt.peer.FramePeer#getState()
     */
    public int getState() {
        return -1;
    }

    /**
     * @see java.awt.peer.FramePeer#setIconImage(java.awt.Image)
     */
    public void setIconImage(Image image) {
    }

    /**
     * @see java.awt.peer.FramePeer#setMaximizedBounds(java.awt.Rectangle)
     */
    public void setMaximizedBounds(Rectangle r) {
    }

    /**
     * @see java.awt.peer.FramePeer#setMenuBar(java.awt.MenuBar)
     */
    @SuppressWarnings("deprecation")
    public void setMenuBar(final MenuBar mb) {
        SwingToolkit.invokeNowOrLater(new Runnable() {
            public void run() {
                mb.addNotify();
                jComponent
                        .setJMenuBar(((SwingMenuBarPeer) mb.getPeer()).jComponent);
                target.invalidate();
            }
        });
    }
    /**
     * @see java.awt.peer.FramePeer#setState(int)
     */
    public void setState(int state) {
        //TODO implement it
    }

    public void setBoundsPrivate(int x, int y, int width, int height) {
        //TODO implement it
    }
}
