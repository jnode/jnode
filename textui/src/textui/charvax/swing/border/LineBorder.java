/* class LineBorder
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

import charva.awt.Color;
import charva.awt.Component;
import charva.awt.Insets;
import charva.awt.Toolkit;

public class LineBorder
    implements Border
{
    public LineBorder(Color color_) {
	_lineColor = color_;
    }

    /** Returns the insets of the border.
     */
    public Insets getBorderInsets(Component component_) {
	return new Insets(1,1,1,1);
    }

    public static Border createBlackLineBorder() {
	return _blackLineBorder;
    }

    public Color getLineColor() {
	return _lineColor;
    }

    /**
     * Paints the border for the specified component with the specified
     * position and size.<p>
     * @param component_ the component around which this border is being
     * drawn. The background color of the border is obtained from the
     * component. If the line color of this border is also set to null,
     * the foreground color of the border is also obtained from component_.
     * @param graphics_ This parameter is just a placeholder where the
     * Swing "Graphics" parameter would be.
     * @param x_ The x coordinate of the top left corner.
     * @param y_ The y coordinate of the top left corner.
     * @param width_ the width of the border box.
     * @param height_ the height of the border box.
     * @param toolkit
     */
    public void paintBorder(Component component_,
                            int graphics_, int x_, int y_, int width_, int height_, Toolkit toolkit) {

	Color background = component_.getBackground();
	if (_lineColor == null)
	    _lineColor = component_.getForeground();

	int curses_colorpair = Color.getCursesColor(_lineColor, background);

	toolkit.drawBoxNative(x_, y_,
		x_ + width_ - 1,
		y_ + height_ - 1,
		curses_colorpair);
    }

    //====================================================================
    // INSTANCE VARIABLES

    private Color _lineColor = null;

    private static Border _blackLineBorder = new LineBorder(Color.black);
}
