/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.Adjustable;
import java.awt.BufferCapabilities;
import java.awt.Component;
import java.awt.Image;
import java.awt.ScrollPane;
import java.awt.event.PaintEvent;
import java.awt.peer.ScrollPanePeer;

import javax.swing.JScrollPane;

/**
 * AWT scroll pane peer implemented as a {@link javax.swing.JScrollPane}.
 */

class SwingScrollPanePeer extends JScrollPane implements ScrollPanePeer {

    //
    // Construction
    //

    public SwingScrollPanePeer(ScrollPane scrollPane) {
        super();
        SwingFramePeer.add(scrollPane, this);
        SwingToolkit.copyAwtProperties(scrollPane, this);
    }

    //
    // ScrollPanePeer
    //

    public int getHScrollbarHeight() {
        return 0;
    }

    public int getVScrollbarWidth() {
        return 0;
    }

    public void setScrollPosition(int x, int y) {
    }

    public void childResized(int w, int h) {
    }

    public void setUnitIncrement(Adjustable adj, int u) {
    }

    public void setValue(Adjustable adj, int v) {
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
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // Private
    /**
     * @see java.awt.peer.ComponentPeer#setEventMask(long)
     */
    public void setEventMask(long mask) {
        // TODO Auto-generated method stub

    }
}