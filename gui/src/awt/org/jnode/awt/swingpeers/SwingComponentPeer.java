/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.BufferCapabilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Point;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.event.PaintEvent;
import java.awt.event.ComponentEvent;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.VolatileImage;
import java.awt.peer.ComponentPeer;

import org.apache.log4j.Logger;
import org.jnode.awt.JNodeToolkit;
import org.jnode.awt.JNodeGenericPeer;

import javax.swing.JComponent;

/**
 * Base class for virtual component peers. Satisfies the requirements for AWT
 * peers without actually displaying anything (hence, they are virtual) or by
 * delegating to the parent component, under the assumption is that somewhere up
 * in the hierarchy there is a parent who can produce a display.
 */

class SwingComponentPeer extends JNodeGenericPeer implements ComponentPeer {

    ///////////////////////////////////////////////////////////////////////////////////////
    // Private

    protected final Component component;

    protected final Logger log = Logger.getLogger(getClass());

    private final JNodeToolkit toolkit;

    protected final JComponent jComponent;

    /**
     * Initialize this instance.
     * 
     * @param toolkit
     * @param component
     */
    public SwingComponentPeer(JNodeToolkit toolkit, Component component, JComponent peer) {
        super(toolkit, component);
        this.toolkit = toolkit;
        this.jComponent = peer;
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

    public final Image createImage(ImageProducer producer) {
        return toolkit.createImage(producer);
    }

    public final Image createImage(int width, int height) {
    	return toolkit.createCompatibleImage(width, height);
    }

    public final VolatileImage createVolatileImage(int width, int height) {
    	return toolkit.createVolatileImage(width, height);
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
        return toolkit.getFontMetrics(font);
    }

    public Graphics getGraphics() {
    	log.debug("getGraphics");
        final Component parent = component.getParent();
        if (parent != null) {
            //System.out.println("creating graphics");
        	final int x = jComponent.getX();
        	final int y = jComponent.getY();
        	final int width = jComponent.getWidth();
        	final int height = jComponent.getHeight();
            return parent.getGraphics().create(x, y, width, height);
        } else {
            throw new Error();
        }
    }

    public GraphicsConfiguration getGraphicsConfiguration() {
        return toolkit.getGraphicsConfiguration();
    }

	/**
	 * @see java.awt.peer.ComponentPeer#getLocationOnScreen()
	 * @return The location on screen
	 */
	public Point getLocationOnScreen() {
		return computeLocationOnScreen(component);
	}

    private Point computeLocationOnScreen(Component component) {
        Container parent = component.getParent();
        if(parent == null)
            return component.getLocation();
        else{
            Point p = computeLocationOnScreen(parent);
            p.translate(component.getX(), component.getY());
            return p;
        }
    }

    public Dimension getMinimumSize() {
        return jComponent.getMinimumSize();
    }

    public Dimension getPreferredSize() {
        return jComponent.getPreferredSize();
    }

    // Events
    public void handleEvent(AWTEvent event) {
        switch (event.getID()) {
            case PaintEvent.PAINT: {
                Graphics g = getGraphics();
                Point p = component.getLocationOnScreen();
                g.translate(p.x, p.y);
                component.paint(g);
                g.translate(-p.x, -p.y);
            } break;
            case PaintEvent.UPDATE: {
                Graphics g = getGraphics();
                Point p = component.getLocationOnScreen();
                g.translate(p.x, p.y);
                component.update(getGraphics());
                g.translate(-p.x, -p.y);
            } break;
        }
    }

    private void paintAWTComponent(){
        if(component != null)
        q.postEvent(new PaintEvent(component, PaintEvent.PAINT, component.getBounds()));
    }

    public boolean handlesWheelScrolling() {
        return false;
    }

    public final void hide() {
        setVisible(false);
    }

    // Focus

    public boolean isFocusable() {
        return jComponent.isFocusable();
    }

    /**
     * @see java.awt.peer.ComponentPeer#isFocusTraversable()
     */
    public boolean isFocusTraversable() {
        return jComponent.isFocusTraversable();
    }

    // Obscurity

    public boolean isObscured() {
        return false;
    }

    public final Dimension minimumSize() {
        return jComponent.getMinimumSize();
    }

    public void paint(Graphics g) {
        jComponent.paint(g);
        paintAWTComponent();
    }

    // Deprecated

    public final Dimension preferredSize() {
        return jComponent.getPreferredSize();
    }

    public boolean prepareImage(Image img, int width, int height, ImageObserver o) {
        return toolkit.prepareImage(img, width, height, o);
    }

    public final void print(Graphics g) {
        jComponent.print(g);
    }

    public final void repaint(long tm, int x, int y, int width, int height) {
        jComponent.repaint(tm, x, y, width, height);
        paintAWTComponent();
    }

    /**
     * @see java.awt.peer.ComponentPeer#requestFocus()
     */
    public final void requestFocus() {
        jComponent.requestFocus();
    }

    public final boolean requestFocus(Component lightweightChild, boolean temporary,
            boolean focusedWindowChangeAllowed, long time) {
        return true;
    }

    public final void reshape(int x, int y, int width, int height) {
        setBounds(x, y, width, height);
    }

    public final void setBackground(Color c) {
        jComponent.setBackground(c);
        paintAWTComponent();
    }

    // Bounds

    public final void setBounds(int x, int y, int width, int height) {
    	final int oldWidth = jComponent.getWidth();
    	final int oldHeight = jComponent.getHeight();
        jComponent.setBounds(x, y, width, height);
        if ((oldWidth != width) || (oldHeight != height)) {
            sendComponentEvent(ComponentEvent.COMPONENT_RESIZED);
        } else {
            sendComponentEvent(ComponentEvent.COMPONENT_MOVED);
        }
    }

    /**
     * @see java.awt.peer.ComponentPeer#setCursor(java.awt.Cursor)
     */
    public final void setCursor(Cursor cursor) {
        jComponent.setCursor(cursor);

    }

    public final void setEnabled(boolean b) {
        jComponent.setEnabled(b);
    }

    /**
     * @see java.awt.peer.ComponentPeer#setEventMask(long)
     */
    public final void setEventMask(long mask) {
        // TODO Auto-generated method stub

    }

    public final void setFont(Font f) {
        jComponent.setFont(f);
        paintAWTComponent();
    }

    public void setForeground(Color c) {
        jComponent.setForeground(c);
        paintAWTComponent();
    }

    // State

    public final void setVisible(boolean b) {
        jComponent.setVisible(b);
        paintAWTComponent();
    }

    public final void show() {
        setVisible(true);
    }

    // Cursor

    public void updateCursorImmediately() {
    }

    /**
     * Posts a component event to the AWT event queue.
     * @param what
     */
    protected final void sendComponentEvent(int what) {
        final EventQueue queue = toolkit.getSystemEventQueue();
        queue.postEvent(new ComponentEvent(component, what));
    }
}