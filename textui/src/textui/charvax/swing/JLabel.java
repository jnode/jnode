/* class JLabel
 *
 * Copyright (C) 2001-2003  R M Pitman
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package charvax.swing;

import charva.awt.Dimension;
import charva.awt.Insets;
import charva.awt.Point;
import charva.awt.Toolkit;
import charva.awt.event.KeyEvent;

/**
 * A display area for a short text string.  A label cannot react to keyboard
 * events and cannot receive the keyboard focus.
 */
public class JLabel
    extends JComponent
{
    /** Construct an empty label.
     */
    public JLabel() {
	this("", SwingConstants.LEFT);
    }

    /** Construct a JLabel with the specified text.
     */
    public JLabel(String text_) {
	this(text_, SwingConstants.LEFT);
    }

    /** 
     * Use this constructor if you want to set the alignment to something
     * other than left-aligned.
     */
    public JLabel(String text_, int align_) {
	_labeltext = text_;
	_width = text_.length();
	switch (align_) {
	    case SwingConstants.LEFT:
		_alignmentX = LEFT_ALIGNMENT;
		break;
	    case SwingConstants.CENTER:
		_alignmentX = CENTER_ALIGNMENT;
		break;
	    case SwingConstants.RIGHT:
		_alignmentX = RIGHT_ALIGNMENT;
		break;
	}
    }

    public void setLength(int length_) { _width = length_; }

    public Dimension minimumSize() {
	return this.getSize();
    }

    public void draw(Toolkit toolkit) {

	// Draw the border if it exists
	super.draw(toolkit);

	/* Get the absolute origin of this component.
	 */
	Point origin = getLocationOnScreen();
	Insets insets = super.getInsets();
	origin.translate(insets.left, insets.top);

	toolkit.setCursor(origin);

	// we'll sort out justification and video-attributes etc later.
	StringBuffer buf = new StringBuffer(_labeltext);
	int textlength = _labeltext.length();
	if (_width > textlength) {
	    for (int i=textlength; i<_width; i++)
		buf.append(' ');
	}
	else if (_width < textlength)
	    buf.setLength(_width);	// truncate

	int colorpair = getCursesColor();
	toolkit.addString(buf.toString(), 0, colorpair);
    }

    /**
     * This may be wrong, but it'll do for now.
     */
    public Dimension getSize() {
	return new Dimension(this.getWidth(), this.getHeight());
    }

    public int getWidth() {
	Insets insets = super.getInsets();
	return _width + insets.left + insets.right; 
    }

    public int getHeight() {
	Insets insets = super.getInsets();
	return 1 + insets.top + insets.bottom;
    }

    public synchronized void setText(String label_) {
	_labeltext = label_;

	/* If the text is shorter than it was previously, blank out the
	 * previous text.  In practice this means that the label grows
	 * in length but never shrinks.
	 */
	if (label_.length() > _width)
	    _width = label_.length();

	/* If this component is already displayed, generate a PaintEvent
	 * and post it onto the queue.
	 */
	repaint();
    }

    public String getText() { return _labeltext; }

    /** This component will not receive focus when Tab or Shift-Tab is pressed.
     */
    public boolean isFocusTraversable() { return false; }

    /** The JLabel class ignores key events. A JLabel should never
     * have input focus anyway.
     */
    public void processKeyEvent(KeyEvent ke_) { }

    /**
     * The JLabel component never gets the keyboard input focus.
     */
    public void requestFocus() {}

    public void debug(int level_) {
	for (int i=0; i<level_; i++)
	    System.err.print("    ");
	System.err.println("JLabel origin=" + _origin + 
	    " size=" + getSize() + " label=" + _labeltext);
    }

    public String toString() {
	return "JLabel: [" + getText() + "]";
    }

    //====================================================================
    // INSTANCE VARIABLES

    private String _labeltext;
    private int _width;

}
