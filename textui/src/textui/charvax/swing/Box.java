/* class Box
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

/*
 * Modified Jul 14, 2003 by Tadpole Computer, Inc.
 * Modifications Copyright 2003 by Tadpole Computer, Inc.
 *
 * Modifications are hereby licensed to all parties at no charge under
 * the same terms as the original.
 *
 * Fixed a trivial bug in createHorizontalBox().
 */

package charvax.swing;

import charva.awt.Container;

/**
 * A convenience container that uses a BoxLayout object as its LayoutManager.
 */
public class Box
    extends Container
{
    public Box(int axis_) {
	setLayout(new BoxLayout(this, axis_));
    }

    /** Does nothing; it's here for compatibility with Swing.
     */
    public void createGlue() {}

    /**
     * Convenience method for creating a Box with vertical axis.
     */
    public static Box createVerticalBox() {
	return new Box(BoxLayout.Y_AXIS);
    }

    /**
     * Convenience method for creating a Box with horizontal axis.
     */
    public static Box createHorizontalBox() {
	return new Box(BoxLayout.X_AXIS);
    }
}
