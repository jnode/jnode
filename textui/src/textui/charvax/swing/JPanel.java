/* class JPanel
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

import charva.awt.Container;
import charva.awt.FlowLayout;
import charva.awt.Insets;
import charva.awt.LayoutManager;
import charva.awt.Point;
import charva.awt.Toolkit;
import charvax.swing.border.Border;

/**
 * JPanel is a generic lightweight container.
 */
public class JPanel
    extends Container
{
    /** 
     * The default constructor sets the layout manager to FlowLayout.
     */
    public JPanel() {
	_layoutMgr = new FlowLayout();
    }

    /**
     * Use this constructor if you want to use a layout manager other than
     * the default FlowLayout.
     */
    public JPanel(LayoutManager layout_) {
	_layoutMgr = layout_;
    }

    public void setBorder(Border border_) {
	_border = border_;
    }

    public Border getBorder() { return _border; }

    public Insets getInsets() { 
	if (_border != null) {
	    return _border.getBorderInsets(this);
	}
	else
	    return super.getInsets();
    }

    /**
     * Draws the border of the panel (if there is one), plus
     * all the contained components.
     * @param toolkit
     */
    public void draw(Toolkit toolkit) {
	Point origin = getLocationOnScreen();

	/* Blank out the area of this component, but only if this
	 * component's color-pair is different than that of the
	 * parent container.
	 */
	int colorpair = getCursesColor();
//	Container parent = getParent();
//	if (parent != null && colorpair != parent.getCursesColor())
	    toolkit.blankBox(origin, _size, colorpair);

	if (_border != null) {
	    _border.paintBorder(this, 0,
		    origin.x, origin.y,
		    _size.width, _size.height, toolkit);
	}

	/* Draw all the components contained by this container.
	 */
	super.draw(toolkit);
    }

    public String toString() {
	return "JPanel origin=" + _origin + " size=" + _size;
    }

    public void debug(int level_) {
	for (int i=0; i<level_; i++)
	    System.err.print("    ");
	System.err.println(this.toString());
	super.debug(level_ + 1);
    }

    //====================================================================
    // INSTANCE VARIABLES

    private Border _border = null;
}
