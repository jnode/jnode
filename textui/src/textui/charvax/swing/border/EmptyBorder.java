/* class EmptyBorder
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

package charvax.swing.border;

import charva.awt.Component;
import charva.awt.Insets;
import charva.awt.Toolkit;

public class EmptyBorder
    implements Border
{
    public EmptyBorder(Insets insets_) {
	_top = insets_.top;
	_left = insets_.left;
	_bottom = insets_.bottom;
	_right = insets_.right;
    }

    public EmptyBorder(int top_, int left_, int bottom_, int right_) {
	_top = top_;
	_left = left_;
	_bottom = bottom_;
	_right = right_;
    }

    /** Returns the insets of the border.
     */
    public Insets getBorderInsets(Component component_) {
	return new Insets(_top, _left, _bottom, _right);
    }

    /**
     * Paints the border for the specified component with the specified
     * position and size.
     */
    public void paintBorder(Component component_,
                            int colorpair_, int x_, int y_, int width_, int height_, Toolkit toolkit) {

	// Does nothing, but must be here to implement the Border interface.
    }

    //====================================================================
    // INSTANCE VARIABLES

    private int _top;
    private int _left;
    private int _bottom;
    private int _right;
}
