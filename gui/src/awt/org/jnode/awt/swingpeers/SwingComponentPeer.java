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

import org.apache.log4j.Logger;
import org.jnode.awt.JNodeGenericPeer;

import javax.swing.JComponent;
import java.awt.AWTEvent;
import java.awt.BufferCapabilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.PaintEvent;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.VolatileImage;
import java.awt.peer.ComponentPeer;
import java.awt.peer.ContainerPeer;

/**
 * Base class for virtual component peers. Satisfies the requirements for AWT
 * peers without actually displaying anything (hence, they are virtual) or by
 * delegating to the parent component, under the assumption is that somewhere up
 * in the hierarchy there is a parent who can produce a display.
 * @author Levente S\u00e1ntha
 */

abstract class SwingComponentPeer<awtT extends Component, swingPeerT extends JComponent>
        extends JNodeGenericPeer<SwingToolkit, awtT> implements ComponentPeer {

    ///////////////////////////////////////////////////////////////////////////////////////
    // Private
    protected final Logger log = Logger.getLogger(getClass());
    protected final swingPeerT jComponent;

    /**
     * Initialize this instance.
     * 
     * @param toolkit
     * @param target
     */
    public SwingComponentPeer(SwingToolkit toolkit, awtT target, swingPeerT swingPeer) {
        super(toolkit, target);
        this.jComponent = swingPeer;
        if (!(swingPeer instanceof ISwingPeer)) {
            throw new IllegalArgumentException("peer must implement ISwingPeer");
        }
        SwingToolkit.copyAwtProperties(target, swingPeer);
        setBounds(target.getX(), target.getY(), target.getWidth(),
                target.getHeight());
    }

    public boolean canDetermineObscurity() {
        return false;
    }

    public final int checkImage(Image img, int width, int height, ImageObserver o) {
        return toolkit.checkImage(img, width, height, o);
    }

    public final void coalescePaintEvent(PaintEvent e) {
        //System.err.println( "coalescePaintEvent: " + e );
    }

    // Buffer

    public final void createBuffers(int x, BufferCapabilities bufferCapabilities) {
    }

    public final void destroyBuffers() {
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

    public final void disable() {
        setEnabled(false);
    }

    public final void enable() {
        setEnabled(true);
    }

    public final void flip(BufferCapabilities.FlipContents flipContents) {
    }

    public final Image getBackBuffer() {
        return null;
    }

    // Color

    public final ColorModel getColorModel() {
        return toolkit.getColorModel();
    }

    // Fonts

    public final FontMetrics getFontMetrics(Font font) {
        return toolkit.getFontMetrics(font);
    }

    public Graphics getGraphics() {
        final Component parent = target.getParent();
        if (parent != null) {
            final int x = jComponent.getX();
            final int y = jComponent.getY();
            final int width = jComponent.getWidth();
            final int height = jComponent.getHeight();
            return parent.getGraphics().create(x, y, width, height);
        } else {
            throw new Error();
        }
    }

    public final GraphicsConfiguration getGraphicsConfiguration() {
        return toolkit.getGraphicsConfiguration();
    }

    /**
     * @see java.awt.peer.ComponentPeer#getLocationOnScreen()
     * @return The location on screen
     */
    public Point getLocationOnScreen() {
        Point p = target.getLocation();
        Container parent = target.getParent();
        while (parent != null) {
            p.translate(parent.getX(), parent.getY());
            parent = parent.getParent();
        }
        return p;
    }

    public final Dimension getMinimumSize() {
        return jComponent.getMinimumSize();
    }

    public final Dimension getPreferredSize() {
        return jComponent.getPreferredSize();
    }

    /**
     * Response on paint events.
     */
    private void processPaintEvent(PaintEvent event) {
        final Graphics g = jComponent.getGraphics();
        if (g != null) {
            //Point p = component.getLocationOnScreen();
            //g.translate(p.x, p.y);
            if (event.getID() == PaintEvent.PAINT) {
                target.paint(g);
            } else {
                target.update(g);
            }
            //g.translate(-p.x, -p.y);
            g.dispose();
        }
    }

    /**
     * Event handling.
     */
    public final void handleEvent(AWTEvent event) {
        final int id = event.getID();
        switch (id) {
            case PaintEvent.PAINT:
            case PaintEvent.UPDATE: {
                processPaintEvent((PaintEvent)event);
            } break;
            default: {
                if (event.getSource() == target) {
                    event.setSource(jComponent);
                }
                ((ISwingPeer<awtT>)jComponent).processAWTEvent(event);
            } break;
        }
    }

    /**
     * Post a paint event for the AWT component on the event queue.
     */
    protected final void postPaintEvent() {
        if (target != null) {
            toolkit.postEvent(new PaintEvent(target, PaintEvent.PAINT,
                    target.getBounds()));
        }
    }

    public boolean handlesWheelScrolling() {
        return false;
    }

    public final void hide() {
        setVisible(false);
    }

    // Focus

    public final boolean isFocusable() {
        return jComponent.isFocusable();
    }

    /**
     * @see java.awt.peer.ComponentPeer#isFocusTraversable()
     */
    @SuppressWarnings("deprecation")
    public final boolean isFocusTraversable() {
        return jComponent.isFocusTraversable();
    }

    // Obscurity

    public boolean isObscured() {
        return false;
    }

    public final Dimension minimumSize() {
        return jComponent.getMinimumSize();
    }

    public final void paint(Graphics g) {
        jComponent.paint(g);
    }

    // Deprecated

    public final Dimension preferredSize() {
        return jComponent.getPreferredSize();
    }

    public final boolean prepareImage(Image img, int width, int height, ImageObserver o) {
        return toolkit.prepareImage(img, width, height, o);
    }

    public final void print(Graphics g) {
        jComponent.print(g);
    }

    public final void repaint(long tm, int x, int y, int width, int height) {
        jComponent.repaint(tm, x, y, width, height);
        postPaintEvent();
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

    boolean isReshapeInProgress = false;
    public final void reshape(int x, int y, int width, int height) {
        if (isReshapeInProgress) {
            return;
        }
        final int oldWidth = jComponent.getWidth();
        final int oldHeight = jComponent.getHeight();
        isReshapeInProgress = true;
        jComponent.setBounds(x, y, width, height);
        isReshapeInProgress = false;
        fireComponentEvent(oldWidth, width, oldHeight, height);
    }

    public final void setBackground(Color c) {
        jComponent.setBackground(c);
        postPaintEvent();
    }

    // Bounds

    public final void setBounds(int x, int y, int width, int height) {
        reshape(x, y, width, height);
    }

    final void fireComponentEvent(final int oldWidth, int width, final int oldHeight, int height) {
        if ((oldWidth != width) || (oldHeight != height)) {
            fireComponentEvent(ComponentEvent.COMPONENT_RESIZED);
        } else {
            fireComponentEvent(ComponentEvent.COMPONENT_MOVED);
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
    }

    public final void setFont(Font f) {
        jComponent.setFont(f);
        postPaintEvent();
    }

    public final void setForeground(Color c) {
        jComponent.setForeground(c);
        postPaintEvent();
    }

    public void setVisible(boolean b) {
        jComponent.setVisible(b);
    }

    public final void show() {
        setVisible(true);
    }

    // Cursor

    public final void updateCursorImmediately() {
    }

    /**
     * Posts a component event to the AWT event queue.
     * @param what
     */
    protected final void fireComponentEvent(int what) {
        final EventQueue queue = toolkit.getSystemEventQueue();
        queue.postEvent(new ComponentEvent(target, what));
    }

    public void layout() {
        jComponent.doLayout();
    }

    public Rectangle getBounds() {
        return jComponent.getBounds();
    }

    public void setBounds(int x, int y, int width, int height, int z) {
        jComponent.setBounds(x, y, width, height);
    }

    public boolean isReparentSupported() {
        //TODO implement it
        return false;
    }

    public void reparent(ContainerPeer parent) {
        //TODO implement it
    }
}
