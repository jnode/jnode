/* class Insets
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
 * The Insets class specifies the blank space that must be left around
 * the inside of the edges of a Container.  The space can be used for 
 * a border, a title, or other items (such as a scrollbar).
 */
public class Insets
{
    public Insets(int top_, int left_, int bottom_, int right_) {
	top = top_;
	left = left_;
	bottom = bottom_;
	right = right_;
    }

    public String toString() {
	return "(" + top + "," + left + "," + bottom + "," + right + ")";
    }

    //====================================================================
    // INSTANCE VARIABLES
    // For compatibility with AWT and Swing, the instance variable names
    // don't have a leading underscore.

    public int top;
    public int left;
    public int bottom;
    public int right;
}
