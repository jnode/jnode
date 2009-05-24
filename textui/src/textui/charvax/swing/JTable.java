/* class JTable
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

import java.util.Enumeration;
import java.util.Vector;

import charva.awt.Dimension;
import charva.awt.EventQueue;
import charva.awt.Point;
import charva.awt.Scrollable;
import charva.awt.Toolkit;
import charva.awt.event.KeyEvent;
import charva.awt.event.ScrollEvent;
import charva.awt.event.ScrollListener;
import charvax.swing.event.ListSelectionEvent;
import charvax.swing.event.ListSelectionListener;
import charvax.swing.event.TableModelEvent;
import charvax.swing.event.TableModelListener;
import charvax.swing.table.DefaultTableModel;
import charvax.swing.table.TableModel;

/**
 * JTable is a user-interface component that displays data in a two-
 * dimensional table format.<p>
 * The user-interface works as follows:<p>
 * The user can select a column by pressing the left or right arrow keys
 * to move to the desired column, and then pressing ENTER.<br>
 * He/she can select a row by pressing the up and down arrow keys to move to
 * the desired row, then pressing ENTER.<br>
 * Depending on the value of the selection mode, multiple rows and/or columns
 * may be selected. By default the selection mode is set to SINGLE_SELECTION
 * so that only a single row or column can be selected at a time. Selection
 * of rows and/or columns can be enabled/disabled by means of the
 * setRowSelectionAllowed() and setColumnSelectionAllowed() methods.
 */
public class JTable
    extends JComponent
    implements TableModelListener, Scrollable, ListSelectionListener
{
    /** Default constructor
     */
    public JTable() {
	this(new DefaultTableModel(0, 0));
    }

    /** Constructs a table of numRows_ and numColumns_ of empty cells
     * using a DefaultTableModel.
     */
    public JTable(int numRows_, int numColumns_) {
	this(new DefaultTableModel(numRows_, numColumns_));
    }

    /**
     * Construct a JTable from the specified data and column names, using
     * a DefaultTableModel.
     */
    public JTable(Object[][] data_, Object[] columnNames_) {
	this(new DefaultTableModel(data_, columnNames_));
    }

    /** Construct a JTable with the specified data model.
     */
    public JTable(TableModel model_) {
	setModel(model_);
	_rowSelectionModel.addListSelectionListener(this);
    }

    /**
     * Sets the data model to the specified TableModel and registers with it
     * as a listener for events from the model.
     */
    public void setModel(TableModel model_) {
	_model = model_;
	_model.addTableModelListener(this);
    }

    public TableModel getModel() { return _model; }

    public void setValueAt(Object object_, int row_, int column_) {
	_model.setValueAt(object_, row_, column_);
    }

    public Object getValueAt(int row_, int column_) {
	return _model.getValueAt(row_, column_);
    }

    /** This method implements the TableModelListener interface;
     * it is invoked when this table's TableModel generates a 
     * TableModelEvent.
     */
    public void tableChanged(TableModelEvent evt_) {
	/* For now, we'll just post a PaintEvent onto the queue.
	 */
	repaint();
    }

    public void requestFocus() {
	/* Generate the FOCUS_GAINED event.
	 */
	super.requestFocus();

	/* Get the absolute origin of this component 
	 */
	Point origin = getLocationOnScreen();

	/* Calculate the x position of the cursor
	 */
	int x=1;
	for (int i=0; i<_currentColumn; i++) {
	    x += getColumnWidth(i) + 1;
	}

	/* Ensure that the new cursor position is not off the screen (which
	 * it can be if the JTable is in a JViewport).
	 */
	Point newCursor = origin.addOffset(x, _currentRow+1);
	if (newCursor.x < 0)
	    newCursor.x = 0;
	if (newCursor.y < 0)
	    newCursor.y = 0;
	Toolkit.getDefaultToolkit().setCursor(newCursor);
    }

    public void draw(Toolkit toolkit) {
	/* Get the absolute origin of this component.
	 */
	Point origin = getLocationOnScreen();

	int rows = _model.getRowCount();
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
	if (_model.getColumnCount() != 0) {
	    x = getColumnWidth(0) + 1;
	    for (int i=0; i<columns-1; i++) {
		toolkit.setCursor(origin.addOffset(x, 0));
		toolkit.addChar(Toolkit.ACS_TTEE, 0, colorpair);	    // top tee
		toolkit.setCursor(origin.addOffset(x, 1));
		toolkit.addVerticalLine(rows, 0, colorpair);
		toolkit.setCursor(origin.addOffset(x, rows+1));
		toolkit.addChar(Toolkit.ACS_BTEE, 0, colorpair);	    // bottom tee
		x += getColumnWidth(i+1) + 1;
	    }
	}

	/* Now draw the contents of the cells.
	 */
	x = 1;
	for (int column = 0; column<columns; column++) {
	    for (int row=0; row<rows; row++) {
		toolkit.setCursor(origin.addOffset(x, row+1));
		Object value = _model.getValueAt(row, column);

		/* Show the currently SELECTED rows and columns in reverse video
		 */
		int attrib = 
		    (isRowSelected(row) || isColumnSelected(column))  ?
		    Toolkit.A_REVERSE : Toolkit.A_NORMAL;

		// Highlight the current row and column
		if (_currentRow == row || _currentColumn == column)
		    attrib += Toolkit.A_BOLD;

		if (value == null)
		    toolkit.addString("", attrib, colorpair);
		else
		    toolkit.addString(value.toString(), attrib, colorpair);
	    }
	    x += getColumnWidth(column) + 1;
	}
    }

    /**
     * Processes key events occurring on this object 
     */
    public void processKeyEvent(KeyEvent ke_) {
	/* First call all KeyListener objects that may have been registered
	 * for this component. 
	 */
	super.processKeyEvent(ke_);

	/* Check if any of the KeyListeners consumed the KeyEvent.
	 */
	if (ke_.isConsumed())
	    return;

	Toolkit term = Toolkit.getDefaultToolkit();
	EventQueue evtqueue = term.getSystemEventQueue();
	int key = ke_.getKeyCode();
	if (key == '\t') {
	    getParent().nextFocus();
	    return;
	}
	else if (key == KeyEvent.VK_BACK_TAB) {
	    getParent().previousFocus();
	    return;
	}
	else if (key == KeyEvent.VK_UP) {
	    if (_currentRow == 0)
		term.beep();
	    else {
		_currentRow--;
		int x=0;
		for (int i=0; i<_currentColumn; i++)
		    x += getColumnWidth(i) + 1;
		evtqueue.postEvent(
		    new ScrollEvent(this, ScrollEvent.DOWN, 
			new Point(x, _currentRow+1)));
	    }
	}
	else if (key == KeyEvent.VK_DOWN) {
	    if (_currentRow == _model.getRowCount() - 1)
		term.beep();
	    else {
		_currentRow++;
		int x=0;
		for (int i=0; i<_currentColumn; i++)
		    x += getColumnWidth(i) + 1;
		evtqueue.postEvent(
		    new ScrollEvent(this, ScrollEvent.UP, 
			new Point(x, _currentRow+2)));
	    }
	}
	else if (key == KeyEvent.VK_LEFT) {
	    if (_currentColumn == 0)
		term.beep();
	    else {
		_currentColumn--;
		int x=0;
		for (int i=0; i<_currentColumn; i++)
		    x += getColumnWidth(i) + 1;
		evtqueue.postEvent(
		    new ScrollEvent(this, ScrollEvent.RIGHT, 
			new Point(x, _currentRow)));
	    }
	}
	else if (key == KeyEvent.VK_RIGHT) {
	    if (_currentColumn == _model.getColumnCount() - 1)
		term.beep();
	    else {
		_currentColumn++;
		int x=0;
		for (int i=0; i<=_currentColumn; i++)
		    x += getColumnWidth(i) + 1;
		evtqueue.postEvent( 
		    new ScrollEvent(this, ScrollEvent.LEFT, 
			new Point(x, _currentRow)));
	    }
	}
	else if (key == KeyEvent.VK_HOME) {
	    int x=0;
	    for (int i=0; i<_currentColumn; i++)
		x += getColumnWidth(i) + 1;
	    evtqueue.postEvent( 
		    new ScrollEvent(this, ScrollEvent.RIGHT, 
			new Point(x, _currentRow)));
	}
	else if (key == KeyEvent.VK_END) {
	    int x=0;
	    for (int i=0; i<=_currentColumn; i++)
		x += getColumnWidth(i) + 1;
	    evtqueue.postEvent( 
		    new ScrollEvent(this, ScrollEvent.LEFT, 
			new Point(x, _currentRow)));
	}
	else if (key == KeyEvent.VK_ENTER) {
	    if (getColumnSelectionAllowed())
		selectCurrentColumn();

	    if (getRowSelectionAllowed())
		selectCurrentRow();

	    repaint();
	}

	if ((getParent() instanceof JViewport) == false) {
	    draw(Toolkit.getDefaultToolkit());
	    requestFocus();
	    super.requestSync();
	}
    }

    /**
     * Register a ScrollListener object for this table.
     */
    public void addScrollListener(ScrollListener sl_) {
	if (_scrollListeners == null)
	    _scrollListeners = new Vector<ScrollListener>();
	_scrollListeners.add(sl_);
    }

    /**
     * Remove a ScrollListener object that is registered for this table.
     */
    public void removeScrollListener(ScrollListener sl_) {
	if (_scrollListeners == null)
	    return;
	_scrollListeners.remove(sl_);
    }

    /** Process scroll events generated by this JTable.
     */
    public void processScrollEvent(ScrollEvent e_) {
	if (_scrollListeners != null) {
	    for (Enumeration<ScrollListener> e = _scrollListeners.elements(); 
		    e.hasMoreElements(); ) {

		ScrollListener sl = (ScrollListener) e.nextElement();
		sl.scroll(e_);
	    }
	}
    }

    public Dimension getSize() {
	return new Dimension(this.getWidth(), this.getHeight());
    }

    public Dimension minimumSize() {
	return this.getSize();
    }

    public int getWidth() {

	int columns = _model.getColumnCount();
	int width = 1;
	for (int i=0; i<columns; i++) {
	    width += getColumnWidth(i) + 1;
	}

	return width;
    }

    public int getHeight() {
	int rows = _model.getRowCount();
	return rows + 2;
    }

    public void setPreferredScrollableViewportSize(Dimension size_) {
	_viewportSize = size_;
	_viewportSizeSet = true;
    }

    public Dimension getPreferredScrollableViewportSize() {
	if (_viewportSizeSet)
	    return new Dimension(_viewportSize);
	else
	    return minimumSize();
    }

    /** Sets the table's row selection model and registers for notifications
     * from the new selection model.
     */
    public void setSelectionModel(ListSelectionModel model_) {
	_rowSelectionModel = model_;
	_rowSelectionModel.addListSelectionListener(this);
    }

    /** Returns the table's row selection model.
     */
    public ListSelectionModel getSelectionModel() {
	return _rowSelectionModel;
    }

    /** Sets the table's selection mode to allow selection of either single
     * rows and/or columns, or multiple rows and/or columns.
     * @param mode_ the selection mode. Allowable values are
     * ListSelectionModel.SINGLE_SELECTION and
     * ListSelectionModel.MULTIPLE_INTERVAL_SELECTION.
     */
    public void setSelectionMode(int mode_) {
	_rowSelectionModel.setSelectionMode(mode_);
	_columnSelectionModel.setSelectionMode(mode_);
    }

    /** Returns the table's row/column selection mode.
     */
    public int getSelectionMode() {
	return _rowSelectionModel.getSelectionMode();
    }

    /** Set whether selection of columns is allowed.
     */
    public void setColumnSelectionAllowed(boolean allowed_) {
	_columnSelectionAllowed = allowed_;
    }

    /** Returns true if columns can be selected; otherwise false.
     */
    public boolean getColumnSelectionAllowed() {
	return _columnSelectionAllowed;
    }

    /** Set whether selection of rows is allowed.
     */
    public void setRowSelectionAllowed(boolean allowed_) {
	_rowSelectionAllowed = allowed_;
    }

    /** Returns true if rows can be selected; otherwise false.
     */
    public boolean getRowSelectionAllowed() {
	return _rowSelectionAllowed;
    }

    /** Adds the columns from <code>index0_</code> to <code>index1_</code>,
     * inclusive, to the current selection.
     */
    public void addColumnSelectionInterval(int index0_, int index1_) {
	_columnSelectionModel.addSelectionInterval(index0_, index1_);
    }

    /** Adds the rows from <code>index0_</code> to <code>index1_</code>,
     * inclusive, to the current selection.
     */
    public void addRowSelectionInterval(int index0_, int index1_) {
	_rowSelectionModel.addSelectionInterval(index0_, index1_);
    }

    /** Selects the columns from <code>index0_</code> to <code>index1_</code>,
     * inclusive.
     */
    public void setColumnSelectionInterval(int index0_, int index1_) {
	_columnSelectionModel.setSelectionInterval(index0_, index1_);
    }

    /** Selects the rows from <code>index0_</code> to <code>index1_</code>,
     * inclusive.
     */
    public void setRowSelectionInterval(int index0_, int index1_) {
	_rowSelectionModel.setSelectionInterval(index0_, index1_);
    }

    /** Returns the index of the first selected row, or -1 if
     * no row is selected.
     */
    public int getSelectedRow() {
	return _rowSelectionModel.getMinSelectionIndex();
    }

    /** Returns the number of selected rows.
     */
    public int getSelectedRowCount() {
	int min = _rowSelectionModel.getMinSelectionIndex();
	if (min == -1)
	    return 0;

	int max = _rowSelectionModel.getMaxSelectionIndex();
	int j = 0;
	for (int i=min; i<=max; i++) {
	    if (_rowSelectionModel.isSelectedIndex(i))
		j++;
	}
	return j;
    }

    /** Returns an array of the indices of all selected rows.
     */
    public int[] getSelectedRows() {
	int rowCount = getSelectedRowCount();
	if (rowCount == 0)
	    return new int[0];

	int[] array = new int[rowCount];
	int min = _rowSelectionModel.getMinSelectionIndex();
	int max = _rowSelectionModel.getMaxSelectionIndex();
	int j = 0;
	for (int i=min; i<=max; i++) {
	    if (_rowSelectionModel.isSelectedIndex(i))
		array[j++] = i;
	}
	return array;
    }

    /** Returns the index of the first selected column, or -1 if
     * no column is selected.
     */
    public int getSelectedColumn() {
	return _columnSelectionModel.getMinSelectionIndex();
    }

    /** Returns the number of selected columns.
     */
    public int getSelectedColumnCount() {
	int min = _columnSelectionModel.getMinSelectionIndex();
	if (min == -1)
	    return 0;

	int max = _columnSelectionModel.getMaxSelectionIndex();
	int j = 0;
	for (int i=min; i<=max; i++) {
	    if (_columnSelectionModel.isSelectedIndex(i))
		j++;
	}
	return j;
    }

    /** Returns an array of the indices of all selected columns.
     */
    public int[] getSelectedColumns() {
	int columnCount = getSelectedColumnCount();
	if (columnCount == 0)
	    return new int[0];

	int[] array = new int[columnCount];
	int min = _columnSelectionModel.getMinSelectionIndex();
	int max = _columnSelectionModel.getMaxSelectionIndex();
	int j = 0;
	for (int i=min; i<=max; i++) {
	    if (_columnSelectionModel.isSelectedIndex(i))
		array[j++] = i;
	}
	return array;
    }

    /** Returns true if the row with the specified index is selected.
     */
    public boolean isRowSelected(int row_) {
	return _rowSelectionModel.isSelectedIndex(row_);
    }

    /** Returns true if the column with the specified index is selected.
     */
    public boolean isColumnSelected(int column_) {
	return _columnSelectionModel.isSelectedIndex(column_);
    }

    /** This method is invoked when the row selection changes. 
     */
    public void valueChanged(ListSelectionEvent e_) {
	repaint();
    }

    public void debug(int level_) {
	for (int i=0; i<level_; i++)
	    System.err.print("    ");
	System.err.println("JTable origin=" + _origin + 
	    " size=" + getSize());
    }

    private int getColumnWidth(int column_) {
	/* Calculate the column width for the specified column.
	 */
	int columnwidth = _model.getColumnName(column_).length() + 2;

	for (int j=0; j<_model.getRowCount(); j++) {
	    Object value = getValueAt(j, column_);
	    if (value != null) {
		int width = value.toString().length();
		if (width > columnwidth)
		    columnwidth = width;
	    }
	}
	return columnwidth;
    }

    private void selectCurrentColumn() {
	if (_columnSelectionModel.isSelectedIndex(_currentColumn)) {
	    _columnSelectionModel.removeSelectionInterval(
		    _currentColumn, _currentColumn);
	}
	else {
	    int selectionMode = _rowSelectionModel.getSelectionMode();

	    // the column is not currently selected; select it.
	    if (selectionMode == ListSelectionModel.SINGLE_SELECTION) {
		_columnSelectionModel.setSelectionInterval(
			_currentColumn, _currentColumn);
	    }
	    else {
		_columnSelectionModel.addSelectionInterval(
			_currentColumn, _currentColumn);
	    }
	}
    }

    private void selectCurrentRow() {
	if (_rowSelectionModel.isSelectedIndex(_currentRow)) {
	    _rowSelectionModel.removeSelectionInterval(
		    _currentRow, _currentRow);
	}
	else {
	    int selectionMode = _rowSelectionModel.getSelectionMode();

	    // the row is not currently selected; select it.
	    if (selectionMode == ListSelectionModel.SINGLE_SELECTION) {
		_rowSelectionModel.setSelectionInterval(
			_currentRow, _currentRow);
	    }
	    else {
		_rowSelectionModel.addSelectionInterval(
			_currentRow, _currentRow);
	    }
	}
    }

    //--------------------------------------------------------------------
    // INSTANCE VARIABLES

    private TableModel _model = null;

    private Dimension _viewportSize;
    private boolean _viewportSizeSet = false;

    /** This instance variable determines the row that will
     * be highlighted when the table has input focus.
     */
    private int _currentRow = 0;

    private int _currentColumn = 0;

    private boolean _columnSelectionAllowed = true;

    private boolean _rowSelectionAllowed = true;

    /** A list of ScrollListeners registered for this JTable.
     */
    private Vector<ScrollListener> _scrollListeners = null;

    protected ListSelectionModel _rowSelectionModel = 
	    new DefaultListSelectionModel();

    protected ListSelectionModel _columnSelectionModel = 
	    new DefaultListSelectionModel();
}
