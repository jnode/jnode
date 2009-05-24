/* class GridBagLayout
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
 * This is an approximation of the AWT GridBagLayout layout manager.
 * See the documentation of the AWT GridBagLayout for details.
 */
public class GridBagLayout
    implements LayoutManager2
{
    public GridBagLayout()
    {
    }

    /**
     * Calculate the geometry for the specified list of Components, 
     * and return the size of the rectangle that encloses all the 
     * Components.
     */
    public Dimension minimumSize(Container container_)
    {
	/* First work out the dimensions of the grid (i.e. the number of
	 * rows and columns).  We do this by iterating through all the 
	 * added components, and inspecting their gridx, gridy, gridwidth and
	 * gridheight constraints.
	 */
	_rows = 0;
	_columns = 0;
	Enumeration<GridBagConstraints> e1 = _constraints.elements();
	while (e1.hasMoreElements()) {
	    GridBagConstraints gbc = e1.nextElement();
	    if (gbc.gridx + gbc.gridwidth > _columns)
		_columns = gbc.gridx + gbc.gridwidth;

	    if (gbc.gridy + gbc.gridheight > _rows)
		_rows = gbc.gridy + gbc.gridheight;
	}

	/* Now that we know the number of rows and columns, we can create
	 * arrays to hold the row heights and column widths.
	 */
	_calculatedRowHeights = new int[_rows];
	_calculatedRowWeights = new double[_rows];
	_calculatedColumnWidths = new int[_columns];
	_calculatedColumnWeights = new double[_columns];

	/* Create a pair of arrays, each the same length as the number of
	 * contained components, to hold the "width-left" and "height-left"
	 * values for each component.
	 */
	int width_left[] = new int[_components.size()];
	int height_left[] = new int[_components.size()];

	Enumeration<Component> e3 = _components.elements();
	Enumeration<GridBagConstraints> e2 = _constraints.elements();
	for (int i=0; e3.hasMoreElements(); i++) {
	    Component c = e3.nextElement();
	    GridBagConstraints gbc = e2.nextElement();

	    /* Calculate the minimum width & height required for this 
	     * component.
	     */
	    Insets insets = gbc.insets;
	    Dimension minsize = c.minimumSize();
	    width_left[i] = 
		    minsize.width + insets.left + insets.right;
	    height_left[i] =
		    minsize.height + insets.top + insets.bottom;
	}

	/* Now iterate through all the rows and allocate heights to each row.
	 */
	for (int row=0; row<_rows; row++) {
	    /* Iterate through the constraints and find those whose bottom edge
	     * is in the current row. The one with the maximum height_left
	     * value determines the height of this row.
	     */
	    _calculatedRowHeights[row] = 0;
	    Enumeration<GridBagConstraints> e = _constraints.elements();
	    for (int i=0; e.hasMoreElements(); i++) {
		GridBagConstraints gbc = e.nextElement();

		if (row == gbc.gridy + gbc.gridheight - 1) {
		    /* This component's bottom edge is in the current row.
		     */
		    if (height_left[i] > _calculatedRowHeights[row])
			_calculatedRowHeights[row] = height_left[i];
		}
	    }

	    /* Now that we have calculated the height of this row, subtract 
	     * this row-height from the height_left value of each component 
	     * which extends to or below this row.
	     */
	    e = _constraints.elements();
	    for (int i=0; e.hasMoreElements(); i++) {
		GridBagConstraints gbc = e.nextElement();

		if (row >= gbc.gridy &&
			row < gbc.gridy + gbc.gridheight) {
		    height_left[i] -= _calculatedRowHeights[row];
		}
	    }
	}

	/* Now iterate through all the columns and allocate widths to each
	 * column.
	 */
	for (int column=0; column<_columns; column++) {
	    /* Iterate through the constraints and find those whose right edge
	     * is in the current column. The one with the maximum width_left
	     * value determines the width of this column.
	     */
	    _calculatedColumnWidths[column] = 0;
	    Enumeration<GridBagConstraints> e = _constraints.elements();
	    for (int i=0; e.hasMoreElements(); i++) {
		GridBagConstraints gbc = e.nextElement();

		if (column == gbc.gridx + gbc.gridwidth - 1) {
		    /* This component's right edge is in the current column.
		     */
		    if (width_left[i] > _calculatedColumnWidths[column])
			_calculatedColumnWidths[column] = width_left[i];
		}
	    }

	    /* Now that we have calculated the width of this column, subtract 
	     * this column-width from the width_left value of each component 
	     * which extends to or to the right of this column.
	     */
	    e = _constraints.elements();
	    for (int i=0; e.hasMoreElements(); i++) {
		GridBagConstraints gbc = e.nextElement();

		if (column >= gbc.gridx &&
			column < gbc.gridx + gbc.gridwidth) {
		    width_left[i] -= _calculatedColumnWidths[column];
		}
	    }
	}

	/* Iterate through all the components and calculate the row 
	 * and column weights.
	 */
	e2 = _constraints.elements();
	while (e2.hasMoreElements()) {
	    GridBagConstraints gbc = e2.nextElement();

	    for (int i=gbc.gridx; i< gbc.gridx + gbc.gridwidth; i++) {
		if (gbc.weightx > _calculatedColumnWeights[i])
		    _calculatedColumnWeights[i] = gbc.weightx;
	    }

	    for (int i=gbc.gridy; i< gbc.gridy + gbc.gridheight; i++) {
		if (gbc.weighty > _calculatedRowWeights[i])
		    _calculatedRowWeights[i] = gbc.weighty;
	    }
	}

	/* Now just add up all the column widths and row heights to find
	 * the minimum size of the container.
	 */
	Insets insets = container_.getInsets();
	int totalwidth = insets.left + insets.right;
	int totalheight = insets.top + insets.bottom;

	for (int i=0; i<_columns; i++) {
	    totalwidth += _calculatedColumnWidths[i];
	    _totalweightx += _calculatedColumnWeights[i];
	}

	for (int i=0; i<_rows; i++) {
	    totalheight += _calculatedRowHeights[i];
	    _totalweighty += _calculatedRowWeights[i];
	}

	return new Dimension(totalwidth, totalheight);
    }

    /**
     * Set the positions of the contained components.
     */
    public void doLayout(Container container_)
    {
	Insets insets = container_.getInsets();
	Dimension size = container_.getSize();
	Dimension minsize = minimumSize(container_);
	int extraColumns = size.width - minsize.width;
	int extraRows = size.height - minsize.height;

	Enumeration<Component> e1 = _components.elements();
	Enumeration<GridBagConstraints> e2 = _constraints.elements();
	while (e1.hasMoreElements()) {
	    Component c = e1.nextElement();

	    GridBagConstraints gbc = e2.nextElement();

	    /* Calculate the boundaries of the grid cell that this
	     * component occupies.
	     */
	    int left = insets.left;
	    if (_totalweightx == 0.0)
		left += extraColumns/2;

	    for (int i=0; i<gbc.gridx; i++) {
		left += _calculatedColumnWidths[i];
		if (_totalweightx != 0.0)
		    left += (extraColumns * _calculatedColumnWeights[i]) /
			    _totalweightx;
	    }

	    int right = left;
	    for (int i=0; i<gbc.gridwidth; i++)
		right += _calculatedColumnWidths[gbc.gridx + i];

	    int top = insets.top;
	    if (_totalweighty == 0.0)
		top += extraRows/2;

	    for (int i=0; i<gbc.gridy; i++) {
		top += _calculatedRowHeights[i];
		if (_totalweighty != 0.0)
		    top += (extraRows * _calculatedRowWeights[i]) /
			    _totalweighty;
	    }

	    int bottom = top;
	    for (int i=0; i<gbc.gridheight; i++)
		bottom += _calculatedRowHeights[gbc.gridy + i];

	    if (c instanceof Container) {
		Container cont = (Container) c;

		/* Get the contained container to lay itself out at its
		 * preferred size, if it is not already laid out.
		 */
		if (cont.isValid() == false)
		    cont.setSize(cont.minimumSize());

		switch (gbc.fill) {
		    case GridBagConstraints.NONE:
			break;

		    case GridBagConstraints.HORIZONTAL:
			cont.setWidth(right - left);
			break;

		    case GridBagConstraints.VERTICAL:
			cont.setHeight(bottom - top);
			break;

		    case GridBagConstraints.BOTH:
			cont.setSize(right - left, bottom - top);
			break;

		    default:
			throw new IllegalArgumentException(
				"Invalid fill parameter");
		}
		cont.doLayout();
	    }


	    /* Calculate the x position of the component's origin (i.e. top
	     * left corner).
	     */
	    int cx = 0;
	    switch (gbc.anchor) {
		case GridBagConstraints.WEST:
		case GridBagConstraints.NORTHWEST:
		case GridBagConstraints.SOUTHWEST:
		    cx = left + gbc.insets.left;
		    break;

		case GridBagConstraints.EAST:
		case GridBagConstraints.NORTHEAST:
		case GridBagConstraints.SOUTHEAST:
		    cx = right - gbc.insets.right - c.getSize().width;
		    break;

		case GridBagConstraints.CENTER:
		case GridBagConstraints.NORTH:
		case GridBagConstraints.SOUTH:
		    cx = (left + gbc.insets.left) + 
			    (right - gbc.insets.right);
		    cx -= c.getSize().width;
		    cx = cx / 2;
		    break;

		default:
		    throw new IllegalArgumentException(
			    "invalid anchor paremeter");
	    }

	    /* Calculate the y position of the component's origin (i.e. top
	     * left corner).
	     */
	    int cy = 0;
	    switch (gbc.anchor) {
		case GridBagConstraints.NORTH:
		case GridBagConstraints.NORTHWEST:
		case GridBagConstraints.NORTHEAST:
		    cy = top + gbc.insets.top;
		    break;

		case GridBagConstraints.SOUTH:
		case GridBagConstraints.SOUTHWEST:
		case GridBagConstraints.SOUTHEAST:
		    cy = bottom - gbc.insets.bottom - c.getSize().height;
		    break;

		case GridBagConstraints.CENTER:
		case GridBagConstraints.WEST:
		case GridBagConstraints.EAST:
		    cy = (top + gbc.insets.top) + 
			    (bottom - gbc.insets.bottom);
		    cy -= c.getSize().height;
		    cy = cy / 2;
	    }

	    c.setLocation(cx, cy);
	}
    }

    public void addLayoutComponent(Component component_, Object constraint_)
    {
	_components.add(component_);

	/* Make a copy of the constraints object passed to us, so that the
	 * caller can re-use it for other components.
	 */
	GridBagConstraints constraint = (GridBagConstraints) constraint_;
	GridBagConstraints newc = new GridBagConstraints();
	newc.gridx = constraint.gridx;
	newc.gridy = constraint.gridy;
	newc.gridwidth = constraint.gridwidth;
	newc.gridheight = constraint.gridheight;
	newc.weightx = constraint.weightx;
	newc.weighty = constraint.weighty;
	newc.anchor = constraint.anchor;
	newc.fill = constraint.fill;
	newc.insets = new Insets(
		constraint.insets.top,
		constraint.insets.left,
		constraint.insets.bottom,
		constraint.insets.right);
	newc.ipadx = constraint.ipadx;
	newc.ipady = constraint.ipady;
	_constraints.add(newc);
    }

    /**
     * Invalidates the layout, indicating that if the layout manager has cached
     * information it should be discarded.
     */
    public void invalidateLayout(Container target_) {
	
    }

    //====================================================================
    // INSTANCE VARIABLES

    /**
     * This field holds the overrides to the column minimum widths.
     */
    public int[] columnWidths;

    /**
     * This field holds the overrides to the row minimum heights.
     */
    public int[] rowHeights;

    /**
     * This field is not used in the CHARVA package but is present to allow
     * compile-time compatibility with AWT.
     */
    public double[] columnWeights;

    /**
     * This field is not used in the CHARVA package but is present to allow
     * compile-time compatibility with AWT.
     */
    public double[] rowWeights;

    /**
     * As components are added, they are stored in this vector.
     */
    private Vector<Component> _components = new Vector<Component>();

    /**
     * As components are added, their constraint objects are stored in 
     * this vector.
     */
    private Vector<GridBagConstraints> _constraints = new Vector<GridBagConstraints>();

    /** The number of rows in the grid (calculated from all the added
     * components and their gridx, gridy, gridwidth and gridheight
     * constraints).
     */
    private int _rows;

    /** The number of rows in the grid (calculated from all the added
     * components and their gridx, gridy, gridwidth and gridheight
     * constraints).
     */
    private int _columns;

    /**
     * This array holds the row heights that we calculate.
     */
    private int[] _calculatedRowHeights;

    /**
     * This array holds the columns widths that we calculate.
     */
    private int[] _calculatedColumnWidths;

    /**
     * This array holds the row weights that we calculate.
     */
    private double[] _calculatedRowWeights;

    /**
     * This array holds the column weights that we calculate.
     */
    private double[] _calculatedColumnWeights;

    private double _totalweightx = 0.0;
    private double _totalweighty = 0.0;
}
