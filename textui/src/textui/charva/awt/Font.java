/* class Font
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
 * The Font class in the CHARVA package exists for "compatibility" with AWT;
 * the only useful information it holds is whether the style is BOLD or not.
 */
public class Font {

    public Font(String name_, int style_, int size_) {
        _name = name_;
        if (style_ < PLAIN && style_ > (BOLD | ITALIC)) { throw new IllegalArgumentException(
                "invalid font style"); }

        _style = style_;
        //_size = size_;
    }

    /**
     * Returns the name of this font.
     */
    public String getFontName() {
        return _name;
    }

    /**
     * Returns the style of this font.
     */
    public int getStyle() {
        return _style;
    }

    //====================================================================
    // INSTANCE VARIABLES

    private String _name;

    private int _style;

    //private final int _size;

    // Style values
    public static final int PLAIN = 0;

    public static final int BOLD = 1;

    public static final int ITALIC = 2;
}
