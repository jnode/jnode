/* class ColorPair
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
 * This class is used to represent a color-pair (a combination
 * of a foreground color and a background color).
 */
public class ColorPair
{
    /**
     * Construct a color-pair from a foreground color and a
     * background color.  The parameters must be fields of the
     * Color class.
     */
    public ColorPair(Color foreground_, Color background_)
    {
	_foreground = foreground_.getCursesColor();
	_background = background_.getCursesColor();
    }

    /**
     * Returns true if this color pair is equal to the specified color
     * pair.
     */
    public boolean equals(Object object_)
    {
	try {
	    ColorPair pair = (ColorPair) object_;
	    return (_foreground == pair.getForeground() &&
		_background == pair.getBackground());
	}
	catch (ClassCastException e) {
	    return false;
	}

    }

    public int getForeground() { return _foreground; }

    public int getBackground() { return _background; }

    /**
     * Set the foreground color.  The parameter must be a field of the
     * Color class.
     */
    public void setForeground(int color_) { _foreground = color_; }

    /**
     * Set the background color.  The parameter must be a field of the
     * Color class.
     */
    public void setBackground(int color_) { _background = color_; }

    /** Return a string representation of this object.
     */
    public String toString() {
	return ("fgnd=" + Color.getColorName(_foreground) + 
	    ", bkgnd=" + Color.getColorName(_background));
    }

    //====================================================================
    // INSTANCE VARIABLES

    private int _foreground;
    private int _background;
}
