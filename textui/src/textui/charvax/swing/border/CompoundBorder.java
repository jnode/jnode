/* class CompoundBorder
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

public class CompoundBorder
    implements Border
{
    public CompoundBorder(Border outsideBorder_, Border insideBorder_) { 
	_outsideBorder = outsideBorder_;
	_insideBorder = insideBorder_;
    }

    /** Returns the insets of the compound border.
     */
    public Insets getBorderInsets(Component component_) {
	Insets outside = _outsideBorder.getBorderInsets(component_);
	Insets inside = _insideBorder.getBorderInsets(component_);
	return new Insets(
		outside.top + inside.top,
		outside.left + inside.left,
		outside.bottom + inside.bottom,
		outside.right + inside.right);
    }

    /**
     * Paints the border for the specified component with the specified
     * position and size.
     * (acually the component_ parameter is unused but is here for
     * compatibility with Swing).
     */
    public void paintBorder(Component component_, int colorpair_,
                            int x_, int y_, int width_, int height_, Toolkit toolkit) {

	_outsideBorder.paintBorder(component_, colorpair_, 
	    x_, y_, width_, height_, toolkit);

	/* Now paint the inside border, making allowance for the space
	 * already used by the outside border.
	 */
	Insets outer = _outsideBorder.getBorderInsets(component_);
	_insideBorder.paintBorder(component_, colorpair_,
		x_ + outer.left, 
		y_ + outer.top, 
		width_ - outer.left - outer.right, 
		height_ - outer.top - outer.bottom, toolkit);
    }

    //====================================================================
    // INSTANCE VARIABLES

    private Border _outsideBorder;
    private Border _insideBorder;
}
