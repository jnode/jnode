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
import java.awt.Label;
import java.awt.peer.LabelPeer;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * AWT label peer implemented as a {@link javax.swing.JLabel}.
 */
final class SwingLabelPeer extends SwingComponentPeer<Label, SwingLabel>
    implements LabelPeer {

    /**
     * Initialize this instance.
     *
     * @param toolkit
     * @param label
     */
    public SwingLabelPeer(SwingToolkit toolkit, Label label) {
        super(toolkit, label, new SwingLabel(label));
        SwingToolkit.add(label, peerComponent);
        SwingToolkit.copyAwtProperties(label, peerComponent);
        setText(label.getText());
        setAlignment(label.getAlignment());
    }

    /**
     * Sets the text.
     *
     * @see java.awt.peer.LabelPeer#setText(java.lang.String)
     */
    public void setText(String text) {
        peerComponent.setText(text);
    }

    /**
     * Sets the text alignment.
     *
     * @see java.awt.peer.LabelPeer#setAlignment(int)
     */
    public void setAlignment(int alignment) {
        switch (alignment) {
            case Label.LEFT:
                peerComponent.setHorizontalAlignment(SwingConstants.LEFT);
                break;
            case Label.CENTER:
                peerComponent.setHorizontalAlignment(SwingConstants.CENTER);
                break;
            case Label.RIGHT:
                peerComponent.setHorizontalAlignment(SwingConstants.RIGHT);
                break;
        }
    }
}

final class SwingLabel extends JLabel implements ISwingPeer<Label> {
    private final Label awtComponent;

    public SwingLabel(Label awtComponent) {
        this.awtComponent = awtComponent;
        setOpaque(true);
    }

    /**
     * @see org.jnode.awt.swingpeers.ISwingPeer#getAWTComponent()
     */
    public Label getAWTComponent() {
        return awtComponent;
    }

    /**
     * Pass an event onto the AWT component.
     *
     * @see java.awt.Component#processEvent(java.awt.AWTEvent)
     */
    protected final void processEvent(AWTEvent event) {
        awtComponent.dispatchEvent(SwingToolkit.convertEvent(event,
            awtComponent));
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
