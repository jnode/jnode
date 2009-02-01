/*
 * $Id$
 *
 * JNode.org
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

import java.awt.Rectangle;
import java.awt.TextComponent;
import java.awt.im.InputMethodRequests;
import java.awt.peer.TextComponentPeer;
import javax.swing.text.JTextComponent;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
abstract class SwingTextComponentPeer<awtT extends TextComponent, peerT extends JTextComponent>
    extends SwingComponentPeer<awtT, peerT> implements TextComponentPeer {

    public SwingTextComponentPeer(SwingToolkit toolkit,
                                  awtT textComponent, peerT peer) {
        super(toolkit, textComponent, peer);

        SwingToolkit.add(textComponent, peerComponent);
        SwingToolkit.copyAwtProperties(textComponent, peerComponent);
        setText(textComponent.getText());
        setEditable(textComponent.isEditable());
    }

    /**
     * @see java.awt.peer.TextComponentPeer#filterEvents(long)
     */
    public long filterEvents(long filter) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see java.awt.peer.TextComponentPeer#getCaretPosition()
     */
    public int getCaretPosition() {
        return peerComponent.getCaretPosition();
    }

    /**
     * @see java.awt.peer.TextComponentPeer#getCharacterBounds(int)
     */
    public Rectangle getCharacterBounds(int pos) {
        // TODO implement me
        return null;
    }

    /**
     * @see java.awt.peer.TextComponentPeer#getIndexAtPoint(int, int)
     */
    public int getIndexAtPoint(int x, int y) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see java.awt.peer.TextComponentPeer#getSelectionEnd()
     */
    public int getSelectionEnd() {
        return peerComponent.getSelectionEnd();
    }

    /**
     * @see java.awt.peer.TextComponentPeer#getSelectionStart()
     */
    public int getSelectionStart() {
        return peerComponent.getSelectionStart();
    }

    /**
     * @see java.awt.peer.TextComponentPeer#getText()
     */
    public String getText() {
        return peerComponent.getText();
    }

    /**
     * @see java.awt.peer.TextComponentPeer#select(int, int)
     */
    public void select(int start_pos, int end_pos) {
        peerComponent.select(start_pos, end_pos);
    }

    /**
     * @see java.awt.peer.TextComponentPeer#setCaretPosition(int)
     */
    public void setCaretPosition(int pos) {
        peerComponent.setCaretPosition(pos);
    }

    /**
     * @see java.awt.peer.TextComponentPeer#setEditable(boolean)
     */
    public void setEditable(boolean editable) {
        peerComponent.setEditable(editable);
    }

    /**
     * @see java.awt.peer.TextComponentPeer#setText(java.lang.String)
     */
    public void setText(String text) {
        peerComponent.setText(text);
    }

    public InputMethodRequests getInputMethodRequests() {
        return null;  //TODO implement it
    }
}
