/* class JProgressBar
 *
 * Copyright (C) 2001, 2002, 2003  R M Pitman
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
 * A component that displays an integer value within a bounded interval.
 * A progress bar is typically used to indicate the progress of some task
 * by displaying a percentage of completion and possibly a textual display
 * of this percentage.
 */
public class JProgressBar
    extends JComponent
{
    /** Creates a horizontal progress bar that displays a border
     * but no progress string.
     */
    public JProgressBar() {
    }

    /** Creates a progress bar with the specified minimum and
     * maximum values.
     */
    public JProgressBar(int min_, int max_) {
	_minimum = min_;
	_maximum = max_;
    }

    /** Set the progress bar's minimum value.
     */
    public void setMinimum(int min_) {
	_minimum = min_;
	if (_maximum <= _minimum)
	    _maximum = _minimum + 1;
	if (_value < _minimum)
	    _value = _minimum;
    }

    /** Set the progress bar's value.
     */
    public void setValue(int value_) {
	if (value_ < _minimum)
	    _value = _minimum;
	else
	    _value = value_;

	/* If this component is already displayed, generate a PaintEvent
	 * and post it onto the queue.
	 */
	repaint();
    }

    /** Set the progress bar's maximum value.
     */
    public void setMaximum(int max_) {
	_maximum = max_;
	if (_minimum > _maximum)
	    _minimum = _maximum - 1;
	if (_value > _maximum)
	    _value = _maximum;
    }

    /**
     * Set the size of the component on the screen. 
     */
    public void setSize(Dimension size_) {
	_width = size_.width;

	if (_width < 3) {
	    throw new IllegalArgumentException(
		    "length of progress bar must be at least 3");
	}
    }

    /** Get the screen size of the progress bar.
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

    public void draw(Toolkit toolkit) {

	// Draw the border if it exists
	super.draw(toolkit);

	/* Get the absolute origin of this component.
	 */
	Point origin = getLocationOnScreen();
	Insets insets = super.getInsets();
	origin.translate(insets.left, insets.top);

	int colorpair = getCursesColor();

	int offset = ((_value - _minimum) * _width) / _maximum;

	if ( ! isIndeterminate()) {
	    for (int i=0; i< offset; i++) {
		toolkit.setCursor(origin.addOffset(i, 0));
		toolkit.addChar(' ', Toolkit.A_REVERSE, colorpair);
	    }
	    for (int k=offset; k<_width; k++) {
		toolkit.setCursor(origin.addOffset(k,0));
		toolkit.addChar(Toolkit.ACS_CKBOARD, 0, colorpair);
	    }
	}
	else {
	    for (int i=0; i< _width; i++) {
		toolkit.setCursor(origin.addOffset(i, 0));
		toolkit.addChar(' ', 0, colorpair);
	    }
	    toolkit.setCursor(origin.addOffset(offset, 0));
	    toolkit.addChar(' ', Toolkit.A_REVERSE, colorpair);
	}

	// Display the progress string if required
	if (isStringPainted()) {
	    offset = (getSize().width - _string.length()) / 2;
	    toolkit.setCursor(origin.addOffset(offset, 0));
	    toolkit.addString(_string, 0, colorpair);
	}
    }

    /** This component will not receive focus when Tab or Shift-Tab is pressed.
     */
    public boolean isFocusTraversable() { return false; }

    /** The JProgressBar class ignores key events. A JProgressBar should never
     * have input focus anyway.
     */
    public void processKeyEvent(KeyEvent ke_) { }

    /**
     * The JProgressBar component never gets the keyboard input focus.
     */
    public void requestFocus() {}

    public void debug(int level_) {
	for (int i=0; i<level_; i++)
	    System.err.print("    ");
	System.err.println("JProgressBar origin=" + _origin + 
	    " size=" + getSize() + " value=" + _value +
	    " minimum=" + _minimum + " maximum=" + _maximum);
    }

    public Dimension minimumSize() { return getSize(); }

    public int getMinimum() { return _minimum; }
    public int getValue() { return _value; }
    public int getMaximum() { return _maximum; }

    /** Returns the value of the _stringPainted property */
    public boolean isStringPainted() { return _stringPainted; }

    /** Set the value of the _stringPainted property */
    public void setStringPainted(boolean stringPainted_) {
	_stringPainted = stringPainted_;
    }

    /** Sets the value of the progress string */
    public void setString(String string_) { 
	_string = string_; 
	repaint();
    }

    public void setIndeterminate(boolean newval) {
	if (newval == _indeterminate)
	    return;   // no change in state.

	_indeterminate = newval;
	if ( newval) {
	    setMinimum(0);
	    setMaximum(100);
	    _indeterminateThread = new IndeterminateThread();
	    _indeterminateThread.start();
	}
	else {
	    if (_indeterminateThread != null &&
		    _indeterminateThread.isAlive()) {

		_indeterminateThread.interrupt();
	    }
	}
    }

    public boolean isIndeterminate() {
	return _indeterminate;
    }

    /** Returns the value of the progress string */
    public String getString() { return _string; }

    public void finalize() {
	if (_indeterminateThread != null &&
	    _indeterminateThread.isAlive()) {

	    _indeterminateThread.interrupt();
	}
    }

    /** A nonstatic inner class that updates the progress bar 
     * once per second when the progress bar is in "indeterminate" mode.
     */
    private class IndeterminateThread extends Thread
    {
	/** Constructor */
	private IndeterminateThread() { }

	/** 
	 * Twice per second, wake up and update the progress bar. Note that
	 * since this thread is not the event-dispatching thread, we cannot
	 * manipulate the screen components directly; instead, we must
	 * call the static method "invokeLater()" of the EventQueue class,
	 * which will cause the event-dispatching thread to update the progress
	 * bar.
	 * See "Core Java, Volume II" by Horstmann and Cornell, chapter 1;
	 * Also see
	 * http://java.sun.com/docs/books/tutorial/uiswing/overview/threads.html
	 */
	public void run() {
	    try {
		while (true) {
		    this.adjust();
		    Thread.sleep(500L);
		    SwingUtilities.invokeLater(new Runnable() 
			{
			    public void run() {
				JProgressBar.this.setValue(_percent);
			    }
			});
		}
	    }
	    catch (InterruptedException e) {
		return;
	    }
	}

	/** Adjust the percent-completed so that the indicator moves right
	 * and left continuously.
	 */
	private void adjust() {
	    if (_right) {
		if (_percent < 96)
		    _percent += 4;
		else {
		    _right = false;
		}
	    }
	    else {
		if (_percent > 0)
		    _percent -= 4;
		else {
		    _right = true;
		}
	    }
	}

	boolean _right = true;
	int _percent = 0;
    }

    //********************************************************************
    // INSTANCE VARIABLES

    protected int _minimum = 0;
    protected int _value = 0;
    protected int _maximum = 100;
    protected boolean _stringPainted = false;
    protected String _string = "";

    /** The length of this component on the screen.
     */
    protected int _width = 50;	// default size

    protected boolean _indeterminate;

    protected Thread _indeterminateThread;
}
