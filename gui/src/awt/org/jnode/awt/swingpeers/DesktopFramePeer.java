/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.BufferCapabilities;
import java.awt.Color;
import java.awt.Component;
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
import java.awt.BufferCapabilities.FlipContents;
import java.awt.event.PaintEvent;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.VolatileImage;
import java.awt.peer.FramePeer;

import org.apache.log4j.Logger;
import org.jnode.awt.JNodeGenericPeer;
import org.jnode.awt.JNodeGraphics;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class DesktopFramePeer extends JNodeGenericPeer implements FramePeer {

    private final SwingToolkit toolkit;
    private final Logger log = Logger.getLogger(getClass());
    private static final Point TOP_LEFT = new Point(0, 0);
    private Insets insets;

    /**
     * Initialize 
     * @param toolkit
     * @param frame
     */
    public DesktopFramePeer(SwingToolkit toolkit, Frame frame) {
        super(toolkit, frame);
        this.toolkit = toolkit;
    }

    /**
     * @see java.awt.peer.FramePeer#getState()
     */
    public int getState() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see java.awt.peer.FramePeer#setIconImage(java.awt.Image)
     */
    public void setIconImage(Image image) {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.FramePeer#setMaximizedBounds(java.awt.Rectangle)
     */
    public void setMaximizedBounds(Rectangle r) {
        // Ignore
    }

    /**
     * @see java.awt.peer.FramePeer#setMenuBar(java.awt.MenuBar)
     */
    public void setMenuBar(MenuBar mb) {
    }

    /**
     * @see java.awt.peer.FramePeer#setResizable(boolean)
     */
    public void setResizable(boolean resizable) {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.FramePeer#setState(int)
     */
    public void setState(int state) {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.FramePeer#setTitle(java.lang.String)
     */
    public void setTitle(String title) {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.WindowPeer#toBack()
     */
    public void toBack() {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.WindowPeer#toFront()
     */
    public void toFront() {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.ContainerPeer#beginLayout()
     */
    public void beginLayout() {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.ContainerPeer#beginValidate()
     */
    public void beginValidate() {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.ContainerPeer#endLayout()
     */
    public void endLayout() {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.ContainerPeer#endValidate()
     */
    public void endValidate() {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.ContainerPeer#getInsets()
     */
    public Insets getInsets() {
    	if (insets == null) {
    		insets = new Insets(0, 0, 0, 0);
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
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see java.awt.peer.ComponentPeer#canDetermineObscurity()
     */
    public boolean canDetermineObscurity() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see java.awt.peer.ComponentPeer#checkImage(java.awt.Image, int, int,
     *      java.awt.image.ImageObserver)
     */
    public int checkImage(Image img, int width, int height, ImageObserver ob) {
        return toolkit.checkImage(img, width, height, ob);
    }

    /**
     * @see java.awt.peer.ComponentPeer#coalescePaintEvent(java.awt.event.PaintEvent)
     */
    public void coalescePaintEvent(PaintEvent e) {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.ComponentPeer#createBuffers(int,
     *      java.awt.BufferCapabilities)
     */
    public void createBuffers(int x, BufferCapabilities capabilities)
            throws AWTException {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.ComponentPeer#createImage(java.awt.image.ImageProducer)
     */
    public Image createImage(ImageProducer producer) {
        return toolkit.createImage(producer);
    }

    /**
     * @see java.awt.peer.ComponentPeer#createImage(int, int)
     */
    public Image createImage(int width, int height) {
    	return toolkit.createCompatibleImage(width, height);
    }

    /**
     * @see java.awt.peer.ComponentPeer#createVolatileImage(int, int)
     */
    public VolatileImage createVolatileImage(int width, int height) {
    	return toolkit.createVolatileImage(width, height);
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
     * @see java.awt.peer.ComponentPeer#enable()
     */
    public void enable() {
        setEnabled(true);
    }

    /**
     * @see java.awt.peer.ComponentPeer#flip(java.awt.BufferCapabilities.FlipContents)
     */
    public void flip(FlipContents contents) {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.ComponentPeer#getBackBuffer()
     */
    public Image getBackBuffer() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see java.awt.peer.ComponentPeer#getColorModel()
     */
    public ColorModel getColorModel() {
        return toolkit.getColorModel();
    }

    /**
     * @see java.awt.peer.ComponentPeer#getFontMetrics(java.awt.Font)
     */
    public FontMetrics getFontMetrics(Font f) {
        return toolkit.getFontMetrics(f);
    }

    /**
     * @see java.awt.peer.ComponentPeer#getGraphics()
     */
    public Graphics getGraphics() {
    	log.debug("getGraphics");
		return new JNodeGraphics(this);
    }

    /**
     * @see java.awt.peer.ComponentPeer#getGraphicsConfiguration()
     */
    public GraphicsConfiguration getGraphicsConfiguration() {
		return toolkit.getGraphicsConfiguration();
    }

    /**
     * @see java.awt.peer.ComponentPeer#getLocationOnScreen()
     */
    public Point getLocationOnScreen() {
        return TOP_LEFT;
    }

    /**
     * @see java.awt.peer.ComponentPeer#getMinimumSize()
     */
    public Dimension getMinimumSize() {
        return toolkit.getScreenSize();
    }

    /**
     * @see java.awt.peer.ComponentPeer#getPreferredSize()
     */
    public Dimension getPreferredSize() {
        return toolkit.getScreenSize();
    }

    /**
     * @see java.awt.peer.ComponentPeer#handleEvent(java.awt.AWTEvent)
     */
    public void handleEvent(AWTEvent e) {
        log.debug("handleEvent(" + e + ")");
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.ComponentPeer#handlesWheelScrolling()
     */
    public boolean handlesWheelScrolling() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see java.awt.peer.ComponentPeer#hide()
     */
    public void hide() {
    }

    /**
     * @see java.awt.peer.ComponentPeer#isFocusable()
     */
    public boolean isFocusable() {
        return false;
    }

    /**
     * @see java.awt.peer.ComponentPeer#isFocusTraversable()
     */
    public boolean isFocusTraversable() {
        return false;
    }

    /**
     * @see java.awt.peer.ComponentPeer#isObscured()
     */
    public boolean isObscured() {
        // TODO Auto-generated method stub
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
        log.debug("Paint");
        // TODO Auto-generated method stub

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
        return toolkit.prepareImage(img, width, height, ob);
    }

    /**
     * @see java.awt.peer.ComponentPeer#print(java.awt.Graphics)
     */
    public void print(Graphics graphics) {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.ComponentPeer#repaint(long, int, int, int, int)
     */
    public void repaint(long tm, int x, int y, int width, int height) {
    	log.info("repaint (" + tm + ", " + x + ", " + y + ", " + width + ", " + height);
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.ComponentPeer#requestFocus()
     */
    public void requestFocus() {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.ComponentPeer#requestFocus(java.awt.Component,
     *      boolean, boolean, long)
     */
    public boolean requestFocus(Component source, boolean bool1, boolean bool2,
            long x) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see java.awt.peer.ComponentPeer#reshape(int, int, int, int)
     */
    public void reshape(int x, int y, int width, int height) {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.ComponentPeer#setBackground(java.awt.Color)
     */
    public void setBackground(Color color) {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.ComponentPeer#setBounds(int, int, int, int)
     */
    public void setBounds(int x, int y, int width, int height) {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.ComponentPeer#setCursor(java.awt.Cursor)
     */
    public void setCursor(Cursor cursor) {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.ComponentPeer#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.ComponentPeer#setEventMask(long)
     */
    public void setEventMask(long mask) {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.ComponentPeer#setFont(java.awt.Font)
     */
    public void setFont(Font font) {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.ComponentPeer#setForeground(java.awt.Color)
     */
    public void setForeground(Color color) {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.ComponentPeer#setVisible(boolean)
     */
    public void setVisible(boolean visible) {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.ComponentPeer#show()
     */
    public void show() {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.ComponentPeer#updateCursorImmediately()
     */
    public void updateCursorImmediately() {
        // TODO Auto-generated method stub

    }
}