/* class MultiLineLabel
 *
 * Copyright (C) 2001  R M Pitman
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

package charva.awt.util;

import java.util.Enumeration;
import java.util.Vector;

import charva.awt.Component;
import charva.awt.Dimension;
import charva.awt.Point;
import charva.awt.Toolkit;
import charva.awt.event.KeyEvent;

/**
 * Implements a label that displays multiple lines of text.
 */
public class MultiLineLabel
    extends Component
{

    public MultiLineLabel(String[] textarray_) {
	for (int i=0; i<textarray_.length; i++)
	    _labeltext.add(textarray_[i]);
    }

    public Dimension minimumSize() {
	int width=0, height=0;

	Enumeration<String> e = _labeltext.elements();
	while (e.hasMoreElements()) {
	    String s = (String) e.nextElement();
	    if (s.length() > width)
		width = s.length();
	    height++;
	}

	return new Dimension(width, height);
    }

    public void draw(Toolkit toolkit) {

	/* Get the absolute origin of this component.
	 */
	Point origin = getLocationOnScreen();


	// we'll sort out justification and video-attributes etc later.
	Enumeration<String> e = _labeltext.elements();
	for (int row=0; e.hasMoreElements(); row++) {
	    String s = (String) e.nextElement();
	    toolkit.setCursor(origin.addOffset(0, row));
	    toolkit.addString(s, 0, 0);
	}
    }

    /**
     * This may be wrong, but it'll do for now.
     */
    public Dimension getSize() {
	return minimumSize();
    }

    public int getWidth() { return getSize().width; }
    public int getHeight() { return getSize().height; }

    public synchronized void setText(String[] textarray_) {
	_labeltext.removeAllElements();
	for (int i=0; i<textarray_.length; i++)
	    _labeltext.add(textarray_[i]);

	/* If this component is already displayed, generate a PaintEvent
	 * and post it onto the queue.
	 */
	repaint();
    }

    public String[] getText() {
	String[] strings = new String[_labeltext.size()];
	Enumeration<String> e = _labeltext.elements();
	for (int i=0; e.hasMoreElements(); i++) {
	    String s = (String) e.nextElement();
	    strings[i] = s;
	}
	return strings;
    }

    /** This component will not receive focus when Tab or Shift-Tab is pressed.
     */
    public boolean isFocusTraversable() { return false; }

    /** The MultiLineLabel class ignores key events. A MultiLineLabel should 
     * never have input focus anyway.
     */
    public void processKeyEvent(KeyEvent ke_) { }

    /**
     * The MultiLineLabel component never gets the keyboard input focus.
     */
    public void requestFocus() {}

    public void debug(int level_) {
	for (int i=0; i<level_; i++)
	    System.err.print("    ");
	System.err.println("MultiLineLabel origin=" + _origin + 
	    " size=" + getSize() + " label=" + (String) _labeltext.elementAt(0));
    }

    private Vector<String> _labeltext = new Vector<String>();

    //private int _justification = LEFT;

    public static final int LEFT = 1;
    public static final int CENTER = 2;
    public static final int RIGHT = 3;
}
