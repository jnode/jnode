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

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MenuBar;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.VMContainer;
import java.awt.event.WindowEvent;
import java.awt.peer.FramePeer;
import java.beans.PropertyVetoException;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

final class SwingFrame extends JInternalFrame implements ISwingPeer<Frame>,
        InternalFrameListener {

    private static final class SwingFrameContentPane extends JComponent {

        private Frame awtFrame;

        private SwingFramePeer swingPeer;

        public void initialize(Frame awtComponent, SwingFramePeer peer) {
            this.awtFrame = awtComponent;
            this.swingPeer = peer;
            awtFrame.invalidate();
        }

        /**
         * @see javax.swing.JComponent#paintChildren(java.awt.Graphics)
         */
        protected void paintChildren(Graphics g) {
            super.paintChildren(g);
            final Insets insets = swingPeer.getInsets();
            SwingToolkit.paintLightWeightChildren(awtFrame, g, insets.left,
                    insets.top);
        }

        // /**
        // * @see java.awt.Component#invalidate()
        // */
        // public void invalidate() {
        // super.invalidate();
        // if (awtFrame != null) {
        // awtFrame.invalidate();
        // }
        // }
        //
        // /**
        // * @see java.awt.Component#doLayout()
        // */
        // public void doLayout() {
        // if (awtFrame != null) {
        // awtFrame.doLayout();
        // }
        // super.doLayout();
        // }
        //
        // /**
        // * @see java.awt.Component#validate()
        // */
        // public void validate() {
        // super.validate();
        // if (awtFrame != null) {
        // awtFrame.validate();
        // }
        // }
        //
        @SuppressWarnings("deprecation")
        public void reshape(int x, int y, int width, int height) {
            super.reshape(x, y, width, height);
            if (!swingPeer.isReshapeInProgress) {
                Point p = awtFrame.isShowing() ? awtFrame.getLocationOnScreen()
                        : new Point();
                // Point p = awtFrame.getLocationOnScreen();
                Insets ins = swingPeer.getInsets();
                awtFrame.reshape(p.x + x, p.y, width + ins.left + ins.right,
                        height + ins.bottom + ins.top);
            }
        }
    }

    private static final class SwingFrameRootPane extends JRootPane {

        /**
         * @see javax.swing.JRootPane#createContentPane()
         */
        protected Container createContentPane() {
            return new SwingFrameContentPane();
        }
    }

    private final Frame awtComponent;

    private SwingFramePeer swingPeer;

    public SwingFrame(Frame awtFrame) {
        this.awtComponent = awtFrame;
//        setFocusCycleRoot(true);
//        final KeyboardFocusManager manager = KeyboardFocusManager
//                .getCurrentKeyboardFocusManager();
//        setFocusTraversalPolicy(manager.getDefaultFocusTraversalPolicy());
        addInternalFrameListener(this);
    }

    /**
     * @see javax.swing.JInternalFrame#createRootPane()
     */
    protected JRootPane createRootPane() {
        return new SwingFrameRootPane();
    }

    /**
     * @see org.jnode.awt.swingpeers.ISwingPeer#getAWTComponent()
     */
    public Frame getAWTComponent() {
        return awtComponent;
    }

    Frame getAwtFrame() {
        return awtComponent;
    }

    SwingFramePeer getSwingPeer() {
        return swingPeer;
    }

    void initialize(SwingFramePeer peer) {
        this.swingPeer = peer;
        ((SwingFrameContentPane) getContentPane()).initialize(awtComponent,
                peer);
    }

    /**
     * Fire a WindowEvent with a given id to the awtComponent.
     */
    private final void fireWindowEvent(int id) {
        //awtComponent.dispatchEvent(new WindowEvent(awtComponent, id));
        swingPeer.getToolkitImpl().postEvent(new WindowEvent(awtComponent, id));
    }

    /**
     * @see javax.swing.event.InternalFrameListener#internalFrameActivated(javax.swing.event.InternalFrameEvent)
     */
    public void internalFrameActivated(InternalFrameEvent event) {
        fireWindowEvent(WindowEvent.WINDOW_ACTIVATED);
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

    /**
     * @see java.awt.Component#invalidate()
     */
    public void invalidate() {
        super.invalidate();
        if (awtComponent != null) {
            awtComponent.invalidate();
        }
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
     * Pass an event onto the AWT component.
     * 
     * @see java.awt.Component#processEvent(java.awt.AWTEvent)
     */
    protected final void processEvent(AWTEvent event) {
        awtComponent.dispatchEvent(SwingToolkit.convertEvent(event,
                awtComponent));
    }

    /**
     * @see java.awt.Component#validate()
     */
    public void validate() {
        super.validate();
        if (awtComponent != null) {
            awtComponent.validate();
        }
    }
}

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Levente S\u00e1ntha
 */
final class SwingFramePeer extends SwingBaseWindowPeer<Frame, SwingFrame>
        implements FramePeer, ISwingContainerPeer {

    /**
     * Initialize this instance.
     */
    public SwingFramePeer(SwingToolkit toolkit, final JDesktopPane desktopPane,
            Frame awtFrame) {
        super(toolkit, awtFrame, new SwingFrame(awtFrame));
        jComponent.initialize(this);
        SwingToolkit.copyAwtProperties(awtFrame, this.jComponent);
        jComponent.getContentPane().setLayout(
                new SwingContainerLayout(awtFrame, this));
        jComponent.setLocation(awtFrame.getLocation());
        jComponent.setSize(awtFrame.getSize());
        setResizable(awtFrame.isResizable());
        jComponent.setIconifiable(true);
        jComponent.setMaximizable(true);
        jComponent.setClosable(true);
        try {
            jComponent.setIcon(awtFrame.getState() == Frame.ICONIFIED);
        } catch (PropertyVetoException x) {
        }
        setState(awtFrame.getState());
        jComponent.setTitle(awtFrame.getTitle());
        // frame.setIconImage(awtFrame.getIconImage());
        MenuBar mb = awtFrame.getMenuBar();
        if (mb != null)
            setMenuBar(mb);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                desktopPane.add(jComponent);
                desktopPane.setSelectedFrame(jComponent);
                jComponent.toFront();
                desktopPane.doLayout();
            }
        });
    }

    /**
     * @see org.jnode.awt.swingpeers.ISwingContainerPeer#addAWTComponent(java.awt.Component,
     *      javax.swing.JComponent)
     */
    public void addAWTComponent(Component awtComponent, JComponent peer) {
        jComponent.getContentPane().add(peer);
    }

    /**
     * @see java.awt.peer.ComponentPeer#getGraphics()
     */
    public Graphics getGraphics() {
        return jComponent.getGraphics();
    }

    /**
     * @see java.awt.peer.ContainerPeer#getInsets()
     */
    public Insets getInsets() {
        final Container contentPane = jComponent.getContentPane();
        if ((contentPane.getWidth() == 0) || (contentPane.getHeight() == 0)) {
            jComponent.doLayout();
            jComponent.getRootPane().doLayout();
        }
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
            System.out.println("cp.bounds: " + contentPane.getBounds());
        }
        return insets;
    }

    public Point getLocationOnScreen() {
        return jComponent.getLocation();
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
                component.invalidate();
            }
        });
    }

    /**
     * @see java.awt.peer.FramePeer#setResizable(boolean)
     */
    public void setResizable(boolean resizable) {
        jComponent.setResizable(resizable);
    }

    /**
     * @see java.awt.peer.FramePeer#setState(int)
     */
    public void setState(int state) {
    }

    /**
     * @see java.awt.peer.FramePeer#setTitle(java.lang.String)
     */
    public void setTitle(String title) {
        jComponent.setTitle(title);
    }

    public void setVisible(final boolean b) {
        SwingToolkit.invokeNowOrLater(new Runnable() {
            public void run() {
                if (b) {
                    jComponent.invalidate();
                    VMContainer.invalidateTree(component);
                    VMContainer.invalidateTree(jComponent);
                    jComponent.validate();
                }

                jComponent.setVisible(b);
                jComponent.repaint();
                toolkit.getAwtContext().getDesktop().repaint();
            }
        });
    }
}
