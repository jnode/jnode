/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import org.apache.log4j.Logger;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JRootPane;
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

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Levente Sántha
 */
final class SwingFramePeer extends SwingWindowPeer implements FramePeer, ISwingContainerPeer {

	private final Logger log = Logger.getLogger(getClass());
	private final SwingFrame frame;

	public SwingFramePeer(SwingToolkit toolkit, JDesktopPane desktopPane, Frame awtFrame) {
        super(toolkit, awtFrame, new SwingFrame());
		frame = (SwingFrame) jComponent;
        frame.initialize(awtFrame, this);
		SwingToolkit.copyAwtProperties(awtFrame, this.frame);
		frame.getContentPane().setLayout(new SwingFrameLayout(this));
		frame.setLocation(awtFrame.getLocation());
		frame.setSize(awtFrame.getSize());
		setResizable(awtFrame.isResizable());
		frame.setIconifiable(true);
		frame.setMaximizable(true);
		frame.setClosable(true);
		try {
			frame.setIcon(awtFrame.getState() == Frame.ICONIFIED);
		} catch (PropertyVetoException x) {
		}
		setState(awtFrame.getState());
		frame.setTitle(awtFrame.getTitle());
		//frame.setIconImage(awtFrame.getIconImage());
		setMenuBar(awtFrame.getMenuBar());
        desktopPane.add(frame);
        desktopPane.setSelectedFrame(frame);
        frame.toFront();
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
        SwingMenuBarPeer mb_peer = new SwingMenuBarPeer((SwingToolkit) toolkit, mb);
		frame.setJMenuBar((JMenuBar) mb_peer.jComponent);
	}

	/**
	 * @see java.awt.peer.FramePeer#setResizable(boolean)
	 */
	public void setResizable(boolean resizable) {
		frame.setResizable(resizable);
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
		frame.setTitle(title);
	}

	/**
	 * @see org.jnode.awt.swingpeers.ISwingContainerPeer#addAWTComponent(java.awt.Component,
	 *      javax.swing.JComponent)
	 */
	public void addAWTComponent(Component awtComponent, JComponent peer) {
		frame.getContentPane().add(peer);
	}

	/**
	 * @see java.awt.peer.ContainerPeer#getInsets()
	 */
	public Insets getInsets() {
		final Container contentPane = frame.getContentPane();
		final int cpWidth = contentPane.getWidth();
		final int cpHeight = contentPane.getHeight();
		final Insets insets;
		if ((cpWidth > 0) && (cpHeight > 0)) {
			insets = new Insets(0, 0, 0, 0);
			Component c = contentPane;
			while (c != frame) {
				insets.left += c.getX();
				insets.top += c.getY();
				c = c.getParent();
			}
			final int dw = frame.getWidth() - contentPane.getWidth();
			final int dh = frame.getHeight() - contentPane.getHeight();
			insets.right = dw - insets.left;
			insets.bottom = dh - insets.top;
		} else {
			insets = frame.getInsets();
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
        frame.dispose();
		((SwingToolkit)toolkit).onDisposeFrame();
	}

	/**
	 * @see java.awt.peer.ComponentPeer#getGraphics()
	 */
	public Graphics getGraphics() {
        Insets ins = getInsets();
        Graphics g = frame.getGraphics();
        g.translate(ins.left, ins.top);
		return g;
	}

	/**
	 * @see java.awt.peer.ComponentPeer#paint(java.awt.Graphics)
	 */
	public void paint(Graphics graphics) {
		frame.paint(graphics);
	}

    // Events
    public void handleEvent(AWTEvent event) {
        //super.handleEvent(event);
    }

	static final class SwingFrame extends JInternalFrame {
		private Frame awtFrame;
        private SwingFramePeer swingPeer;

        private void initialize(Frame awtFrame, SwingFramePeer peer) {
            this.awtFrame = awtFrame;
            this.swingPeer = peer;
			((SwingFrameContentPane) getContentPane()).initialize(awtFrame, peer);
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
			if (awtFrame != null) {
				awtFrame.invalidate();
			}
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

        Frame getAwtFrame() {
            return awtFrame;
        }

        SwingFramePeer getSwingPeer() {
            return swingPeer;
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
