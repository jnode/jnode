/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.BufferCapabilities;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.TextArea;
import java.awt.event.PaintEvent;
import java.awt.peer.TextAreaPeer;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;

/**
 * AWT text area peer implemented as a {@link javax.swing.JTextArea}.
 */

class SwingTextAreaPeer extends JTextArea implements TextAreaPeer, SwingPeer {

	private final TextArea textArea;

	//
	// Construction
	//

	public SwingTextAreaPeer(TextArea textArea) {
		this.textArea = textArea;

		switch (textArea.getScrollbarVisibility()) {
		case TextArea.SCROLLBARS_BOTH:
			SwingToolkit.add(textArea, new JScrollPane(this,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS));
			break;
		case TextArea.SCROLLBARS_HORIZONTAL_ONLY:
			SwingToolkit.add(textArea, new JScrollPane(this,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS));
			break;
		case TextArea.SCROLLBARS_VERTICAL_ONLY:
			SwingToolkit.add(textArea, new JScrollPane(this,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
			break;
		case TextArea.SCROLLBARS_NONE:
			SwingToolkit.add(textArea, this);
			break;
		}

		SwingToolkit.copyAwtProperties(textArea, this);
		setText(textArea.getText());
		setRows(textArea.getRows());
		setColumns(textArea.getColumns());
		setEditable(textArea.isEditable());
	}

	public boolean canDetermineObscurity() {
		return false;
	}

	public void coalescePaintEvent(PaintEvent e) {
		System.err.println(e);
	}

	// Buffer

	public void createBuffers(int x, BufferCapabilities bufferCapabilities) {
	}

	public void destroyBuffers() {
	}

	// Misc

	public void dispose() {
	}

	public long filterEvents(long mask) {
		return 0;
	}

	public void flip(BufferCapabilities.FlipContents flipContents) {
	}

	/**
	 * @see org.jnode.awt.swingpeers.SwingPeer#getAWTComponent()
	 */
	public Component getAWTComponent() {
		return textArea;
	}

	public Image getBackBuffer() {
		return null;
	}

	public Rectangle getCharacterBounds(int i) {
		return null;
	}

	//
	// TextComponentPeer
	//

	public int getIndexAtPoint(int x, int y) {
		return 0;
	}

	public Dimension getMinimumSize(int rows, int columns) {
		return null;
	}

	//
	// TextAreaPeer
	//

	public Dimension getPreferredSize(int rows, int columns) {
		return null;
	}

	//
	// ComponentPeer
	//

	// Events

	public void handleEvent(AWTEvent e) {
		//System.err.println(e);
	}

	public boolean handlesWheelScrolling() {
		return false;
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// Private
	/**
	 * @see java.awt.peer.TextAreaPeer#insert(java.lang.String, int)
	 */
	public void insert(String text, int pos) {
		try {
			super.getDocument().insertString(pos, text, null);
		} catch (BadLocationException ex) {
			throw new RuntimeException(ex);
		}
	}

	// Deprectated

	public void insertText(String txt, int pos) {
		insert(txt, pos);
	}

	// Obscurity

	public boolean isObscured() {
		return false;
	}

	public Dimension minimumSize(int rows, int cols) {
		return getMinimumSize(rows, cols);
	}

	public Dimension preferredSize(int rows, int cols) {
		return getPreferredSize(rows, cols);
	}

	/**
	 * @see java.awt.peer.TextAreaPeer#replaceRange(java.lang.String, int, int)
	 */
	public void replaceRange(String text, int start_pos, int end_pos) {
		// TODO implement me
	}

	public void replaceText(String txt, int start, int end) {
		replaceRange(txt, start, end);
	}

	// Focus

	public boolean requestFocus(Component lightweightChild, boolean temporary,
			boolean focusedWindowChangeAllowed, long time) {
		return true;
	}

	/**
	 * @see java.awt.peer.ComponentPeer#setEventMask(long)
	 */
	public void setEventMask(long mask) {
		// TODO Auto-generated method stub

	}

	// Cursor

	public void updateCursorImmediately() {
	}
}