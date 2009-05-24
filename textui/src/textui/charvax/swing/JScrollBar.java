/* class JScrollBar
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

package charvax.swing;

import java.util.Enumeration;
import java.util.Vector;

import charva.awt.Adjustable;
import charva.awt.Dimension;
import charva.awt.Point;
import charva.awt.Toolkit;
import charva.awt.event.AdjustmentEvent;
import charva.awt.event.AdjustmentListener;
import charva.awt.event.KeyEvent;

/**
 * An implementation of a scrollbar. The user positions the knob in the
 * scrollbar to determine the contents of the viewing area.
 */
public class JScrollBar
    extends JComponent
    implements Adjustable
{
    /** Creates a vertical scrollbar with default values.
     */
    public JScrollBar() {
    }

    /** Creates a scrollbar with the specified orientation
     * and with default values.
     */
    public JScrollBar(int orientation_) {
	setOrientation(orientation_);
    }

    /** Creates a scrollbar with the specified orientation, value,
     * extent, min and max.
     */
    public JScrollBar(int orientation_, int value_, 
	    int extent_, int min_, int max_) {

	setOrientation(orientation_);
	if (min_ > value_ || extent_ < 0 || value_ + extent_ > max_)
	    throw new IllegalArgumentException("invalid values for scrollbar");

	_minimum = min_;
	_value = value_;
	_extent = extent_;
	_blockIncrement = _extent;
	_maximum = max_;
    }

    /** Gets the scrollbar's orientation (VERTICAL or HORIZONTAL)
     */
    public int getOrientation() { return _orientation; }

    /** Set the scrollbar's minimum value.
     */
    public void setMinimum(int min_) {
	_minimum = min_;
	if (_maximum <= _minimum)
	    _maximum = _minimum + 1;
	if (_value < _minimum)
	    _value = _minimum;
	if (_value + _extent > _maximum)
	    _extent = _maximum - _value;
    }

    /** Set the scrollbar's value.
     */
    public void setValue(int value_) {
	if (value_ < _minimum)
	    _value = _minimum;
	else if (value_ > _maximum - _extent)
	    _value = _maximum - _extent;
	else
	    _value = value_;

	/* If this component is already displayed, generate a PaintEvent
	 * and post it onto the queue.
	 */
	repaint();
    }

    /** Set the scrollbar's extent (a.k.a "visible amount").
     */
    public void setVisibleAmount(int extent_) {
	if (_value + _extent > _maximum)
	    _extent = _maximum - _value;
	else
	    _extent = extent_;
    }

    /** Set the scrollbar's maximum value.
     */
    public void setMaximum(int max_) {
	_maximum = max_;
	if (_minimum > _maximum)
	    _minimum = _maximum - 1;
	if (_value > _maximum)
	    _value = _maximum;
	if (_value + _extent > _maximum)
	    _extent = _maximum - _value;
    }

    /** Sets the block increment of the scrollbar.
     */
    public void setBlockIncrement(int val_) {
	_blockIncrement = val_;
    }

    /**
     * Set the size of the component on the screen.  If the scrollbar is
     * vertical, ignore the specified width, and if it is horizontal, ignore
     * the specified height.
     */
    public void setSize(Dimension size_) {
	if (_orientation == Adjustable.VERTICAL)
	    _length = size_.height;
	else
	    _length = size_.width;

	if (_length < 3) {
	    throw new IllegalArgumentException(
		    "length of scrollbar must be at least 3");
	}
    }

    /** Get the screen size of the scrollbar.
     */
    public Dimension getSize() {
	return new Dimension(this.getWidth(), this.getHeight());
    }

    public int getWidth() {
	return (_orientation == Adjustable.VERTICAL) ? 1 : _length;
    }

    public int getHeight() {
	return (_orientation == Adjustable.VERTICAL) ? _length : 1;
    }

    public void draw(Toolkit toolkit) {

	/* Get the absolute origin of this component.
	 */
	Point origin = getLocationOnScreen();

	int colorpair = getCursesColor();

	if (super._enabled) {
	    int offset = _value * (_length-2) / _maximum;
	    int visible = _extent * (_length-2) / _maximum;
	    visible = (visible == 0) ? 1 : visible;

	    if (_orientation == Adjustable.VERTICAL) {
		toolkit.setCursor(origin);
		toolkit.addChar('^', Toolkit.A_REVERSE, colorpair);
		for (int k=1; k<_length-1; k++) {
		    toolkit.setCursor(origin.addOffset(0, k));
		    toolkit.addChar(Toolkit.ACS_CKBOARD, 0, colorpair);
		}
		toolkit.setCursor(origin.addOffset(0, _length-1));
		toolkit.addChar('v', Toolkit.A_REVERSE, colorpair);

		for (int i=0; i< visible; i++) {
		    toolkit.setCursor(origin.addOffset(0, 1+offset+i));
		    toolkit.addChar(' ', Toolkit.A_REVERSE, colorpair);
		}
	    }
	    else {
		toolkit.setCursor(origin);
		toolkit.addChar('<', Toolkit.A_REVERSE, colorpair);
		for (int k=1; k<_length-1; k++) {
		    toolkit.setCursor(origin.addOffset(k,0));
		    toolkit.addChar(Toolkit.ACS_CKBOARD, 0, colorpair);
		}
		toolkit.setCursor(origin.addOffset(_length-1, 0));
		toolkit.addChar('>', Toolkit.A_REVERSE, colorpair);

		for (int i=0; i< visible; i++) {
		    toolkit.setCursor(origin.addOffset(1+offset+i, 0));
		    toolkit.addChar(' ', Toolkit.A_REVERSE, colorpair);
		}
	    }
	}
    }

    public void processKeyEvent(KeyEvent ke_) {
	/* First call all KeyListener objects that may have been registered
	 * for this component. 
	 */
	super.processKeyEvent(ke_);

	/* Check if any of the KeyListeners consumed the KeyEvent.
	 */
	if (ke_.isConsumed())
	    return;

	int key = ke_.getKeyCode();
	if (key == '\t') {
	    getParent().nextFocus();
	    return;
	}
	else if (key == KeyEvent.VK_BACK_TAB) {
	    getParent().previousFocus();
	    return;
	}

	/* Post an AdjustmentEvent if LEFT or UP arrow was pressed.
	 */
	else if ((key == KeyEvent.VK_LEFT && 
		_orientation == Adjustable.HORIZONTAL) ||
		(key == KeyEvent.VK_UP && 
		_orientation == Adjustable.VERTICAL)) {

	    int newvalue = _value - _blockIncrement;
	    setValue(newvalue);

	    AdjustmentEvent ae = new AdjustmentEvent(this, _value);
	    Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(ae);
	}

	/* Post an AdjustmentEvent if RIGHT or DOWN arrow was pressed.
	 */
	else if ((key == KeyEvent.VK_RIGHT && 
		_orientation == Adjustable.HORIZONTAL) ||
		(key == KeyEvent.VK_DOWN && 
		_orientation == Adjustable.VERTICAL)) {

	    int newvalue = _value + _blockIncrement;
	    setValue(newvalue);

	    AdjustmentEvent ae = new AdjustmentEvent(this, _value);
	    Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(ae);
	}
    }

    public void requestFocus() {
	/* Generate the FOCUS_GAINED event.
	 */
	super.requestFocus();

	/* Get the absolute origin of this component.
	 */
	Point origin = getLocationOnScreen();
	int offset = _value * (_length-2) / _maximum;
	Toolkit.getDefaultToolkit().setCursor(origin.addOffset(1+offset, 0));
    }

    /**
     * Register an AdjustmentListener object for this component.
     */
    public void addAdjustmentListener(AdjustmentListener listener_) {
	if (_adjustmentListeners == null)
	    _adjustmentListeners = new Vector<AdjustmentListener>();
	_adjustmentListeners.add(listener_);
    }

    public void removeAdjustmentListener(AdjustmentListener listener_) {
	if (_adjustmentListeners == null)
	    return;
	_adjustmentListeners.remove(listener_);
    }

    public void processAdjustmentEvent(AdjustmentEvent evt_) {
	if (_adjustmentListeners != null) {
	    for (Enumeration<AdjustmentListener> e = _adjustmentListeners.elements(); 
		    e.hasMoreElements(); ) {

		AdjustmentListener al = (AdjustmentListener) e.nextElement();
		al.adjustmentValueChanged(evt_);
	    }
	}
    }

    public void debug(int level_) {
	for (int i=0; i<level_; i++)
	    System.err.print("    ");
	System.err.println("JScrollBar origin=" + _origin + 
	    " size=" + getSize() + " value=" + _value +
	    " extent=" + _extent + " minimum=" + _minimum +
	    " maximum=" + _maximum);
    }

    public Dimension minimumSize() { return getSize(); }

    public int getMinimum() { return _minimum; }
    public int getValue() { return _value; }
    public int getVisibleAmount() { return _extent; }
    public int getMaximum() { return _maximum; }
    public int getBlockIncrement() { return _blockIncrement; }

    /** Set the orientation to VERTICAL or HORIZONTAL.
     */
    private void setOrientation(int orientation_) {
	if (orientation_ != Adjustable.VERTICAL && 
		orientation_ != Adjustable.HORIZONTAL) {

	    throw new IllegalArgumentException(
		    "Orientation must be VERTICAL or HORIZONTAL");
	}
	_orientation = orientation_;
    }

    //********************************************************************
    // INSTANCE VARIABLES

    private int _orientation = VERTICAL;
    private int _minimum = 0;
    private int _value = 0;
    private int _extent = 10;
    private int _maximum = 100;
    private int _blockIncrement = 10;

    /** The length of this component on the screen.
     */
    private int _length = 12;

    /**
     * A list of AdjustmentListeners registered for this component.
     */
    protected Vector<AdjustmentListener> _adjustmentListeners = null;
}
