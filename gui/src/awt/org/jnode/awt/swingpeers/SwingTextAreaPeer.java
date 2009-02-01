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

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.TextArea;
import java.awt.peer.TextAreaPeer;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * AWT text area peer implemented as a {@link javax.swing.JTextArea}.
 */

final class SwingTextAreaPeer extends
    SwingTextComponentPeer<TextArea, SwingTextArea> implements TextAreaPeer {

    //
    // Construction
    //

    public SwingTextAreaPeer(SwingToolkit toolkit, TextArea textArea) {
        super(toolkit, textArea, new SwingTextArea(textArea));

        switch (textArea.getScrollbarVisibility()) {
            case TextArea.SCROLLBARS_BOTH:
                SwingToolkit.add(textArea, new JScrollPane(peerComponent,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS));
                break;
            case TextArea.SCROLLBARS_HORIZONTAL_ONLY:
                SwingToolkit.add(textArea, new JScrollPane(peerComponent,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS));
                break;
            case TextArea.SCROLLBARS_VERTICAL_ONLY:
                SwingToolkit.add(textArea, new JScrollPane(peerComponent,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
                break;
            case TextArea.SCROLLBARS_NONE:
                SwingToolkit.add(textArea, peerComponent);
                break;
        }

        final SwingTextArea jta = (SwingTextArea) peerComponent;
        SwingToolkit.copyAwtProperties(textArea, jta);
        setText(textArea.getText());
        jta.setRows(textArea.getRows());
        jta.setColumns(textArea.getColumns());
        setEditable(textArea.isEditable());
    }

    /**
     * @see java.awt.peer.TextAreaPeer#getMinimumSize(int, int)
     */
    public Dimension getMinimumSize(int rows, int cols) {
        return ((JTextArea) peerComponent).getMinimumSize();
    }

    /**
     * @see java.awt.peer.TextAreaPeer#getPreferredSize(int, int)
     */
    public Dimension getPreferredSize(int rows, int cols) {
        return ((JTextArea) peerComponent).getPreferredSize();
    }

    /**
     * @see java.awt.peer.TextAreaPeer#insert(java.lang.String, int)
     */
    public void insert(String text, int pos) {
        insertText(text, pos);
    }

    /**
     * @see java.awt.peer.TextAreaPeer#insertText(java.lang.String, int)
     */
    public void insertText(String text, int pos) {
        try {
            ((JTextArea) peerComponent).getDocument().insertString(pos, text, null);
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @see java.awt.peer.TextAreaPeer#minimumSize(int, int)
     */
    public Dimension minimumSize(int rows, int cols) {
        return getMinimumSize(rows, cols);
    }

    /**
     * @see java.awt.peer.TextAreaPeer#preferredSize(int, int)
     */
    public Dimension preferredSize(int rows, int cols) {
        return getPreferredSize(rows, cols);
    }

    /**
     * @see java.awt.peer.TextAreaPeer#replaceRange(java.lang.String, int, int)
     */
    public void replaceRange(String text, int start_pos, int end_pos) {
        replaceText(text, start_pos, end_pos);
    }

    /**
     * @see java.awt.peer.TextAreaPeer#replaceText(java.lang.String, int, int)
     */
    public void replaceText(String text, int start_pos, int end_pos) {
        try {
            final Document doc = ((JTextArea) peerComponent).getDocument();
            doc.remove(start_pos, end_pos - start_pos);
            doc.insertString(start_pos, text, null);
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }
    }

}

final class SwingTextArea extends JTextArea implements ISwingPeer<TextArea> {
    private final TextArea awtComponent;

    public SwingTextArea(TextArea awtComponent) {
        this.awtComponent = awtComponent;
    }

    /**
     * @see org.jnode.awt.swingpeers.ISwingPeer#getAWTComponent()
     */
    public TextArea getAWTComponent() {
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
