/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.BufferCapabilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MenuBar;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.BufferCapabilities.FlipContents;
import java.awt.event.PaintEvent;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.VolatileImage;
import java.awt.peer.FramePeer;
import java.beans.PropertyVetoException;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JRootPane;

import org.apache.log4j.Logger;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class SwingFramePeer implements FramePeer, ISwingContainerPeer {

	private final Logger log = Logger.getLogger(getClass());

	private final SwingToolkit toolkit;

	final Frame awtFrame;

	private final SwingFrame frame;

	public SwingFramePeer(SwingToolkit toolkit, JDesktopPane desktopPane,
			Frame awtFrame) {

		this.awtFrame = awtFrame;
		this.toolkit = toolkit;
		this.frame = new SwingFrame(awtFrame);
		desktopPane.add(frame);

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
		frame.setMenuBar(new SwingMenuBarPeer(mb));
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
	 * @see java.awt.peer.WindowPeer#toBack()
	 */
	public void toBack() {
		frame.toBack();
	}

	/**
	 * @see java.awt.peer.WindowPeer#toFront()
	 */
	public void toFront() {
		frame.toFront();
	}

	/**
	 * @see java.awt.peer.ContainerPeer#beginLayout()
	 */
	public void beginLayout() {
	}

	/**
	 * @see java.awt.peer.ContainerPeer#beginValidate()
	 */
	public void beginValidate() {
	}

	/**
	 * @see java.awt.peer.ContainerPeer#endLayout()
	 */
	public void endLayout() {
	}

	/**
	 * @see java.awt.peer.ContainerPeer#endValidate()
	 */
	public void endValidate() {
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

	/**
	 * @see java.awt.peer.ContainerPeer#insets()
	 */
	public Insets insets() {
		return getInsets();
	}

	/**
	 * @see java.awt.peer.ContainerPeer#isPaintPending()
	 */
	public boolean isPaintPending() {
		return false;
	}

	/**
	 * @see java.awt.peer.ComponentPeer#canDetermineObscurity()
	 */
	public boolean canDetermineObscurity() {
		return false;
	}

	/**
	 * @see java.awt.peer.ComponentPeer#checkImage(java.awt.Image, int, int,
	 *      java.awt.image.ImageObserver)
	 */
	public int checkImage(Image img, int width, int height, ImageObserver ob) {
		return frame.checkImage(img, width, height, ob);
	}

	/**
	 * @see java.awt.peer.ComponentPeer#coalescePaintEvent(java.awt.event.PaintEvent)
	 */
	public void coalescePaintEvent(PaintEvent e) {
	}

	/**
	 * @see java.awt.peer.ComponentPeer#createBuffers(int,
	 *      java.awt.BufferCapabilities)
	 */
	public void createBuffers(int x, BufferCapabilities capabilities)
			throws AWTException {
	}

	/**
	 * @see java.awt.peer.ComponentPeer#createImage(java.awt.image.ImageProducer)
	 */
	public Image createImage(ImageProducer prod) {
		return createImage(prod);
	}

	/**
	 * @see java.awt.peer.ComponentPeer#createImage(int, int)
	 */
	public Image createImage(int width, int height) {
		return createImage(width, height);
	}

	/**
	 * @see java.awt.peer.ComponentPeer#createVolatileImage(int, int)
	 */
	public VolatileImage createVolatileImage(int width, int height) {
		return createVolatileImage(width, height);
	}

	/**
	 * @see java.awt.peer.ComponentPeer#destroyBuffers()
	 */
	public void destroyBuffers() {
	}

	/**
	 * @see java.awt.peer.ComponentPeer#disable()
	 */
	public void disable() {
		setEnabled(false);
	}

	/**
	 * @see java.awt.peer.ComponentPeer#dispose()
	 */
	public void dispose() {
		toolkit.onDisposeFrame();
	}

	/**
	 * @see java.awt.peer.ComponentPeer#enable()
	 */
	public void enable() {
		setEnabled(true);
	}

	/**
	 * @see java.awt.peer.ComponentPeer#flip(java.awt.BufferCapabilities.FlipContents)
	 */
	public void flip(FlipContents contents) {
	}

	/**
	 * @see java.awt.peer.ComponentPeer#getBackBuffer()
	 */
	public Image getBackBuffer() {
		return null;
	}

	/**
	 * @see java.awt.peer.ComponentPeer#getColorModel()
	 */
	public ColorModel getColorModel() {
		return frame.getColorModel();
	}

	/**
	 * @see java.awt.peer.ComponentPeer#getFontMetrics(java.awt.Font)
	 */
	public FontMetrics getFontMetrics(Font f) {
		return frame.getFontMetrics(f);
	}

	/**
	 * @see java.awt.peer.ComponentPeer#getGraphics()
	 */
	public Graphics getGraphics() {
		return frame.getGraphics();
	}

	/**
	 * @see java.awt.peer.ComponentPeer#getGraphicsConfiguration()
	 */
	public GraphicsConfiguration getGraphicsConfiguration() {
		return frame.getGraphicsConfiguration();
	}

	/**
	 * @see java.awt.peer.ComponentPeer#getLocationOnScreen()
	 */
	public Point getLocationOnScreen() {
		return frame.getLocationOnScreen();
	}

	/**
	 * @see java.awt.peer.ComponentPeer#getMinimumSize()
	 */
	public Dimension getMinimumSize() {
		return frame.getMinimumSize();
	}

	/**
	 * @see java.awt.peer.ComponentPeer#getPreferredSize()
	 */
	public Dimension getPreferredSize() {
		return frame.getPreferredSize();
	}

	/**
	 * @see java.awt.peer.ComponentPeer#getToolkit()
	 */
	public Toolkit getToolkit() {
		return frame.getToolkit();
	}

	/**
	 * @see java.awt.peer.ComponentPeer#handleEvent(java.awt.AWTEvent)
	 */
	public void handleEvent(AWTEvent e) {
	}

	/**
	 * @see java.awt.peer.ComponentPeer#handlesWheelScrolling()
	 */
	public boolean handlesWheelScrolling() {
		return false;
	}

	/**
	 * @see java.awt.peer.ComponentPeer#hide()
	 */
	public void hide() {
		setVisible(false);
	}

	/**
	 * @see java.awt.peer.ComponentPeer#isFocusable()
	 */
	public boolean isFocusable() {
		return frame.isFocusable();
	}

	/**
	 * @see java.awt.peer.ComponentPeer#isFocusTraversable()
	 */
	public boolean isFocusTraversable() {
		return frame.isFocusTraversable();
	}

	/**
	 * @see java.awt.peer.ComponentPeer#isObscured()
	 */
	public boolean isObscured() {
		return false;
	}

	/**
	 * @see java.awt.peer.ComponentPeer#minimumSize()
	 */
	public Dimension minimumSize() {
		return getMinimumSize();
	}

	/**
	 * @see java.awt.peer.ComponentPeer#paint(java.awt.Graphics)
	 */
	public void paint(Graphics graphics) {
		frame.paint(graphics);
	}

	/**
	 * @see java.awt.peer.ComponentPeer#preferredSize()
	 */
	public Dimension preferredSize() {
		return getPreferredSize();
	}

	/**
	 * @see java.awt.peer.ComponentPeer#prepareImage(java.awt.Image, int, int,
	 *      java.awt.image.ImageObserver)
	 */
	public boolean prepareImage(Image img, int width, int height,
			ImageObserver ob) {
		return frame.prepareImage(img, width, height, ob);
	}

	/**
	 * @see java.awt.peer.ComponentPeer#print(java.awt.Graphics)
	 */
	public void print(Graphics graphics) {
		frame.print(graphics);
	}

	/**
	 * @see java.awt.peer.ComponentPeer#repaint(long, int, int, int, int)
	 */
	public void repaint(long tm, int x, int y, int width, int height) {
		frame.repaint(tm, x, y, width, height);
	}

	/**
	 * @see java.awt.peer.ComponentPeer#requestFocus()
	 */
	public void requestFocus() {
		frame.requestFocus();
	}

	/**
	 * @see java.awt.peer.ComponentPeer#requestFocus(java.awt.Component,
	 *      boolean, boolean, long)
	 */
	public boolean requestFocus(Component source, boolean bool1, boolean bool2,
			long x) {
		return true;
	}

	/**
	 * @see java.awt.peer.ComponentPeer#reshape(int, int, int, int)
	 */
	public void reshape(int x, int y, int width, int height) {
		frame.reshape(x, y, width, height);
	}

	/**
	 * @see java.awt.peer.ComponentPeer#setBackground(java.awt.Color)
	 */
	public void setBackground(Color color) {
		frame.setBackground(color);
	}

	/**
	 * @see java.awt.peer.ComponentPeer#setBounds(int, int, int, int)
	 */
	public void setBounds(int x, int y, int width, int height) {
		frame.setBounds(x, y, width, height);
	}

	/**
	 * @see java.awt.peer.ComponentPeer#setCursor(java.awt.Cursor)
	 */
	public void setCursor(Cursor cursor) {
		frame.setCursor(cursor);
	}

	/**
	 * @see java.awt.peer.ComponentPeer#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		frame.setEnabled(enabled);
	}

	/**
	 * @see java.awt.peer.ComponentPeer#setEventMask(long)
	 */
	public void setEventMask(long mask) {
	}

	/**
	 * @see java.awt.peer.ComponentPeer#setFont(java.awt.Font)
	 */
	public void setFont(Font font) {
		frame.setFont(font);
	}

	/**
	 * @see java.awt.peer.ComponentPeer#setForeground(java.awt.Color)
	 */
	public void setForeground(Color color) {
		frame.setForeground(color);
	}

	/**
	 * @see java.awt.peer.ComponentPeer#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		frame.setVisible(visible);
	}

	/**
	 * @see java.awt.peer.ComponentPeer#show()
	 */
	public void show() {
		setVisible(true);
	}

	/**
	 * @see java.awt.peer.ComponentPeer#updateCursorImmediately()
	 */
	public void updateCursorImmediately() {
	}

	private static final class SwingFrame extends JInternalFrame {

		private Frame awtFrame;

		public SwingFrame(Frame awtFrame) {
			this.awtFrame = awtFrame;
			((SwingFrameContentPane)getContentPane()).setFrame(awtFrame);
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

		public void setFrame(Frame awtFrame) {
			this.awtFrame = awtFrame;
			awtFrame.invalidate();
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
	}
}
