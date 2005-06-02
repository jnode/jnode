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
import java.awt.peer.FramePeer;
import java.beans.PropertyVetoException;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JRootPane;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Levente S\u00e1ntha
 */
final class SwingFramePeer extends SwingBaseWindowPeer<Frame, SwingFrame> implements FramePeer,
        ISwingContainerPeer {

	public SwingFramePeer(SwingToolkit toolkit, JDesktopPane desktopPane, Frame awtFrame) {
        super(toolkit, awtFrame, new SwingFrame(awtFrame));
        jComponent.initialize(this);
		SwingToolkit.copyAwtProperties(awtFrame, this.jComponent);
        jComponent.getContentPane().setLayout(new SwingFrameLayout(this));
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
		//frame.setIconImage(awtFrame.getIconImage());
		setMenuBar(awtFrame.getMenuBar());
        desktopPane.add(jComponent);
        desktopPane.setSelectedFrame(jComponent);
        jComponent.toFront();
        desktopPane.doLayout();
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
	public void setMenuBar(MenuBar mb) {
        SwingMenuBarPeer mb_peer = new SwingMenuBarPeer(toolkit, mb);
        jComponent.setJMenuBar(mb_peer.jComponent);
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

	/**
	 * @see org.jnode.awt.swingpeers.ISwingContainerPeer#addAWTComponent(java.awt.Component,
	 *      javax.swing.JComponent)
	 */
	public void addAWTComponent(Component awtComponent, JComponent peer) {
        jComponent.getContentPane().add(peer);
	}

	/**
	 * @see java.awt.peer.ContainerPeer#getInsets()
	 */
	public Insets getInsets() {
		final Container contentPane = jComponent.getContentPane();
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

    public Point getLocationOnScreen() {
        return jComponent.getLocation();
    }

	/**
	 * @see java.awt.peer.ComponentPeer#dispose()
	 */
	public void dispose() {
        jComponent.dispose();
		toolkit.onDisposeFrame();
	}

	/**
	 * @see java.awt.peer.ComponentPeer#getGraphics()
	 */
	public Graphics getGraphics() {
        Insets ins = getInsets();
        Graphics g = jComponent.getGraphics();
        g.translate(ins.left, ins.top);
		return g;
	}
}

final class SwingFrame extends JInternalFrame implements ISwingPeer<Frame> {
    private final Frame awtComponent;
    private SwingFramePeer swingPeer;
    
    public SwingFrame(Frame awtFrame) {
        this.awtComponent = awtFrame;
    }

    /**
     * @see org.jnode.awt.swingpeers.ISwingPeer#getAWTComponent()
     */
    public Frame getAWTComponent() {
        return awtComponent;
    }

    void initialize(SwingFramePeer peer) {
        this.swingPeer = peer;
        ((SwingFrameContentPane) getContentPane()).initialize(awtComponent, peer);
    }

    /**
     * @see javax.swing.JInternalFrame#createRootPane()
     */
    protected JRootPane createRootPane() {
        return new SwingFrameRootPane();
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
     * @see java.awt.Component#validate()
     */
    public void validate() {
        if (awtComponent != null) {
            awtComponent.validate();
        }
        super.validate();
    }

    Frame getAwtFrame() {
        return awtComponent;
    }

    SwingFramePeer getSwingPeer() {
        return swingPeer;
    }
    
    /**
     * Pass an event onto the AWT component.
     * @see java.awt.Component#processEvent(java.awt.AWTEvent)
     */
    protected final void processEvent(AWTEvent event) {
        awtComponent.dispatchEvent(event);
    }
    
    /**
     * Process an event within this swingpeer
     * @param event
     */
    public final void processAWTEvent(AWTEvent event) {
        super.processEvent(event);
    }

    private static final class SwingFrameRootPane extends JRootPane {

        /**
         * @see javax.swing.JRootPane#createContentPane()
         */
        protected Container createContentPane() {
            return new SwingFrameContentPane();
        }
    }

    private static final class SwingFrameContentPane extends JComponent {

        private Frame awtFrame;
        private SwingFramePeer swingPeer;

        public void initialize(Frame awtComponent, SwingFramePeer peer) {
            this.awtFrame = awtComponent;
            this.swingPeer = peer;
            awtComponent.invalidate();
        }

        /**
         * @see java.awt.Component#invalidate()
         */
        public void invalidate() {
            super.invalidate();
            if (awtFrame != null) {
                awtFrame.invalidate();
            }
        }

        /**
         * @see java.awt.Component#doLayout()
         */
        public void doLayout() {
            if (awtFrame != null) {
                awtFrame.doLayout();
            }
            super.doLayout();
        }

        /**
         * @see java.awt.Component#validate()
         */
        public void validate() {
            if (awtFrame != null) {
                awtFrame.validate();
            }
            super.validate();
        }

        @SuppressWarnings("deprecation")
        public void reshape(int x, int y, int width, int height) {
            super.reshape(x, y, width, height);
            //TODO fix it
            /*
            if (awtFrame.isVisible()) {
                Point p = awtFrame.getLocationOnScreen();
                Insets ins = swingPeer.getInsets();
                awtFrame.reshape(p.x + x, p.y + y, width + ins.left + ins.right, height + ins.top + ins.bottom);
            }*/
            if (!swingPeer.isReshapeInProgress) {
                Insets ins = swingPeer.getInsets();
                awtFrame.reshape(x + x, y + y, width + ins.left + ins.right, height + ins.top + ins.bottom);
            }
        }

        public void setVisible(boolean v) {
            super.setVisible(v);
            if(!swingPeer.isSetVisibleInProgress)
                awtFrame.setVisible(v);
        }


        /**
         * @see javax.swing.JComponent#paintChildren(java.awt.Graphics)
         */
        protected void paintChildren(Graphics g) {
            super.paintChildren(g);
            final Insets insets = swingPeer.getInsets();
            SwingToolkit.paintLightWeightChildren(awtFrame, g, insets.left, insets.top);
        }
    }
}

