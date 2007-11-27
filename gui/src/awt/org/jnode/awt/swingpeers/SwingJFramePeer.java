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
 
package org.jnode.awt.swingpeers;

import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.JComponent;
import java.awt.*;
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

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (target != null) {
            target.paint(g);
        }
    }

    @Override
    public void update(Graphics g) {
        super.update(g);
        if (target != null) {
            target.update(g);
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
            ContentPane p = new ContentPane();
            p.setName(this.getName() + ".contentPane");
            p.setLayout(new BorderLayout());
            return p;
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

    public SwingJBaseWindowPeer(SwingToolkit toolkit, awtT window,
            swingPeerT jComponent) {
        super(toolkit, window, jComponent);
        jComponent.initialize(this);
        jComponent.getContentPane().setLayout(new SwingContainerLayout(targetComponent, this));
        jComponent.setLocation(targetComponent.getLocation());
        jComponent.setSize(targetComponent.getSize());
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
        //toolkit.onDisposeFrame(this);
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

    public void updateAlwaysOnTop() {
        //TODO implement it
    }

    public boolean requestWindowFocus() {
        return peerComponent.requestFocusInWindow();
    }

    public void setAlwaysOnTop(boolean alwaysOnTop) {
        //TODO implement it
    }

    public void updateFocusableWindowState() {
        //TODO implement it
    }

    public void setModalBlocked(Dialog blocker, boolean blocked) {
        //TODO implement it
    }

    public void updateMinimumSize() {
        //TODO implement it
    }

    public void updateIconImages() {
        //TODO implement it
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
        setState(target.getState());
        peerComponent.setTitle(target.getTitle());
        MenuBar mb = target.getMenuBar();
        if (mb != null) {
            setMenuBar(mb);
        }
    }

    /**
     * @see java.awt.peer.FramePeer#setIconImage(java.awt.Image)
     */
    public void setIconImage(Image image) {
        peerComponent.setIconImage(image);
    }

    /**
     * @see java.awt.peer.FramePeer#setMaximizedBounds(java.awt.Rectangle)
     */
    public void setMaximizedBounds(Rectangle r) {
        peerComponent.setMaximizedBounds(r);
    }

    /**
     * @see java.awt.peer.FramePeer#setMenuBar(java.awt.MenuBar)
     */
    @SuppressWarnings("deprecation")
    public void setMenuBar(final MenuBar mb) {
        SwingToolkit.invokeNowOrLater(new Runnable() {
            public void run() {
                mb.addNotify();
                peerComponent
                        .setJMenuBar(((SwingMenuBarPeer) mb.getPeer()).jComponent);
                targetComponent.invalidate();
            }
        });
    }
    /**
     * @see java.awt.peer.FramePeer#setState(int)
     */
    public void setState(int state) {
        peerComponent.setState(state);
    }

    /**
     * @see java.awt.peer.FramePeer#getState()
     */
    public int getState() {
        return peerComponent.getState();
    }

    public void setBoundsPrivate(int x, int y, int width, int height) {

    }


    //jnode openjdk
    public Rectangle getBoundsPrivate() {
        //todo implement it
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
