/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.BufferCapabilities;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.List;
import java.awt.event.PaintEvent;
import java.awt.peer.ListPeer;

import javax.swing.JList;

/**
 * AWT list peer implemented as a {@link javax.swing.JList}.
 */

class SwingListPeer extends JList implements ListPeer {

    //
    // Construction
    //

    public SwingListPeer(List list) {
        super();
        SwingFramePeer.add(list, this);
    }

    //
    // ListPeer
    //

    public int[] getSelectedIndexes() {
        return null;
    }

    public void add(String item, int index) {
    }

    public void delItems(int start, int end) {
    }

    public void select(int index) {
    }

    public void deselect(int index) {
    }

    public void makeVisible(int index) {
    }

    public void setMultipleMode(boolean b) {
    }

    public Dimension getPreferredSize(int rows) {
        return null;
    }

    public Dimension getMinimumSize(int rows) {
        return null;
    }

    // Deprecated

    public void addItem(String item, int index) {
        add(item, index);
    }

    public void clear() {
        removeAll();
    }

    public void setMultipleSelections(boolean v) {
        setMultipleMode(v);
    }

    public Dimension preferredSize(int rows) {
        return getPreferredSize(rows);
    }

    public Dimension minimumSize(int rows) {
        return getMinimumSize(rows);
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