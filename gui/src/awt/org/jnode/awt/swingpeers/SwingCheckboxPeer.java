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
        final JCheckBox jcb = (JCheckBox) peerComponent;
        SwingToolkit.add(checkBox, jcb);
        SwingToolkit.copyAwtProperties(checkBox, jcb);
        jcb.setText(checkBox.getLabel());
        setState(checkBox.getState());

    }

    public void setCheckboxGroup(CheckboxGroup g) {
    }

    public void setState(boolean state) {
        ((JCheckBox) peerComponent).setSelected(state);
    }

    public void setLabel(String label) {
        ((JCheckBox) peerComponent).setText(label);
    }
}

final class SwingCheckBox extends JCheckBox implements ISwingPeer<Checkbox> {
    private final Checkbox awtComponent;

    public SwingCheckBox(Checkbox awtComponent) {
        this.awtComponent = awtComponent;
    }

    /**
     * @see org.jnode.awt.swingpeers.ISwingPeer#getAWTComponent()
     */
    public Checkbox getAWTComponent() {
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
