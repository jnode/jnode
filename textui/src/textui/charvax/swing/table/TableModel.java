/* interface TableModel
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

import charvax.swing.event.TableModelEvent;
import charvax.swing.event.TableModelListener;

/**
 * The TableModel interface specifies the methods that the JTable
 * class will use to interrogate a tabular data model.
 */
public interface TableModel
{
    /** Adds a listener that will be notified each time the data model 
     * changes.
     */
    public void addTableModelListener(TableModelListener l);

    /** Removes the specified listener from the list of listeners.
     */
    public void removeTableModelListener(TableModelListener l);

    /** Get the number of columns in the model. */
    public int getColumnCount();

    /** Get the name of the specified column. */
    public String getColumnName(int column_);

    /** Get the number of rows in the model. */
    public int getRowCount();

    /** Returns an attribute value for the cell at (rowIndex, columnIndex)
     */
    public Object getValueAt(int rowIndex_, int columnIndex_);

    /** Sets the attribute value for the cell at position (row, column).
     */
    public void setValueAt(Object value_, int row_, int column_);

    public void fireTableChanged(TableModelEvent evt_);

}
