/* class JViewport
 *
 * Copyright (C) 2001-2003  R M Pitman
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
import charva.awt.Point;
import charva.awt.Rectangle;
import charva.awt.Scrollable;
import charva.awt.Toolkit;
import charvax.swing.table.TableHeader;

/**
 * The JViewport class provides a scrollable window onto an underlying
 * component, whose size can be greater than the size of the JViewport.
 * The underlying component must implement the Scrollable interface.
 */
public class JViewport
    extends Container
{
    /** Construct a JViewport.
     */
    public JViewport () {
    }

    /** Set the JViewport's one child.
     */
    public void setView(Component child_) {
	if ((child_ instanceof Scrollable) == false &&
	    (child_ instanceof TableHeader) == false) {

	    throw new IllegalArgumentException(
		"JViewport's view must be a Scrollable or a TableHeader");
	}

	if (_child != null)
	    super.remove(_child);   // only allow one child.

	super.add(child_);
	_child = child_;
    }

    /** Returns the JViewport's one child.
     */
    public Component getView() {
	return _child;
    }

    public void draw(Toolkit toolkit) {

	/* Get the absolute origin of this Viewport
	 */
	Point origin = getLocationOnScreen();

	toolkit.setClipRect(new Rectangle(origin, getExtentSize()));
	_child.draw(toolkit);
	toolkit.resetClipRect();
    }

    /** Gets the coordinates of the origin of the view, relative to
     * the origin of the viewport.
     */
    public Point getViewPosition() { return _child.getLocation(); }

    /** Sets the coordinates of the origin of the view, relative to
     * the origin of the viewport.
     */
    public void setViewPosition(Point origin_) {
	_child.setLocation(origin_);
    }

    /** Returns the child component's size.
     */
    public Dimension getViewSize() {
	return _child.getSize();
    }

    public Rectangle getBounds() {
	return new Rectangle(getLocation(), getExtentSize());
    }

    /** This package-private method is called by JScrollPane to
     * determine the size of the visible viewport (when the
     * component being displayed does not implement the Scrollable 
     * interface - for example, a TableHeader).
     */
    void setExtentSize(int width, int height) {
	_extent = new Dimension(width, height);
    }

    /** Returns the size of the visible part of the view.
     */
    public Dimension getExtentSize() {
	return new Dimension(_extent);
    }

    public void debug(int level_) {
	for (int i=0; i<level_; i++)
	    System.err.print("    ");
	System.err.println("Viewport origin=" + _origin + 
	    " size=" + getSize() );
	super.debug(level_ + 1);
    }

    //====================================================================
    // INSTANCE VARIABLES

    private Component _child;

    private Dimension _extent;
}
