/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.BufferCapabilities;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Image;
import java.awt.MenuBar;
import java.awt.event.KeyEvent;
import java.awt.event.PaintEvent;
import java.awt.peer.ComponentPeer;
import java.awt.peer.FramePeer;
import java.beans.PropertyVetoException;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

/**
 * AWT frame peer implemented as a {@link javax.swing.JInternalFrame}.
 */

class SwingFramePeer extends JInternalFrame implements FramePeer {

    private final SwingToolkit toolkit;
    
    //
    // Static operations
    //

    public static void add(Component component, Component peer) {
        SwingFramePeer framePeer = getFramePeer(component);
        if (framePeer != null) framePeer.getContentPane().add(peer);
    }

    //
    // Construction
    //

    public SwingFramePeer(SwingToolkit toolkit, JDesktopPane desktopPane, Frame frame) {
        super();
        this.toolkit = toolkit;
        desktopPane.add(this);

        setLocation(frame.getLocation());
        setSize(frame.getSize());
        setResizable(frame.isResizable());
        setIconifiable(true);
        setMaximizable(true);
        setClosable(true);
        try {
            setIcon(frame.getState() == Frame.ICONIFIED);
        } catch (PropertyVetoException x) {
        }
        setState(frame.getState());
        setTitle(frame.getTitle());
        setIconImage(frame.getIconImage());
        setMenuBar(frame.getMenuBar());
    }

    //
    // FramePeer
    //

    public void setIconImage(Image im) {
    }

    public void setMenuBar(MenuBar mb) {
    }

    public void setState(int state) {
        if (state == Frame.ICONIFIED) {
        } else // state == Frame.NORMAL
        {
        }
    }

    public int getState() {
        return -1;
    }

    public void setMaximizedBounds(java.awt.Rectangle bounds) {
    }

    //
    // WindowPeer
    //

    public int handleFocusTraversalEvent(KeyEvent e) {
        return -1;
    }

    //
    // ContainerPeer
    //

    public void beginValidate() {
    }

    public void endValidate() {
    }

    public void beginLayout() {
    }

    public void endLayout() {
    }

    public boolean isPaintPending() {
        return false;
    }

    //
    // ComponentPeer
    //

    // Events

    public void handleEvent(AWTEvent e) {
        //System.err.println(e);
    }

    public void coalescePaintEvent(PaintEvent e) {
        System.err.println(e);
    }

    public boolean handlesWheelScrolling() {
        return false;
    }

    // Obscurity

    public boolean isObscured() {
        return false;
    }

    public boolean canDetermineObscurity() {
        return false;
    }

    // Focus

    public boolean requestFocus(Component lightweightChild, boolean temporary,
            boolean focusedWindowChangeAllowed, long time) {
        return true;
    }

    // Buffer

    public void createBuffers(int x, BufferCapabilities bufferCapabilities) {
    }

    public void destroyBuffers() {
    }

    public void flip(BufferCapabilities.FlipContents flipContents) {
    }

    public Image getBackBuffer() {
        return null;
    }

    // Cursor

    public void updateCursorImmediately() {
    }

    // Misc

    public void dispose() {
        toolkit.onDisposeFrame();
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // Private

    private static SwingFramePeer getFramePeer(Component component) {
        Component parent = component.getParent();
        if (parent == null) {
            return null;
        } else {
            ComponentPeer parentPeer = parent.getPeer();
            if (parentPeer instanceof SwingFramePeer)
                return (SwingFramePeer) parentPeer;
            else
                return getFramePeer(parent);
        }
    }

    /**
     * @see java.awt.peer.ComponentPeer#setEventMask(long)
     */
    public void setEventMask(long mask) {
        // TODO Auto-generated method stub

    }
}