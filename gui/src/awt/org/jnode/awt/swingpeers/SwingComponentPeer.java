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

import gnu.classpath.SystemProperties;
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
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.event.PaintEvent;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.VolatileImage;
import java.awt.peer.ComponentPeer;
import java.awt.peer.ContainerPeer;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.apache.log4j.Logger;
import org.jnode.awt.GraphicsFactory;
import org.jnode.awt.JNodeGenericPeer;
import org.jnode.awt.JNodeGraphics2D;
import org.jnode.awt.JNodeToolkit;
import sun.awt.CausedFocusEvent;
import sun.java2d.pipe.Region;

/**
 * Base class for virtual component peers. Satisfies the requirements for AWT
 * peers without actually displaying anything (hence, they are virtual) or by
 * delegating to the parent component, under the assumption is that somewhere up
 * in the hierarchy there is a parent who can produce a display.
 *
 * @author Levente S\u00e1ntha
 */

abstract class SwingComponentPeer<awtT extends Component, swingPeerT extends Component>
    extends JNodeGenericPeer<SwingToolkit, awtT> implements ComponentPeer {

    ///////////////////////////////////////////////////////////////////////////////////////
    // Private
    protected final Logger log = Logger.getLogger(getClass());
    protected final swingPeerT peerComponent;

    /**
     * Initialize this instance.
     *
     * @param toolkit
     * @param target
     */
    public SwingComponentPeer(SwingToolkit toolkit, awtT target, swingPeerT swingPeer) {
        super(toolkit, target);
        this.peerComponent = swingPeer;
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
        /*
        final Component parent = targetComponent.getParent();
        if (parent != null) {
            final int x = peerComponent.getX();
            final int y = peerComponent.getY();
            final int width = peerComponent.getWidth();
            final int height = peerComponent.getHeight();
            return parent.getGraphics().create(x, y, width, height);
        } else {
            throw new Error();
        }
        */
        //TODO review this !!!
        final int x = peerComponent.getX();
        final int y = peerComponent.getY();
        final int width = peerComponent.getWidth();
        final int height = peerComponent.getHeight();
        Graphics g = SystemProperties.getProperty("gnu.javax.swing.noGraphics2D") == null ?
            new JNodeGraphics2D(this) : GraphicsFactory.getInstance().createGraphics(this);
        g.translate(x, y);
        g.clipRect(0, 0, width, height);

        /*
        Throwable t = new Throwable();
        StackTraceElement[] st = t.getStackTrace();
        for(StackTraceElement ste : st){
            if(ste.getClassName().contains("BoxWorld") && ste.getMethodName().equals("changeState")){
                System.out.println("SwingComponent.getGraphics");
                t.printStackTrace(System.out);
                System.out.println("target compoennt: " + targetComponent);
                System.out.println("peer compoennt: " + peerComponent);
            }
        }
        */

        return g;
    }

    public final GraphicsConfiguration getGraphicsConfiguration() {
        return toolkit.getGraphicsConfiguration();
    }

    /**
     * @return The location on screen
     * @see java.awt.peer.ComponentPeer#getLocationOnScreen()
     */
    public Point getLocationOnScreen() {
        Point p = targetComponent.getLocation();
        Container parent = targetComponent.getParent();
        while (parent != null) {
            p.translate(parent.getX(), parent.getY());
            parent = parent.getParent();
        }
        return p;
    }

    public final Dimension getMinimumSize() {
        return peerComponent.getMinimumSize();
    }

    public final Dimension getPreferredSize() {
        return peerComponent.getPreferredSize();
    }

    /**
     * Response on paint events.
     */
    private void processPaintEvent(PaintEvent event) {
        if (!targetComponent.isVisible() || !peerComponent.isVisible())
            return;

        final Graphics g = peerComponent.getGraphics();
        if (g != null) {
            if (event.getID() == PaintEvent.PAINT) {
                if (!(targetComponent instanceof java.awt.Window))
                    peerComponent.paint(g);
                targetComponent.paint(g);
            } else {
                if (!(targetComponent instanceof java.awt.Window))
                    peerComponent.update(g);
                targetComponent.update(g);
            }
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
                processPaintEvent((PaintEvent) event);
                break;
            }
            default: {
                if (event.getSource() == targetComponent) {
                    event.setSource(peerComponent);
                }
                if (event.getID() == MouseEvent.MOUSE_ENTERED) {
                    Object source = event.getSource();
                    if (source instanceof Component) {
                        Component comp = (Component) source;
                        Cursor cur = comp.getCursor();
                        if (cur != null) {
                            comp.setCursor(cur);
                        } else {
                            comp.setCursor(Cursor.getDefaultCursor());
                        }
                    }
                }
                ((ISwingPeer<awtT>) peerComponent).processAWTEvent(event);
                break;
            }
        }
    }

    /**
     * Post a paint event for the AWT component on the event queue.
     */
    protected final void postPaintEvent() {
        if (targetComponent != null) {
            final PaintEvent event = new PaintEvent(targetComponent, PaintEvent.PAINT, targetComponent.getBounds());
            JNodeToolkit.postToTarget(event, targetComponent);
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
        return peerComponent.isFocusable();
    }

    // Obscurity

    public boolean isObscured() {
        return false;
    }

    public final Dimension minimumSize() {
        return peerComponent.getMinimumSize();
    }

    public final void paint(Graphics g) {
        if (!(targetComponent instanceof java.awt.Window)) {
            peerComponent.paint(g);
            targetComponent.paint(g);
        } else
            toolkit.postEvent(new PaintEvent(targetComponent, PaintEvent.PAINT, targetComponent.getBounds()));
    }

    // Deprecated

    public final Dimension preferredSize() {
        return peerComponent.getPreferredSize();
    }

    public final boolean prepareImage(Image img, int width, int height, ImageObserver o) {
        return toolkit.prepareImage(img, width, height, o);
    }

    public final void print(Graphics g) {
        peerComponent.print(g);
    }

    public final void repaint(long tm, int x, int y, int width, int height) {
        //peerComponent.repaint(tm, x, y, width, height);
        //postPaintEvent();
        toolkit.postEvent(new PaintEvent(targetComponent, PaintEvent.UPDATE, targetComponent.getBounds()));
    }

    /**
     * @see java.awt.peer.ComponentPeer#requestFocus()
     */
    public final void requestFocus() {


        peerComponent.requestFocus();
    }

    public final boolean requestFocus(final Component lightweightChild, final boolean temporary,
                                      final boolean focusedWindowChangeAllowed, final long time) {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            public Boolean run() {
                try {
                    Method processSynchronousLightweightTransferMethod =
                        KeyboardFocusManager.class.getDeclaredMethod("processSynchronousLightweightTransfer",
                            new Class[]{Component.class, Component.class, Boolean.TYPE, Boolean.TYPE, Long.TYPE});
                    processSynchronousLightweightTransferMethod.setAccessible(true);

                    Object[] params = new Object[]{targetComponent, peerComponent, Boolean.valueOf(temporary),
                        Boolean.valueOf(focusedWindowChangeAllowed), Long.valueOf(time)};
                    return ((Boolean) processSynchronousLightweightTransferMethod.invoke(null, params)).booleanValue();
                } catch (Exception x) {
                    x.printStackTrace();
                    org.jnode.vm.Unsafe.debug("SwingComponentPeer.requestFocus() exception\n");
                    org.jnode.vm.Unsafe.debugStackTrace();
                    return false;
                }
            }
        });
    }

    public boolean requestFocus(final Component lwChild, final boolean temporary,
                                final boolean focusedWindowChangeAllowed, final long time,
                                final CausedFocusEvent.Cause cause) {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            public Boolean run() {
                try {
                    Component lightweightChild = lwChild;
                    Method processSynchronousLightweightTransferMethod =
                        KeyboardFocusManager.class.getDeclaredMethod("processSynchronousLightweightTransfer",
                            new Class[]{Component.class, Component.class, Boolean.TYPE, Boolean.TYPE, Long.TYPE});
                    processSynchronousLightweightTransferMethod.setAccessible(true);

                    Object[] params = new Object[]{targetComponent, peerComponent,
                        Boolean.valueOf(temporary), Boolean.valueOf(focusedWindowChangeAllowed), Long.valueOf(time)};
                    boolean ret =
                        ((Boolean) processSynchronousLightweightTransferMethod.invoke(null, params)).booleanValue();
                    //if(ret)
                    //  return true;

                    if (lightweightChild == null) {
                        lightweightChild = targetComponent;
                    }
                    Component currentOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                    if (currentOwner != null && currentOwner.getPeer() == null) {
                        currentOwner = null;
                    }

                    //if (focusLog.isLoggable(Level.FINER)) focusLog.finer("Simulating transfer from " + currentOwner +
                    // " to " + lightweightChild);
                    FocusEvent fg =
                        new CausedFocusEvent(lightweightChild, FocusEvent.FOCUS_GAINED, false, currentOwner, cause);
                    FocusEvent fl = null;
                    if (currentOwner != null) {
                        fl = new CausedFocusEvent(currentOwner, FocusEvent.FOCUS_LOST, false, lightweightChild, cause);
                    }

                    if (fl != null) {
                        postPaintEvent();
                        JNodeToolkit.postToTarget(fl, currentOwner);
                    }
                    JNodeToolkit.postToTarget(fg, (Component) fg.getSource());
                    return true;


                } catch (Exception x) {
                    x.printStackTrace();
                    return false;
                }
            }
        });
    }

    boolean isReshapeInProgress = false;

    public final void reshape(int x, int y, int width, int height) {
        if (isReshapeInProgress) {
            return;
        }
        final int oldWidth = peerComponent.getWidth();
        final int oldHeight = peerComponent.getHeight();
        isReshapeInProgress = true;
        peerComponent.setBounds(x, y, width, height);
        isReshapeInProgress = false;
        fireComponentEvent(oldWidth, width, oldHeight, height);
    }

    public final void setBackground(Color c) {
        peerComponent.setBackground(c);
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
        peerComponent.setCursor(cursor);

    }

    public final void setEnabled(boolean b) {
        peerComponent.setEnabled(b);
    }

    /**
     * @see java.awt.peer.ComponentPeer#setEventMask(long)
     */
    public final void setEventMask(long mask) {
    }

    public final void setFont(Font f) {
        peerComponent.setFont(f);
        postPaintEvent();
    }

    public final void setForeground(Color c) {
        peerComponent.setForeground(c);
        postPaintEvent();
    }

    public void setVisible(boolean b) {
        peerComponent.setVisible(b);
    }

    public final void show() {
        setVisible(true);
    }

    // Cursor

    public final void updateCursorImmediately() {

        Cursor cur = targetComponent.getCursor();
//        org.jnode.vm.Unsafe.debug("JNodeToolkit.updateCursor()-2 " + cur +  " for " + targetComponent + "\n");
//        org.jnode.vm.Unsafe.debugStackTrace(100);
        peerComponent.setCursor(cur);
        toolkit.updateCursor(cur);
    }

    /**
     * Posts a component event to the AWT event queue.
     *
     * @param what
     */
    protected final void fireComponentEvent(int what) {
        JNodeToolkit.postToTarget(new ComponentEvent(targetComponent, what), targetComponent);
    }

    public void layout() {
        peerComponent.doLayout();
        targetComponent.doLayout();
    }

    public Rectangle getBounds() {
        return peerComponent.getBounds();
    }

    public void setBounds(int x, int y, int width, int height, int z) {
        setBounds(x, y, width, height);
        //peerComponent.setBounds(x, y, width, height);
    }

    public boolean isReparentSupported() {
        //TODO implement it
        return false;
    }

    public void reparent(ContainerPeer parent) {
        //TODO implement it
    }

    public void applyShape(Region shape) {
        //TODO
    }
}
