/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.BufferCapabilities;
import java.awt.Component;
import java.awt.Image;
import java.awt.Scrollbar;
import java.awt.event.PaintEvent;
import java.awt.peer.ScrollbarPeer;

import javax.swing.JScrollBar;

/**
 * AWT scrollbar peer implemented as a {@link javax.swing.JScrollBar}.
 */

class SwingScrollbarPeer extends JScrollBar implements ScrollbarPeer {

    //
    // Construction
    //

    public SwingScrollbarPeer(Scrollbar sb) {
        SwingFramePeer.add(sb, this);
        SwingToolkit.copyAwtProperties(sb, this);
        setOrientation(sb.getOrientation());
        setBlockIncrement(sb.getBlockIncrement());
        setUnitIncrement(sb.getUnitIncrement());
        setValues(sb.getValue(), sb.getVisibleAmount(), sb.getMinimum(), sb.getMaximum());
    }

    //
    // ScrollbarPeer
    //

    public void setLineIncrement(int l) {
    }

    public void setPageIncrement(int l) {
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
    }

    /**
     * @see java.awt.peer.ComponentPeer#setEventMask(long)
     */
    public void setEventMask(long mask) {
        // TODO Auto-generated method stub

    }
}