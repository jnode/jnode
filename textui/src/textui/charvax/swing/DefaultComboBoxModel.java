/* class DefaultComboBoxModel
 *
 * Copyright (C) 2001, 2002  R M Pitman
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
import java.util.Vector;

/**
 * The default model for combo boxes.
 */
public class DefaultComboBoxModel
    extends AbstractListModel
    implements MutableComboBoxModel
{
    /** Default constructor
     */
    public DefaultComboBoxModel() {
	super();
    }

    /** Constructs a DefaultComboBoxModel object initialized with a vector.
     */
    public DefaultComboBoxModel(Vector<?> items_) {
	super();
	for (int i=0; i<items_.size(); i++) {
	    _list.add(items_.elementAt(i));
	}
	if (getSize() > 0)
	    _selectedItem = _list.get(0);
    }

    /** Constructs a DefaultComboBoxModel object initialized with an array
     * of objects.
     */
    public DefaultComboBoxModel(Object[] items_) {
	super();
	for (int i=0; i<items_.length; i++) {
	    _list.add(items_[i]);
	}
	if (getSize() > 0)
	    _selectedItem = _list.get(0);
    }

    /** Returns the value at the specified index.
     */
    public Object getElementAt(int index_) {
	return _list.get(index_);
    }

    /** Returns the length of the list.
     */
    public int getSize() {
	return _list.size();
    }

    /** Returns true if the specified element is in the list.
     */
    public boolean contains(Object elem_) {
	return _list.contains(elem_);
    }

    /** Inserts the specified object at the specified position in this list.
    public void add(int index_, Object obj_) {
	_list.add(index_, obj_);
	super.fireContentsChanged(this, index_, index_);
    }
     */

    /** Returns the index of the first occurrence of the specified object.
     */
    public int indexOf(Object elem_) {
	return _list.indexOf(elem_);
    }

    // The following two methods implement the ComboBoxModel interface

    /** Return the selected item.
     */
    public Object getSelectedItem() {
	return _selectedItem;
    }

    /** Set the selected item
     */
    public void setSelectedItem(Object anItem_) {
	_selectedItem = anItem_;
    }

    // The following methods implement the MutableComboBoxModel interface.

    /** Add the specified object to the end of the list.
     */
    public void addElement(Object obj_) {
	_list.add(obj_);
	super.fireContentsChanged(this, _list.size(), _list.size());
	if (getSize() == 1)
	    _selectedItem = obj_;
    }

    /** Deletes the component at the specified index.
     */
    public void removeElementAt(int index_) {
	if (_selectedItem == _list.get(index_)) {
	    /* If the selected item is being removed, replace it with the
	     * item above it, unless the item being removed is first in the
	     * list.
	     */
	    if (index_ == 0)
		_selectedItem = (getSize() == 1) ? null : _list.get(1);
	    else
		_selectedItem = _list.get(index_ - 1);
	}

	_list.remove(index_);
	super.fireIntervalRemoved(this, index_, index_);
    }

    /** Removes all elements from this list and sets its size to zero.
     */
    public void removeAllElements() {
	int size = _list.size();
	_list.clear();
	_selectedItem = null;
	super.fireIntervalRemoved(this, 0, size - 1);
    }


    /** Insert an item at the specified index.
     */
    public void insertElementAt(Object obj_, int index_) {
	_list.add(index_, obj_);
	super.fireIntervalAdded(this, index_, index_);
	if (getSize() == 1)
	    _selectedItem = obj_;
    }

    /** Remove the specified object from the model.
     */
    public void removeElement(Object obj_) {
	int index = _list.indexOf(obj_);
	if (index >= 0)
	    this.removeElementAt(index);
    }

    //====================================================================
    // INSTANCE VARIABLES

    private ArrayList<Object> _list = new ArrayList<Object>();

    private Object _selectedItem;
}
