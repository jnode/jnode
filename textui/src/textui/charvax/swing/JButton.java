/* class JButton
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
 * Modified Jul 14, 2003 by Tadpole Computer, Inc.
 * Modifications Copyright 2003 by Tadpole Computer, Inc.
 *
 * Modifications are hereby licensed to all parties at no charge under
 * the same terms as the original.
 *
 * Modified to allow JButton to properly support color properties.
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
 * An implementation of a "pushbutton" with a text label.
 */
public class JButton
    extends AbstractButton
{
    /** Default constructor - construct a button with an empty label.
     */
    public JButton() {
	this("");
    }

    public JButton(String text_) {
	super.setText(text_);
	super.setActionCommand(text_);
    }

    /** @deprecated Replaced by setText(String text)
     */
    public void setLabel(String label_) {
	setText(label_);
    }

    public void setText(String label_) {
	super.setText(label_);

	/* If this component is already displayed, generate a PaintEvent
	 * and post it onto the queue.
	 */
	repaint();
    }

    /**
     * Return the size of the button. The button is always one line
     * high, and two columns wider than the label, plus the size
     * of the border (if any). 
     */
    public Dimension getSize() {
	return new Dimension(this.getWidth(), this.getHeight());
    }

    public int getWidth() {
	Insets insets = super.getInsets();
	return super.getLabelString().length() + 2 + insets.left + insets.right;
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
     * Draw the button. Called by this JButton's parent container.
     * @param toolkit
     */
    public void draw(Toolkit toolkit) {

	// Draw the border if it exists
	super.draw(toolkit);

	/* Get the absolute origin of this component.
	 */
	Point origin = getLocationOnScreen();
	Insets insets = super.getInsets();
	origin.translate(insets.left, insets.top);

	toolkit.setCursor(origin);

	int	colorpair = getCursesColor();
	if (super.isEnabled()) {
	    toolkit.addString(" ", Toolkit.A_REVERSE, colorpair);
	    toolkit.addString(super.getLabelString(), Toolkit.A_REVERSE,
		colorpair);
	    toolkit.addString(" ", Toolkit.A_REVERSE, colorpair);

	    if (super.getMnemonic() > 0) {
		int mnemonicPos = super.getLabelString().indexOf((char) super.getMnemonic());
		if (mnemonicPos != -1) {
		    toolkit.setCursor(origin.addOffset(1 + mnemonicPos, 0));
		    toolkit.addChar(super.getMnemonic(),
			    Toolkit.A_UNDERLINE | Toolkit.A_REVERSE, colorpair);
		}
	    }
	}
	else {
	    toolkit.addString("<", 0, colorpair);
	    toolkit.addString(getText(), 0, colorpair);
	    toolkit.addString(">", 0, colorpair);
	}

    }

    /**
     * Processes key events occurring on this object by dispatching them
     * to any registered KeyListener objects.
     */
    public void processKeyEvent(KeyEvent ke_) {
	/* First call all KeyListener objects that may have been registered
	 * for this component. 
	 */
	super.processKeyEvent(ke_);

	/* Check if any of the KeyListeners consumed the KeyEvent.
	 */
	if (ke_.isConsumed()) {
	    return;
	}

	int key = ke_.getKeyCode();
	if (key == '\t') {
	    getParent().nextFocus();
	    return;
	}
	else if (key == KeyEvent.VK_BACK_TAB) {
	    getParent().previousFocus();
	    return;
	}

	/* Post an ItemEvent if ENTER was pressed (but only if the 
	 * button is not disabled). 
	 */
	if (super.isEnabled() && (key == KeyEvent.VK_ENTER)) {

	    EventQueue queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
	    queue.postEvent(new ItemEvent(this, this, ItemEvent.SELECTED));
	}
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
	return "JButton location=" + getLocation() +
	    " label=\"" + getText() + 
	    "\" actionCommand=\"" + getActionCommand() + "\"";
    }

    public void debug(int level_) {
	for (int i=0; i<level_; i++)
	    System.err.print("    ");
	System.err.println("JButton origin=" + _origin + 
	    " size=" + getSize() + " label=" + super.getText());
    }

}
