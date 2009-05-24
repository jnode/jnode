/* class DefaultTableModel
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

import java.util.Vector;

/**
 * This is an implementation of the TableModel interface that uses a 
 * Vector of Vectors to store the cell values.
 */
public class DefaultTableModel
    extends AbstractTableModel
{
    /** Constructs a DefaultTableModel with the specified number of
     * rows and columns, and with cell values of null.
     */
    public DefaultTableModel(int rows_, int columns_) {
	_rows = rows_;
	_columns = columns_;

	// Create empty vectors
	setDataVector(rows_, columns_);
    }

    /** Constructs a DefaultTableModel and initialises the table by passing
     * data_ and columnNames_ to the setDataVector method.
     */
    public DefaultTableModel(Object[][] data_, Object[] columnNames_) {

	_rows = data_.length;
	_columns = columnNames_.length;
	setDataVector(data_, columnNames_);
    }

    /** Get the number of columns in the model. */
    public int getColumnCount() { return _columns; }

    /** Get the name of the specified column. */
    public String getColumnName(int column_) {
	if (_columnNames == null) {
	    char heading = (char) (0x41 + column_);
	    return String.valueOf(heading);
	}
	else {
	    return _columnNames.elementAt(column_).toString();
	}
    }

    /** Get the width of the specified column. */
    public int getColumnWidth(int column_) {
	int width = ((Integer) _columnWidths.elementAt(column_)).intValue();
	return width;
    }

    /** Get the number of rows in the model. */
    public int getRowCount() { return _rows; }

    /** Returns an attribute value for the cell at (rowIndex, columnIndex)
     */
    public Object getValueAt(int rowIndex_, int columnIndex_) {
	if (_dataVector == null)
	    return null;

	Vector<Object> rowVector = (Vector<Object>) _dataVector.elementAt(rowIndex_);
	return rowVector.elementAt(columnIndex_);
    }

    /** Sets the attribute value for the cell at position (row, column).
     */
    public void setValueAt(Object value_, int row_, int column_) {
	Vector<Object> rowVector = (Vector<Object>) _dataVector.elementAt(row_);
	rowVector.set(column_, value_);

	/* Recalculate the column width for the affected column.
	 */
	int columnwidth = 3;	    // default width
	if (_columnNames != null) {
	    Object header = _columnNames.get(column_);
	    columnwidth = header.toString().length() + 2;
	}
	for (int j=0; j<_rows; j++) {
	    Object value = getValueAt(j, column_);
	    if (value != null) {
		int width = value.toString().length();
		if (width > columnwidth)
		    columnwidth = width;
	    }
	}
	_columnWidths.set(column_, new Integer(columnwidth));
	fireTableCellUpdated(row_, column_);
    }

    /** Replaces the values in the _dataVector instance variable with the
     * values in the data_ array. The first index is the row index, the
     * second index is the column index.
     * The columnNames_ array supplies the new column names.
     */
    public void setDataVector(Object[][] data_, Object[] columnNames_) {

	_rows = data_.length;
	_dataVector = new Vector<Vector<Object>>(_rows);
	for (int i=0; i<_rows; i++) {
	    _columns = data_[i].length;
	    Vector<Object> rowVector = new Vector<Object>(_columns);
	    _dataVector.add(rowVector);
	    for (int j=0; j<_columns; j++)
		rowVector.add(data_[i][j]);
	}

	/* Set up the column-name  and column-width vectors
	 */
	_columnNames = new Vector<Object>(_columns);
	_columnWidths = new Vector<Integer>(_columns);
	for (int i=0; i<_columns; i++) {
	    _columnNames.add(columnNames_[i]);

	    int columnwidth = columnNames_[i].toString().length() + 2;
	    for (int j=0; j<_rows; j++) {
		int width = getValueAt(j, i).toString().length();
		if (width > columnwidth)
		    columnwidth = width;
	    }
	    _columnWidths.add(new Integer(columnwidth));
	}
    }

    /** Set up an empty data vector with the specified number of rows
     * and columns.
     */
    public void setDataVector(int rows_, int columns_) {
	_dataVector = new Vector<Vector<Object>>(rows_);
	for (int i=0; i<rows_; i++) {
	    Vector<Object> rowVector = new Vector<Object>(columns_);
	    rowVector.setSize(columns_);
	    _dataVector.add(rowVector);
	}

	_columnWidths = new Vector<Integer>(columns_);
	for (int i=0; i<columns_; i++) {
	    int width;
	    if (_columnNames == null)
		width = 3;
	    else
		width = _columnNames.get(i).toString().length();

	    _columnWidths.add(new Integer(width));
	}
    }

    //--------------------------------------------------------------------
    // INSTANCE VARIABLES

    private int _rows;
    private int _columns;

    /** A vector of vectors of data values. Each vector in the _dataVector
     * represents a row of data.
     */
    private Vector<Vector<Object>> _dataVector = null;

    /* A vector of column names
     */
    private Vector<Object> _columnNames = null;

    /** A Vector of column widths
     */
    private Vector<Integer> _columnWidths = null;
}
