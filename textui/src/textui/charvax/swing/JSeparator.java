/* class JSeparator
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

import charva.awt.Component;
import charva.awt.Container;
import charva.awt.Dimension;
import charva.awt.Insets;
import charva.awt.Point;
import charva.awt.Toolkit;

/**
 * A horizontal separator in a menu.
 */
public class JSeparator
    extends Component
{
    /** Create a separator
     */
    public JSeparator()
    {
    }

    public void draw(Toolkit toolkit)
    {
	/* Get the absolute origin of this component.
	 */
	Point origin = getLocationOnScreen();

	toolkit.setCursor(origin);

	StringBuffer buf = new StringBuffer();
	for (int i=0; i<getSize().width; i++) {
	    buf.append("-");
	}
	toolkit.addString(buf.toString(), Toolkit.A_NORMAL, 0);
    }

    /** Determines whether this component will accept the keyboard focus
     * during keyboard traversal.
     */
    public boolean isFocusTraversable() { return false; }

    public Dimension minimumSize()
    {
	return this.getSize();
    }

    public Dimension getSize()
    {
	return new Dimension(this.getWidth(), this.getHeight());
    }

    public int getWidth() {
	Container parent = getParent();
	Insets insets = parent.getInsets();
	int availableWidth = parent.getSize().width - 
	    insets.left - insets.right;

	return availableWidth;
    }

    public int getHeight() {
	return 1;
    }

    /** This is never invoked. */
    public void requestFocus() { }

    /** Outputs a textual description of this component to stderr.
     */
    public void debug(int level_) {
	for (int i=0; i<level_; i++)
	    System.err.print("    ");
	System.err.println("JSeparator origin=" + _origin );
    }

    //====================================================================
    // INSTANCE VARIABLES

}
