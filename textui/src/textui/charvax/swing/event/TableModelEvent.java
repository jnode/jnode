/* class TableModelEvent
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

package charvax.swing.event;

import charvax.swing.table.TableModel;

/**
 * This event is fired when the data in a table changes.
 */
public class TableModelEvent extends java.util.EventObject
{
    private static final long serialVersionUID = 1L;
    
    /** All row data has changed; listeners should discard all state
     * and re-query the TableModel.
     */
    public TableModelEvent(TableModel source_) {
	this (source_, 0, source_.getRowCount()-1, ALL_COLUMNS, UPDATE);
    }

    /** This row of data has been updated.
     */
    public TableModelEvent(TableModel source_, int row_) {
	this(source_, row_, row_, ALL_COLUMNS, UPDATE);
    }

    /** The data in rows [firstRow_, lastRow] have been updated.
     */
    public TableModelEvent(TableModel source_, int firstRow_,
	    int lastRow_) {
	this(source_, firstRow_, lastRow_, ALL_COLUMNS, UPDATE);
    }

    /** The cells in the specified column in rows [firstRow_, lastRow] 
     * have been updated.
     */
    public TableModelEvent(TableModel source_, int firstRow_,
	    int lastRow_, int column_) {
	this(source_, firstRow_, lastRow_, column_, UPDATE);
    }

    public TableModelEvent(TableModel source_,
	int firstRow_, int lastRow_, int column_, int type_)
    {
	super(source_);
	_firstRow = firstRow_;
	//_lastRow = lastRow_;
	_column = column_;
	_type = type_;
    }

    /** Get the index of the first row that changed
     */
    public int getFirstRow() { return _firstRow; }

    /** Get the index of the last row that changed
     */
    public int getLastRow() { return _firstRow; }

    public int getColumn() { return _column; }

    /** Returns the type of event - one of INSERT, UPDATE
     * or DELETE.
     */
    public int getType() { return _type; }

    // INSTANCE VARIABLES
    private int _firstRow;
    //private final int _lastRow;
    private int _column;
    private int _type;

    /* Allowed values for the "type" parameter.
     */
    public static final int DELETE = 1;
    public static final int INSERT = 2;
    public static final int UPDATE = 3;

    /* Allowed value for the "column" parameter.
     */
    public static final int ALL_COLUMNS = -1;
}
