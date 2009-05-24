/* class JList
 *
 * Copyright (C) 2003  R M Pitman
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

/*
 * Modified Jul 15, 2003 by Tadpole Computer, Inc.
 * Modifications Copyright 2003 by Tadpole Computer, Inc.
 *
 * Modifications are hereby licensed to all parties at no charge under
 * the same terms as the original.
 *
 * Fixed case where visible row count was larger that the total number
 * of rows in the model.
 */

package charvax.swing;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import charva.awt.Container;
import charva.awt.Dimension;
import charva.awt.EventQueue;
import charva.awt.Insets;
import charva.awt.Point;
import charva.awt.Scrollable;
import charva.awt.Toolkit;
import charva.awt.event.KeyEvent;
import charva.awt.event.MouseEvent;
import charva.awt.event.ScrollEvent;
import charva.awt.event.ScrollListener;
import charvax.swing.event.ListSelectionListener;

/**
 * A component that allows the user to select one or more objects from a 
 * list.<p>
 *
 * The JList does not provide scrolling capability. The 
 * JList is normally inserted into a JScrollPane to provide scrolling.<p>
 *
 */
public class JList
    extends JComponent 
    implements Scrollable
{
    /** Constructs a JList with 5 rows, 10 columns wide.
     */
    public JList() {
	setModel(new DefaultListModel());
    }

    /**
     * Construct a JList that displays the elements in the specified 
     * non-null model.
     */
    public JList(ListModel model_) {
	setModel(model_);
    }

    /**
     * Construct a JList containing the items in the specified array.
     */
    public JList(Object[] items_)
    {
	setListData(items_);
    }

    /**
     * Construct a JList containing the items in the specified Vector.
     */
    public JList(Vector<?> items_)
    {
	setListData(items_);
    }

    /** Constructs a ListModel from an array of Objects and then applies 
     * setModel to it.
     */
    public void setListData(Object[] listData_) {
	DefaultListModel model = new DefaultListModel();

	for (int i=0; i<listData_.length; i++) {
	    model.addElement(listData_[i]);
	}
	setModel(model);
    }

    /** Constructs a ListModel from a Vector and then applies setModel to it.
     */
    public void setListData(Vector<?> listData_) {
	Enumeration<?> e = listData_.elements();
	DefaultListModel model = new DefaultListModel();

	while (e.hasMoreElements()) {
	    Object item = e.nextElement();
	    model.addElement(item);
	}
	setModel(model);
    }

    /** Sets the model that represents the "contents" of the list, and
     * clears the selection.
     */
    public void setModel(ListModel model_) {
	_listModel = model_;
	_selectionModel.clearSelection();
    }

    /** Returns the data model that holds the list of items displayed by this
     * JList.
     */
    public ListModel getModel() {
	return _listModel;
    }

    /** Set the maximum number of rows that can be displayed at a time
     * by the JScrollPane which contains this JList.
     */
    public void setVisibleRowCount(int rows_) {
	_visibleRows = rows_;
    }

    public int getVisibleRowCount() {
	return _visibleRows;
    }

    /** Set the number of columns INSIDE the list.
     */
    public void setColumns(int cols_) {
	_columns = cols_;
    }

    public Dimension getSize() { 
	return new Dimension(getWidth(), getHeight());
    }

    public int getWidth() {
	Insets insets = super.getInsets();
	return _columns + insets.left + insets.right; 
    }

    public int getHeight() { 
	Insets insets = super.getInsets();
	return _listModel.getSize() + insets.top + insets.bottom;
    }

    /** Returns the size of the viewport needed to display visibleRows rows.
     */
    public Dimension getPreferredScrollableViewportSize() {
	return new Dimension(getWidth(), getVisibleRowCount());
    }

    /** Sets the selection model of the JList to an implementation
     * of the ListSelectionModel interface.
     */
    public void setSelectionModel(ListSelectionModel model_) {
	_selectionModel = model_;
    }

    /** Returns the list's implementation of ListSelectionModel.
     */
    public ListSelectionModel getSelectionModel() {
	return _selectionModel;
    }

    /**
     * Register an ListSelectionListener object for this component.
     * The listener is notified each time a change to the selection occurs.
     */
    public void addListSelectionListener(ListSelectionListener il_) {
	_selectionModel.addListSelectionListener(il_);
    }

    /** Remove the specified ListSelectionListener from the list of listeners
     * that will be notified when the selection changes.
     */
    public void removeListSelectionListener(ListSelectionListener listener_) {
	_selectionModel.removeListSelectionListener(listener_);
    }

    /**
     * Get the first selected index, or -1 if there is no selected index.
     */
    public int getSelectedIndex() {
	return _selectionModel.getMinSelectionIndex();
    }

    /** Returns an array of the selected indices. The indices are
     * sorted in increasing index order.
     */
    public int[] getSelectedIndices() {
	ArrayList<Integer> objects = new ArrayList<Integer>();
	if ( ! _selectionModel.isSelectionEmpty()) {
	    int first = _selectionModel.getMinSelectionIndex();
	    int last = _selectionModel.getMaxSelectionIndex();
	    for (int i=first; i<=last; i++) {
		if (_selectionModel.isSelectedIndex(i))
		    objects.add(new Integer(i));
	    }
	}

	int[] values = new int[objects.size()];
	for (int i=0; i<values.length; i++) {
	    values[i] = objects.get(i).intValue();
	}
	return values;
    }

    /**
     * Get the first selected item on this list, or <code>null</code>
     * if the selection is empty.
     */
    public Object getSelectedValue() {

	/* Return null if there are no selected items.
	 */
	int index = _selectionModel.getMinSelectionIndex();
	if (index == -1)
	    return null;

	return _listModel.getElementAt(index);
    }

    /** Returns an array of the selected values. The objects are
     * sorted in increasing index order.
     */
    public Object[] getSelectedValues() {
	ArrayList<Object> objects = new ArrayList<Object>();
	if ( ! _selectionModel.isSelectionEmpty()) {
	    int first = _selectionModel.getMinSelectionIndex();
	    int last = _selectionModel.getMaxSelectionIndex();
	    for (int i=first; i<=last; i++) {
		if (_selectionModel.isSelectedIndex(i))
		    objects.add(_listModel.getElementAt(i));
	    }
	}
	return objects.toArray();
    }

    /**
     * Make the specified item visible (by scrolling the list up or down).
     * This method does not do anything unless the JList is in a JViewport.
     * Note that the list is not redrawn by this method; the redrawing is
     * done by the JScrollPane (that is registered as a ScrollListener).
     */
    public synchronized void ensureIndexIsVisible(int index_) {
	if ( ! (getParent() instanceof JViewport) )
	    return;

	if (index_ < 0)
	    index_ = 0;
	else if (index_ > _listModel.getSize() - 1) {
	    index_ = _listModel.getSize() - 1;
	}

	// It seems reasonable to assume that the "current row" should
	// be set to the index that is being made visible.
	_currentRow = index_;

	Toolkit term = Toolkit.getDefaultToolkit();
	EventQueue evtqueue = term.getSystemEventQueue();

	// First scroll the list DOWN so that index 0 is visible.
	evtqueue.postEvent(
	        new ScrollEvent(this, ScrollEvent.DOWN, 
		new Point(0, 0)));

	// Then (if necessary) scroll it UP so that the specified index
	// is not below the bottom of the viewport.
	evtqueue.postEvent(
	        new ScrollEvent(this, ScrollEvent.UP, 
		new Point(0, index_)));
    }

    /**
     * Select the item at the specified index. Note that this method
     * does not redraw the JList.
     */
    public void setSelectedIndex(int index_) {
	_selectionModel.setSelectionInterval(index_, index_);
    }

    /** 
     * Sets the selection to be the set union between the current
     * selection and the specified interval between index0_ and index1_
     * (inclusive).
     */
    public void addSelectionInterval(int index0_, int index1_) {
	_selectionModel.addSelectionInterval(index0_, index1_);
    }

    /** 
     * Sets the selection to be the set difference between the current
     * selection and the specified interval between index0_ and index1_
     * (inclusive).
     */
    public void removeSelectionInterval(int index0_, int index1_) {
	_selectionModel.removeSelectionInterval(index0_, index1_);
    }

    /** Clears the selection. After this <code>isSelectionEmpty()</code> 
     * will return true.
     */
    public void clearSelection() {
	_selectionModel.clearSelection();
    }

    /** Returns the lowest selected item index.
     */
    public int getMinSelectionIndex() {
	return _selectionModel.getMinSelectionIndex();
    }

    /** Returns the highest selected item index.
     */
    public int getMaxSelectionIndex() {
	return _selectionModel.getMaxSelectionIndex();
    }

    /**
     * Sets the flag that determines whether this list allows multiple
     * selections.
     * @param mode_ the selection mode. Allowed values are:<p>
     * <ul>
     * <li> ListSelectionModel.SINGLE_SELECTION. Only one list index 
     * can be selected at a time.
     * <li> ListSelectionModel.SINGLE_INTERVAL_SELECTION. 
     * <li> ListSelectionModel.MULTIPLE_INTERVAL_SELECTION. Any number 
     * of list items can be selected simultaneously.
     * </ul>
     */
    public void setSelectionMode(int mode_) { 
	_selectionModel.setSelectionMode(mode_);
    }

    /**
     * Determines whether this list allows multiple selections.
     */
    public int getSelectionMode() { 
	return _selectionModel.getSelectionMode();
    }

    /** Determines if the specified item in this scrolling list is selected.
     */
    public boolean isIndexSelected(int index_) {
	return _selectionModel.isSelectedIndex(index_);
    }

    /**
     * Called by LayoutManager.
     */
    public Dimension minimumSize() {

	/* Calculate the minimum number of columns that will contain all the
	 * items in the list.
	 */
	_columns = 1;
	for (int i=0; i< _listModel.getSize(); i++) {
	    String c = _listModel.getElementAt(i).toString();
	    if (c.length() > _columns)
		_columns = c.length();
	}

	/* Take into account the border inherited from the JComponent
	 * superclass.
	 */
	Insets insets = super.getInsets();
	return new Dimension(_columns + insets.left + insets.right, 
		_visibleRows + insets.top + insets.bottom);
    }

    public void requestFocus() {
	/* Generate the FOCUS_GAINED event.
	 */
	super.requestFocus();

	/* Get the absolute origin of this component 
	 */
	Point origin = getLocationOnScreen();
	Insets insets = super.getInsets();
	origin.translate(insets.left, insets.top);
	Toolkit.getDefaultToolkit().setCursor(
		origin.addOffset(0, _currentRow));
    }

    /** Draws this component. Overrides draw() in Component.
     * @param toolkit
     */
    public void draw(Toolkit toolkit) {

	/* This situation can occur if the ListModel has been changed.
	 */
	if (_currentRow >= _listModel.getSize()) {
	    if (_listModel.getSize() == 0)
		_currentRow = 0;
	    else
		_currentRow = _listModel.getSize() - 1;
	}

	/* Draw the border if it exists
	 */
	super.draw(toolkit);

	/* Get the absolute origin of this component.
	 */
	Point origin = getLocationOnScreen();
	Insets insets = super.getInsets();
	origin.translate(insets.left, insets.top);

	int colorpair = getCursesColor();
	int attribute;

	StringBuffer blanks = new StringBuffer();
	for (int j=0; j<_columns; j++)
	    blanks.append(' ');

	for (int i = 0; i < _listModel.getSize(); i++) {

	    toolkit.setCursor(origin.x, origin.y + i);

	    if (isIndexSelected(i))
		attribute = Toolkit.A_REVERSE;
	    else
		attribute = 0;

	    if (i == _currentRow)
		attribute += Toolkit.A_BOLD;

	    String item = _listModel.getElementAt(i).toString();

	    StringBuffer buffer = new StringBuffer(item);
	    for (int k=item.length(); k<_columns; k++)
		buffer.append(' ');

	    toolkit.addString(buffer.toString(), attribute, colorpair);
	}	// end FOR loop

	for (int i = _listModel.getSize(); i < _visibleRows; i++) {
	    toolkit.setCursor(origin.x, origin.y + i);
	    toolkit.addString(blanks.toString(), 0, colorpair);
	}
    }

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
	switch (key) {
	    case '\t':
		getParent().nextFocus();
		return;

	    case KeyEvent.VK_BACK_TAB:
		getParent().previousFocus();
		return;

	    case KeyEvent.VK_DOWN:
		/* If we are already at the bottom of the list, ignore
		 * this keystroke.
		 */
		if (_currentRow >= _listModel.getSize() -1)
		    return;

		evtqueue.postEvent(
		    new ScrollEvent(this, ScrollEvent.UP, 
			new Point(0, ++_currentRow)));
		break;

	    case KeyEvent.VK_PAGE_DOWN:
		_currentRow += _visibleRows;
		if (_currentRow >= _listModel.getSize())
		    _currentRow = _listModel.getSize() -1;

		evtqueue.postEvent(
		    new ScrollEvent(this, ScrollEvent.UP, 
			new Point(0, _currentRow)));
		break;

	    case KeyEvent.VK_END:
		_currentRow = _listModel.getSize() - 1;
		evtqueue.postEvent(new ScrollEvent(
		        this, ScrollEvent.UP, new Point(0, _currentRow)));
		break;

	    case KeyEvent.VK_UP:
		/* If we are already at the top of the list, ignore
		 * this keystroke.
		 */
		if (_currentRow < 1)
		    return;

		evtqueue.postEvent(new ScrollEvent(
		        this, ScrollEvent.DOWN, new Point(0, --_currentRow)));
		break;

	    case KeyEvent.VK_PAGE_UP:
		_currentRow -= _visibleRows;
		if (_currentRow < 0)
		    _currentRow = 0;

		evtqueue.postEvent(new ScrollEvent(this, ScrollEvent.DOWN, 
			new Point(0, _currentRow)));
		break;

	    case KeyEvent.VK_ENTER:
		_doSelect();
		break;

	    case KeyEvent.VK_HOME:
		_currentRow = 0;
		break;
	}

	if ((getParent() instanceof JViewport) == false) {
	    draw(Toolkit.getDefaultToolkit());
	    requestFocus();
	    super.requestSync();
	}
    }

    private void _doSelect() {
	/* Pressing ENTER or double-clicking on a row selects/deselects 
	 * the current row. If the list is empty, ignore the keystroke.
	 */
	if (_listModel.getSize() == 0)
	    return;

	if (isIndexSelected(_currentRow))
	    removeSelectionInterval(_currentRow, _currentRow);
	else {
	    int mode = getSelectionMode();
	    if (mode == ListSelectionModel.SINGLE_SELECTION)
		setSelectedIndex(_currentRow);
	    else
		addSelectionInterval(_currentRow, _currentRow);
	}
	repaint();
    }

    public void processMouseEvent(MouseEvent e_) {
	super.processMouseEvent(e_);

	if (e_.getButton() == MouseEvent.BUTTON1 &&
		e_.getModifiers() == MouseEvent.MOUSE_CLICKED &&
		this.isFocusTraversable()) {

	    if (e_.getClickCount() == 1) {
		int y = e_.getY();
		Point origin = getLocationOnScreen();
		Container parent = getParent();
		if (parent instanceof JViewport) {
		    _currentRow = y - origin.y;
		    repaint();
		}
	    }
	    else
		_doSelect();
	}
    }

    /**
     * Register a ScrollListener object for this JList.
     */
    public void addScrollListener(ScrollListener sl_) {
	if (_scrollListeners == null)
	    _scrollListeners = new Vector<ScrollListener>();
	_scrollListeners.add(sl_);
    }

    /**
     * Remove a ScrollListener object that is registered for this JList.
     */
    public void removeScrollListener(ScrollListener sl_) {
	if (_scrollListeners == null)
	    return;
	_scrollListeners.remove(sl_);
    }

    /** Process scroll events generated by this JList.
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

    /** Outputs a textual description of this component to stderr.
     */
    public void debug(int level_) {
	for (int i=0; i<level_; i++)
	    System.err.print("    ");
	System.err.println("JList origin=" + _origin + 
	    " size=" + minimumSize());
    }

    //================================================================
    // INSTANCE VARIABLES

    private int _visibleRows = 5;
    private int _columns = 10;

    /** Offset (from start of list) of the item under the cursor (i.e. the
     * item that will be selected/deselected if the user presses ENTER)
     */
    protected int _currentRow = 0;

    /** The ListSelectionModel used by this JList.
     */
    protected ListSelectionModel _selectionModel = 
	    new DefaultListSelectionModel();

    /** The ListModel that holds the items that are displayed by
     * this JList.
     */
    protected ListModel _listModel;

    /** A list of ScrollListeners registered for this JList.
     */
    private Vector<ScrollListener> _scrollListeners = null;

}
