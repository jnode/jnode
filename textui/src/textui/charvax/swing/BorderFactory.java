/* class BorderFactory
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

package charvax.swing;

import charva.awt.Color;
import charvax.swing.border.Border;
import charvax.swing.border.LineBorder;
import charvax.swing.border.TitledBorder;

/**
 * A factory class for creating standard instances of the Border class. In the
 * genuine Swing library, this tries to share instances wherever possible.
 * In the Charva library, all the borders (except TitleBorders) are actually
 * just LineBorders.
 */
public class BorderFactory
{
    public static Border createLineBorder(Color color_) {
	return new LineBorder(color_);
    }

    /** Create a LineBorder - ignore the thickness.
     */
    public static Border createLineBorder(Color color_, int thickness_) {
	return new LineBorder(color_);
    }

    public static TitledBorder createTitledBorder(String title)
    {
	return new TitledBorder(title);
    }
}
