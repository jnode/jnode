/* interface Border
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

/**
 * Interface describing an object capable of rendering a border
 * around the edges of a component.
 */
public interface Border
{
    /** Returns the insets of the border.
     */
    public Insets getBorderInsets(Component component_);

    /**
     * Paints the border for the specified component with the specified
     * position and size.
     */
    public void paintBorder(Component component_,
                            int colorpair_, int x_, int y_, int width_, int height_, Toolkit toolkit);
}
