/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.BufferCapabilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.PaintEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.VolatileImage;
import java.awt.peer.ComponentPeer;

import org.apache.log4j.Logger;
import org.jnode.awt.image.JNodeBufferedImage;

/**
 * Base class for virtual component peers. Satisfies the requirements for AWT
 * peers without actually displaying anything (hence, they are virtual) or by
 * delegating to the parent component, under the assumption is that somewhere up
 * in the hierarchy there is a parent who can produce a display.
 */

class SwingComponentPeer implements ComponentPeer {

    ///////////////////////////////////////////////////////////////////////////////////////
    // Private

    private final Component component;

    private Point location = new Point();

    private final Logger log = Logger.getLogger(getClass());

    protected Dimension size = new Dimension();

    private final Toolkit toolkit;

    /**
     * Initialize this instance.
     * 
     * @param toolkit
     * @param component
     */
    public SwingComponentPeer(Toolkit toolkit, Component component) {
        this.toolkit = toolkit;
        this.component = component;
        setBounds(component.getX(), component.getY(), component.getWidth(),
                component.getHeight());

        // Disable double-buffering for Swing components
        //javax.swing.RepaintManager.currentManager( component
        // ).setDoubleBufferingEnabled( false );
    }

    public boolean canDetermineObscurity() {
        return false;
    }

    public int checkImage(Image img, int width, int height, ImageObserver o) {
        return toolkit.checkImage(img, width, height, o);
    }

    public void coalescePaintEvent(PaintEvent e) {
        //System.err.println( "coalescePaintEvent: " + e );
    }

    // Buffer

    public void createBuffers(int x, BufferCapabilities bufferCapabilities) {
    }

    // Image

    public Image createImage(ImageProducer producer) {
        return toolkit.createImage(producer);
    }

    public Image createImage(int width, int height) {
		return new JNodeBufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    public VolatileImage createVolatileImage(int width, int height) {
	    throw new RuntimeException("Not implemented");
    }

    public void destroyBuffers() {
    }

    public void disable() {
        setEnabled(false);
    }

    public void dispose() {
    }

    public void enable() {
        setEnabled(true);
    }

    public void flip(BufferCapabilities.FlipContents flipContents) {
    }

    public Image getBackBuffer() {
        return null;
    }

    // Color

    public ColorModel getColorModel() {
        return toolkit.getColorModel();
    }

    // Fonts

    public FontMetrics getFontMetrics(Font font) {
        return null;
    }

    public Graphics getGraphics() {
        Component parent = component.getParent();
        if (parent != null) {
            System.err.println("creating graphics");
            return parent.getGraphics().create(location.x, location.y,
                    size.width, size.height);
        } else
            throw new Error();
    }

    public GraphicsConfiguration getGraphicsConfiguration() {
        //System.err.println("getGraphicsConfiguration");
        return null;
    }

    public Point getLocationOnScreen() {
        Point screen = new Point(location);
        Component parent = component.getParent();
        if (parent != null) {
            Point parentScreen = parent.getLocationOnScreen();
            screen.translate(parentScreen.x, parentScreen.y);
        }

        return screen;
    }

    public Dimension getMinimumSize() {
        return size;
    }

    public Dimension getPreferredSize() {
        return size;
    }

    /*
     * public void setCursor( Cursor cursor ) { }
     */

    // Misc
    public Toolkit getToolkit() {
        return toolkit;
    }

    /*
     * public boolean isFocusTraversable() { return true; }
     */

    /*
     * public void requestFocus() { }
     */

    // Events
    public void handleEvent(AWTEvent e) {
        //System.err.println(e);
    }

    public boolean handlesWheelScrolling() {
        return false;
    }

    public void hide() {
        setVisible(false);
    }

    // Focus

    public boolean isFocusable() {
        return true;
    }

    /**
     * @see java.awt.peer.ComponentPeer#isFocusTraversable()
     */
    public boolean isFocusTraversable() {
        // TODO Auto-generated method stub
        return false;
    }

    // Obscurity

    public boolean isObscured() {
        return false;
    }

    public Dimension minimumSize() {
        return getMinimumSize();
    }

    public void paint(Graphics g) {
        log.info("Paint");
        //System.err.println("paint");
    }

    // Deprecated

    public Dimension preferredSize() {
        return getPreferredSize();
    }

    public boolean prepareImage(Image img, int width, int height, ImageObserver o) {
        return toolkit.prepareImage(img, width, height, o);
    }

    public void print(Graphics g) {
    }

    public void repaint(long tm, int x, int y, int width, int height) {
        //System.err.println("repaint");
    }

    /**
     * @see java.awt.peer.ComponentPeer#requestFocus()
     */
    public void requestFocus() {
        // TODO Auto-generated method stub

    }

    public boolean requestFocus(Component lightweightChild, boolean temporary,
            boolean focusedWindowChangeAllowed, long time) {
        return true;
    }

    public void reshape(int x, int y, int width, int height) {
        setBounds(x, y, width, height);
    }

    public void setBackground(Color c) {
    }

    // Bounds

    public void setBounds(int x, int y, int width, int height) {
        //System.err.println("setBounds "+x+","+y+","+width+","+height);
        size.width = width;
        size.height = height;
    }

    /**
     * @see java.awt.peer.ComponentPeer#setCursor(java.awt.Cursor)
     */
    public void setCursor(Cursor cursor) {
        // TODO Auto-generated method stub

    }

    public void setEnabled(boolean b) {
    }

    /**
     * @see java.awt.peer.ComponentPeer#setEventMask(long)
     */
    public void setEventMask(long mask) {
        // TODO Auto-generated method stub

    }

    public void setFont(Font f) {
    }

    public void setForeground(Color c) {
    }

    // State

    public void setVisible(boolean b) {
    }

    public void show() {
        setVisible(true);
    }

    // Cursor

    public void updateCursorImmediately() {
    }
}