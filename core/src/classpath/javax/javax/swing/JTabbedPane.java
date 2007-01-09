/* JTabbedPane.java --
   Copyright (C) 2002, 2004, 2005, 2006,  Free Software Foundation, Inc.

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


package javax.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.Locale;
import java.util.Vector;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleSelection;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.plaf.UIResource;

/**
 * This is a container for components where only one component is displayed at
 * a given time and the displayed component can be switched by clicking on
 * tabs.
 * 
 * <p>
 * Tabs can be oriented in several ways. They can be above, below, left and
 * right of the component. Tabs can either wrap around (by creating multiple
 * rows of tabs) or they can be scrolled (where only a subset of the  tabs
 * can be seen at once). More tabs can be added by calling the
 * add/addTab/insertTab methods.
 * </p>
 */
public class JTabbedPane extends JComponent implements Serializable,
                                                       Accessible,
                                                       SwingConstants
{
  /**
   * Accessibility support for <code>JTabbedPane</code>.
   */
  protected class AccessibleJTabbedPane extends JComponent.AccessibleJComponent
    implements AccessibleSelection, ChangeListener
  {
    /**
     * The serialization UID.
     */
    private static final long serialVersionUID = 7610530885966830483L;

    /**
     * Creates a new AccessibleJTabbedPane object.
     */
    public AccessibleJTabbedPane()
    {
      super();
    }

    /**
     * Receives notification when the selection state of the
     * <code>JTabbedPane</code> changes and fires appropriate property change
     * events to interested listeners.
     *
     * @param e the change event describing the change
     */
    public void stateChanged(ChangeEvent e)
    {
      // I couldn't figure out what else should be done here.
      Object source = e.getSource();
      firePropertyChange(AccessibleContext.ACCESSIBLE_SELECTION_PROPERTY,
                         null, source);
    }

    /**
     * Returns the accessible role of the <code>JTabbedPane</code>, which is
     * {@link AccessibleRole#PAGE_TAB_LIST}.
     *
     * @return the accessible role of the <code>JTabbedPane</code>
     */
    public AccessibleRole getAccessibleRole()
    {
      return AccessibleRole.PAGE_TAB_LIST;
    }

    /**
     * Returns the number of accessible child components of the
     * <code>JTabbedPane</code>.
     *
     * @return the number of accessible child components of the
     *         <code>JTabbedPane</code>
     */
    public int getAccessibleChildrenCount()
    {
      return getTabCount();
    }

    /**
     * Returns the accessible child component at the specified index.
     *
     * @param i the index of the child component to fetch
     *
     * @return the accessible child component at the specified index
     */
    public Accessible getAccessibleChild(int i)
    {
      // Testing shows that the reference implementation returns instances
      // of page here.
      Accessible child = null;
      if (i >= 0 && i < tabs.size())
        child = (Page) tabs.get(i);
      return child;
    }

    /**
     * Returns the current selection state of the <code>JTabbedPane</code>
     * as AccessibleSelection object.
     *
     * @return the current selection state of the <code>JTabbedPane</code>
     */
    public AccessibleSelection getAccessibleSelection()
    {
      return this;
    }

    /**
     * Returns the accessible child component at the specified coordinates.
     * If there is no child component at this location, then return the
     * currently selected tab.
     *
     * @param p the coordinates at which to look up the child component
     *
     * @return the accessible child component at the specified coordinates or
     *         the currently selected tab if there is no child component at
     *         this location
     */
    public Accessible getAccessibleAt(Point p)
    {
      int tabIndex = indexAtLocation(p.x, p.y);
      if (tabIndex >= 0)
        return getAccessibleChild(tabIndex);
      else
        return getAccessibleSelection(0);
    }

    /**
     * Returns the number of selected child components of the
     * <code>JTabbedPane</code>. The reference implementation appears
     * to return <code>1</code> always and we do the same. 
     *
     * @return <code>1</code>
     */
    public int getAccessibleSelectionCount()
    {
      return 1;
    }

    /**
     * Returns the selected tab, or <code>null</code> if there is no 
     * selection.
     *
     * @param i  the selection index (ignored here).
     *
     * @return The selected tab, or <code>null</code>.
     */
    public Accessible getAccessibleSelection(int i)
    {
      Accessible result = null;
      int selected = getSelectedIndex();
      if (selected >= 0)
        result = (Page) tabs.get(selected);
      return result;
    }

    /**
     * Returns <code>true</code> if the specified child is selected,
     * and <code>false</code> otherwise.
     *
     * @param i the child index.
     *
     * @return A boolean.
     */
    public boolean isAccessibleChildSelected(int i)
    {
      return i == getSelectedIndex();
    }

    /**
     * Selects the specified tab.
     *
     * @param i  the index of the item to select.
     */
    public void addAccessibleSelection(int i)
    {
      setSelectedIndex(i);
    }

    /**
     * Does nothing - it makes no sense to remove a selection for a
     * tabbed pane, since one tab must always be selected.
     *
     * @param i  the item index.
     *
     * @see #addAccessibleSelection(int)
     */
    public void removeAccessibleSelection(int i)
    {
      // do nothing
    }

    /**
     * Does nothing - it makes no sense to clear the selection for
     * a tabbed pane, since one tab must always be selected.
     * 
     * @see #addAccessibleSelection(int)
     */
    public void clearAccessibleSelection()
    {
      // do nothing
    }

    /**
     * Does nothing - it makes no sense to select all for a tabbed
     * pane, since only one tab can be selected at a time.
     * 
     * @see #addAccessibleSelection(int)
     */
    public void selectAllAccessibleSelection()
    {
      // do nothing
    }
  }

  /**
   * A helper class that listens for changes to the model.
   */
  protected class ModelListener implements ChangeListener, Serializable
  {
    private static final long serialVersionUID = 497359819958114132L;

    /**
     * Creates a new ModelListener object.
     */
    protected ModelListener()
    {
      // Nothing to do here.
    }

    /**
     * This method is called whenever the model  is changed.
     *
     * @param e The ChangeEvent that is passed from the model.
     */
    public void stateChanged(ChangeEvent e)
    {
      // Propagate to our listeners.
      fireStateChanged();
    }
  }

  /**
   * A private class that holds all the information  for each tab.
   */
  private class Page
    extends AccessibleContext
    implements Accessible
  {
    /** The tooltip string. */
    private String tip;

    /** The component associated with the tab. */
    private Component component;

    /** The active icon associated with the tab. */
    private transient Icon icon;

    /** The disabled icon associated with the tab. */
    private transient Icon disabledIcon;

    /** The tab's enabled status. */
    private transient boolean enabled = true;

    /** The string painted on the tab. */
    private transient String title;

    /** The background color of the tab. */
    private transient Color bg;

    /** The foreground color of the tab. */
    private transient Color fg;

    /** The mnemonic associated with the tab. */
    private transient int mnemonicKey;

    /** The index of the underlined character in the string. */
    private transient int underlinedChar = -1;

    /**
     * Creates a new data storage for the tab.
     *
     * @param title The string displayed on the tab.
     * @param icon The active icon displayed on the tab.
     * @param component The component associated with the tab.
     * @param tip The tooltip associated with the tab.
     */
    protected Page(String title, Icon icon, Component component, String tip)
    {
      this.title = title;
      this.icon = icon;
      this.component = component;
      this.tip = tip;
    }

    /**
     * This method returns the component associated with the tab.
     *
     * @return The component associated with the tab.
     */
    public Component getComponent()
    {
      return component;
    }

    /**
     * This method sets the component associated with the tab.
     *
     * @param c The component associated with the tab.
     */
    public void setComponent(Component c)
    {
      int i = indexOfComponent(component);
      insertTab(title, icon, c, tip, i);
      component = c;
      removeTabAt(i);
    }

    /**
     * This method returns the tooltip string.
     *
     * @return The tooltip string.
     */
    public String getTip()
    {
      return tip;
    }

    /**
     * This method sets the tooltip string.
     *
     * @param tip The tooltip string.
     */
    public void setTip(String tip)
    {
      this.tip = tip;
    }

    /**
     * This method returns the background color.
     *
     * @return The background color.
     */
    public Color getBackground()
    {
      Color background;
      if (bg == null)
        background = JTabbedPane.this.getBackground();
      else
        background = bg;
      return background;
    }

    /**
     * This method sets the background color.
     *
     * @param background The background color.
     */
    public void setBackground(Color background)
    {
      bg = background;
    }

    /**
     * This method returns the foreground color.
     *
     * @return The foreground color.
     */
    public Color getForeground()
    {
      Color foreground;
      if (fg == null)
        foreground = JTabbedPane.this.getForeground();
      else
        foreground = fg;
      return foreground;
    }

    /**
     * This method sets the foreground color.
     *
     * @param foreground The foreground color.
     */
    public void setForeground(Color foreground)
    {
      fg = foreground;
    }

    /**
     * This method returns the title associated with the tab.
     *
     * @return The title of the tab.
     */
    public String getTitle()
    {
      return title;
    }

    private static final long serialVersionUID = 1614381073220130939L;

    /**
     * This method sets the title of the tab.
     *
     * @param text The title of the tab.
     */
    public void setTitle(String text)
    {
      title = text;
      if (title != null && title.length() <= underlinedChar)
	setDisplayedMnemonicIndex(title.length() - 1);
    }

    /**
     * This method returns the active icon.
     *
     * @return The active icon.
     */
    public Icon getIcon()
    {
      return icon;
    }

    /**
     * This method sets the active icon.
     *
     * @param icon The active icon.
     */
    public void setIcon(Icon icon)
    {
      this.icon = icon;
    }

    /**
     * This method returns the disabled icon.
     *
     * @return The disabled icon.
     */
    public Icon getDisabledIcon()
    {
      if (disabledIcon == null && icon instanceof ImageIcon)
	setDisabledIcon(icon);
      return disabledIcon;
    }

    /**
     * This method sets the disabled icon.
     *
     * @param disabledIcon The disabled icon.
     */
    public void setDisabledIcon(Icon disabledIcon)
    {
      this.disabledIcon = disabledIcon;
    }

    /**
     * This method returns whether the tab is enabled.
     *
     * @return Whether the tab is enabled.
     */
    public boolean isEnabled()
    {
      return enabled;
    }

    /**
     * This method sets whether the tab is enabled.
     *
     * @param enabled Whether this tab is enabled.
     */
    public void setEnabled(boolean enabled)
    {
      this.enabled = enabled;
    }

    /**
     * This method returns the mnemonic.
     *
     * @return The mnemonic.
     */
    public int getMnemonic()
    {
      return mnemonicKey;
    }

    /**
     * This method sets the mnemonic. If the title is set, it will update the
     * mnemonicIndex.
     *
     * @param key The mnemonic.
     */
    public void setMnemonic(int key)
    {
      setMnemonic((char) key);
    }

    /**
     * This method sets the mnemonic. If the title is set, it will update the
     * mnemonicIndex.
     *
     * @param aChar The mnemonic.
     */
    public void setMnemonic(char aChar)
    {
      mnemonicKey = aChar;
      if (title != null)
	setDisplayedMnemonicIndex(title.indexOf(mnemonicKey));
    }

    /**
     * This method returns the mnemonicIndex.
     *
     * @return The mnemonicIndex.
     */
    public int getDisplayedMnemonicIndex()
    {
      return underlinedChar;
    }

    /**
     * This method sets the mnemonicIndex.
     *
     * @param index The mnemonicIndex.
     *
     * @throws IllegalArgumentException If index less than -1 || index greater
     *         or equal to title.length.
     */
    public void setDisplayedMnemonicIndex(int index)
      throws IllegalArgumentException
    {
      if (index < -1 || title != null && index >= title.length())
	throw new IllegalArgumentException();

      if (title == null || mnemonicKey == 0 || (index > -1 && title.charAt(index) != mnemonicKey))
	index = -1;

      underlinedChar = index;
    }

    /**
     * Returns the accessible context, which is this object itself.
     *
     * @return the accessible context, which is this object itself
     */
    public AccessibleContext getAccessibleContext()
    {
      return this;
    }

    /**
     * Returns the accessible name for this tab.
     * 
     * @return The accessible name.
     */
    public String getAccessibleName()
    {
      if (accessibleName != null)
        return accessibleName;
      else
        return title;
    }
    
    /**
     * Returns the accessible role of this tab, which is always
     * {@link AccessibleRole#PAGE_TAB}.
     *
     * @return the accessible role of this tab
     */
    public AccessibleRole getAccessibleRole()
    {
      return AccessibleRole.PAGE_TAB;
    }

    /**
     * Returns the accessible state set of this object.
     *
     * @return the accessible state set of this object
     */
    public AccessibleStateSet getAccessibleStateSet()
    {
      AccessibleContext parentCtx = JTabbedPane.this.getAccessibleContext(); 
      AccessibleStateSet state = parentCtx.getAccessibleStateSet();
      state.add(AccessibleState.SELECTABLE);
      if (component == getSelectedComponent())
        state.add(AccessibleState.SELECTED);
      return state;
    }

    /**
     * Returns the index of this tab inside its parent.
     *
     * @return the index of this tab inside its parent
     */
    public int getAccessibleIndexInParent()
    {
      // TODO: Not sure if the title is unambiguous, but I can't figure
      // another way of doing this.
      return indexOfTab(title);
    }

    /**
     * Returns the number of accessible children, which is always one (the
     * component of this tab).
     *
     * @return the number of accessible children
     */
    public int getAccessibleChildrenCount()
    {
      return 1;
    }

    /**
     * Returns the accessible child of this tab, which is the component
     * displayed by the tab.
     *
     * @return the accessible child of this tab
     */
    public Accessible getAccessibleChild(int i)
    {
      // A quick test shows that this method always returns the component
      // displayed by the tab, regardless of the index.
      return (Accessible) component;
    }

    /**
     * Returns the locale of this accessible object.
     *
     * @return the locale of this accessible object
     */
    public Locale getLocale()
    {
      // TODO: Is this ok?
      return Locale.getDefault();
    }
  }

  private static final long serialVersionUID = 1614381073220130939L;

  /** The changeEvent used to fire changes to listeners. */
  protected ChangeEvent changeEvent;

  /** The listener that listens to the model. */
  protected ChangeListener changeListener;

  /** The model that describes this JTabbedPane. */
  protected SingleSelectionModel model;

  /** Indicates that the TabbedPane is in scrolling mode. */
  public static final int SCROLL_TAB_LAYOUT = 1;

  /** Indicates that the TabbedPane is in wrap mode. */
  public static final int WRAP_TAB_LAYOUT = 0;

  /** The current tabPlacement of the TabbedPane. */
  protected int tabPlacement = SwingConstants.TOP;

  /** The current tabLayoutPolicy of the TabbedPane. */
  private transient int layoutPolicy;

  /** The list of tabs associated with the TabbedPane. */
  transient Vector tabs = new Vector();

  /**
   * Creates a new JTabbedPane object with tabs on top and using wrap tab
   * layout.
   */
  public JTabbedPane()
  {
    this(SwingConstants.TOP, WRAP_TAB_LAYOUT);
  }

  /**
   * Creates a new JTabbedPane object using wrap tab layout  and the given
   * <code>tabPlacement</code>, where <code>tabPlacement</code> can be one
   * of the following values: {@link #TOP}, {@link #BOTTOM}, {@link #LEFT} or
   * {@link #RIGHT}.
   *
   * @param tabPlacement where the tabs will be placed
   */
  public JTabbedPane(int tabPlacement)
  {
    this(tabPlacement, WRAP_TAB_LAYOUT);
  }

  /**
   * Creates a new JTabbedPane object with the given <code>tabPlacement</code>
   * and <code>tabLayoutPolicy</code>. The <code>tabPlacement</code> can be one
   * of the following values: {@link #TOP}, {@link #BOTTOM}, {@link #LEFT} or
   * {@link #RIGHT}. The <code>tabLayoutPolicy</code> can be either
   * {@link #SCROLL_TAB_LAYOUT} or {@link #WRAP_TAB_LAYOUT}.
   *
   * @param tabPlacement where the tabs will be placed
   * @param tabLayoutPolicy the way tabs will be placed
   *
   * @throws IllegalArgumentException If tabLayoutPolicy or tabPlacement are
   *         not valid.
   */
  public JTabbedPane(int tabPlacement, int tabLayoutPolicy)
  {
    if (tabPlacement != TOP && tabPlacement != BOTTOM && tabPlacement != RIGHT
        && tabPlacement != LEFT)
      throw new IllegalArgumentException("tabPlacement is not valid.");
    if (tabLayoutPolicy != SCROLL_TAB_LAYOUT
        && tabLayoutPolicy != WRAP_TAB_LAYOUT)
      throw new IllegalArgumentException("tabLayoutPolicy is not valid.");
    this.tabPlacement = tabPlacement;
    layoutPolicy = tabLayoutPolicy;

    setModel(new DefaultSingleSelectionModel());

    updateUI();
  }

  /**
   * This method returns the UI used to display the JTabbedPane.
   *
   * @return The UI used to display the JTabbedPane.
   */
  public TabbedPaneUI getUI()
  {
    return (TabbedPaneUI) ui;
  }

  /**
   * This method sets the UI used to display the JTabbedPane.
   *
   * @param ui The UI used to display the JTabbedPane.
   */
  public void setUI(TabbedPaneUI ui)
  {
    super.setUI(ui);
  }

  /**
   * This method restores the UI to the defaults given by the UIManager.
   */
  public void updateUI()
  {
    setUI((TabbedPaneUI) UIManager.getUI(this));
  }

  /**
   * This method returns a string identifier that  is used to determine which
   * UI will be used with  the JTabbedPane.
   *
   * @return A string identifier for the UI.
   */
  public String getUIClassID()
  {
    return "TabbedPaneUI";
  }

  /**
   * This method creates a ChangeListener that is used to  listen to the model
   * for events.
   *
   * @return A ChangeListener to listen to the model.
   */
  protected ChangeListener createChangeListener()
  {
    return new ModelListener();
  }

  /**
   * This method adds a ChangeListener to the JTabbedPane.
   *
   * @param l The ChangeListener to add.
   */
  public void addChangeListener(ChangeListener l)
  {
    listenerList.add(ChangeListener.class, l);
  }

  /**
   * This method removes a ChangeListener to the JTabbedPane.
   *
   * @param l The ChangeListener to remove.
   */
  public void removeChangeListener(ChangeListener l)
  {
    listenerList.remove(ChangeListener.class, l);
  }

  /**
   * This method fires a ChangeEvent to all the JTabbedPane's ChangeListeners.
   */
  protected void fireStateChanged()
  {
    Object[] changeListeners = listenerList.getListenerList();
    if (changeEvent == null)
      changeEvent = new ChangeEvent(this);
    for (int i = changeListeners.length - 2; i >= 0; i -= 2)
      {
	if (changeListeners[i] == ChangeListener.class)
	  ((ChangeListener) changeListeners[i + 1]).stateChanged(changeEvent);
      }
  }

  /**
   * This method returns all ChangeListeners registered with the JTabbedPane.
   *
   * @return The ChangeListeners registered with the JTabbedPane.
   */
  public ChangeListener[] getChangeListeners()
  {
    return (ChangeListener[]) super.getListeners(ChangeListener.class);
  }

  /**
   * This method returns the model used with the JTabbedPane.
   *
   * @return The JTabbedPane's model.
   */
  public SingleSelectionModel getModel()
  {
    return model;
  }

  /**
   * This method changes the model property of the JTabbedPane.
   *
   * @param m The new model to use with the JTabbedPane.
   */
  public void setModel(SingleSelectionModel m)
  {
    if (m != model)
      {
	SingleSelectionModel oldModel = this.model;
        if (oldModel != null && changeListener != null)
          oldModel.removeChangeListener(changeListener);

	model = m;

        if (model != null)
          {
            if (changeListener == null)
              changeListener = createChangeListener();
            model.addChangeListener(changeListener);
          }
	firePropertyChange("model", oldModel, this.model);
      }
  }

  /**
   * This method returns the tabPlacement.
   *
   * @return The tabPlacement used with the JTabbedPane.
   */
  public int getTabPlacement()
  {
    return tabPlacement;
  }

  /**
   * This method changes the tabPlacement property of the JTabbedPane.
   *
   * @param tabPlacement The tabPlacement to use.
   *
   * @throws IllegalArgumentException If tabPlacement is not one of TOP,
   *         BOTTOM, LEFT, or RIGHT.
   */
  public void setTabPlacement(int tabPlacement)
  {
    if (tabPlacement != TOP && tabPlacement != BOTTOM && tabPlacement != RIGHT
        && tabPlacement != LEFT)
      throw new IllegalArgumentException("tabPlacement is not valid.");
    if (tabPlacement != this.tabPlacement)
      {
	int oldPlacement = this.tabPlacement;
	this.tabPlacement = tabPlacement;
	firePropertyChange("tabPlacement", oldPlacement, this.tabPlacement);
      }
  }

  /**
   * This method returns the tabLayoutPolicy.
   *
   * @return The tabLayoutPolicy.
   */
  public int getTabLayoutPolicy()
  {
    return layoutPolicy;
  }

  /**
   * This method changes the tabLayoutPolicy property of the JTabbedPane.
   *
   * @param tabLayoutPolicy The tabLayoutPolicy to use.
   *
   * @throws IllegalArgumentException If tabLayoutPolicy is not one of
   *         SCROLL_TAB_LAYOUT or WRAP_TAB_LAYOUT.
   */
  public void setTabLayoutPolicy(int tabLayoutPolicy)
  {
    if (tabLayoutPolicy != SCROLL_TAB_LAYOUT
        && tabLayoutPolicy != WRAP_TAB_LAYOUT)
      throw new IllegalArgumentException("tabLayoutPolicy is not valid.");
    if (tabLayoutPolicy != layoutPolicy)
      {
	int oldPolicy = layoutPolicy;
	layoutPolicy = tabLayoutPolicy;
	firePropertyChange("tabLayoutPolicy", oldPolicy, layoutPolicy);
      }
  }

  /**
   * This method returns the index of the tab that is currently selected.
   *
   * @return The index of the selected tab.
   */
  public int getSelectedIndex()
  {
    return model.getSelectedIndex();
  }

  /**
   * This method checks the index.
   *
   * @param index The index to check.
   * @param start DOCUMENT ME!
   * @param end DOCUMENT ME!
   *
   * @throws IndexOutOfBoundsException DOCUMENT ME!
   */
  private void checkIndex(int index, int start, int end)
  {
    if (index < start || index >= end)
      throw new IndexOutOfBoundsException("Index < " + start + " || Index >= "
                                          + end);
  }

  /**
   * This method sets the selected index. This method will hide the old
   * component and show the new component.
   *
   * @param index The index to set it at.
   */
  public void setSelectedIndex(int index)
  {
    checkIndex(index, -1, tabs.size());
    if (index != getSelectedIndex())
      {
        // Hiding and showing the involved components
        // is done by the JTabbedPane's UI.
	model.setSelectedIndex(index);
      }
  }

  /**
   * This method returns the component at the selected index.
   *
   * @return The component at the selected index.
   */
  public Component getSelectedComponent()
  {
    int selectedIndex = getSelectedIndex();
    Component selected = null;
    if (selectedIndex >= 0)
      selected = getComponentAt(selectedIndex);
    return selected;
  }

  /**
   * This method sets the component at the selected index.
   *
   * @param c The component associated with the selected index.
   */
  public void setSelectedComponent(Component c)
  {
    if (c.getParent() == this)
      setSelectedIndex(indexOfComponent(c));
    else
      setComponentAt(getSelectedIndex(), c);
  }

  /**
   * This method inserts tabs into JTabbedPane. This includes adding the
   * component to the JTabbedPane and hiding it.
   *
   * @param title the title of the tab; may be <code>null</code>
   * @param icon the tab's icon; may be <code>null</code>
   * @param component the component associated with the tab
   * @param tip the tooltip for the tab
   * @param index the index to insert the tab at
   */
  public void insertTab(String title, Icon icon, Component component,
                        String tip, int index)
  {
    if (title == null)
      title = "";
    Page p = new Page(title, icon, component, tip);
    tabs.insertElementAt(p, index);

    // Hide the component so we don't see it. Do it before we parent it
    // so we don't trigger a repaint.
    if (component != null)
      {
	component.hide();
	super.add(component);
      }

    if (getSelectedIndex() == -1)
      {
      setSelectedIndex(0);
        fireStateChanged();
      }

    revalidate();
    repaint();
  }

  /**
   * This method adds a tab to the JTabbedPane.
   *
   * @param title the title of the tab; may be <code>null</code>
   * @param icon the icon for the tab; may be <code>null</code>
   * @param component the associated component
   * @param tip the associated tooltip
   */
  public void addTab(String title, Icon icon, Component component, String tip)
  {
    insertTab(title, icon, component, tip, tabs.size());
  }

  /**
   * This method adds a tab to the JTabbedPane.
   *
   * @param title the title of the tab; may be <code>null</code>
   * @param icon the icon for the tab; may be <code>null</code>
   * @param component the associated component
   */
  public void addTab(String title, Icon icon, Component component)
  {
    insertTab(title, icon, component, null, tabs.size());
  }

  /**
   * This method adds a tab to the JTabbedPane.
   *
   * @param title the title of the tab; may be <code>null</code>
   * @param component the associated component
   */
  public void addTab(String title, Component component)
  {
    insertTab(title, null, component, null, tabs.size());
  }

  /**
   * This method adds a tab to the JTabbedPane. The title of the tab is the
   * Component's name. If the Component is an instance of UIResource, it
   * doesn't add the tab and instead add the component directly to the
   * JTabbedPane.
   *
   * @param component The associated component.
   *
   * @return The Component that was added.
   */
  public Component add(Component component)
  {
    if (component instanceof UIResource)
      super.add(component);
    else
      insertTab(component.getName(), null, component, null, tabs.size());
    
    return component;
  }

  /**
   * This method adds a tab to the JTabbedPane. If the Component is an
   * instance of UIResource, it doesn't add the tab and instead add the
   * component directly to the JTabbedPane.
   *
   * @param title the title of the tab; may be <code>null</code>
   * @param component the associated component
   *
   * @return The Component that was added.
   */
  public Component add(String title, Component component)
  {
    if (component instanceof UIResource)
      super.add(component);
    else
      insertTab(title, null, component, null, tabs.size());
    return component;
  }

  /**
   * This method adds a tab to the JTabbedPane. If the Component is an
   * instance of UIResource, it doesn't add the tab and instead add the
   * component directly to the JTabbedPane.
   *
   * @param component The associated component.
   * @param index The index to insert the tab at.
   *
   * @return The Component that was added.
   */
  public Component add(Component component, int index)
  {
    if (component instanceof UIResource)
      super.add(component);
    else
      insertTab(component.getName(), null, component, null, index);
    return component;
  }

  /**
   * This method adds a tab to the JTabbedPane. If the Component is an
   * instance of UIResource, it doesn't add the tab and instead add the
   * component directly to the JTabbedPane. If the constraints object is an
   * icon, it will be used as the tab's icon. If the constraints object is a
   * string, we will use it as the title.
   *
   * @param component The associated component.
   * @param constraints The constraints object.
   */
  public void add(Component component, Object constraints)
  {
    add(component, constraints, tabs.size());
  }

  /**
   * This method adds a tab to the JTabbedPane. If the Component is an
   * instance of UIResource, it doesn't add the tab and instead add the
   * component directly to the JTabbedPane. If the constraints object is an
   * icon, it will be used as the tab's icon. If the constraints object is a
   * string, we will use it as the title.
   *
   * @param component The associated component.
   * @param constraints The constraints object.
   * @param index The index to insert the tab at.
   */
  public void add(Component component, Object constraints, int index)
  {
    if (component instanceof UIResource)
      super.add(component);
    else
      {
	if (constraints instanceof String)
	  insertTab((String) constraints, null, component, null, index);
	else
	  insertTab(component.getName(),
	            (constraints instanceof Icon) ? (Icon) constraints : null,
	            component, null, index);
      }
  }

  /**
   * Removes the tab at index. After the component associated with 
   * index is removed, its visibility is reset to true to ensure it 
   * will be visible if added to other containers.
   *
   * @param index The index of the tab to remove.
   */
  public void removeTabAt(int index)
  {
    checkIndex(index, 0, tabs.size());

    // We need to adjust the selection if we remove a tab that comes
    // before the selected tab or if the selected tab is removed.
    // This decrements the selected index by 1 if any of this is the case.
    // Note that this covers all cases:
    // - When the selected tab comes after the removed tab, this simply
    //   adjusts the selection so that after the removal the selected tab
    //   is still the same.
    // - When we remove the currently selected tab, then the tab before the
    //   selected tab gets selected.
    // - When the last tab is removed, then we have an index==0, which gets
    //   decremented to -1, which means no selection, which is 100% perfect.
    int selectedIndex = getSelectedIndex();
    if (selectedIndex >= index)
      setSelectedIndex(selectedIndex - 1);

    Component comp = getComponentAt(index);

    // Remove the tab object.
    tabs.remove(index);

    // Remove the component. I think we cannot assume that the tab order
    // is equal to the component order, so we iterate over the children
    // here to find the and remove the correct component.
    if (comp != null)
      {
        Component[] children = getComponents();
        for (int i = children.length - 1; i >= 0; --i)
          {
            if (children[i] == comp)
              {
                super.remove(i);
                comp.setVisible(true);
                break;
              }
          }
      }
    revalidate();
    repaint();
  }

  /**
   * Removes the specified Component from the JTabbedPane.
   *
   * @param component The Component to remove.
   */
  public void remove(Component component)
  {
    // Since components implementing UIResource
    // are not added as regular tabs by the add()
    // methods we have to take special care when
    // removing these object. Especially 
    // Container.remove(Component) cannot be used
    // because it will call JTabbedPane.remove(int)
    // later which is overridden and can only
    // handle tab components.
    // This implementation can even cope with a
    // situation that someone called insertTab()
    // with a component that implements UIResource.
    int index = indexOfComponent(component);
    
    // If the component is not a tab component
    // find out its Container-given index
    // and call that class' implementation
    // directly.
    if (index == -1)
      {
        Component[] cs = getComponents();
        for (int i = 0; i< cs.length; i++)
          if (cs[i] == component)
            super.remove(i);
      }
    else
      removeTabAt(index);
  }

  /**
   * Removes the tab and component which corresponds to the specified index.
   *
   * @param index The index of the tab to remove.
   */
  public void remove(int index)
  {
    removeTabAt(index);
  }

  /**
   * This method removes all tabs and associated components from the
   * JTabbedPane.
   */
  public void removeAll()
  {
    setSelectedIndex(-1);
    for (int i = getTabCount() - 1; i >= 0; i--)
      removeTabAt(i);
  }

  /**
   * This method returns how many tabs are in the JTabbedPane.
   *
   * @return The number of tabs in the JTabbedPane.
   */
  public int getTabCount()
  {
    return tabs.size();
  }

  /**
   * This method returns the number of runs used  to paint the JTabbedPane.
   *
   * @return The number of runs.
   */
  public int getTabRunCount()
  {
    return ((TabbedPaneUI) ui).getTabRunCount(this);
  }

  /**
   * This method returns the tab title given the index.
   *
   * @param index The index of the tab.
   *
   * @return The title for the tab.
   */
  public String getTitleAt(int index)
  {
    checkIndex(index, 0, tabs.size());
    return ((Page) tabs.elementAt(index)).getTitle();
  }

  /**
   * This method returns the active icon given the index.
   *
   * @param index The index of the tab.
   *
   * @return The active icon for the tab.
   */
  public Icon getIconAt(int index)
  {
    checkIndex(index, 0, tabs.size());
    return ((Page) tabs.elementAt(index)).getIcon();
  }

  /**
   * This method returns the disabled icon given the index.
   *
   * @param index The index of the tab.
   *
   * @return The disabled icon for the tab.
   */
  public Icon getDisabledIconAt(int index)
  {
    checkIndex(index, 0, tabs.size());
    return ((Page) tabs.elementAt(index)).getDisabledIcon();
  }

  /**
   * This method returns the tooltip string for the tab.
   *
   * @param index The index of the tab.
   *
   * @return The tooltip string for the tab.
   */
  public String getToolTipTextAt(int index)
  {
    checkIndex(index, 0, tabs.size());
    return ((Page) tabs.elementAt(index)).getTip();
  }

  /**
   * This method returns the foreground color for the tab.
   *
   * @param index The index of the tab.
   *
   * @return The foreground color for the tab.
   */
  public Color getForegroundAt(int index)
  {
    checkIndex(index, 0, tabs.size());
    return ((Page) tabs.elementAt(index)).getForeground();
  }

  /**
   * This method returns the background color for the tab.
   *
   * @param index The index of the tab.
   *
   * @return The background color for the tab.
   */
  public Color getBackgroundAt(int index)
  {
    checkIndex(index, 0, tabs.size());
    return ((Page) tabs.elementAt(index)).getBackground();
  }

  /**
   * This method returns the component associated with the tab.
   *
   * @param index The index of the tab.
   *
   * @return The component associated with the tab.
   */
  public Component getComponentAt(int index)
  {
    checkIndex(index, 0, tabs.size());
    return ((Page) tabs.elementAt(index)).getComponent();
  }

  /**
   * This method returns whether this tab is enabled. Disabled tabs cannot be
   * selected.
   *
   * @param index The index of the tab.
   *
   * @return Whether the tab is enabled.
   */
  public boolean isEnabledAt(int index)
  {
    checkIndex(index, 0, tabs.size());
    return ((Page) tabs.elementAt(index)).isEnabled();
  }

  /**
   * This method returns the mnemonic for the tab.
   *
   * @param tabIndex The index of the tab.
   *
   * @return The mnemonic for the tab.
   */
  public int getMnemonicAt(int tabIndex)
  {
    checkIndex(tabIndex, 0, tabs.size());
    return ((Page) tabs.elementAt(tabIndex)).getMnemonic();
  }

  /**
   * This method returns the mnemonic index for the tab.
   *
   * @param tabIndex The index of the tab.
   *
   * @return The mnemonic index for the tab.
   */
  public int getDisplayedMnemonicIndexAt(int tabIndex)
  {
    checkIndex(tabIndex, 0, tabs.size());
    return ((Page) tabs.elementAt(tabIndex)).getDisplayedMnemonicIndex();
  }

  /**
   * This method returns the bounds of the tab given the index.
   *
   * @param index The index of the tab.
   *
   * @return A rectangle describing the bounds of the tab.
   */
  public Rectangle getBoundsAt(int index)
  {
    checkIndex(index, 0, tabs.size());
    return ((TabbedPaneUI) ui).getTabBounds(this, index);
  }

  /**
   * This method sets the title of the tab.
   *
   * @param index The index of the tab.
   * @param title The new title.
   */
  public void setTitleAt(int index, String title)
  {
    checkIndex(index, 0, tabs.size());
    ((Page) tabs.elementAt(index)).setTitle(title);
  }

  /**
   * This method sets the icon of the tab.
   *
   * @param index The index of the tab.
   * @param icon The new icon.
   */
  public void setIconAt(int index, Icon icon)
  {
    checkIndex(index, 0, tabs.size());
    ((Page) tabs.elementAt(index)).setIcon(icon);
  }

  /**
   * This method sets the disabled icon of the tab.
   *
   * @param index The index of the tab.
   * @param disabledIcon The new disabled icon.
   */
  public void setDisabledIconAt(int index, Icon disabledIcon)
  {
    checkIndex(index, 0, tabs.size());
    ((Page) tabs.elementAt(index)).setDisabledIcon(disabledIcon);
  }

  /**
   * This method sets the tooltip text of the tab.
   *
   * @param index The index of the tab.
   * @param toolTipText The tooltip text.
   */
  public void setToolTipTextAt(int index, String toolTipText)
  {
    checkIndex(index, 0, tabs.size());
    ((Page) tabs.elementAt(index)).setTip(toolTipText);
  }

  /**
   * This method sets the background color of the tab.
   *
   * @param index The index of the tab.
   * @param background The background color of the tab.
   */
  public void setBackgroundAt(int index, Color background)
  {
    checkIndex(index, 0, tabs.size());
    ((Page) tabs.elementAt(index)).setBackground(background);
  }

  /**
   * This method sets the foreground color of the tab.
   *
   * @param index The index of the tab.
   * @param foreground The foreground color of the tab.
   */
  public void setForegroundAt(int index, Color foreground)
  {
    checkIndex(index, 0, tabs.size());
    ((Page) tabs.elementAt(index)).setForeground(foreground);
  }

  /**
   * This method sets whether the tab is enabled.
   *
   * @param index The index of the tab.
   * @param enabled Whether the tab is enabled.
   */
  public void setEnabledAt(int index, boolean enabled)
  {
    checkIndex(index, 0, tabs.size());
    ((Page) tabs.elementAt(index)).setEnabled(enabled);
  }

  /**
   * This method sets the component associated with the tab.
   *
   * @param index The index of the tab.
   * @param component The component associated with the tab.
   */
  public void setComponentAt(int index, Component component)
  {
    checkIndex(index, 0, tabs.size());
    ((Page) tabs.elementAt(index)).setComponent(component);
  }

  /**
   * This method sets the displayed mnemonic index of the tab.
   *
   * @param tabIndex The index of the tab.
   * @param mnemonicIndex The mnemonic index.
   */
  public void setDisplayedMnemonicIndexAt(int tabIndex, int mnemonicIndex)
  {
    checkIndex(tabIndex, 0, tabs.size());
    ((Page) tabs.elementAt(tabIndex)).setDisplayedMnemonicIndex(mnemonicIndex);
  }

  /**
   * This method sets the mnemonic for the tab.
   *
   * @param tabIndex The index of the tab.
   * @param mnemonic The mnemonic.
   */
  public void setMnemonicAt(int tabIndex, int mnemonic)
  {
    checkIndex(tabIndex, 0, tabs.size());
    ((Page) tabs.elementAt(tabIndex)).setMnemonic(mnemonic);
  }

  /**
   * This method finds the index of a tab given the title.
   *
   * @param title The title that belongs to a tab.
   *
   * @return The index of the tab that has the title or -1 if not found.
   */
  public int indexOfTab(String title)
  {
    int index = -1;
    for (int i = 0; i < tabs.size(); i++)
      {
	if (((Page) tabs.elementAt(i)).getTitle().equals(title))
	  {
	    index = i;
	    break;
	  }
      }
    return index;
  }

  /**
   * This method finds the index of a tab given the icon.
   *
   * @param icon The icon that belongs to a tab.
   *
   * @return The index of the tab that has the icon or -1 if not found.
   */
  public int indexOfTab(Icon icon)
  {
    int index = -1;
    for (int i = 0; i < tabs.size(); i++)
      {
	if (((Page) tabs.elementAt(i)).getIcon() == icon)
	  {
	    index = i;
	    break;
	  }
      }
    return index;
  }

  /**
   * This method finds the index of a tab given the component.
   *
   * @param component A component associated with a tab.
   *
   * @return The index of the tab that has this component or -1 if not found.
   */
  public int indexOfComponent(Component component)
  {
    int index = -1;
    for (int i = 0; i < tabs.size(); i++)
      {
	if (((Page) tabs.elementAt(i)).getComponent() == component)
	  {
	    index = i;
	    break;
	  }
      }
    return index;
  }

  /**
   * This method returns a tab index given an (x,y) location. The origin of
   * the (x,y) pair will be the JTabbedPane's top left position. The  tab
   * returned will be the one that contains the point. This method is
   * delegated to the UI.
   *
   * @param x The x coordinate of the point.
   * @param y The y coordinate of the point.
   *
   * @return The index of the tab that contains the point.
   */
  public int indexAtLocation(int x, int y)
  {
    return ((TabbedPaneUI) ui).tabForCoordinate(this, x, y);
  }

  /**
   * This method returns the tooltip text given a mouse event.
   *
   * @param event The mouse event.
   *
   * @return The tool tip text that is associated with this mouse event.
   */
  public String getToolTipText(MouseEvent event)
  {
    int index = indexAtLocation(event.getX(), event.getY());
    return ((Page) tabs.elementAt(index)).getTip();
  }

  /**
   * Returns a string describing the attributes for the 
   * <code>JTabbedPane</code> component, for use in debugging.  The return 
   * value is guaranteed to be non-<code>null</code>, but the format of the 
   * string may vary between implementations.
   *
   * @return A string describing the attributes of the 
   *     <code>JTabbedPane</code>.
   */
  protected String paramString()
  {
    StringBuffer sb = new StringBuffer(super.paramString());
    sb.append(",tabPlacement=");
    if (tabPlacement == TOP)
      sb.append("TOP");
    if (tabPlacement == BOTTOM)
      sb.append("BOTTOM");
    if (tabPlacement == LEFT)
      sb.append("LEFT");
    if (tabPlacement == RIGHT)
      sb.append("RIGHT");
    return sb.toString();
  }

  /**
   * Returns the object that provides accessibility features for this
   * <code>JTabbedPane</code> component.
   *
   * @return The accessible context (an instance of 
   *         {@link AccessibleJTabbedPane}).
   */
  public AccessibleContext getAccessibleContext()
  {
    if (accessibleContext == null)
      {
        AccessibleJTabbedPane ctx = new AccessibleJTabbedPane();
        addChangeListener(ctx);
        accessibleContext = ctx;
      }

    return accessibleContext;
  }
}
