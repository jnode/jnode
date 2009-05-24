/* class JMenu
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

import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.Vector;

import charva.awt.Color;
import charva.awt.Component;
import charva.awt.Dimension;
import charva.awt.Point;
import charva.awt.Toolkit;
import charva.awt.event.ActionEvent;
import charva.awt.event.KeyEvent;

/**
 * Implements a menu containing JMenuItems and JSeparators.
 */
public class JMenu
    extends JMenuItem
{
    /** Constructs a new JMenu with no text
     * (the text can be set later with the setText() method of the
     * superclass).
     */
    public JMenu()
    {
	super();
    }

    /** Constructs a new JMenu with the specified string as its text
     */
    public JMenu(String text_)
    {
	this(text_, -1);
    }

    /** Constructs a new JMenu with the specified text and the
     * specified mnemonic character (which must appear in the
     * text).
     */
    public JMenu(String text_, int mnemonic_)
    {
	super(text_, mnemonic_);
    }

    /**
     * Add a JMenuItem (or JMenu) to the end of this JMenu.
     * @return the JMenuItem that was added.
     */
    public JMenuItem add(JMenuItem item_)
    {
	_menuItems.add(item_);
	if (item_ instanceof JMenu) {
	    ((JMenu) item_).setParentMenu(this);
	}

	return item_;
    }

    /** Add a horizontal separator to the end of the menu.
     */
    public void addSeparator()
    {
	_menuItems.add(new JSeparator());
    }

    /**
     * Create a JMenuItem with the specified label and add it to the
     * menu.
     * @return a reference to the newly created JMenuItem.
     */
    public JMenuItem add(String text_)
    {
	JMenuItem item = new JMenuItem(text_);
	add(item);
	return item;
    }

    /**
     * Sets the foreground color of this JMenu and all its
     * contained JMenuItems that do not yet have their foreground 
     * color set.  Overrides the same method in the Component class.
     */
    public void setForeground(Color color_)
    {
	super.setForeground(color_);

	Enumeration<Component> e = _menuItems.elements();
	while (e.hasMoreElements()) {
	    Component c = e.nextElement();
	    if (c.getForeground() == null)
	    	c.setForeground(color_);
	}
    }

    /**
     * Sets the background color of this JMenu and all its
     * contained JMenuItems that do not yet have their background
     * color set.  Overrides the same method in the Component class.
     */
    public void setBackground(Color color_)
    {
	super.setBackground(color_);

	Enumeration<Component> e = _menuItems.elements();
	while (e.hasMoreElements()) {
	    Component c = e.nextElement();
	    if (c.getBackground() == null)
		c.setBackground(color_);
	}
    }

    public void draw(Toolkit toolkit) {
	/* Get the absolute origin of this component.
	 */
	Point origin = getLocationOnScreen();

	int colorpair = getCursesColor();

	toolkit.setCursor(origin);

	int attribute = 0;
	if ( ! (getParent() instanceof JMenuBar)) {
	    // This menu is in a JPopupMenu.
	    super.draw(toolkit);
	}
	else {
	    attribute = (super.hasFocus()) ? Toolkit.A_BOLD : Toolkit.A_REVERSE;
	    toolkit.addString(" ", attribute, colorpair);
	    toolkit.addString(super.getText(), attribute, colorpair);
	    toolkit.addString(" ", attribute, colorpair);

	    if (super.getMnemonic() > 0) {
		int mnemonicPos = super.getText().indexOf((char) super.getMnemonic());
		if (mnemonicPos != -1) {
		    toolkit.setCursor(origin.addOffset(mnemonicPos + 1, 0));
		    toolkit.addChar(super.getMnemonic(), attribute | Toolkit.A_UNDERLINE, colorpair);
		}
	    }
	}
    }

    /** Returns the menu item at the specified index.
     * If the object at the specified index is a JSeparator, it returns null.
     */
    public JMenuItem getMenuItem(int index_)
    {
	Object o = _menuItems.elementAt(index_);
	if (o instanceof JMenuItem)
	    return (JMenuItem) o;
	else
	    return null;
    }

    public void fireActionPerformed(ActionEvent ae_) {
	// Notify all the registered ActionListeners.
	super.fireActionPerformed(ae_);

	setPopupMenuVisible(true);

	// We get here when the popup menu has hidden itself.
	if (_popup.leftWasPressed()) {
	    Toolkit.getDefaultToolkit().fireKeystroke(KeyEvent.VK_LEFT);
	    SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    Toolkit.getDefaultToolkit().fireKeystroke(KeyEvent.VK_ENTER);
		}
	    });
	}
	else if (_popup.rightWasPressed()) {
	    Toolkit.getDefaultToolkit().fireKeystroke(KeyEvent.VK_RIGHT);
	    SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    Toolkit.getDefaultToolkit().fireKeystroke(KeyEvent.VK_ENTER);
		}
	    });
	}
	else if ( ! isTopLevelMenu()) {
	    getAncestorWindow().hide();
	}
    }

    public Dimension minimumSize()
    {
	return new Dimension(this.getWidth(), getHeight());
    }

    public Dimension getSize()
    {
	return minimumSize();
    }

    public int getWidth() {
	return getText().length() + 2;
    }

    public int getHeight() {
	return 1;
    }

    /** Returns true if the popup window of this menu is displayed.
     */
    public boolean isPopupMenuVisible()
    {
	return _popupMenuVisible;
    }

    /** Displays this menu's popup menu if the specified value is true;
     * hides the menu if it is false.
     */
    public void setPopupMenuVisible(boolean visible_)
    {
	_popupMenuVisible = visible_;
	if ( ! visible_) {
	    _popup.hide();
	    return;
	}

	if (_popup == null)
	    _popup = new JPopupMenu(_menuItems);

	Point p;
	if ( ! this.isTopLevelMenu()) {

	    /* If this menu is a submenu (i.e. it is not a direct
	     * child of the menubar), check if there is enough
	     * space on the right hand side of the parent menu.
	     * If there is not enough space, position it on the
	     * left of the parent menu.
	     */
	    JMenu parentmenu = (JMenu) getParentMenu();
	    JPopupMenu parentpopup = parentmenu.getPopupMenu();
	    p = parentpopup.getLocation();

	    int verticalOffset = parentpopup.getComponentIndex(this);
	    _popup.setInvoker(parentpopup);
	    int parentwidth = parentpopup.getSize().width;
	    Toolkit term = Toolkit.getDefaultToolkit();
	    if (p.x + parentwidth + _popup.getWidth() <
		    term.getScreenColumns()) {

		_popup.setLocation(
			    p.addOffset(parentwidth - 1, verticalOffset));
	    }
	    else {
		_popup.setLocation(
			p.addOffset(-_popup.getWidth() + 1, verticalOffset));
	    }
	}
	else {
	    JMenuBar parentMenuBar = (JMenuBar) getParent();
	    p = parentMenuBar.getPopupMenuLocation(this);
	    _popup.setInvoker(parentMenuBar);
	    _popup.setLocation(p);
	}
	_popup.show();
    }

    /** Returns true if this menu is the direct child of a menubar.
     */
    public boolean isTopLevelMenu()
    {
	return (getParent() instanceof JMenuBar);
    }

    /** Returns a reference to this JMenu's popup menu.
     */
    public JPopupMenu getPopupMenu()
    {
	return _popup;
    }

    /** Output a text description of the menu.
     */
    public void debug(int level_)
    {
	for (int i=0; i<level_; i++)
	    System.err.print("    ");
	System.err.println("JMenu origin=" + _origin + " text=" + getText());
    }

    public String toString() {
	return "JMenu: text=" + getText();
    }

    //====================================================================
    // PACKAGE-PRIVATE METHODS

    /** This package-private method is called by JMenuwhen this JMenu 
     * is added to it as a submenu. It is not intended to be called 
     * by application programmers.
     */
    void setParentMenu(Component parent_)
    {
	_parentMenu = new WeakReference<Component>(parent_);

	// If the colors of this menu have not been set yet, inherit the
	// colors of the parent.
	if (super.getForeground() == null)
	    super.setForeground(parent_.getForeground());

	if (super.getBackground() == null)
	    super.setBackground(parent_.getBackground());
    }

    Component getParentMenu() {
	return (Component) _parentMenu.get();
    }

    //====================================================================
    // INSTANCE VARIABLES

    private Vector<Component> _menuItems = new Vector<Component>();

    // Note that the reference to the parent is stored as a WeakReference
    // so that the parent can be garbage-collected when it no longer has
    // any strong references to it.
    private WeakReference<Component> _parentMenu;

    private boolean _popupMenuVisible = false;
    //private final Point _popupMenuOrigin;
    private JPopupMenu _popup;
}
