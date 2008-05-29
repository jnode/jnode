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

import java.awt.AWTEvent;
import java.awt.Adjustable;
import java.awt.ScrollPane;
import java.awt.peer.ScrollPanePeer;
import javax.swing.JScrollPane;

/**
 * AWT scroll pane peer implemented as a {@link javax.swing.JScrollPane}.
 */

final class SwingScrollPanePeer extends
    SwingContainerPeer<ScrollPane, SwingScrollPane> implements
    ScrollPanePeer {

    //
    // Construction
    //

    public SwingScrollPanePeer(SwingToolkit toolkit, ScrollPane scrollPane) {
        super(toolkit, scrollPane, new SwingScrollPane(scrollPane));

        SwingToolkit.add(scrollPane, peerComponent);
        SwingToolkit.copyAwtProperties(scrollPane, peerComponent);
    }

    /**
     * @see java.awt.peer.ScrollPanePeer#childResized(int, int)
     */
    public void childResized(int width, int height) {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.ScrollPanePeer#getHScrollbarHeight()
     */
    public int getHScrollbarHeight() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see java.awt.peer.ScrollPanePeer#getVScrollbarWidth()
     */
    public int getVScrollbarWidth() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see java.awt.peer.ScrollPanePeer#setScrollPosition(int, int)
     */
    public void setScrollPosition(int h, int v) {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.ScrollPanePeer#setUnitIncrement(java.awt.Adjustable,
     *      int)
     */
    public void setUnitIncrement(Adjustable item, int inc) {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.awt.peer.ScrollPanePeer#setValue(java.awt.Adjustable, int)
     */
    public void setValue(Adjustable item, int value) {
        // TODO Auto-generated method stub

    }

}

final class SwingScrollPane extends JScrollPane implements ISwingPeer<ScrollPane> {
    private final ScrollPane awtComponent;

    public SwingScrollPane(ScrollPane awtComponent) {
        this.awtComponent = awtComponent;
    }

    /**
     * @see org.jnode.awt.swingpeers.ISwingPeer#getAWTComponent()
     */
    public ScrollPane getAWTComponent() {
        return awtComponent;
    }

    /**
     * Pass an event onto the AWT component.
     *
     * @see java.awt.Component#processEvent(java.awt.AWTEvent)
     */
    protected final void processEvent(AWTEvent event) {
        awtComponent.dispatchEvent(SwingToolkit.convertEvent(event, awtComponent));
    }

    /**
     * Process an event within this swingpeer
     *
     * @param event
     */
    public final void processAWTEvent(AWTEvent event) {
        super.processEvent(event);
    }

    /**
     * @see org.jnode.awt.swingpeers.ISwingPeer#validatePeerOnly()
     */
    public final void validatePeerOnly() {
        super.validate();
    }
}
