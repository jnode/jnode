/* BasicComboPopup.java --
   Copyright (C) 2004, 2005  Free Software Foundation, Inc.

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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * UI Delegate for ComboPopup
 *
 * @author Olga Rodimina
 */
public class BasicComboPopup extends JPopupMenu implements ComboPopup
{
  /* Timer for autoscrolling */
  protected Timer autoscrollTimer;

  /** ComboBox associated with this popup */
  protected JComboBox comboBox;

  /** FIXME: Need to document */
  protected boolean hasEntered;

  /**
   * Indicates whether the scroll bar located in popup menu with comboBox's
   * list of items is currently autoscrolling. This happens when mouse event
   * originated in the combo box and is dragged outside of its bounds
   */
  protected boolean isAutoScrolling;

  /** ItemListener listening to the selection changes in the combo box */
  protected ItemListener itemListener;

  /** This listener is not used */
  protected KeyListener keyListener;

  /** JList which is used to display item is the combo box */
  protected JList list;

  /** This listener is not used */
  protected ListDataListener listDataListener;

  /**
   * MouseListener listening to mouse events occuring in the  combo box's
   * list.
   */
  protected MouseListener listMouseListener;

  /**
   * MouseMotionListener listening to mouse motion events occuring  in the
   * combo box's list
   */
  protected MouseMotionListener listMouseMotionListener;

  /** This listener is not used */
  protected ListSelectionListener listSelectionListener;

  /** MouseListener listening to mouse events occuring in the combo box */
  protected MouseListener mouseListener;

  /**
   * MouseMotionListener listening to mouse motion events occuring in the
   * combo box
   */
  protected MouseMotionListener mouseMotionListener;

  /**
   * PropertyChangeListener listening to changes occuring in the bound
   * properties of the combo box
   */
  protected PropertyChangeListener propertyChangeListener;

  /** direction for scrolling down list of combo box's items */
  protected static final int SCROLL_DOWN = 1;

  /** direction for scrolling up list of combo box's items */
  protected static final int SCROLL_UP = 0;

  /** Indicates auto scrolling direction */
  protected int scrollDirection;

  /** JScrollPane that contains list portion of the combo box */
  protected JScrollPane scroller;

  /** This field is not used */
  protected boolean valueIsAdjusting;

  /**
   * Creates a new BasicComboPopup object.
   *
   * @param comboBox the combo box with which this popup should be associated
   */
  public BasicComboPopup(JComboBox comboBox)
  {
    this.comboBox = comboBox;
    installComboBoxListeners();
    configurePopup();
    setLightWeightPopupEnabled(comboBox.isLightWeightPopupEnabled());
  }

  /**
   * This method displays drow down list of combo box items on the screen.
   */
  public void show()
  {
    Rectangle cbBounds = comboBox.getBounds();

    // popup should have same width as the comboBox and should be hight anough
    // to display number of rows equal to 'maximumRowCount' property
    int popupHeight = getPopupHeightForRowCount(comboBox.getMaximumRowCount());

    list.setPreferredSize(new Dimension(cbBounds.width, popupHeight));
    super.setPopupSize(cbBounds.width, popupHeight);

    // Highlight selected item in the combo box's drop down list
    if (comboBox.getSelectedIndex() != -1)
      list.setSelectedIndex(comboBox.getSelectedIndex());

    //scroll scrollbar s.t. selected item is visible
    JScrollBar scrollbar = scroller.getVerticalScrollBar();
    int selectedIndex = comboBox.getSelectedIndex();
    if (selectedIndex > comboBox.getMaximumRowCount())
      scrollbar.setValue(getPopupHeightForRowCount(selectedIndex));

    // location specified is relative to comboBox
    super.show(comboBox, 0, cbBounds.height);
  }

  /**
   * This method hides drop down list of items
   */
  public void hide()
  {
    super.setVisible(false);
  }

  /**
   * Return list cointaining JComboBox's items
   *
   * @return list cointaining JComboBox's items
   */
  public JList getList()
  {
    return list;
  }

  /**
   * Returns MouseListener that is listening to mouse events occuring in the
   * combo box.
   *
   * @return MouseListener
   */
  public MouseListener getMouseListener()
  {
    return mouseListener;
  }

  /**
   * Returns MouseMotionListener that is listening to mouse  motion events
   * occuring in the combo box.
   *
   * @return MouseMotionListener
   */
  public MouseMotionListener getMouseMotionListener()
  {
    return mouseMotionListener;
  }

  /**
   * Returns KeyListener listening to key events occuring in the combo box.
   * This method returns null because KeyHandler is not longer used.
   *
   * @return KeyListener
   */
  public KeyListener getKeyListener()
  {
    return keyListener;
  }

  /**
   * This method uninstalls the UI for the  given JComponent.
   */
  public void uninstallingUI()
  {
    uninstallComboBoxModelListeners(comboBox.getModel());

    uninstallListeners();
    uninstallKeyboardActions();
  }

  /**
   * This method uninstalls listeners that were listening to changes occuring
   * in the comb box's data model
   *
   * @param model data model for the combo box from which to uninstall
   *        listeners
   */
  protected void uninstallComboBoxModelListeners(ComboBoxModel model)
  {
    model.removeListDataListener(listDataListener);
  }

  /**
   * This method uninstalls keyboard actions installed by the UI.
   */
  protected void uninstallKeyboardActions()
  {
    // FIXME: Need to implement
  }

  /**
   * This method fires PopupMenuEvent indicating that combo box's popup list
   * of items will become visible
   */
  protected void firePopupMenuWillBecomeVisible()
  {
    PopupMenuListener[] ll = comboBox.getPopupMenuListeners();

    for (int i = 0; i < ll.length; i++)
      ll[i].popupMenuWillBecomeVisible(new PopupMenuEvent(comboBox));
  }

  /**
   * This method fires PopupMenuEvent indicating that combo box's popup list
   * of items will become invisible.
   */
  protected void firePopupMenuWillBecomeInvisible()
  {
    PopupMenuListener[] ll = comboBox.getPopupMenuListeners();

    for (int i = 0; i < ll.length; i++)
      ll[i].popupMenuWillBecomeInvisible(new PopupMenuEvent(comboBox));
  }

  /**
   * This method fires PopupMenuEvent indicating that combo box's popup list
   * of items was closed without selection.
   */
  protected void firePopupMenuCanceled()
  {
    PopupMenuListener[] ll = comboBox.getPopupMenuListeners();

    for (int i = 0; i < ll.length; i++)
      ll[i].popupMenuCanceled(new PopupMenuEvent(comboBox));
  }

  /**
   * Creates MouseListener to listen to mouse events occuring in the combo
   * box. Note that this listener doesn't listen to mouse events occuring in
   * the popup portion of the combo box, it only listens to main combo box
   * part.
   *
   * @return new MouseMotionListener that listens to mouse events occuring in
   *         the combo box
   */
  protected MouseListener createMouseListener()
  {
    return new InvocationMouseHandler();
  }

  /**
   * Create Mouse listener that listens to mouse dragging events occuring in
   * the combo box. This listener is responsible for changing the selection
   * in the combo box list to the component over which mouse is being
   * currently dragged
   *
   * @return new MouseMotionListener that listens to mouse dragging events
   *         occuring in the combo box
   */
  protected MouseMotionListener createMouseMotionListener()
  {
    return new InvocationMouseMotionHandler();
  }

  /**
   * KeyListener created in this method is not used anymore.
   *
   * @return KeyListener that does nothing
   */
  protected KeyListener createKeyListener()
  {
    return new InvocationKeyHandler();
  }

  /**
   * ListSelectionListener created in this method is not used anymore
   *
   * @return ListSelectionListener that does nothing
   */
  protected ListSelectionListener createListSelectionListener()
  {
    return new ListSelectionHandler();
  }

  /**
   * Creates ListDataListener. This method returns null, because
   * ListDataHandler class is obsolete and is no longer used.
   *
   * @return null
   */
  protected ListDataListener createListDataListener()
  {
    return null;
  }

  /**
   * This method creates ListMouseListener to listen to mouse events occuring
   * in the combo box's item list.
   *
   * @return MouseListener to listen to mouse events occuring in the combo
   *         box's items list.
   */
  protected MouseListener createListMouseListener()
  {
    return new ListMouseHandler();
  }

  /**
   * Creates ListMouseMotionlistener to listen to mouse motion events occuring
   * in the combo box's list. This listener is responsible for highlighting
   * items in the list when mouse is moved over them.
   *
   * @return MouseMotionListener that handles mouse motion events occuring in
   *         the list of the combo box.
   */
  protected MouseMotionListener createListMouseMotionListener()
  {
    return new ListMouseMotionHandler();
  }

  /**
   * Creates PropertyChangeListener to handle changes in the JComboBox's bound
   * properties.
   *
   * @return PropertyChangeListener to handle changes in the JComboBox's bound
   *         properties.
   */
  protected PropertyChangeListener createPropertyChangeListener()
  {
    return new PropertyChangeHandler();
  }

  /**
   * Creates new ItemListener that will listen to ItemEvents occuring in the
   * combo box.
   *
   * @return ItemListener to listen to ItemEvents occuring in the combo box.
   */
  protected ItemListener createItemListener()
  {
    return new ItemHandler();
  }

  /**
   * Creates JList that will be used to display items in the combo box.
   *
   * @return JList that will be used to display items in the combo box.
   */
  protected JList createList()
  {
    JList l = new JList(comboBox.getModel());
    l.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    return l;
  }

  /**
   * This method configures the list of comboBox's items by setting  default
   * properties and installing listeners.
   */
  protected void configureList()
  {
    list.setModel(comboBox.getModel());
    list.setVisibleRowCount(comboBox.getMaximumRowCount());
    installListListeners();
  }

  /**
   * This method installs list listeners.
   */
  protected void installListListeners()
  {
    // mouse listener listening to mouse events occuring in the 
    // combo box's list of items.
    listMouseListener = createListMouseListener();
    list.addMouseListener(listMouseListener);

    // mouse listener listening to mouse motion events occuring in the
    // combo box's list of items
    listMouseMotionListener = createListMouseMotionListener();
    list.addMouseMotionListener(listMouseMotionListener);

    listSelectionListener = createListSelectionListener();
    list.addListSelectionListener(listSelectionListener);
  }

  /**
   * This method creates scroll pane that will contain the list of comboBox's
   * items inside of it.
   *
   * @return JScrollPane
   */
  protected JScrollPane createScroller()
  {
    return new JScrollPane();
  }

  /**
   * This method configures scroll pane to contain list of comboBox's  items
   */
  protected void configureScroller()
  {
    scroller.getViewport().setView(list);
    scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
  }

  /**
   * This method configures popup menu that will be used to display Scrollpane
   * with list of items inside of it.
   */
  protected void configurePopup()
  {
    // initialize list that will be used to display combo box's items
    this.list = createList();
    ((JLabel) list.getCellRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
    configureList();

    // initialize scroller. Add list to the scroller.	
    scroller = createScroller();
    configureScroller();

    // add scroller with list inside of it to JPopupMenu
    super.add(scroller);
  }

  /*
   * This method installs listeners that will listen to changes occuring
   * in the combo box.
   */
  protected void installComboBoxListeners()
  {
    // mouse listener that listens to mouse event in combo box
    mouseListener = createMouseListener();
    comboBox.addMouseListener(mouseListener);

    // mouse listener that listens to mouse dragging events in the combo box
    mouseMotionListener = createMouseMotionListener();
    comboBox.addMouseMotionListener(mouseMotionListener);

    // item listener listenening to selection events in the combo box
    itemListener = createItemListener();
    comboBox.addItemListener(itemListener);

    propertyChangeListener = createPropertyChangeListener();
    comboBox.addPropertyChangeListener(propertyChangeListener);
  }

  /**
   * This method installs listeners that will listen to changes occuring in
   * the comb box's data model
   *
   * @param model data model for the combo box for which to install listeners
   */
  protected void installComboBoxModelListeners(ComboBoxModel model)
  {
    // list data listener to listen for ListDataEvents in combo box.
    // This listener is now obsolete and nothing is done here
    listDataListener = createListDataListener();
    comboBox.getModel().addListDataListener(listDataListener);
  }

  /**
   * DOCUMENT ME!
   */
  protected void installKeyboardActions()
  {
    // FIXME: Need to implement
  }

  /**
   * This method always returns false to indicate that  items in the combo box
   * list are not focus traversable.
   *
   * @return false
   */
  public boolean isFocusTraversable()
  {
    return false;
  }

  /**
   * This method start scrolling combo box's list of items  either up or down
   * depending on the specified 'direction'
   *
   * @param direction of the scrolling.
   */
  protected void startAutoScrolling(int direction)
  {
    // FIXME: add timer
    isAutoScrolling = true;

    if (direction == SCROLL_UP)
      autoScrollUp();
    else
      autoScrollDown();
  }

  /**
   * This method stops scrolling the combo box's list of items
   */
  protected void stopAutoScrolling()
  {
    // FIXME: add timer
    isAutoScrolling = false;
  }

  /**
   * This method scrolls up list of combo box's items up and highlights that
   * just became visible.
   */
  protected void autoScrollUp()
  {
    // scroll up the scroll bar to make the item above visible    
    JScrollBar scrollbar = scroller.getVerticalScrollBar();
    int scrollToNext = list.getScrollableUnitIncrement(super.getBounds(),
                                                       SwingConstants.VERTICAL,
                                                       SCROLL_UP);

    scrollbar.setValue(scrollbar.getValue() - scrollToNext);

    // If we haven't reached the begging of the combo box's list of items, 
    // then highlight next element above currently highlighted element	
    if (list.getSelectedIndex() != 0)
      list.setSelectedIndex(list.getSelectedIndex() - 1);
  }

  /**
   * This method scrolls down list of combo box's and highlights item in the
   * list that just became visible.
   */
  protected void autoScrollDown()
  {
    // scroll scrollbar down to make next item visible    
    JScrollBar scrollbar = scroller.getVerticalScrollBar();
    int scrollToNext = list.getScrollableUnitIncrement(super.getBounds(),
                                                       SwingConstants.VERTICAL,
                                                       SCROLL_DOWN);
    scrollbar.setValue(scrollbar.getValue() + scrollToNext);

    // If we haven't reached the end of the combo box's list of items
    // then highlight next element below currently highlighted element
    if (list.getSelectedIndex() + 1 != comboBox.getItemCount())
      list.setSelectedIndex(list.getSelectedIndex() + 1);
  }

  /**
   * This method helps to delegate focus to the right component in the
   * JComboBox. If the comboBox is editable then focus is sent to
   * ComboBoxEditor, otherwise it is delegated to JComboBox.
   *
   * @param e MouseEvent
   */
  protected void delegateFocus(MouseEvent e)
  {
    // FIXME: Need to implement
  }

  /**
   * This method displays combo box popup if the popup is  not currently shown
   * on the screen and hides it if it is  currently visible
   */
  protected void togglePopup()
  {
    if (BasicComboPopup.this.isVisible())
      hide();
    else
      show();
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  protected MouseEvent convertMouseEvent(MouseEvent e)
  {
    return null;
  }

  /**
   * Returns required height of the popup such that number of items visible in
   * it are equal to the maximum row count.  By default
   * comboBox.maximumRowCount=8
   *
   * @param maxRowCount number of maximum visible rows in the  combo box's
   *        popup list of items
   *
   * @return height of the popup required to fit number of items  equal to
   *         JComboBox.maximumRowCount.
   */
  protected int getPopupHeightForRowCount(int maxRowCount)
  {
    int totalHeight = 0;
    ListCellRenderer rend = list.getCellRenderer();

    if (comboBox.getItemCount() < maxRowCount)
      maxRowCount = comboBox.getItemCount();

    for (int i = 0; i < maxRowCount; i++)
      {
	Component comp = rend.getListCellRendererComponent(list,
	                                                   comboBox.getModel()
	                                                           .getElementAt(i),
	                                                   -1, false, false);
	Dimension dim = comp.getPreferredSize();
	totalHeight += dim.height;
      }

    return totalHeight;
  }

  /**
   * DOCUMENT ME!
   *
   * @param px DOCUMENT ME!
   * @param py DOCUMENT ME!
   * @param pw DOCUMENT ME!
   * @param ph DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  protected Rectangle computePopupBounds(int px, int py, int pw, int ph)
  {
    return new Rectangle(px, py, pw, ph);
  }

  /**
   * This method changes the selection in the list to the item over which  the
   * mouse is currently located.
   *
   * @param anEvent MouseEvent
   * @param shouldScroll DOCUMENT ME!
   */
  protected void updateListBoxSelectionForEvent(MouseEvent anEvent,
                                                boolean shouldScroll)
  {
    // FIXME: Need to implement
  }

  /**
   * InvocationMouseHandler is a listener that listens to mouse events
   * occuring in the combo box. Note that this listener doesn't listen to
   * mouse events occuring in the popup portion of the combo box, it only
   * listens to main combo box part(area that displays selected item).  This
   * listener is responsible for showing and hiding popup portion  of the
   * combo box.
   */
  protected class InvocationMouseHandler extends MouseAdapter
  {
    /**
     * Creates a new InvocationMouseHandler object.
     */
    protected InvocationMouseHandler()
    {
    }

    /**
     * This method is invoked whenever mouse is being pressed over the main
     * part of the combo box. This method will show popup if  the popup is
     * not shown on the screen right now, and it will hide popup otherwise.
     *
     * @param e MouseEvent that should be handled
     */
    public void mousePressed(MouseEvent e)
    {
      if (comboBox.isEnabled())
	togglePopup();
    }

    /**
     * This method is invoked whenever mouse event was originated in the combo
     * box and released either in the combBox list of items or in the combo
     * box itself.
     *
     * @param e MouseEvent that should be handled
     */
    public void mouseReleased(MouseEvent e)
    {
      // Get component over which mouse was released
      Component src = (Component) e.getSource();
      int x = e.getX();
      int y = e.getY();
      Component releasedComponent = SwingUtilities.getDeepestComponentAt(src,
                                                                         x, y);

      // if mouse was released inside the bounds of combo box then do nothing,
      // Otherwise if mouse was released inside the list of combo box items
      // then change selection and close popup
      if (! (releasedComponent instanceof JComboBox))
        {
	  // List model contains the item over which mouse is released,
	  // since it is updated every time the mouse is moved over a different
	  // item in the list. Now that the mouse is released we need to
	  // update model of the combo box as well. 	  
	  comboBox.setSelectedIndex(list.getSelectedIndex());

	  if (isAutoScrolling)
	    stopAutoScrolling();
	  hide();
        }
    }
  }

  /**
   * InvocationMouseMotionListener is a mouse listener that listens to mouse
   * dragging events occuring in the combo box.
   */
  protected class InvocationMouseMotionHandler extends MouseMotionAdapter
  {
    /**
     * Creates a new InvocationMouseMotionHandler object.
     */
    protected InvocationMouseMotionHandler()
    {
    }

    /**
     * This method is responsible for highlighting item in the drop down list
     * over which the mouse is currently being dragged.
     */
    public void mouseDragged(MouseEvent e)
    {
      // convert point of the drag event relative to combo box list component
      // figure out over which list cell the mouse is currently being dragged
      // and highlight the cell. The list model is changed but the change has 
      // no effect on combo box's data model. The list model is changed so 
      // that the appropriate item would be highlighted in the combo box's 
      // list.
      if (BasicComboPopup.this.isVisible())
        {
	  int cbHeight = (int) comboBox.getPreferredSize().getHeight();
	  int popupHeight = BasicComboPopup.this.getSize().height;

	  // if mouse is dragged inside the the combo box's items list.
	  if (e.getY() > cbHeight && ! (e.getY() - cbHeight >= popupHeight))
	    {
	      int index = list.locationToIndex(new Point(e.getX(),
	                                                 (int) (e.getY()
	                                                 - cbHeight)));

	      int firstVisibleIndex = list.getFirstVisibleIndex();

	      // list.locationToIndex returns item's index that would
	      // be located at the specified point if the first item that
	      // is visible is item 0. However in the JComboBox it is not 
	      // necessarily the case since list is contained in the 
	      // JScrollPane so we need to adjust the index returned. 
	      if (firstVisibleIndex != 0)
		// FIXME: adjusted index here is off by one. I am adding one
		// here to compensate for that. This should be
		// index += firstVisibleIndex. Remove +1 once the bug is fixed.
		index += firstVisibleIndex + 1;

	      list.setSelectedIndex(index);
	    }
	  else
	    {
	      // if mouse is being dragged at the bottom of combo box's list 
	      // of items or at the very top then scroll the list in the 
	      // desired direction.
	      boolean movingUP = e.getY() < cbHeight;
	      boolean movingDown = e.getY() > cbHeight;

	      if (movingUP)
	        {
		  scrollDirection = SCROLL_UP;
		  startAutoScrolling(SCROLL_UP);
	        }
	      else if (movingDown)
	        {
		  scrollDirection = SCROLL_DOWN;
		  startAutoScrolling(SCROLL_DOWN);
	        }
	    }
        }
    }
  }

  /**
   * ItemHandler is an item listener that listens to selection events occuring
   * in the combo box. FIXME: should specify here what it does when item is
   * selected or deselected in the combo box list.
   */
  protected class ItemHandler extends Object implements ItemListener
  {
    /**
     * Creates a new ItemHandler object.
     */
    protected ItemHandler()
    {
    }

    /**
     * This method responds to the selection events occuring in the combo box.
     *
     * @param e ItemEvent specifying the combo box's selection
     */
    public void itemStateChanged(ItemEvent e)
    {
    }
  }

  /**
   * ListMouseHandler is a listener that listens to mouse events occuring in
   * the combo box's list of items. This class is responsible for hiding
   * popup portion of the combo box if the mouse is released inside the combo
   * box's list.
   */
  protected class ListMouseHandler extends MouseAdapter
  {
    protected ListMouseHandler()
    {
    }

    public void mousePressed(MouseEvent e)
    {
    }

    public void mouseReleased(MouseEvent anEvent)
    {
      int index = list.locationToIndex(anEvent.getPoint());
      comboBox.setSelectedIndex(index);
      hide();
    }
  }

  /**
   * ListMouseMotionHandler listens to mouse motion events occuring in the
   * combo box's list. This class is responsible for highlighting items in
   * the list when mouse is moved over them
   */
  protected class ListMouseMotionHandler extends MouseMotionAdapter
  {
    protected ListMouseMotionHandler()
    {
    }

    public void mouseMoved(MouseEvent anEvent)
    {
      // Highlight list cells over which the mouse is located. 
      // This changes list model, but has no effect on combo box's data model
      int index = list.locationToIndex(anEvent.getPoint());
      list.setSelectedIndex(index);
      list.repaint();
    }
  }

  /**
   * This class listens to changes occuring in the bound properties of the
   * combo box
   */
  protected class PropertyChangeHandler extends Object
    implements PropertyChangeListener
  {
    protected PropertyChangeHandler()
    {
    }

    public void propertyChange(PropertyChangeEvent e)
    {
      if (e.getPropertyName().equals("renderer"))
        {
	  list.setCellRenderer((ListCellRenderer) e.getNewValue());
	  revalidate();
	  repaint();
        }
      if (e.getPropertyName().equals("dataModel"))
        {
	  list.setModel((ComboBoxModel) e.getNewValue());
	  revalidate();
	  repaint();
        }
    }
  }

  // ------ private helper methods --------------------

  /**
   * This method uninstalls listeners installed by the UI
   */
  private void uninstallListeners()
  {
    uninstallListListeners();
    uninstallComboBoxListeners();
    uninstallComboBoxModelListeners(comboBox.getModel());
  }

  /**
   * This method uninstalls Listeners registered with combo boxes list of
   * items
   */
  private void uninstallListListeners()
  {
    list.removeMouseListener(listMouseListener);
    listMouseListener = null;

    list.removeMouseMotionListener(listMouseMotionListener);
    listMouseMotionListener = null;
  }

  /**
   * This method uninstalls listeners listening to combo box  associated with
   * this popup menu
   */
  private void uninstallComboBoxListeners()
  {
    comboBox.removeMouseListener(mouseListener);
    mouseListener = null;

    comboBox.removeMouseMotionListener(mouseMotionListener);
    mouseMotionListener = null;

    comboBox.removeItemListener(itemListener);
    itemListener = null;

    comboBox.removePropertyChangeListener(propertyChangeListener);
    propertyChangeListener = null;
  }

  // --------------------------------------------------------------------
  //  The following classes are here only for backwards API compatibility
  //  They aren't used.
  // --------------------------------------------------------------------

  /**
   * This class is not used any more.
   */
  public class ListDataHandler extends Object implements ListDataListener
  {
    public ListDataHandler()
    {
    }

    public void contentsChanged(ListDataEvent e)
    {
    }

    public void intervalAdded(ListDataEvent e)
    {
    }

    public void intervalRemoved(ListDataEvent e)
    {
    }
  }

  /**
   * This class is not used anymore
   */
  protected class ListSelectionHandler extends Object
    implements ListSelectionListener
  {
    protected ListSelectionHandler()
    {
    }

    public void valueChanged(ListSelectionEvent e)
    {
    }
  }

  /**
   * This class is not used anymore
   */
  public class InvocationKeyHandler extends KeyAdapter
  {
    public InvocationKeyHandler()
    {
    }

    public void keyReleased(KeyEvent e)
    {
    }
  }
}
