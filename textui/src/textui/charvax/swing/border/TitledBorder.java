/* class TitledBorder
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
import charva.awt.Font;
import charva.awt.Insets;
import charva.awt.Point;
import charva.awt.Toolkit;

/**
 * A class that implements a border with the specified title.
 */
public class TitledBorder
    implements Border
{
    /** Create a titled border with the specified border and an
     * empty title.
     */
    public TitledBorder(Border border_) {
	this(border_, "", 0, 0, null, null);
    }

    /** Create a TitledBorder instance with a line border and
     * the specified title, with the title text in black.
     */
    public TitledBorder(String title_) {
	this(new LineBorder(null), title_, 0, 0, null, null);
    }

    /** Create a TitledBorder instance with the specified border
     * and title, and with the title text in black.
     */
    public TitledBorder(Border border_, String title_) {
	this(border_, title_, 0, 0, null, null);
    }

    /** Creates a TitledBorder instance with the specified border, 
     * title, title-justification, title-position, title-font, 
     * and title-color.
     * @param border_ the border.
     * @param title_ the title the border should display.
     * @param titleJustification_ the title justification (UNUSED).
     * @param titlePosition_ the position of the title (UNUSED)
     * @param titleFont_ the title font (UNUSED)
     * @param titleColor_ the title color.
     */
    public TitledBorder(Border border_, String title_, 
	int titleJustification_, int titlePosition_, 
	Font titleFont_, Color titleColor_) 
    {
	_border = border_;
	_title = title_;
	_titleColor = titleColor_;
    }

    /** Returns the insets of the border.
     */
    public Insets getBorderInsets(Component component_) {
	return new Insets(1,1,1,1);
    }

    /** Sets the title text.
     */
    public void setTitle(String title_) {
	_title = title_;
    }

    /** Returns the title
     */
    public String getTitle() { 
	return _title; 
    }

    /** Set the color of the title text.
     */
    public void setTitleColor(Color titleColor_) {
	_titleColor = titleColor_;
    }

    /** Returns the color of the title text.
     */
    public Color getTitleColor() {
	return _titleColor;
    }

    /** Sets the border of this titled border.
     */
    public void setBorder(Border border_) {
	_border = border_;
    }

    /** Returns the border of the titled border.
     */
    public Border getBorder() {
	return _border;
    }

    /**
     * Paints the border for the specified component with the specified
     * position and size.
     */
    public void paintBorder(Component component_,
                            int graphics_, int x_, int y_, int width_, int height_, Toolkit toolkit) {

	/* First draw the specified border (which, in the case of the CHARVA
	 * package, is always a LineBorder).
	 */
	_border.paintBorder(component_, graphics_, x_, y_, width_, height_, toolkit);

	/* Now insert the title. The background color is obtained from
	 * component_. If the titleColor has not been set explicitly,
	 * the foreground color is also obtained from component_.
	 */
	Color background = component_.getBackground();
	if (_titleColor == null)
	    _titleColor = component_.getForeground();

	int colorpair = Color.getCursesColor(_titleColor, background);
	if (_title.length() != 0) {
	    Point origin = new Point(x_, y_);
	    toolkit.setCursor(origin.addOffset(1,0));
	    toolkit.addChar(' ', 0, colorpair);
	    toolkit.addString(_title, 0, colorpair);
	    toolkit.addChar(' ', 0, colorpair);
	}
    }

    //====================================================================
    // INSTANCE VARIABLES

    protected String _title;
    protected Color _titleColor;
    protected Border _border;
}
