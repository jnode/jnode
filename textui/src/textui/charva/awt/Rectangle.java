/* class Rectangle
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

package charva.awt;


/**
 * The Rectangle class respresents a rectangular area of the screen; the
 * boundaries are INCLUSIVE (i.e. the _right instance variable refers to
 * the rightmost column that is included in the rectangular area, and the
 * _bottom instance variable refers to the bottom row that is included
 * in the rectangle.
 * The width of the rectangle is given by (_right - _left +1).
 * The height of the rectangle is given by (_bottom - _top + 1).
 */
public class Rectangle
{
    private int _top;
    private int _left;
    private int _bottom;
    private int _right;

    /**
     * Construct a Rectangle with the specified boundaries.
     */
    public Rectangle(int top_, int left_, int bottom_, int right_) {
	_top = top_;
	_left = left_;
	_bottom = bottom_;
	_right = right_;
    }

    public Rectangle(Point topleft_, Point bottomright_) {
	_top = topleft_.y;
	_left = topleft_.x;
	_bottom = bottomright_.y;
	_right = bottomright_.x;
    }

    public Rectangle(Point topleft_, Dimension size_) {
	_top = topleft_.y;
	_left = topleft_.x;
	_bottom = _top + size_.height - 1;
	_right = _left + size_.width - 1;
    }

    public Rectangle(Rectangle rect_) {
	_top = rect_.getTop();
	_left = rect_.getLeft();
	_bottom = rect_.getBottom();
	_right = rect_.getRight();
    }

    /** Returns true if the specified point is inside this Rectangle.
     */
    public boolean contains(Point p) {
	return this.contains(p.x, p.y);
    }

    /** Returns true if the specified point is inside this Rectangle.
     */
    public boolean contains(int x, int y) {
	return ((x >= _left) && 
		(x <= _right) &&
		(y >= _top) &&
		(y <= _bottom));
    }

    public int getLeft() { return _left; }
    public int getRight() { return _right; }
    public int getTop() { return _top; }
    public int getBottom() { return _bottom; }
    
    /**
     * Check if the specified rectangle intersects at all with this rectangle.
     */
    public boolean intersects(Rectangle rect_) {
	if (rect_._left > _right)
	    return false;
	if (rect_._right < _left)
	    return false;
	if (rect_._top > _bottom)
	    return false;
	if (rect_._bottom < _top)
	    return false;
	return true;
    }

    /** 
     * Return the intersection between this Rectangle and the specified
     * Rectangle, or null if the two rectangles don't intersect.
     */
    public Rectangle intersection(Rectangle rect_) {

	if (_top > rect_._bottom)
	    return null;
	if (_bottom < rect_._top)
	    return null;
	if (_left > rect_._right)
	    return null;
	if (_right < rect_._left)
	    return null;

	Rectangle ret = new Rectangle(0,0,0,0);
	ret._left   = (_left > rect_._left) ? _left : rect_._left;
	ret._right  = (_right < rect_._right) ? _right : rect_._right;
	ret._top    = (_top > rect_._top) ? _top : rect_._top;
	ret._bottom = (_bottom < rect_._bottom) ? _bottom : rect_._bottom;

	return ret;
    }

    /** Returns true if this rectangle has the same bounds as the
     * specified rectangle.
     */
    public boolean equals(Rectangle rect_)
    {
	if (rect_ == null)
	    return false;
	if (_top != rect_._top)
	    return false;
	if (_bottom != rect_._bottom)
	    return false;
	if (_left != rect_._left)
	    return false;
	if (_right != rect_._right)
	    return false;
	return true;
    }

    /** Returns a clone of this rectangle.
     */
    public Object clone() {
	return new Rectangle(_top, _left, _bottom, _right);
    }

    public String toString() {
	return ("(" + _top + "," + _left + "," + 
	    _bottom + "," + _right + ")");
    }
}
