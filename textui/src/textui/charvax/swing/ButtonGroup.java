/* class ButtonGroup
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

/*
 * Modified Jul 14, 2003 by Tadpole Computer, Inc.
 * Modifications Copyright 2003 by Tadpole Computer, Inc.
 *
 * Modifications are hereby licensed to all parties at no charge under
 * the same terms as the original.
 *
 * ButtonGroup modified to work with AbstractButton rather than JRadioButton.
 * (Note that this may mean recompiling classes which use the older
 * implementation.)
 */

package charvax.swing;

import java.util.Enumeration;
import java.util.Vector;

import charva.awt.Component;
import charva.awt.event.ItemEvent;
import charva.awt.event.ItemListener;

/**
 * This class is used to create a multiple-exclusion scope for a set of
 * buttons. Adding a set of buttons to a ButtonGroup object means that
 * turning any one of these buttons "on" turns off all other buttons in the
 * group.<p>
 * Initially, all buttons in the group are unselected. Once any button in 
 * the group is selected, one button is always selected in the group.<p>
 * Note that the ButtonGroup is a logical grouping, not a physical grouping.
 * To create a button panel, you should still create a JPanel and add the
 * JRadioButtons to it.
 */
public class ButtonGroup
    implements ItemListener
{
    /** Creates a new ButtonGroup
     */
    public ButtonGroup()
    {
	_buttons = new Vector<AbstractButton>();
    }

    /** Adds the specified button to the group.
     */
    public void add(AbstractButton button_)
    {
	_buttons.add(button_);
	button_.addItemListener(this);
	button_.setSelected(false);
    }

    /** Returns the number of buttons in the ButtonGroup.
     */
    public int getButtonCount()
    {
	return _buttons.size();
    }

    /** Returns an Enumeration of all the buttons in this group.
     */
    public Enumeration<AbstractButton> getElements()
    {
	return _buttons.elements();
    }

    /** Get the selected radiobutton. Returns null if no button is selected.
     */
    public AbstractButton getSelection()
    {
	for (Enumeration<AbstractButton> e = _buttons.elements(); e.hasMoreElements();) {

	    AbstractButton b = (AbstractButton) e.nextElement();
	    if (b.isSelected() == true)
		return b;
	}
	return null;	// no button was selected.
    }

    /** Returns the selected value for the specified button.
     */
    public boolean isSelected(AbstractButton button_)
    {
	return button_.isSelected();
    }

    /** Remove the specified button from the group.
     */
    public void remove(AbstractButton button_)
    {
	_buttons.remove(button_);
	button_.removeItemListener(this);
    }

    /** Sets the selected value for the specified button.
     * This method doesn't appear to make sense but it is present in
     * the Swing version of ButtonGroup.
     */
    public void setSelected(AbstractButton button_, boolean val_)
    {
	button_.setSelected(val_);
    }

    /** Implements the ItemListener interface. Listens for state
     * changes from all the buttons in the group.
     */
    public void itemStateChanged(ItemEvent e_)
    {
	Component source = (Component) e_.getSource();
	int statechange = e_.getStateChange();

	/* There should have been only one button in the selected
	 * state. All the other buttons should be in the deselected state.
	 */
	for (Enumeration<AbstractButton> e = _buttons.elements(); e.hasMoreElements(); ) {

	    AbstractButton b = (AbstractButton) e.nextElement();
	    if (source != b || statechange != ItemEvent.SELECTED) {
		if (b.isSelected()) {
		    b._selected = false;
		    b.setEnabled(true);
		}
	    }
	}
    }

    //====================================================================
    // INSTANCE VARIABLES

    private Vector<AbstractButton> _buttons;
}
