/* class AbstractTableModel
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

import java.util.Enumeration;
import java.util.Vector;

import charvax.swing.event.TableModelEvent;
import charvax.swing.event.TableModelListener;

/**
 * This abstract class provides default implementations for most of the methods
 * in the TableModel class. It takes care of the management of listeners, and
 * provides some convenience methods for generating TableModeEvents and
 * dispatching them to the listeners. To implement a concrete TableModel as a
 * subclass of AbstractTableModel, you only need to provide implementations of
 * the following methods:<pre>
    public int getRowCount();
    public int getColumnCount();
    public Object getValueAt(int row, int column);
</pre>
 */
public abstract class AbstractTableModel
    implements TableModel
{
    /** Adds a listener that will be notified each time the data model 
     * changes.
     */
    public void addTableModelListener(TableModelListener l) {
	_listeners.add(l);
    }

    /** Removes the specified listener from the list of listeners.
     */
    public void removeTableModelListener(TableModelListener l) {
	_listeners.remove(l);
    }

    /** Returns a default name for the column using spreadsheet conventions: A,
     * B, C... 
     */
    public String getColumnName(int column_) {
	StringBuffer buf = new StringBuffer('A' + column_);
	return buf.toString();
    }

    /** This empty implementation is provided so that users don't have to
     * provide their own implementation if their table is not editable.
     */
    public void setValueAt(Object value_, int row_, int column_) {}

    /** Forwards the specified event to all TableModelListeners that 
     * registered themselves as listeners for this TableModel.
     */
    public void fireTableChanged(TableModelEvent evt_) {
	Enumeration<TableModelListener> e = _listeners.elements();
	while (e.hasMoreElements()) {
	    TableModelListener l = (TableModelListener) e.nextElement();
	    l.tableChanged(evt_);
	}
    }

    /** Notifies all listeners that the value at [row, column] has been
     * updated.
     */
    public void fireTableCellUpdated(int row_, int column_) {
	TableModelEvent evt = new TableModelEvent(this, row_, row_, column_);
	fireTableChanged(evt);
    }

    /** Notifies all listeners that all cell values in the table may have
     * changed.
     */
    public void fireTableDataChanged() {
	TableModelEvent evt = new TableModelEvent(
	    this, 0, this.getRowCount()-1, TableModelEvent.ALL_COLUMNS);
	fireTableChanged(evt);
    }

    /** Notifies all listeners that rows in the range [firstRow_, lastRow_],
     * inclusive, have been deleted.
     */
    public void fireTableRowsDeleted(int firstRow_, int lastRow_) {
	TableModelEvent evt = new TableModelEvent(
	    this, firstRow_, lastRow_, TableModelEvent.ALL_COLUMNS,
	    TableModelEvent.DELETE);
	fireTableChanged(evt);
    }

    /** Notifies all listeners that rows in the range [firstRow_, lastRow_],
     * inclusive, have been inserted.
     */
    public void fireTableRowsInserted(int firstRow_, int lastRow_) {
	TableModelEvent evt = new TableModelEvent(
	    this, firstRow_, lastRow_, TableModelEvent.ALL_COLUMNS,
	    TableModelEvent.INSERT);
	fireTableChanged(evt);
    }

    //====================================================================
    // INSTANCE VARIABLES
    private Vector<TableModelListener> _listeners = new Vector<TableModelListener>();
}
