/* class DefaultListSelectionModel
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import charvax.swing.event.ListSelectionEvent;
import charvax.swing.event.ListSelectionListener;

/**
 * Default data model for list selections.
 */
public class DefaultListSelectionModel
    implements ListSelectionModel
{
    public DefaultListSelectionModel() {
    }

    /** Add a listener to the list that is notified each time a change to
     * the selection occurs.
     */
    public void addListSelectionListener(ListSelectionListener l_) {
	_listeners.add(l_);
    }

    /** Remove a listener from the list.
     */
    public void removeListSelectionListener(ListSelectionListener l_) {
	_listeners.remove(l_);
    }

    /** Set the selection mode. The following modes are allowed:
     * <ul>
     * <li> SINGLE_SELECTION. Only one list index can be selected at a time.
     * <li> SINGLE_INTERVAL_SELECTION. One contiguous index interval can be set
     * at at time.
     * </ul>
     */
    public void setSelectionMode(int mode_) {
	_selectionMode = mode_;
    }

    /** Returns the current selection mode.
     */
    public int getSelectionMode() {
	return _selectionMode;
    }

    /** Returns true if the specified index is selected.
     */
    public boolean isSelectedIndex(int index) {
	return _selection.contains(new Integer(index));
    }

    /** Returns the first selected index, or -1 if the selection is empty.
     */
    public int getMinSelectionIndex() {
	try {
	    Integer first = (Integer) _selection.first();
	    return first.intValue();
	}
	catch (NoSuchElementException e) {
	    return -1;
	}
    }

    /** Returns the last selected index, or -1 if the selection is empty.
     */
    public int getMaxSelectionIndex() {
	try {
	    Integer last = (Integer) _selection.last();
	    return last.intValue();
	}
	catch (NoSuchElementException e) {
	    return -1;
	}
    }

    /** Returns true if no indices are selected.
     */
    public boolean isSelectionEmpty() {
	return _selection.isEmpty();
    }

    /** Change the selection to be the empty set. If this represents a change
     * to the selection then notify each ListSelectionListener.
     */
    public void clearSelection() {
	if ( ! isSelectionEmpty()) {
	    int first = getMinSelectionIndex();
	    int last = getMaxSelectionIndex();
	    fireValueChanged(first, last);

	    _selection.clear();
	}
    }

    /** Change the selection to be the set union between the current
     * selection and the indices between index0 and index1 inclusive. If
     * this represents a change to the current selection, then notify each
     * ListSelectionListener. Note that index0 does not have to be less 
     * than or equal to index1.
     */
    public void addSelectionInterval(int index0, int index1) {

	TreeSet<Integer> range = getRange(index0, index1);

	/* Find the differences
	 */
	TreeSet<Integer> newSelection = new TreeSet<Integer>(_selection);
	newSelection.addAll(range);

	handleSelectionChange(newSelection);
    }

    /** Change the selection to be the set difference between the current
     * selection and the indices between index0 and index1 inclusive. If
     * this represents a change to the current selection, then notify each
     * ListSelectionListener. Note that index0 does not have to be less 
     * than or equal to index1.
     */
    public void removeSelectionInterval(int index0, int index1) {

	TreeSet<Integer> range = getRange(index0, index1);

	/* Find the differences
	 */
	TreeSet<Integer> newSelection = new TreeSet<Integer>(_selection);
	newSelection.removeAll(range);

	handleSelectionChange(newSelection);
    }

    /** Change the selection to be between index0 and index1 inclusive.
     * If this represents a change to the selection, then notify each
     * ListSelectionListener. Note that index0 doesn't have to be less than or
     * equal to index1.
     */
    public void setSelectionInterval(int index0, int index1) {

	TreeSet<Integer> newSelection = getRange(index0, index1);
	handleSelectionChange(newSelection);
    }

    /** Insert length indices beginning before/after index, without 
     * notifying the ListSelectionListeners. This 
     * is typically called to sync the selection model with a 
     * corresponding change in the data model.
     */
    public void insertIndexInterval(int index, int length, boolean before) {
	if (before) {
	    for (int i=index-length; i>=index; i++)
	    	_selection.add(new Integer(i));
	}
	else {
	    for (int i=index; i<=index+length; i++)
	    	_selection.add(new Integer(i));
	}
    }

    /** Remove the indices in the interval index0,index1 (inclusive) 
     * from the selection model, without notifying the ListSelectionListeners.
     * This is typically called to sync the 
     * selection model with a corresponding change in the data model.
     */
    public void removeIndexInterval(int index0, int index1) {
	for (int i=index0; i<=index1; i++)
	    _selection.remove(new Integer(i));
    }

    /** Notify the listeners that the selection has changed.
     * @param firstindex_ The first index in the interval
     * @param lastindex_ The last index in the interval.
     */
    protected void fireValueChanged(int firstindex_, int lastindex_) {
	ListSelectionEvent event = 
	    new ListSelectionEvent(this, firstindex_, lastindex_, false);

	Iterator<ListSelectionListener> iter = _listeners.iterator();
	while (iter.hasNext()) {
	    ListSelectionListener l = (ListSelectionListener) iter.next();

	    l.valueChanged(event);
	}
    }

    /** Returns a TreeSet that contains the indices between index0 and index1
     * inclusive.
     */
    private TreeSet<Integer> getRange(int index0, int index1) {

	int start = 0;
	int end = 0;
	if (index0 <= index1) {
	    start = index0; end = index1;
	}
	else {
	    start = index1; end = index0;
	}

	TreeSet<Integer> range = new TreeSet<Integer>();
	for (int i=start; i<=end; i++) {
	    range.add(new Integer(i));
	}
	return range;
    }

    private void handleSelectionChange(TreeSet<Integer> newSelection_) {
	/* Find the differences between the old selection and the new
	 * selection.
	 */
	TreeSet<Integer> copyOld =  new TreeSet<Integer>(_selection);
	TreeSet<Integer> differences = new TreeSet<Integer>(newSelection_);

	differences.removeAll(_selection);
	copyOld.removeAll(newSelection_);
	differences.addAll(copyOld);

	/* We must set the new selection before calling "valueChanged"
	 * so that the "valueChanged" method sees the correct new selection.
	 */
	_selection = newSelection_;

	if ( ! differences.isEmpty()) {
	    Integer first = (Integer) differences.first();
	    Integer last = (Integer) differences.last();
	    fireValueChanged(first.intValue(), last.intValue());
	}
    }

    //====================================================================
    // INSTANCE VARIABLES

    /** The list of listeners.
     */
    protected ArrayList<ListSelectionListener> _listeners = new ArrayList<ListSelectionListener>();

    /** The set of selected indices.
     */
    protected TreeSet<Integer> _selection = new TreeSet<Integer>();

    private int _selectionMode = ListSelectionModel.SINGLE_SELECTION;
}
