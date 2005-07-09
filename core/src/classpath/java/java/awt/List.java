/* List.java -- A listbox widget
   Copyright (C) 1999, 2002, 2004  Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package java.awt;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.peer.ListPeer;
import java.util.EventListener;
import java.util.Vector;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleSelection;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;

/**
  * Class that implements a listbox widget
  *
  * @author Aaron M. Renn (arenn@urbanophile.com)
  */
public class List extends Component
  implements ItemSelectable, Accessible
{

/*
	 * Static Variables
	 */

// Serialization constant
private static final long serialVersionUID = -3304312411574666869L;

/*************************************************************************/

/*
	 * Instance Variables
	 */

// FIXME: Need read/writeObject

/**
	  * @serial The items in the list.
	  */
private Vector items = new Vector();

/**
	  * @serial Indicates whether or not multiple items can be selected
	  * simultaneously.
	  */
private boolean multipleMode;

/**
	  * @serial The number of rows in the list.  This is set on creation
	  * only and cannot be modified.
	  */
private int rows;

/**
	  * @serial An array of the item indices that are selected.
	  */
private int[] selected;

/**
	  * @serial An index value used by <code>makeVisible()</code> and
	  * <code>getVisibleIndex</code>.
	  */
private int visibleIndex;

// The list of ItemListeners for this object.
private ItemListener item_listeners;

// The list of ActionListeners for this object.
private ActionListener action_listeners;


/*************************************************************************/

/*
	 * Constructors
	 */

/**
	  * Initializes a new instance of <code>List</code> with no visible lines
	  * and multi-select disabled.
	  *
	  * @exception HeadlessException If GraphicsEnvironment.isHeadless() is true.
	  */
public
List()
{
		this(4, false);
}

/*************************************************************************/

/**
	  * Initializes a new instance of <code>List</code> with the specified
	  * number of visible lines and multi-select disabled.
	  *
  * @param rows The number of visible rows in the list.
	  *
	  * @exception HeadlessException If GraphicsEnvironment.isHeadless() is true.
	  */
public
List(int rows)
{
		this(rows, false);
}

/*************************************************************************/

/**
	  * Initializes a new instance of <code>List</code> with the specified
	  * number of lines and the specified multi-select setting.
	  *
  * @param rows The number of visible rows in the list.
	  * @param multipleMode <code>true</code> if multiple lines can be selected
	  * simultaneously, <code>false</code> otherwise.
	  *
	  * @exception HeadlessException If GraphicsEnvironment.isHeadless() is true.
	  */
public 
List(int rows, boolean multipleMode)
{
		this.rows = rows;
		this.multipleMode = multipleMode;

		if (GraphicsEnvironment.isHeadless())
    throw new HeadlessException ();
}

/*************************************************************************/

/*
	 * Instance Variables
	 */

/**
	  * Returns the number of items in this list.
	  *
	  * @return The number of items in this list.
	  */
public int
getItemCount()
{
  return countItems ();
}

/*************************************************************************/

/**
	  * Returns the number of items in this list.
	  *
	  * @return The number of items in this list.
	  *
	  * @deprecated This method is deprecated in favor of
	  * <code>getItemCount()</code>
	  */
public int
countItems()
{
  return items.size ();
}

/*************************************************************************/

/**
	  * Returns the complete list of items.
	  *
	  * @return The complete list of items in the list.
	  */
public synchronized String[]
getItems()
{
		String[] l_items = new String[getItemCount()];

		items.copyInto(l_items);
  return(l_items);
}

/*************************************************************************/

/**
	  * Returns the item at the specified index.
	  *
	  * @param index The index of the item to retrieve.
	  *
	  * @exception IndexOutOfBoundsException If the index value is not valid.
	  */
public String
getItem(int index)
{
  return((String)items.elementAt(index));
}

/*************************************************************************/

/**
	  * Returns the number of visible rows in the list.
	  *
	  * @return The number of visible rows in the list.
	  */
public int
getRows()
{
  return(rows);
}

/*************************************************************************/

/**
	  * Tests whether or not multi-select mode is enabled.
	  *
	  * @return <code>true</code> if multi-select mode is enabled,
	  * <code>false</code> otherwise.
	  */
public boolean
isMultipleMode()
{
  return allowsMultipleSelections ();
}

/*************************************************************************/

/**
	  * Tests whether or not multi-select mode is enabled.
	  *
	  * @return <code>true</code> if multi-select mode is enabled,
	  * <code>false</code> otherwise.
	  *
	  * @deprecated This method is deprecated in favor of 
	  * <code>isMultipleMode()</code>.
	  */
public boolean
allowsMultipleSelections()
{
  return multipleMode;
}

/*************************************************************************/

/**
	  * This method enables or disables multiple selection mode for this
	  * list.
	  *
	  * @param multipleMode <code>true</code> to enable multiple mode,
	  * <code>false</code> otherwise.
	  */
public void
setMultipleMode(boolean multipleMode)
{
  setMultipleSelections (multipleMode);
}

/*************************************************************************/

/**
	  * This method enables or disables multiple selection mode for this
	  * list.
	  *
	  * @param multipleMode <code>true</code> to enable multiple mode,
	  * <code>false</code> otherwise.
  *
  * @deprecated
	  */
public void
setMultipleSelections(boolean multipleMode)
{
  this.multipleMode = multipleMode;

  ListPeer peer = (ListPeer) getPeer ();
  if (peer != null)
    peer.setMultipleMode (multipleMode);
}

/*************************************************************************/

/**
	  * Returns the minimum size of this component.
	  *
	  * @return The minimum size of this component.
	  */
public Dimension
getMinimumSize()
{
  return getMinimumSize (getRows ());
}

/*************************************************************************/

/**
	  * Returns the minimum size of this component.
	  *
	  * @return The minimum size of this component.
	  *
	  * @deprecated This method is deprecated in favor of
	  * <code>getMinimumSize</code>.
	  */
public Dimension
minimumSize()
{
  return minimumSize (getRows ());
}

/*************************************************************************/

/**
	  * Returns the minimum size of this component assuming it had the specified
	  * number of rows.
	  *
	  * @param rows The number of rows to size for.
	  *
	  * @return The minimum size of this component.
	  */
public Dimension
getMinimumSize(int rows)
{
  return minimumSize (rows);
}

/*************************************************************************/

/**
	  * Returns the minimum size of this component assuming it had the specified
	  * number of rows.
	  *
	  * @param rows The number of rows to size for.
	  *
	  * @return The minimum size of this component.
	  *
	  * @deprecated This method is deprecated in favor of 
	  * <code>getMinimumSize(int)</code>>
	  */
public Dimension
minimumSize(int rows)
{
  ListPeer peer = (ListPeer) getPeer ();
  if (peer != null)
    return peer.minimumSize (rows);
  else
    return new Dimension (0, 0);
}

/*************************************************************************/

/**
	  * Returns the preferred size of this component.
	  *
	  * @return The preferred size of this component.
	  */
public Dimension
getPreferredSize()
{
  return getPreferredSize (getRows ());
}

/*************************************************************************/

/**
	  * Returns the preferred size of this component.
	  *
	  * @return The preferred size of this component.
	  *
	  * @deprecated This method is deprecated in favor of
	  * <code>getPreferredSize</code>.
	  */
public Dimension
preferredSize()
{
  return preferredSize (getRows ());
}

/*************************************************************************/

/**
	  * Returns the preferred size of this component assuming it had the specified
	  * number of rows.
	  *
	  * @param rows The number of rows to size for.
	  *
	  * @return The preferred size of this component.
	  */
public Dimension
getPreferredSize(int rows)
{
  return preferredSize (rows);
}

/*************************************************************************/

/**
	  * Returns the preferred size of this component assuming it had the specified
	  * number of rows.
	  *
	  * @param rows The number of rows to size for.
	  *
	  * @return The preferred size of this component.
	  *
	  * @deprecated This method is deprecated in favor of 
	  * <code>getPreferredSize(int)</code>>
	  */
public Dimension
preferredSize(int rows)
{
  ListPeer peer = (ListPeer) getPeer ();
  if (peer != null)
    return peer.preferredSize (rows);
  else
    return new Dimension (0, 0);
}

/*************************************************************************/

/**
	  * This method adds the specified item to the end of the list.
	  *
	  * @param item The item to add to the list.
	  */
public void
add(String item)
{
  add (item, -1);
}

/*************************************************************************/

/**
	  * This method adds the specified item to the end of the list.
	  *
	  * @param item The item to add to the list.
	  *
	  * @deprecated Use add() instead.
	  */
public void
addItem(String item)
{
  addItem (item, -1);
}

/*************************************************************************/

/**
	  * Adds the specified item to the specified location in the list.
	  * If the desired index is -1 or greater than the number of rows
	  * in the list, then the item is added to the end.
	  *
	  * @param item The item to add to the list.
	  * @param index The location in the list to add the item, or -1 to add
	  * to the end.
	  */
public void
add(String item, int index)
{
  addItem (item, index);
}

/*************************************************************************/

/**
	  * Adds the specified item to the specified location in the list.
	  * If the desired index is -1 or greater than the number of rows
	  * in the list, then the item is added to the end.
	  *
	  * @param item The item to add to the list.
	  * @param index The location in the list to add the item, or -1 to add
	  * to the end.
	  *
	  * @deprecated Use add() instead.
	  */
public void
addItem(String item, int index)
{
  if ((index == -1) || (index >= items.size ()))
    items.addElement (item);
  else
    items.insertElementAt (item, index);

  ListPeer peer = (ListPeer) getPeer ();
  if (peer != null)
    peer.add (item, index);
}

/*************************************************************************/

/**
	  * Deletes the item at the specified index.
	  *
	  * @param index The index of the item to delete.
	  *
	  * @exception IllegalArgumentException If the index is not valid
  *
  * @deprecated
	  */
public void
delItem(int index) throws IllegalArgumentException
{
  items.removeElementAt (index);

  ListPeer peer = (ListPeer) getPeer ();
  if (peer != null)
    peer.delItems (index, index);
}

/*************************************************************************/

/**
	  * Deletes the item at the specified index.
	  *
	  * @param index The index of the item to delete.
	  *
	  * @exception IllegalArgumentException If the index is not valid
	  */
public void
remove(int index) throws IllegalArgumentException
{
  delItem (index);
}

/*************************************************************************/

/**
	  * Deletes all items in the specified index range.
	  *
	  * @param start The beginning index of the range to delete.
	  * @param end The ending index of the range to delete.
	  *
	  * @exception IllegalArgumentException If the indexes are not valid
	  *
	  * @deprecated This method is deprecated for some unknown reason.
	  */
public synchronized void
delItems(int start, int end) throws IllegalArgumentException
{
		if ((start < 0) || (start >= items.size()))
			throw new IllegalArgumentException("Bad list start index value: " + start);

		if ((start < 0) || (start >= items.size()))
			throw new IllegalArgumentException("Bad list start index value: " + start);

		if (start > end)
			throw new IllegalArgumentException("Start is greater than end!");

		// We must run the loop in reverse direction.
		for (int i = end; i >= start; --i)
    items.removeElementAt (i);
  if (peer != null)
    {
			ListPeer l = (ListPeer) peer;
      l.delItems (start, end);
	}
}

/*************************************************************************/

/**
	  * Deletes the first occurrence of the specified item from the list.
	  *
	  * @param item The item to delete.
	  *
	  * @exception IllegalArgumentException If the specified item does not exist.
	  */
public synchronized void
remove(String item) throws IllegalArgumentException
{
		int index = items.indexOf(item);
		if (index == -1)
			throw new IllegalArgumentException("List element to delete not found");

		remove(index);
}

/*************************************************************************/

/**
	  * Deletes all of the items from the list.
	  */
public synchronized void
removeAll()
{
  clear ();
}

/*************************************************************************/

/**
	  * Deletes all of the items from the list.
	  * 
	  * @deprecated This method is deprecated in favor of <code>removeAll()</code>.
	  */
public void
clear()
{
  items.clear();

  ListPeer peer = (ListPeer) getPeer ();
  if (peer != null)
    peer.removeAll ();
}

/*************************************************************************/

/**
	  * Replaces the item at the specified index with the specified item.
	  *
	  * @param item The new item value.
	  * @param index The index of the item to replace.
	  *
	  * @exception IllegalArgumentException If the index is not valid.
	  */
public synchronized void
replaceItem(String item, int index) throws IllegalArgumentException
{
  if ((index < 0) || (index >= items.size()))
    throw new IllegalArgumentException("Bad list index: " + index);

  items.insertElementAt(item, index + 1);
  items.removeElementAt (index);

  if (peer != null)
    {
      ListPeer l = (ListPeer) peer;

      /* We add first and then remove so that the selected
	 item remains the same */
      l.add (item, index + 1);
      l.delItems (index, index);
	}
}

/*************************************************************************/

/**
	  * Returns the index of the currently selected item.  -1 will be returned
	  * if there are no selected rows or if there are multiple selected rows.
	  *
	  * @return The index of the selected row.
	  */
public synchronized int
getSelectedIndex()
{
  if (peer != null)
    {
			ListPeer l = (ListPeer) peer;
      selected = l.getSelectedIndexes ();
		}

  if (selected == null || selected.length != 1)
			return -1;
		return selected[0];
}

/*************************************************************************/

/**
	  * Returns an array containing the indexes of the rows that are 
	  * currently selected.
	  *
	  * @return A list of indexes of selected rows.
	  */
public synchronized int[]
getSelectedIndexes()
{
  if (peer != null)
    {
			ListPeer l = (ListPeer) peer;
      selected = l.getSelectedIndexes ();
		}
		return selected;
}

/*************************************************************************/

/**
	  * Returns the item that is currently selected, or <code>null</code> if there 
	  * is no item selected.  FIXME: What happens if multiple items selected?
	  *
	  * @return The selected item, or <code>null</code> if there is no
	  * selected item.
	  */
public synchronized String
getSelectedItem()
{
		int index = getSelectedIndex();
		if (index == -1)
    return(null);

  return((String)items.elementAt(index));
}

/*************************************************************************/

/**
	  * Returns the list of items that are currently selected in this list.
	  *
	  * @return The list of currently selected items.
	  */
public synchronized String[]
getSelectedItems()
{
		int[] indexes = getSelectedIndexes();
		if (indexes == null)
    return(new String[0]);

		String[] retvals = new String[indexes.length];
		if (retvals.length > 0)
    for (int i = 0 ; i < retvals.length; i++)
       retvals[i] = (String)items.elementAt(indexes[i]);

  return(retvals);
}

/*************************************************************************/

/**
	  * Returns the list of items that are currently selected in this list as
	  * an array of type <code>Object[]</code> instead of <code>String[]</code>.
	  *
	  * @return The list of currently selected items.
	  */
public synchronized Object[]
getSelectedObjects()
{
		int[] indexes = getSelectedIndexes();
		if (indexes == null)
    return(new Object[0]);

		Object[] retvals = new Object[indexes.length];
		if (retvals.length > 0)
    for (int i = 0 ; i < retvals.length; i++)
				retvals[i] = items.elementAt(indexes[i]);

  return(retvals);
}

/*************************************************************************/

/**
	  * Tests whether or not the specified index is selected.
	  *
	  * @param index The index to test.
	  *
	  * @return <code>true</code> if the index is selected, <code>false</code>
	  * otherwise.
	  */
public boolean
isIndexSelected(int index)
{
  return isSelected (index);
}

/*************************************************************************/

/**
	  * Tests whether or not the specified index is selected.
	  *
	  * @param index The index to test.
	  *
	  * @return <code>true</code> if the index is selected, <code>false</code>
	  * otherwise.
	  *
	  * @deprecated This method is deprecated in favor of
	  * <code>isIndexSelected(int)</code>.
	  */
public boolean
isSelected(int index)
{
  int[] indexes = getSelectedIndexes ();

  for (int i = 0; i < indexes.length; i++)
    if (indexes[i] == index)
      return true;

  return false;
}

/*************************************************************************/

/**
	  * This method ensures that the item at the specified index is visible.
	  *
	  * @exception IllegalArgumentException If the specified index is out of
	  * range.
	  */
public synchronized void
makeVisible(int index) throws IllegalArgumentException
{
		if ((index < 0) || (index >= items.size()))
			throw new IllegalArgumentException("Bad list index: " + index);

		visibleIndex = index;
  if (peer != null)
    {
			ListPeer l = (ListPeer) peer;
      l.makeVisible (index);
	}
}

/*************************************************************************/

/**
	  * Returns the index of the last item that was made visible via the
	  * <code>makeVisible()</code> method.
	  *
	  * @return The index of the last item made visible via the 
	  * <code>makeVisible()</code> method.
	  */
public int
getVisibleIndex()
{
  return(visibleIndex);
}

/*************************************************************************/

/**
	  * Makes the item at the specified index selected.
	  *
	  * @param index The index of the item to select.
	  */
public synchronized void
select(int index)
{
  ListPeer lp = (ListPeer)getPeer();
		if (lp != null)
			lp.select(index);
}

/*************************************************************************/

/**
	  * Makes the item at the specified index not selected.
	  *
	  * @param index The index of the item to unselect.
	  */
public synchronized void
deselect(int index)
{
  ListPeer lp = (ListPeer)getPeer();
		if (lp != null)
			lp.deselect(index);
}

/*************************************************************************/

/**
	  * Notifies this object to create its native peer.
	  */
public void
addNotify()
{
		if (peer == null)
    peer = getToolkit ().createList (this);
  super.addNotify ();
}

/*************************************************************************/

/**
	  * Notifies this object to destroy its native peer.
	  */
public void
removeNotify()
{
		super.removeNotify();
}

/*************************************************************************/

/**
	  * Adds the specified <code>ActionListener</code> to the list of
	  * registered listeners for this object.
	  *
	  * @param listener The listener to add.
	  */
public synchronized void
addActionListener(ActionListener listener)
{
		action_listeners = AWTEventMulticaster.add(action_listeners, listener);
}

/*************************************************************************/

/**
	  * Removes the specified <code>ActionListener</code> from the list of
	  * registers listeners for this object.
	  *
	  * @param listener The listener to remove.
	  */
public synchronized void
removeActionListener(ActionListener listener)
{
		action_listeners = AWTEventMulticaster.remove(action_listeners, listener);
}

/*************************************************************************/

/**
	  * Adds the specified <code>ItemListener</code> to the list of
	  * registered listeners for this object.
	  *
	  * @param listener The listener to add.
	  */
public synchronized void
addItemListener(ItemListener listener)
{
		item_listeners = AWTEventMulticaster.add(item_listeners, listener);
}

/*************************************************************************/

/**
	  * Removes the specified <code>ItemListener</code> from the list of
	  * registers listeners for this object.
	  *
	  * @param listener The listener to remove.
	  */
public synchronized void
removeItemListener(ItemListener listener)
{
		item_listeners = AWTEventMulticaster.remove(item_listeners, listener);
}

/*************************************************************************/

/**
	  * Processes the specified event for this object.  If the event is an
	  * instance of <code>ActionEvent</code> then the
	  * <code>processActionEvent()</code> method is called.  Similarly, if the
	  * even is an instance of <code>ItemEvent</code> then the
	  * <code>processItemEvent()</code> method is called.  Otherwise the
	  * superclass method is called to process this event.
	  *
	  * @param event The event to process.
	  */
protected void
processEvent(AWTEvent event)
{
		if (event instanceof ActionEvent)
    processActionEvent((ActionEvent)event);
		else if (event instanceof ItemEvent)
    processItemEvent((ItemEvent)event);
		else
			super.processEvent(event);
}

/*************************************************************************/

/**
	  * This method processes the specified event by dispatching it to any
	  * registered listeners.  Note that this method will only get called if
	  * action events are enabled.  This will happen automatically if any
	  * listeners are added, or it can be done "manually" by calling
	  * the <code>enableEvents()</code> method.
	  *
	  * @param event The event to process.
	  */
protected void 
processActionEvent(ActionEvent event)
{
		if (action_listeners != null)
			action_listeners.actionPerformed(event);
}

/*************************************************************************/

/**
	  * This method processes the specified event by dispatching it to any
	  * registered listeners.  Note that this method will only get called if
	  * item events are enabled.  This will happen automatically if any
	  * listeners are added, or it can be done "manually" by calling
	  * the <code>enableEvents()</code> method.
	  *
	  * @param event The event to process.
	  */
protected void 
processItemEvent(ItemEvent event)
{
		if (item_listeners != null)
			item_listeners.itemStateChanged(event);
}

void
dispatchEventImpl(AWTEvent e)
{
  if (e.id <= ItemEvent.ITEM_LAST
      && e.id >= ItemEvent.ITEM_FIRST
      && (item_listeners != null 
	  || (eventMask & AWTEvent.ITEM_EVENT_MASK) != 0))
			processEvent(e);
  else if (e.id <= ActionEvent.ACTION_LAST 
	   && e.id >= ActionEvent.ACTION_FIRST
	   && (action_listeners != null 
	       || (eventMask & AWTEvent.ACTION_EVENT_MASK) != 0))
			processEvent(e);
		else
			super.dispatchEventImpl(e);
}

/*************************************************************************/

/**
	  * Returns a debugging string for this object.
	  *
	  * @return A debugging string for this object.
	  */
protected String
paramString()
{
		return "multiple=" + multipleMode + ",rows=" + rows + super.paramString();
}

  /**
   * Returns an array of all the objects currently registered as FooListeners
   * upon this <code>List</code>. FooListeners are registered using the 
   * addFooListener method.
   *
   * @exception ClassCastException If listenerType doesn't specify a class or
   * interface that implements java.util.EventListener.
   */
  public EventListener[] getListeners (Class listenerType)
  {
    if (listenerType == ActionListener.class)
      return AWTEventMulticaster.getListeners (action_listeners, listenerType);
    
    if (listenerType == ItemListener.class)
      return AWTEventMulticaster.getListeners (item_listeners, listenerType);

    return super.getListeners (listenerType);
	}

  /**
   * Returns all action listeners registered to this object.
   */
  public ActionListener[] getActionListeners ()
  {
    return (ActionListener[]) getListeners (ActionListener.class);
  }
  
  /**
   * Returns all action listeners registered to this object.
   */
  public ItemListener[] getItemListeners ()
  {
    return (ItemListener[]) getListeners (ItemListener.class);
  }
  
  // Accessibility internal class 
  protected class AccessibleAWTList extends AccessibleAWTComponent
    implements AccessibleSelection, ItemListener, ActionListener
  {
    protected class AccessibleAWTListChild extends AccessibleAWTComponent
      implements Accessible
    {
      private int index;
      private List parent;
      
      public AccessibleAWTListChild(List parent, int indexInParent)
      {
        this.parent = parent;
        index = indexInParent;
        if (parent == null)
          index = -1;
      }
      
      /* (non-Javadoc)
       * @see javax.accessibility.Accessible#getAccessibleContext()
       */
      public AccessibleContext getAccessibleContext()
      {
        return this;
      }
      
      public AccessibleRole getAccessibleRole()
      {
        return AccessibleRole.LIST_ITEM;
      }
      
      public AccessibleStateSet getAccessibleStateSet()
      {
        AccessibleStateSet states = super.getAccessibleStateSet();
        if (parent.isIndexSelected(index))
          states.add(AccessibleState.SELECTED);
        return states;
      }
      
      public int getAccessibleIndexInParent()
      {
        return index;
      }

    }
    
    public AccessibleAWTList()
    {
      addItemListener(this);
      addActionListener(this);
    }
    
    public AccessibleRole getAccessibleRole()
    {
      return AccessibleRole.LIST;
    }
    
    public AccessibleStateSet getAccessibleStateSet()
    {
      AccessibleStateSet states = super.getAccessibleStateSet();
      states.add(AccessibleState.SELECTABLE);
      if (isMultipleMode())
        states.add(AccessibleState.MULTISELECTABLE);
      return states;
    }

    public int getAccessibleChildrenCount()
    {
      return getItemCount();
    }

    public Accessible getAccessibleChild(int i)
    {
      if (i >= getItemCount())
        return null;
      return new AccessibleAWTListChild(List.this, i);
    }
    
    /* (non-Javadoc)
     * @see javax.accessibility.AccessibleSelection#getAccessibleSelectionCount()
     */
    public int getAccessibleSelectionCount()
    {
      return getSelectedIndexes().length;
    }

    /* (non-Javadoc)
     * @see javax.accessibility.AccessibleSelection#getAccessibleSelection()
     */
    public AccessibleSelection getAccessibleSelection()
    {
      return this;
    }

    /* (non-Javadoc)
     * @see javax.accessibility.AccessibleSelection#getAccessibleSelection(int)
     */
    public Accessible getAccessibleSelection(int i)
    {
      int[] items = getSelectedIndexes();
      if (i >= items.length)
        return null;
      return new AccessibleAWTListChild(List.this, items[i]);
    }

    /* (non-Javadoc)
     * @see javax.accessibility.AccessibleSelection#isAccessibleChildSelected(int)
     */
    public boolean isAccessibleChildSelected(int i)
    {
      return isIndexSelected(i);
    }

    /* (non-Javadoc)
     * @see javax.accessibility.AccessibleSelection#addAccessibleSelection(int)
     */
    public void addAccessibleSelection(int i)
    {
      select(i);
    }

    /* (non-Javadoc)
     * @see javax.accessibility.AccessibleSelection#removeAccessibleSelection(int)
     */
    public void removeAccessibleSelection(int i)
    {
      deselect(i);
    }

    /* (non-Javadoc)
     * @see javax.accessibility.AccessibleSelection#clearAccessibleSelection()
     */
    public void clearAccessibleSelection()
    {
      for (int i = 0; i < getItemCount(); i++)
        deselect(i);
    }

    /* (non-Javadoc)
     * @see javax.accessibility.AccessibleSelection#selectAllAccessibleSelection()
     */
    public void selectAllAccessibleSelection()
    {
      if (isMultipleMode())
        for (int i = 0; i < getItemCount(); i++)
          select(i);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
     */
    public void itemStateChanged(ItemEvent event)
    {
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event)
    {
    }
    
  }

  /**
   * Gets the AccessibleContext associated with this <code>List</code>.
   * The context is created, if necessary.
   *
   * @return the associated context
   */
  public AccessibleContext getAccessibleContext()
  {
    /* Create the context if this is the first request */
    if (accessibleContext == null)
      accessibleContext = new AccessibleAWTList();
    return accessibleContext;
  }
} // class List
