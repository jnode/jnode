/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.BufferCapabilities;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.PaintEvent;
import java.awt.peer.DialogPeer;

import javax.swing.JInternalFrame;

/**
 * AWT dialog peer implemented as a {@link javax.swing.JInternalFrame}.
 */

class SwingDialogPeer extends JInternalFrame implements DialogPeer {

    //
    // Construction
    //

    public SwingDialogPeer(Dialog dialog) {
        SwingToolkit.copyAwtProperties(dialog, this);
        setTitle(dialog.getTitle());
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