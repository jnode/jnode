/* class FlowLayout
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

import java.util.Enumeration;
import java.util.Vector;

/**
 * A concrete implementation of LayoutManager that lays out its
 * components left-to-right.
 */
public class FlowLayout 
    implements LayoutManager
{
    /** Default constructor. Sets alignment to CENTER, hgap to 1,
     * and vgap to 0.
     */
    public FlowLayout() { 
	this(CENTER, 1, 0);
    }

    /**
     * Use this constructor when you want to set the alignment and the
     * horizontal and vertical gaps.
     */
    public FlowLayout(int align_, int hgap_, int vgap_) {
	_align = align_;
	_hgap = hgap_;
	_vgap = vgap_;
    }

    /** Sets the alignment for this layout. Allowable values are
     * FlowLayout.LEFT, FlowLayout.CENTER and FlowLayout.RIGHT.
     */
    public void setAlignment(int align_) {
	_align = align_;
    }

    /** Gets the alignment for this layout.
     */
    public int getAlignment() { return _align; }

    /**
     * Calculate the minimum-size rectangle that can enclose all the
     * components in the given container.
     */
    public Dimension minimumSize(Container container_) {

	int width = 0;
	int height = 0;

	Component[] components = container_.getComponents();
	for (int i=0; i<components.length; i++) {
	    Dimension d = components[i].minimumSize();

	    /* Make allowance for the gap between this component and the
	     * previous component.
	     */
	    if (i!= 0)
		width += _hgap;

	    width += d.width;
	    if (d.height > height)
		height = d.height;
	}

	/* Take into account the border frame (if any).
	 */
	Insets insets = container_.getInsets();
	height += insets.top + insets.bottom;
	width += insets.left + insets.right;

	return new Dimension(width, height);
    }

    /**
     * Lay out the components according to the specified alignment, hgap 
     * and vgap.
     * This is called when the size of the container has already been
     * calculated.  
     * It lays out the components in a row, one at a time, until it
     * determines that there is not enough space left in the row.
     * Then it moves to the next row. If there is not enough vertical
     * space in the container to lay out all of the components, it 
     * removes the remaining components from the container; they don't
     * appear at all.
     */
    public void doLayout(Container container_) {

	Insets insets = container_.getInsets();
	int availableWidth = container_.getSize().width -
		insets.left - insets.right;
	int widthLeft = availableWidth;
	int heightLeft = container_.getSize().height -
		insets.top - insets.bottom;

	int voffset = insets.top;

	Component[] components = container_.getComponents();
	Vector<Component> localvector = new Vector<Component>();
	for (int i=0; i<components.length; i++) {
	    Component c = components[i];

	    /* Get the contained container to lay itself out at its
	     * preferred size, if it is not already laid out.
	     */
	    if (c instanceof Container) {
		Container cont = (Container) c;
		if (cont.isValid() == false) {
		    cont.setSize(cont.minimumSize());
		    cont.doLayout();
		}
	    }

	    /* Determine the width required to lay out the current 
	     * component (including the gap between this component and
	     * the previous component).
	     */
	    int requiredWidth = c.getSize().width;
	    if (i != 0)
		requiredWidth += _hgap;

	    if (requiredWidth > widthLeft) {
		int rowHeight = 0;
		if (localvector.size() != 0) {
		    rowHeight = layoutRow(container_, localvector, 
			    widthLeft, heightLeft, voffset);
		    localvector.removeAllElements();
		}
		voffset += rowHeight + _vgap;
		widthLeft = availableWidth;
		heightLeft -= rowHeight + _vgap;
	    }
	    widthLeft -= requiredWidth;

	    // Build up a temporary list of components for this row.
	    localvector.add(c);
	}
	layoutRow(container_, localvector, widthLeft, heightLeft, voffset);

    }

    /** private function to layout a single row of components.
     * @return The height of the laid-out row.
     */
    private int layoutRow(Container container_, Vector<Component> components_, 
	int widthleft_, int heightleft_, int voffset_) {

	int hoffset = 0;
	int rowHeight = 0;
	Insets insets = container_.getInsets();

	switch (_align) {
	    case LEFT:
		hoffset = insets.left;
		break;
	    case CENTER:
		hoffset = insets.left + widthleft_/2;
		break;
	    case RIGHT:
		hoffset = insets.left + widthleft_;
		break;
	}

	Enumeration<Component> e = components_.elements();
	while (e.hasMoreElements()) {
	    Component c = (Component) e.nextElement();
	    if (c.getSize().height > rowHeight)
		rowHeight = c.getSize().height;

	    if (rowHeight > heightleft_) {
		container_.remove(c);	// we have run out of space
		continue;
	    }

	    c.setLocation(hoffset, voffset_);
	    hoffset += c.getSize().width + _hgap;
	}
	return rowHeight;
    }

    //====================================================================
    // INSTANCE VARIABLES

    /** Alignment of components (LEFT, RIGHT or CENTER) */
    private int _align = CENTER;

    /** Horizontal gap between components */
    private int _hgap = 1;

    /** Vertical gap between components */
    private int _vgap = 0;

    public static final int LEFT = 1;
    public static final int CENTER = 2;
    public static final int RIGHT = 3;

}
