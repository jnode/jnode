/* class JRadioButton
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

import charva.awt.Dimension;
import charva.awt.EventQueue;
import charva.awt.Insets;
import charva.awt.Point;
import charva.awt.Toolkit;
import charva.awt.event.ItemEvent;
import charva.awt.event.KeyEvent;

/**
 * An implementation of a radiobutton - an item that is always in one of two
 * states (SELECTED or DESELECTED) and which displays its state to the user.
 * JRadioButtons are used with a ButtonGroup object to create a group of
 * buttons in which only one button at a time can be selected (Create a
 * ButtonGroup object and use its add() method to add the radio buttons to 
 * it).
 */
public class JRadioButton
    extends AbstractButton 
{
    /** Create a new JRadioButton with an empty label.
     */
    public JRadioButton() {
	this("", false);
    }

    /** Use this constructor when you want to initialize the value.
     */
    public JRadioButton(String text_) {
	this(text_, false);
    }

    /** 
     * Use this constructor when you want to set both the label and the value.
     */
    public JRadioButton(String label_, boolean value_) {
	super.setText(label_);
	super._selected = value_;
    }

    /**
     * Return the size of the text field. Overrides the method in the
     * Component superclass.
     */
    public Dimension getSize() {
	return new Dimension(this.getWidth(), this.getHeight());
    }

    public int getWidth() {
	Insets insets = super.getInsets();
	return super.getText().length() + 4 + insets.left + insets.right;
    }

    public int getHeight() {
	Insets insets = super.getInsets();
	return 1 + insets.top + insets.bottom;
    }

    /** Called by the LayoutManager.
     */
    public Dimension minimumSize() {
	return this.getSize();
    }

    /**
     * Called by this JRadioButton's parent container.
     * @param toolkit
     */
    public void draw(Toolkit toolkit) {

	// Draw the border if it exists
	super.draw(toolkit);

	String valstring;

	/* Get the absolute origin of this component.
	 */
	Point origin = getLocationOnScreen();
	Insets insets = super.getInsets();

	toolkit.setCursor(origin.addOffset(insets.left, insets.top));
	if (super.isSelected())
	    valstring = "(*) ";
	else
	    valstring = "( ) ";

	int colorpair = getCursesColor();
	int attribute = super._enabled ? Toolkit.A_BOLD : 0;
	toolkit.addString(valstring + super.getLabelString(), attribute, colorpair);
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
	int key = ke_.getKeyCode();
	switch (key) {
	    case '\t':
		getParent().nextFocus();
		return;

	    case KeyEvent.VK_BACK_TAB:
		getParent().previousFocus();
		return;

	    /* Set the button's state to SELECTED on ENTER.
	     */
	    case KeyEvent.VK_ENTER:
		// Check if the button is disabled or ALREADY selected.
		if ( (!super.isEnabled()) || super.isSelected()) {
		    return;	
		}
		super.setSelected(true);

		// post an ItemEvent.
		EventQueue queue = term.getSystemEventQueue();
		queue.postEvent(new ItemEvent(this, this, ItemEvent.SELECTED));
		break;
	}

	draw(Toolkit.getDefaultToolkit());
	requestFocus();
	super.requestSync();
    }

    public void requestFocus() {
	/* Generate the FOCUS_GAINED event.
	 */
	super.requestFocus();

	/* Get the absolute origin of this component.
	 */
	Point origin = getLocationOnScreen();
	Insets insets = super.getInsets();
	Toolkit.getDefaultToolkit().setCursor(
		origin.addOffset(1 + insets.left, 0 + insets.top));
    }

    public String toString() {
	return "JRadioButton location=" + getLocation() +
	    " label=\"" + getText() +
	    "\" actionCommand=\"" + getActionCommand() +
	    "\" selected=" + isSelected();
    }

    public void debug(int level_) {
	for (int i=0; i<level_; i++)
	    System.err.print("    ");
	System.err.println("JRadioButton origin=" + _origin + 
	    " size=" + getSize() + " label=" + super.getText());
    }

    //====================================================================
    // INSTANCE VARIABLES

}
