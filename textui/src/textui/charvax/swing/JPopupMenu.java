/* class JPopupMenu
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

import java.util.Vector;

import charva.awt.Component;
import charva.awt.Toolkit;
import charva.awt.event.ActionEvent;
import charva.awt.event.KeyEvent;

/**
 * An implementation of a popup menu - a small window that pops up and
 * displays a number of choices.
 */
public class JPopupMenu
    extends JFrame
{
    /** Constructs a JPopupMenu that contains the specified items.
     * Each element in the Vector must be a JMenuItem or JSeparator.
     */
    public JPopupMenu(Vector<Component> items_)
    {
	super();

	for (Component item : items_) {
	    add(item);
	}

	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	pack();
    }

    /**
     * Sets the "invoker" of this popup menu; must be a JMenuBar
     * or another JPopupMenu.
     */
    public void setInvoker(Component invoker_)
    {
	_invoker = invoker_;
	setForeground(invoker_.getForeground());
	setBackground(invoker_.getBackground());
    }

    /** Returns the component that invoked this popup menu.
     */
    public Component getInvoker() { return _invoker; }

    /** Returns the menu item at the specified index. If the item
     * is a JSeparator, it returns null.
     */
    public JMenuItem getMenuItem(int index_)
    {
	Object o = super._components.elementAt(index_);
	if (o instanceof JMenuItem)
	    return (JMenuItem) o;
	else
	    return null;
    }

    public int getComponentIndex(Component c)
    {
	return super._components.indexOf(c);
    }

    public void processKeyEvent(KeyEvent e) {
	_wasCancelled = false;
	_leftWasPressed = false;
	_rightWasPressed = false;

	if ( ! _visible)
	    return;	// the popup has already been dismissed.

	int key = e.getKeyCode();
	Toolkit term = Toolkit.getDefaultToolkit();

	if (key == KeyEvent.VK_UP) {
	    super.previousFocus();
	}

	else if (key == KeyEvent.VK_DOWN) {
	    super.nextFocus();
	}

	else if (key == KeyEvent.VK_LEFT) {
	    /* Pressing the LEFT cursor key has the effect of cancelling
	     * the selected menu and invoking the next menu on the left.
	     */
	    _leftWasPressed = true;
	    hide();
	}

	else if (key == KeyEvent.VK_RIGHT) {
	    /* Pressing the RIGHT cursor key has the effect of cancelling
	     * the selected menu and invoking the next menu on the right.
	     */
	    _rightWasPressed = true;
	    hide();
	}

	else if (key == KeyEvent.VK_ENTER) {
	    /* Pressing ENTER sends an ActionEvent. The source of the
	     * event is the menu item, not the menu; this means that the
	     * client program has to add an ActionListener to each menu
	     * item. This is inconvenient, but it's the way that the Java
	     * Swing menus do it.
	     */
	    JMenuItem item = (JMenuItem) super.getCurrentFocus();
	    _activate(item);
	    e.consume();
	}

	else if (key == KeyEvent.VK_BACK_SPACE || key == 0x1b) {
	    // Backspace or ESC was pressed
	    _wasCancelled = true;
	    hide();
	}

	else {
	    /* Check if one of the mnemonic keys was pressed.
	     * Note that the user can press a lowercase or an uppercase
	     * key.
	     */
	    char keyLower = Character.toLowerCase((char) key);
	    for (int i=0; i < super._components.size(); i++) {
		JMenuItem item = getMenuItem(i);
		if (item != null) {
		    if (item.getMnemonic() == -1)
			continue;   // this item doesn't have a mnemonic

		    char mnemonicLower = 
			Character.toLowerCase((char) item.getMnemonic());
		    if (keyLower == mnemonicLower) {
			_activate(item);
			return;
		    }
		}
	    }
	    term.beep();
	}
    }	    // end of processKeyEvent()

    public boolean wasCancelled() {
	return _wasCancelled;
    }

    boolean leftWasPressed() {
	return _leftWasPressed;
    }

    boolean rightWasPressed() {
	return _rightWasPressed;
    }

    /** Private helper method for activating a menu item (either a
     * JMenuItem or a JMenu).
     */
    private void _activate(JMenuItem item_)
    {
	if (item_ instanceof JMenu) {
	    JMenu menu = (JMenu) item_;
	    menu.setPopupMenuVisible(true);

	    // The popup menu has hidden itself
	    if (menu.getPopupMenu().leftWasPressed()) {
		Toolkit.getDefaultToolkit().fireKeystroke(KeyEvent.VK_LEFT);
	    }
	    else if (menu.getPopupMenu().rightWasPressed()) {
		Toolkit.getDefaultToolkit().fireKeystroke(KeyEvent.VK_RIGHT);
	    }
	    else if (menu.getPopupMenu().wasCancelled() == false)
		hide();
	}
	else {
	    ActionEvent evt = new ActionEvent(item_, item_.getActionCommand());
	    Toolkit term = Toolkit.getDefaultToolkit();
	    term.getSystemEventQueue().postEvent(evt);
	    hide();
	}
    }

    public String toString() {
	String str = "JPopupMenu: [";
	for (int i=0; i<getComponentCount(); i++) {
	    str += getMenuItem(i);
	    str += " ";
	}
	return str + "]";
    }

    protected boolean _wasCancelled;
    protected boolean _leftWasPressed;
    protected boolean _rightWasPressed;
    private Component _invoker;
}
