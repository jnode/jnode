/* class Dimension
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
 * Encapsulates the width and height of a component in a single object.
 */
public class Dimension
    implements Cloneable
{
    /** Creates a new Dimension object with width and height = 0;
     */
    public Dimension() {
	this(0, 0);
    }

    public Dimension(int width_, int height_) {
	width = width_;
	height = height_;
    }

    public Dimension(Dimension dimension_) {
	width = dimension_.width;
	height = dimension_.height;
    }

    public void setSize(Dimension dimension_) {
	width = dimension_.width;
	height = dimension_.height;
    }

    public Object clone() {
	return new Dimension (this);
    }

    public String toString() {
	return "(" + width + "," + height + ")";
    }

    //====================================================================
    // INSTANCE VARIABLES

    public int width;
    public int height;
}
