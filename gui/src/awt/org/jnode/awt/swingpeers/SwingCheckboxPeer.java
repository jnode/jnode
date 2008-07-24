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
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.peer.CheckboxPeer;
import javax.swing.JCheckBox;

/**
 * AWT checkbox peer implemented as a {@link javax.swing.JCheckBox}.
 */
final class SwingCheckboxPeer extends SwingComponentPeer<Checkbox, SwingCheckBox> implements CheckboxPeer {

    //
    // Construction
    //

    public SwingCheckboxPeer(SwingToolkit toolkit, Checkbox checkBox) {
        super(toolkit, checkBox, new SwingCheckBox(checkBox));
        final SwingCheckBox jcb = peerComponent;
        peerComponent.setPeer(this);
        SwingToolkit.add(checkBox, jcb);
        SwingToolkit.copyAwtProperties(checkBox, jcb);
        jcb.setText(checkBox.getLabel());
        peerComponent.setSelected(checkBox.getState());

    }


    public void setCheckboxGroup(CheckboxGroup g) {

    }

    private boolean peerSetState = false;

    public void setState(boolean state) {
        if (!peerSetState)
            peerComponent.setSelected(state);
    }

    void peerSetState(boolean state) {
        try {
            peerSetState = true;
            targetComponent.setState(state);
        } finally {
            peerSetState = false;
        }
    }

    public void setLabel(String label) {
        peerComponent.setText(label);
    }
}

final class SwingCheckBox extends JCheckBox implements ISwingPeer<Checkbox> {
    private final Checkbox awtComponent;
    private SwingCheckboxPeer peer;

    public SwingCheckBox(Checkbox awtComponent) {
        this.awtComponent = awtComponent;
    }

    public void setPeer(SwingCheckboxPeer peer) {
        this.peer = peer;
    }

    /**
     * @see org.jnode.awt.swingpeers.ISwingPeer#getAWTComponent()
     */
    public Checkbox getAWTComponent() {
        return awtComponent;
    }

    @Override
    protected void fireActionPerformed(ActionEvent event) {
        super.fireActionPerformed(event);
        awtComponent.dispatchEvent(new ActionEvent(awtComponent, ActionEvent.ACTION_PERFORMED, getActionCommand()));
    }

    @Override
    protected void fireItemStateChanged(ItemEvent event) {
        super.fireItemStateChanged(event);
        awtComponent.dispatchEvent(SwingToolkit.convertEvent(event, awtComponent));
        peer.peerSetState(isSelected());
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
     * @param event the AWT event
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
