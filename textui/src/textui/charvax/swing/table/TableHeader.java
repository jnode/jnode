/* class TableHeader
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

package charvax.swing.table;

import charva.awt.Component;
import charva.awt.Dimension;
import charva.awt.Point;
import charva.awt.Toolkit;

/**
 * The TableHeader class is used by the ScrollPane for drawing the
 * column headers of a table.  It shares the TableModel of its companion
 * Table object.
 */
public class TableHeader
    extends Component
{
    /** Constructs a table of numRows_ and numColumns_ of empty cells
     * using a DefaultTableModel.
     */

    public TableHeader(TableModel model_) {
	_model = model_;
    }

    public void setModel(TableModel model_) {
	_model = model_;
    }

    public boolean isFocusTraversable() { return false; }

    public void requestFocus() { }

    public void draw(Toolkit toolkit) {
	/* Get the absolute origin of this component.
	 */
	Point origin = getLocationOnScreen();

	int columns = _model.getColumnCount();
	int colorpair = getCursesColor();

	/* Start by blanking out the table area and drawing the box 
	 * around the table.
	 */
	toolkit.blankBox(origin, getSize(), colorpair);
	toolkit.drawBox(origin, getSize(), colorpair);

	/* Now fill in the table headings
	 */
	int x = 1;
	int attr = Toolkit.A_BOLD;
	for (int i=0; i<columns; i++) {
	    toolkit.setCursor(origin.addOffset(x, 0));
	    toolkit.addChar(' ', attr, colorpair);
	    toolkit.addString(_model.getColumnName(i), attr, colorpair);
	    toolkit.addChar(' ', attr, colorpair);
	    x += getColumnWidth(i) + 1;
	}

	/* Now draw the vertical lines that divide the columns.
	 */
	x = getColumnWidth(0) + 1;
	for (int i=0; i<columns-1; i++) {
	    toolkit.setCursor(origin.addOffset(x, 0));
	    toolkit.addChar(Toolkit.ACS_TTEE, 0, colorpair);	    // top tee
	    x += getColumnWidth(i+1) + 1;
	}
    }

    /**
     * We pretend that the table header is two rows in height so that the
     * box gets drawn correctly.
     */
    public Dimension getSize() {
	return new Dimension(this.getWidth(), this.getHeight());
    }

    public Dimension minimumSize() { return getSize(); }

    public int getWidth() {
	int columns = _model.getColumnCount();
	int width = 1;
	for (int i=0; i<columns; i++) {
	    width += getColumnWidth(i) + 1;
	}
	return width;
    }

    public int getHeight() {
	return 2;
    }

    public void debug(int level_) {
	for (int i=0; i<level_; i++)
	    System.err.print("    ");
	System.err.println("TableHeader origin=" + _origin + 
	    " size=" + getSize());
    }

    private int getColumnWidth(int column_) {
	/* Calculate the column width for the specified column.
	 */
	int columnwidth = _model.getColumnName(column_).length() + 2;

	for (int j=0; j<_model.getRowCount(); j++) {
	    Object value = _model.getValueAt(j, column_);
	    if (value != null) {
		int width = value.toString().length();
		if (width > columnwidth)
		    columnwidth = width;
	    }
	}
	return columnwidth;
    }

    //--------------------------------------------------------------------
    // INSTANCE VARIABLES

    private TableModel _model = null;

}
