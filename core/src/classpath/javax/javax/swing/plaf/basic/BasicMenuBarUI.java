/* BasicMenuBarUI.java --
   Copyright (C) 2002, 2004, 2005  Free Software Foundation, Inc.

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


package javax.swing.plaf.basic;

import java.awt.Dimension;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.MenuBarUI;

/**
 * UI Delegate for JMenuBar.
 */
public class BasicMenuBarUI extends MenuBarUI
{
  protected ChangeListener changeListener;

  /*ContainerListener that listens to the ContainerEvents fired from menu bar*/
  protected ContainerListener containerListener;

  /*Property change listeners that listener to PropertyChangeEvent from menu bar*/
  protected PropertyChangeListener propertyChangeListener;

  /* menu bar for which this UI delegate is for*/
  protected JMenuBar menuBar;

  /**
   * Creates a new BasicMenuBarUI object.
   */
  public BasicMenuBarUI()
  {
    changeListener = createChangeListener();
    containerListener = createContainerListener();
    propertyChangeListener = new PropertyChangeHandler();
  }

  /**
   * Creates ChangeListener
   *
   * @return The ChangeListener
   */
  protected ChangeListener createChangeListener()
  {
    return new ChangeHandler();
  }

  /**
   * Creates ContainerListener() to listen for ContainerEvents
   * fired by JMenuBar.
   *
   * @return The ContainerListener
   */
  protected ContainerListener createContainerListener()
  {
    return new ContainerHandler();
  }

  /**
   * Factory method to create a BasicMenuBarUI for the given {@link
   * JComponent}, which should be a {@link JMenuBar}.
   *
   * @param b The {@link JComponent} a UI is being created for.
   *
   * @return A BasicMenuBarUI for the {@link JComponent}.
   */
  public static ComponentUI createUI(JComponent x)
  {
    return new BasicMenuBarUI();
  }

  /**
   * Returns maximum size for the specified menu bar
   *
   * @param c component for which to get maximum size
   *
   * @return  Maximum size for the specified menu bar
   */
  public Dimension getMaximumSize(JComponent c)
  {
    // let layout manager calculate its size
    return null;
  }

  /**
   * Returns maximum allowed size of JMenuBar.
   *
   * @param c menuBar for which to return maximum size
   *
   * @return Maximum size of the give menu bar.
   */
  public Dimension getMinimumSize(JComponent c)
  {
    // let layout manager calculate its size
    return null;
  }

  /**
   * Returns preferred size of JMenuBar.
   *
   * @param c menuBar for which to return preferred size
   *
   * @return Preferred size of the give menu bar.
   */
  public Dimension getPreferredSize(JComponent c)
  {
    // let layout manager calculate its size
    return null;
  }

  /**
   * Initializes any default properties that this UI has from the defaults for
   * the Basic look and feel.
   */
  protected void installDefaults()
  {
    UIDefaults defaults = UIManager.getLookAndFeelDefaults();

    menuBar.setBackground(defaults.getColor("MenuBar.background"));
    menuBar.setBorder(defaults.getBorder("MenuBar.border"));
    menuBar.setFont(defaults.getFont("MenuBar.font"));
    menuBar.setForeground(defaults.getColor("MenuBar.foreground"));
  }

  /**
   * This method installs the keyboard actions for the JMenuBar.
   */
  protected void installKeyboardActions()
  {
    // FIXME: implement
  }

  /**
   * This method installs the listeners needed for this UI to function.
   */
  protected void installListeners()
  {
    menuBar.addContainerListener(containerListener);
    menuBar.addPropertyChangeListener(propertyChangeListener);
  }

  /**
  * Installs and initializes all fields for this UI delegate. Any properties
  * of the UI that need to be initialized and/or set to defaults will be
  * done now. It will also install any listeners necessary.
  *
  * @param c The {@link JComponent} that is having this UI installed.
  */
  public void installUI(JComponent c)
  {
    super.installUI(c);
    menuBar = (JMenuBar) c;
    menuBar.setLayout(new BoxLayout(menuBar, BoxLayout.X_AXIS));
    installDefaults();
    installListeners();
    installKeyboardActions();
  }

  /**
   * This method uninstalls the defaults and nulls any objects created during
   * install.
   */
  protected void uninstallDefaults()
  {
    menuBar.setBackground(null);
    menuBar.setBorder(null);
    menuBar.setFont(null);
    menuBar.setForeground(null);
  }

  /**
   * This method reverses the work done in installKeyboardActions.
   */
  protected void uninstallKeyboardActions()
  {
    // FIXME: implement. 
  }

  /**
   * Unregisters all the listeners that this UI delegate was using.
   */
  protected void uninstallListeners()
  {
    menuBar.removeContainerListener(containerListener);
    menuBar.removePropertyChangeListener(propertyChangeListener);
  }

  /**
   * Performs the opposite of installUI. Any properties or resources that need
   * to be cleaned up will be done now. It will also uninstall any listeners
   * it has. In addition, any properties of this UI will be nulled.
   *
   * @param c The {@link JComponent} that is having this UI uninstalled.
   */
  public void uninstallUI(JComponent c)
  {
    uninstallDefaults();
    uninstallListeners();
    uninstallKeyboardActions();
    menuBar = null;
  }

  protected class ChangeHandler implements ChangeListener
  {
    public void stateChanged(ChangeEvent event)
    {
    }
  }

  /**
   * This class handles ContainerEvents fired by JMenuBar. It revalidates
   * and repaints menu bar whenever menu is added or removed from it.
   */
  protected class ContainerHandler implements ContainerListener
  {
    /**
     * This method is called whenever menu is added to the menu bar
     *
     * @param e The ContainerEvent.
     */
    public void componentAdded(ContainerEvent e)
    {
      menuBar.revalidate();
      menuBar.repaint();
    }

    /**
     * This method is called whenever menu is removed from the menu bar.
     *
     * @param e The ContainerEvent.
     */
    public void componentRemoved(ContainerEvent e)
    {
      menuBar.revalidate();
      menuBar.repaint();
    }
  }

  /**
   * This class handles PropertyChangeEvents fired from the JMenuBar
   */
  protected class PropertyChangeHandler implements PropertyChangeListener
  {
    /**
     * This method is called whenever one of the properties of the MenuBar
     * changes.
     *
     * @param e The PropertyChangeEvent.
     */
    public void propertyChange(PropertyChangeEvent e)
    {
      if (e.getPropertyName().equals("borderPainted"))
	menuBar.repaint();
      if (e.getPropertyName().equals("margin"))
	menuBar.repaint();
    }
  }
}
