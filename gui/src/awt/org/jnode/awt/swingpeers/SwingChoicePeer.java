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
import java.awt.Choice;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.peer.ChoicePeer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 * AWT choice peer implemented as a {@link javax.swing.JButton}.
 *
 * @author Levente S\u00e1ntha
 */

final class SwingChoicePeer extends SwingComponentPeer<Choice, SwingChoice> implements ChoicePeer {

    //
    // Construction
    //

    public SwingChoicePeer(SwingToolkit toolkit, Choice choice) {
        super(toolkit, choice, new SwingChoice(choice));
        peerComponent.setPeer(this);
        final JComboBox combo = peerComponent;
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        final int cnt = choice.getItemCount();
        for (int i = 0; i < cnt; i++) {
            model.addElement(choice.getItem(i));
        }
        combo.setModel(model);
        SwingToolkit.add(choice, combo);
        SwingToolkit.copyAwtProperties(choice, combo);
    }

    //
    // ChoicePeer
    //

    public void remove(int index) {
        model().removeElementAt(index);
    }

    public void removeAll() {
        model().removeAllElements();
    }

    public void add(String item, int index) {
        model().insertElementAt(item, index);
    }

    private DefaultComboBoxModel model() {
        return ((DefaultComboBoxModel) ((JComboBox) peerComponent).getModel());
    }

    // Deprecated

    public void addItem(String item, int index) {
        add(item, index);
    }

    public void select(int index) {
        if (!peersSelection)
            peerComponent.setSelectedIndex(index);
    }

    private boolean peersSelection = false;

    void peerSelect(int index) {
        try {
            peersSelection = true;
            targetComponent.select(index);
        } finally {
            peersSelection = false;
        }
    }

}

final class SwingChoice extends JComboBox implements ISwingPeer<Choice> {
    private final Choice awtComponent;
    private SwingChoicePeer peer;

    public SwingChoice(Choice awtComponent) {
        this.awtComponent = awtComponent;
    }

    public void setPeer(SwingChoicePeer peer) {
        this.peer = peer;
    }

    public Choice getAWTComponent() {
        return awtComponent;
    }

    @Override
    protected void fireActionEvent() {
        super.fireActionEvent();
        awtComponent.dispatchEvent(new ActionEvent(awtComponent, ActionEvent.ACTION_PERFORMED, getActionCommand()));
    }

    @Override
    protected void fireItemStateChanged(ItemEvent e) {
        super.fireItemStateChanged(e);
        if (e.getStateChange() == ItemEvent.SELECTED) {
            peer.peerSelect(getSelectedIndex());
            awtComponent.dispatchEvent(SwingToolkit.convertEvent(e, awtComponent));
        }
    }

    /**
     * Pass an event onto the AWT component.
     *
     * @see java.awt.Component#processEvent(java.awt.AWTEvent)
     */
    protected final void processEvent(AWTEvent event) {
        awtComponent.dispatchEvent(SwingToolkit.convertEvent(event, awtComponent));
    }

    public final void processAWTEvent(AWTEvent event) {
        super.processEvent(event);
    }

    public final void validatePeerOnly() {
        super.validate();
    }
}

