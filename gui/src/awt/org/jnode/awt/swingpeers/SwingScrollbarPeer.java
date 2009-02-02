/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
import java.awt.Scrollbar;
import java.awt.peer.ScrollbarPeer;
import javax.swing.JScrollBar;

/**
 * AWT scrollbar peer implemented as a {@link javax.swing.JScrollBar}.
 */

final class SwingScrollbarPeer extends SwingComponentPeer<Scrollbar, SwingScrollbar> implements
    ScrollbarPeer {

    //
    // Construction
    //

    public SwingScrollbarPeer(SwingToolkit toolkit, Scrollbar sb) {
        super(toolkit, sb, new SwingScrollbar(sb));
        SwingToolkit.add(sb, peerComponent);
        SwingToolkit.copyAwtProperties(sb, peerComponent);
        final SwingScrollbar jsb = (SwingScrollbar) peerComponent;
        jsb.setOrientation(sb.getOrientation());
        jsb.setBlockIncrement(sb.getBlockIncrement());
        jsb.setUnitIncrement(sb.getUnitIncrement());
        setValues(sb.getValue(), sb.getVisibleAmount(), sb.getMinimum(), sb
            .getMaximum());
    }

    /**
     * @see java.awt.peer.ScrollbarPeer#setLineIncrement(int)
     */
    public void setLineIncrement(int inc) {
        ((JScrollBar) peerComponent).setUnitIncrement(inc);
    }

    /**
     * @see java.awt.peer.ScrollbarPeer#setPageIncrement(int)
     */
    public void setPageIncrement(int inc) {
        ((JScrollBar) peerComponent).setBlockIncrement(inc);
    }

    /**
     * @see java.awt.peer.ScrollbarPeer#setValues(int, int, int, int)
     */
    public void setValues(int value, int visible, int min, int max) {
        ((JScrollBar) peerComponent).setValues(value, visible, min, max);
    }

}

final class SwingScrollbar extends JScrollBar implements ISwingPeer<Scrollbar> {
    private final Scrollbar awtComponent;

    public SwingScrollbar(Scrollbar awtComponent) {
        this.awtComponent = awtComponent;
    }

    /**
     * @see org.jnode.awt.swingpeers.ISwingPeer#getAWTComponent()
     */
    public Scrollbar getAWTComponent() {
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
