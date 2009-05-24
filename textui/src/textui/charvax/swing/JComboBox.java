/* class JComboBox
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

/*
 * Modified Jul 15, 2003 by Tadpole Computer, Inc.
 * Modifications Copyright 2003 by Tadpole Computer, Inc.
 *
 * Modifications are hereby licensed to all parties at no charge under
 * the same terms as the original.
 *
 * Added the JComboBox(ComboBoxModel) constructor.
 * Added the removeAllItems method.
 * Added a stubbed out setEditable method (it would be nice to have editable
 * ComboBoxes, too.
 * Fixed color handling to inherit colors from our parent.
 */

package charvax.swing;

import java.util.Enumeration;
import java.util.Vector;

import charva.awt.Dimension;
import charva.awt.EventQueue;
import charva.awt.Frame;
import charva.awt.Insets;
import charva.awt.ItemSelectable;
import charva.awt.Point;
import charva.awt.Toolkit;
import charva.awt.event.AWTEvent;
import charva.awt.event.ActionEvent;
import charva.awt.event.ActionListener;
import charva.awt.event.ItemEvent;
import charva.awt.event.ItemListener;
import charva.awt.event.KeyEvent;
import charva.awt.event.MouseEvent;
import charvax.swing.border.LineBorder;
import charvax.swing.event.ListSelectionEvent;
import charvax.swing.event.ListSelectionListener;

/**
 * The JComboBox component allows the user to select an item from a pop-up
 * list of choices.<p>
 * When the combobox is in a non-popped-up state, it looks like a JButton
 * with a "diamond" character on the right. To make the popup menu appear,
 * the user positions the cursor over the combobox and presses ENTER.<p>
 *
 * When the user selects an item in the popup menu (by positioning the 
 * cursor on it and pressing ENTER), the pop-up menu disappears and 
 * only the selected item is shown. At the same time the JComboBox 
 * posts an ActionEvent onto the system event queue. The "action command"
 * encapsulated in the ActionEvent is the list item that was selected.<p>
 * Note that this class provides an "Uneditable" JComboBox only.
 */
public class JComboBox
    extends JComponent
    implements ListSelectionListener, // (an implementation side-effect)
	ItemSelectable
{
    /** Creates an empty JComboBox.
     */
    public JComboBox() {
	DefaultComboBoxModel model = new DefaultComboBoxModel();
	setModel(model);
    }

    /** Creates a JComboBox with the given model.
     */
    public JComboBox(ComboBoxModel model)
    {
	setModel(model);
    }

    /** Creates a JComboBox that contains the elements in the specified 
     * array. 
     * @param items_ the array of items to display in the combobox
     */
    public JComboBox(Object[] items_)
    {
	DefaultComboBoxModel model = new DefaultComboBoxModel(items_);
	setModel(model);
    }

    /** Creates a JComboBox that contains the elements in the specified 
     * Vector. 
     * @param items_ the vector of items to display in the combobox
     */
    public JComboBox(Vector<?> items_)
    {
	DefaultComboBoxModel model = new DefaultComboBoxModel(items_);
	setModel(model);
    }

    /** Sets the data model that the JComboBox uses to obtain the list of
     * items.
     */
    public void setModel(ComboBoxModel model_) {
	_model = model_;

	for (int i=0; i<_model.getSize(); i++) {
	    String str = _model.getElementAt(i).toString();
	    if (str.length() > _columns)
		_columns = str.length();
	}

	if (super.isDisplayed())
	    super.repaint();
    }

    /** Add the specified item into the list of items.<p>
     *
     * Note that this method works only if the data model is
     * a MutableComboBoxModel (by default, it is).
     */
    public void addItem(Object item_) {
	((MutableComboBoxModel) _model).addElement(item_);
	if (item_.toString().length() > _columns)
	    _columns = item_.toString().length();
    }

    /** Insert the specified item at the specified index.<p>
     *
     * Note that this method works only if the data model is
     * a MutableComboBoxModel (by default, it is).
     */
    public void insertItemAt(Object item_, int index_)
    {
	((MutableComboBoxModel) _model).insertElementAt(item_, index_);
	if (item_.toString().length() > _columns)
	    _columns = item_.toString().length();
    }

    /** Remove the item at the specified index. <p>
     *
     * Note that this method works only if the data model is
     * a MutableComboBoxModel (by default, it is).
     */
    public void removeItemAt(int index_)
    {
	((MutableComboBoxModel) _model).removeElementAt(index_);
    }

    /** Removes the specified item from the combobox's list. If the
     * item was not in the list, the list is not changed.<p>
     *
     * Note that this method works only if the data model is
     * a MutableComboBoxModel (by default, it is).
     */
    public void removeItem(Object item_)
    {
	((MutableComboBoxModel) _model).removeElement(item_);
    }

    /** Removes all items.
     *
     * Note that this method works only if the data model is
     * a MutableComboBoxModel (by default, it is).
     */
    public void removeAllItems()
    {
	while (_model.getSize() > 0) {
	    removeItemAt(0);
	}
    }

    /** Returns the selected item.
     */
    public Object getSelectedItem()
    {
	return _model.getSelectedItem();
    }

    /** Sets the selected item in the JComboBox by specifying the 
     * object in the list.
     */
    public void setSelectedItem(Object obj_) {
	_model.setSelectedItem(obj_);
    }

    /** Sets the selected item in the JComboBox by specifying
     * the index in the list.
     */
    public void setSelectedIndex(int index_) {
	Object selected = _model.getElementAt(index_);
	_model.setSelectedItem(selected);
    }

    /**
     * Sets the maximum number of rows that the JComboBox displays.
     */
    public void setMaximumRowCount(int rows_)
    {
	_maxRows = rows_;
    }

    /** Returns width (including the diamond symbol).
     */
    public int getWidth() {
	Insets insets = super.getInsets();
	return _columns + insets.left + insets.right + 2;
    }

    public int getHeight() {
	Insets insets = super.getInsets();
	return 1 + insets.top + insets.bottom;
    }

    public Dimension getSize() {
	return new Dimension(this.getWidth(), this.getHeight());
    }

    public Dimension minimumSize() { return this.getSize(); }

    /**
     * Draw the selected item, surrounded by a box.
     * @param toolkit
     */
    public void draw(Toolkit toolkit) {

	// Draw the border if it exists
	super.draw(toolkit);

	/* Get the absolute origin of this component.
	 */
	Point origin = getLocationOnScreen();
	Insets insets = super.getInsets();
	origin.translate(insets.left, insets.right);

	int colorpair = getCursesColor();
	toolkit.setCursor(origin);
	String selectedItem = (String) _model.getSelectedItem();
	StringBuffer buf = new StringBuffer();
	for (int i=0; i<_columns + 1; i++)
	    buf.append(' ');
	if (selectedItem != null) {
	    buf.replace(1, selectedItem.length()+1, selectedItem);
	}

	int attribute = 
	    super.isEnabled() ?  Toolkit.A_REVERSE : Toolkit.A_NORMAL;

	toolkit.addString(buf.toString(), attribute, colorpair);
	toolkit.setCursor(origin.addOffset(_columns + 1, 0));
	toolkit.addChar(Toolkit.ACS_DIAMOND, attribute, colorpair);
    }

    /**
     * Register an ItemListener object for this component.
     */
    public void addItemListener(ItemListener il_) {
	if (_itemListeners == null)
	    _itemListeners = new Vector<ItemListener>();
	_itemListeners.add(il_);
    }

    public void removeItemListener(ItemListener listener_) {
	if (_itemListeners == null)
	    return;
	_itemListeners.remove(listener_);
    }

    /**
     * Invoke all the ItemListener callbacks that may have been registered
     * for this component. 
     */
    protected void fireItemStateChanged(ItemEvent ie_) {
	if (_itemListeners != null) {
	    for (Enumeration<ItemListener> e = _itemListeners.elements(); 
		    e.hasMoreElements(); ) {

		ItemListener il = (ItemListener) e.nextElement();
		il.itemStateChanged(ie_);
	    }
	}
    }

    /** Overrides method in superclass.
     */
    public void processEvent(AWTEvent evt_) {
	super.processEvent(evt_);

	if (evt_ instanceof ActionEvent) {
	    fireActionEvent((ActionEvent) evt_);
	    ItemEvent item_event = 
		new ItemEvent(this, this, ItemEvent.SELECTED);
	    fireItemStateChanged(item_event);
	}
    }

    /**
     * Register an ActionListener object for this component.
     */
    public void addActionListener(ActionListener al_) {
	if (_actionListeners == null)
	    _actionListeners = new Vector<ActionListener>();
	_actionListeners.add(al_);
    }

    /** Invoke all the ActionListener callbacks that may have been registered
     * for this component. 
     */
    protected void fireActionEvent(ActionEvent ae_) {
	if (_actionListeners != null) {
	    for (Enumeration<ActionListener> e = _actionListeners.elements(); 
		    e.hasMoreElements(); ) {

		ActionListener al = (ActionListener) e.nextElement();
		al.actionPerformed(ae_);
	    }
	}
    }

    /**
     * Process KeyEvents that have been generated by this JComboBox component.
     */
    public void processKeyEvent(KeyEvent ke_) {

	int key = ke_.getKeyCode();
	if (key == '\t') {
	    getParent().nextFocus();
	    return;
	}
	else if (key == KeyEvent.VK_BACK_TAB) {
	    getParent().previousFocus();
	    return;
	}
	else if (key == KeyEvent.VK_ENTER) {
	    _activate();
	}
    }

    /** Process a MouseEvent that was generated by clicking the mouse
     * on this JComboBox.
     */
    public void processMouseEvent(MouseEvent e_) {
	// Request focus if this is a MOUSE_PRESSED
	super.processMouseEvent(e_);

	if (e_.getButton() == MouseEvent.BUTTON1 &&
		e_.getModifiers() == MouseEvent.MOUSE_CLICKED &&
		this.isFocusTraversable()) {

	    _activate();
	}
    }

    /** Make the combobox editable.
     */
    public void setEditable(boolean editable)
    {
	// implement me
    }

    /** Implements the ListSelectionListener interface.
     */
    public void valueChanged(ListSelectionEvent e_) {
	_popup.hide();

	/* Put an ActionEvent onto the system event queue.
	 */
	EventQueue evtqueue = 
		Toolkit.getDefaultToolkit().getSystemEventQueue();

	Object selectedItem = _popup.getSelectedItem();
	if (selectedItem != null)
	    _model.setSelectedItem(selectedItem);

	evtqueue.postEvent(new ActionEvent(
	    this, _model.getSelectedItem().toString()));
    }

    public void requestFocus() {
	/* Generate the FOCUS_GAINED event.
	 */
	super.requestFocus();

	/* Get the absolute origin of this component 
	 */
	Point origin = getLocationOnScreen();
	Insets insets = super.getInsets();
	Toolkit.getDefaultToolkit().setCursor(
		origin.addOffset(insets.left, insets.top));
    }

    /** Returns a String representation of this component.
     */
    public String toString() {
	return "JComboBox location=" + getLocation() +
	    " selectedItem=\"" + getSelectedItem() + "\"";
    }

    /** Output a description of this component to stderr.
     */
    public void debug(int level_) {
	for (int i=0; i<level_; i++)
	    System.err.print("    ");
	System.err.println(toString());
    }

    private void _activate() {
	    _popup = this.new Popup(this, _model);
	    _popup.setMaximumRowCount(_maxRows);

	    /* Get the absolute origin of this component.
	     */
	    Point origin = getLocationOnScreen();
	    _popup.setLocation(origin);
	    _popup.show();
    }

    //====================================================================
    // INSTANCE VARIABLES

    private ComboBoxModel _model;

    private int _columns = 3;	// initial width

    /** The default value of 0 indicates that there is no limit on the
     * number of rows to display in the popup menu.
     */
    private int _maxRows = 3;

    /**
     * A list of ActionListeners registered for this component.
     */
    protected Vector<ActionListener> _actionListeners = null;

    /**
     * A list of ItemListeners registered for this component.
     */
    protected Vector<ItemListener> _itemListeners = null;

    /**
     * This is a non-static inner class that implements the popup 
     * window for the JComboBox component.
     */
    private class Popup
	extends Frame
    {
	Popup(JComboBox parent_, ComboBoxModel model_) {

	    _list = new JList();

	    setBackground(parent_.getBackground());
	    setForeground(parent_.getForeground());

	    _scrollpane = new JScrollPane(_list);
	    _scrollpane.setViewportBorder(new LineBorder(getForeground()));

	    // ComboBoxModel is a subclass of ListModel, so this works.
	    _list.setModel(model_);
	    _list.setColumns(_columns);
	    Object selected = model_.getSelectedItem();
	    int selectedIndex = 0;
	    for (int i=0; i<model_.getSize(); i++) {
		Object obj = model_.getElementAt(i);

		if (selected == obj)
		    selectedIndex = i;

		//String str = obj.toString();
	    }
	    _list.setSelectedIndex(selectedIndex);

	    /* Ensure we use the add() method inherited from Container
	     * rather than the add() method in the outer class.
	     */
	    super.add(_scrollpane);
	    _list.addListSelectionListener(parent_);
	    _list.ensureIndexIsVisible(selectedIndex);
	}

	Object getSelectedItem() {
	    /* If the user presses ENTER on the list entry that is
	     * already selected, it becomes deselected in the JList;
	     * so that _list.getselectedValue() returns null. The
	     * caller must take this into account and ignore the value.
	     */
	    return _list.getSelectedValue();
	}

	/** Set the maximum number of rows to be displayed
	 */
	void setMaximumRowCount(int rows_)
	{
	    _list.setVisibleRowCount(rows_);

	    pack();
	}

	private JList _list;
	private JScrollPane _scrollpane;
    }
    // end of nonstatic inner class Popup.

    private JComboBox.Popup _popup;
}
