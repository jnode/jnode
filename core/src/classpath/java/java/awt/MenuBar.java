/* MenuBar.java -- An AWT menu bar class
   Copyright (C) 1999, 2000, 2001, 2002 Free Software Foundation, Inc.

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
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

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

import java.awt.peer.MenuBarPeer;
import java.awt.peer.MenuComponentPeer;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

/**
  * This class implements a menu bar in the AWT system.
  *
  * @author Aaron M. Renn (arenn@urbanophile.com)
  * @author Tom Tromey <tromey@redhat.com>
  */
public class MenuBar extends MenuComponent
  implements MenuContainer, Serializable
{

/*
 * Static Variables
 */

// Serialization Constant
private static final long serialVersionUID = -4930327919388951260L;

/*************************************************************************/

/*
 * Instance Variables
 */

/**
  * @serial The menu used for providing help information
  */
private Menu helpMenu;

/**
  * @serial The menus contained in this menu bar.
  */
private Vector menus = new Vector();

/*************************************************************************/

/*
 * Constructors
 */

/**
  * Initializes a new instance of <code>MenuBar</code>.
  *
  * @exception HeadlessException If GraphicsEnvironment.isHeadless() is true.
  */
public
MenuBar()
{
  if (GraphicsEnvironment.isHeadless())
    throw new HeadlessException ();
}

/*************************************************************************/

/*
 * Instance Methods
 */

/**
  * Returns the help menu for this menu bar.  This may be <code>null</code>.
  *
  * @return The help menu for this menu bar.
  */
public Menu
getHelpMenu()
{
  return(helpMenu);
}

/*************************************************************************/

/**
  * Sets the help menu for this menu bar.
  *
  * @param helpMenu The new help menu for this menu bar.
  */
public synchronized void
setHelpMenu(Menu menu)
{
  if (helpMenu != null)
    {
      helpMenu.removeNotify ();
      helpMenu.parent = null;
    }

  if (menu.parent != null)
    menu.parent.remove (menu);
  if (menu.parent != null)
    menu.parent.remove (menu);
  menu.parent = this;

  if (peer != null)
    {
      MenuBarPeer mp = (MenuBarPeer) peer;
      mp.addHelpMenu (menu);
    }
}

/*************************************************************************/

/** Add a menu to this MenuBar.  If the menu has already has a
 * parent, it is first removed from its old parent before being
 * added.
 *
 * @param menu The menu to add.
 *
 * @return The menu that was added.
 */
public synchronized Menu
add(Menu menu)
{
  if (menu.parent != null)
    menu.parent.remove (menu);

  menu.parent = this;
  menus.addElement(menu);

  if (peer != null)
    {
      MenuBarPeer mp = (MenuBarPeer) peer;
      mp.addMenu (menu);
    }

  return(menu);
}

/*************************************************************************/

/**
  * Removes the menu at the specified index.
  *
  * @param index The index of the menu to remove from the menu bar.
  */
public synchronized void
remove(int index)
{
  Menu m = (Menu) menus.get (index);
  menus.remove (index);
  m.removeNotify ();
  m.parent = null;

  if (peer != null)
    {
      MenuBarPeer mp = (MenuBarPeer) peer;
      mp.delMenu (index);
    }
}

/*************************************************************************/

/**
  * Removes the specified menu from the menu bar.
  *
  * @param menu The menu to remove from the menu bar.
  */
public void
remove(MenuComponent menu)
{
  int index = menus.indexOf(menu);
  if (index == -1)
    return;

  remove(index);
}

/*************************************************************************/

/**
  * Returns the number of elements in this menu bar.
  *
  * @return The number of elements in the menu bar.
  */
public int
getMenuCount()
{
  // FIXME: How does the help menu fit in here?
  return(menus.size());
}

/*************************************************************************/

/**
  * Returns the number of elements in this menu bar.
  *
  * @return The number of elements in the menu bar.
  *
  * @deprecated This method is deprecated in favor of <code>getMenuCount()</code>.
  */
public int
countMenus()
{
  return(getMenuCount());
}

/*************************************************************************/

/**
  * Returns the menu at the specified index.
  *
  * @return The requested menu.
  *
  * @exception ArrayIndexOutOfBoundsException If the index is not valid.
  */
public Menu
getMenu(int index)
{
  return((Menu)menus.elementAt(index));
}

/*************************************************************************/

/**
  * Creates this object's native peer.
  */
public void
addNotify()
{
  if (getPeer() == null)
    setPeer((MenuComponentPeer)getToolkit().createMenuBar(this));
}

/*************************************************************************/

/**
  * Destroys this object's native peer.
  */
public void
removeNotify()
{
  super.removeNotify();
}

/*************************************************************************/

/**
  * Returns a list of all shortcuts for the menus in this menu bar.
  *
  * @return A list of all shortcuts for the menus in this menu bar.
  */
public synchronized Enumeration
shortcuts()
{
  Vector shortcuts = new Vector();
  Enumeration e = menus.elements();

  while (e.hasMoreElements())
    {
      Menu menu = (Menu)e.nextElement();
      if (menu.getShortcut() != null)
        shortcuts.addElement(menu.getShortcut());
    }

  return(shortcuts.elements());
}

/*************************************************************************/

/**
  * Returns the menu item for the specified shortcut, or <code>null</code>
  * if no such item exists.
  *
  * @param shortcut The shortcut to return the menu item for.
  *
  * @return The menu item for the specified shortcut.
  */
public MenuItem
getShortcutMenuItem(MenuShortcut shortcut)
{
  Enumeration e = menus.elements();

  while (e.hasMoreElements())
    {
      Menu menu = (Menu)e.nextElement();
      MenuShortcut s = menu.getShortcut();
      if ((s != null) && (s.equals(shortcut)))
        return(menu);
    }

  return(null);
}

/*************************************************************************/

/**
  * Deletes the specified menu shortcut.
  *
  * @param shortcut The shortcut to delete.
  */
public void
deleteShortcut(MenuShortcut shortcut)
{
  MenuItem it;
  // This is a slow implementation, but it probably doesn't matter.
  while ((it = getShortcutMenuItem (shortcut)) != null)
    it.deleteShortcut ();
}

} // class MenuBar
