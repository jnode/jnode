/* class Point
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
 * Represents a point in (x,y) coordinate space, with integer precision.
 */
public class Point
    implements Cloneable
{
    public Point(int x_, int y_) {
	x = x_;
	y = y_;
    }

    public Point(Point point_) {
	x = point_.x;
	y = point_.y;
    }

    public Point addOffset(Point p_) {
	return new Point(x + p_.x, y + p_.y);
    }

    public Point addOffset(Dimension d_) {
	return new Point(x + d_.width, y + d_.height);
    }

    public Point addOffset(int x_, int y_) {
	return new Point(x + x_, y + y_);
    }

    public Point subtractOffset(Point p) {
	return new Point(x - p.x, y - p.y);
    }

    /** Translates this point by dx_ along the x axis and by dy_ along
     * the y axis.
     */
    public void translate(int dx_, int dy_) {
	x += dx_;
	y += dy_;
    }

    /** Translates this point by the x and y values given in "point_"
     */
    public void translate(Point point_) {
	x += point_.x;
	y += point_.y;
    }

    /**
     * Returns true if this point is inside the specified rectangle.
     */
    public boolean isInside(Rectangle rect_) {
	if (x < rect_.getLeft())
	    return false;
	if (x > rect_.getRight())
	    return false;
	if (y < rect_.getTop())
	    return false;
	if (y > rect_.getBottom())
	    return false;
	return true;
    }

    /**
     * Returns true is this Point is equal to the specified Point.
     */
    public boolean equals(Point other_)
    {
	return (x == other_.x && y == other_.y);
    }

    public String toString() {
	return "(" + x + "," + y + ")";
    }

    public Object clone() {
	return new Point(this);
    }

    //====================================================================
    // INSTANCE VARIABLES

    public int x;
    public int y;
}
