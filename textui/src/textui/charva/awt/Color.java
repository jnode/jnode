/* class Color
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
 * A class used to represent the color values available on a text terminal.
 */
public class Color
{
    /**
     * Construct a Color from the specified RGB values. Each value must
     * be in the range 0-255.
     */
    public Color(int red_, int green_, int blue_) {
	_red = (red_ != 0) ? 255 : 0;
	_green = (green_ != 0) ? 255 : 0;
	_blue = (blue_ != 0) ? 255 : 0;
    }

    public boolean equals(Object obj_) {
	if (obj_ == null)
	    return false;

	if ( ! (obj_ instanceof Color))
	    return false;

	Color othercolor = (Color) obj_;
	if (_red != othercolor._red)
	    return false;
	if (_green != othercolor._green)
	    return false;
	if (_blue != othercolor._blue)
	    return false;

	return true;
    }

    /** Convert the Color object to an integer value compatible with
     * the ncurses library.
     */
    public int getCursesColor()
    {
	if (_red != 0) {
	    if (_green != 0) {
		if (_blue != 0)
		    return Toolkit.WHITE;
		else
		    return Toolkit.YELLOW;
	    }
	    else {
		if (_blue != 0)
		    return Toolkit.MAGENTA;
		else
		    return Toolkit.RED;
	    }
	}
	else {
	    if (_green != 0) {
		if (_blue != 0)
		    return Toolkit.CYAN;
		else
		    return Toolkit.GREEN;
	    }
	    else {
		if (_blue != 0)
		    return Toolkit.BLUE;
		else
		    return Toolkit.BLACK;
	    }
	}
    }

    public String toString()
    {
	if (_red != 0) {
	    if (_green != 0) {
		if (_blue != 0)
		    return "white";
		else
		    return "cyan";
	    }
	    else {
		if (_blue != 0)
		    return "magenta";
		else
		    return "red";
	    }
	}
	else {
	    if (_green != 0) {
		if (_blue != 0)
		    return "white";
		else
		    return "green";
	    }
	    else {
		if (_blue != 0)
		    return "blue";
		else
		    return "black";
	    }
	}
    }

    /** Compute the ncurses color-pair number corresponding to the specified
     * foreground and background color.
     */
    public static int getCursesColor(Color foreground_, Color background_)
    {
	if ( ! Toolkit.isColorEnabled)
	    return 0;

	/* The default colors (in case an exception occurs) are white 
	 * on black.
	 */
	int curses_color_pair = 0;
	try {
	    Toolkit toolkit = Toolkit.getDefaultToolkit();
	    
	    // if the terminal is capable of colors
	    if (toolkit.hasColors()) {
		ColorPair color_pair = new ColorPair(foreground_, background_);
		curses_color_pair = toolkit.getColorPairIndex(color_pair);
	    }
	}
	catch (TerminfoCapabilityException e) {
	    System.err.println( "can't set color pair: foreground " + 
		foreground_ + " background " + background_);
	}
	return curses_color_pair;
    }


    /** Convert an ncurses color value to a color name.
     */
    public static String getColorName(int colorval_) {
	if (colorval_ == Toolkit.BLACK)		return "black";
	else if (colorval_ == Toolkit.RED)	return "red";
	else if (colorval_ == Toolkit.GREEN)	return "green";
	else if (colorval_ == Toolkit.YELLOW)   return "yellow";
	else if (colorval_ == Toolkit.BLUE)	return "blue";
	else if (colorval_ == Toolkit.MAGENTA)  return "magenta";
	else if (colorval_ == Toolkit.CYAN)	return "cyan";
	else if (colorval_ == Toolkit.WHITE)	return "white";
	    else
	return "UNKNOWN";
    }

    /** Convert from an integer (ncurses-compatible) value to
     * a Color object.
     */
    public static Color fromCursesColor(int colorval_) {
	if (colorval_ == Toolkit.BLACK)		return black;
	else if (colorval_ == Toolkit.RED)	return red;
	else if (colorval_ == Toolkit.GREEN)	return green;
	else if (colorval_ == Toolkit.YELLOW)   return yellow;
	else if (colorval_ == Toolkit.BLUE)	return blue;
	else if (colorval_ == Toolkit.MAGENTA)  return magenta;
	else if (colorval_ == Toolkit.CYAN)	return cyan;
	else if (colorval_ == Toolkit.WHITE)	return white;
	    else
	return null;
    }

    //====================================================================
    // INSTANCE VARIABLES
    private int _red;
    private int _green;
    private int _blue;

    public static final Color black = new Color(0, 0, 0);
    public static final Color red = new Color(255, 0, 0);
    public static final Color green = new Color(0, 255, 0);
    public static final Color yellow = new Color(255, 255, 0);
    public static final Color blue = new Color(0, 0, 255);
    public static final Color magenta = new Color(255, 0, 255);
    public static final Color cyan = new Color(0, 255, 255);
    public static final Color white = new Color(255, 255, 255);

}
