/* BasicScrollPaneUI.java
   Copyright (C) 2002, 2004, 2005 Free Software Foundation, Inc.

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


package javax.swing.plaf.basic;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.ScrollPaneLayout;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ScrollPaneUI;

public class BasicScrollPaneUI extends ScrollPaneUI
  implements ScrollPaneConstants
{

  /**
   * Listens for changes in the state of the horizontal scrollbar's model and
   * updates the scrollpane accordingly.
   *
   * @author Roman Kennke (kennke@aicas.com)
   */
  public class HSBChangeListener implements ChangeListener
  {

    /**
     * Receives notification when the state of the horizontal scrollbar
     * model has changed.
     *
     * @param event the change event
     */
    public void stateChanged(ChangeEvent event)
    {
      JScrollBar hsb = scrollpane.getHorizontalScrollBar();
      JViewport vp = scrollpane.getViewport();
      Point viewPosition = vp.getViewPosition();
      int xpos = hsb.getValue();

      if (xpos != viewPosition.x)
        {
          viewPosition.x = xpos;
          vp.setViewPosition(viewPosition);
        }

      viewPosition.y = 0;
      JViewport columnHeader = scrollpane.getColumnHeader();
      if (columnHeader != null 
          && !columnHeader.getViewPosition().equals(viewPosition))
        columnHeader.setViewPosition(viewPosition);
    }

  }

  /**
   * Listens for changes in the state of the vertical scrollbar's model and
   * updates the scrollpane accordingly.
   *
   * @author Roman Kennke (kennke@aicas.com)
   */
  public class VSBChangeListener implements ChangeListener
  {

    /**
     * Receives notification when the state of the vertical scrollbar
     * model has changed.
     *
     * @param event the change event
     */
    public void stateChanged(ChangeEvent event)
    {
      JScrollBar vsb = scrollpane.getVerticalScrollBar();
      JViewport vp = scrollpane.getViewport();
      Point viewPosition = vp.getViewPosition();
      int ypos = vsb.getValue();

      if (ypos != viewPosition.x)
        {
          viewPosition.y = ypos;
          vp.setViewPosition(viewPosition);
        }

      viewPosition.x = 0;
      JViewport rowHeader = scrollpane.getRowHeader();
      if (rowHeader != null 
          && !rowHeader.getViewPosition().equals(viewPosition))
        rowHeader.setViewPosition(viewPosition);
    }
 
  }

  /**
   * Listens for changes of the viewport's extent size and updates the
   * scrollpane accordingly.
   *
   * @author Roman Kennke (kennke@aicas.com)
   */
  public class ViewportChangeHandler implements ChangeListener
  {

    /**
     * Receives notification when the view's size, position or extent size
     * changes. When the extents size has changed, this method calls
     * {@link BasicScrollPaneUI#syncScrollPaneWithViewport()} to adjust the
     * scrollbars extents as well.
     * 
     * @param event the change event
     */
    public void stateChanged(ChangeEvent event)
    {
      JViewport vp = scrollpane.getViewport();
      JScrollBar hsb = scrollpane.getHorizontalScrollBar();
      JScrollBar vsb = scrollpane.getVerticalScrollBar();
      Dimension extents = vp.getExtentSize();
      if (extents.width != hsb.getModel().getExtent()
          || extents.height != vsb.getModel().getExtent())
        syncScrollPaneWithViewport();
    }

  }

  /**
   * Listens for property changes on the scrollpane and update the view
   * accordingly.
   *
   * @author Roman Kennke (kennke@aicas.com)
   */
  public class PropertyChangeHandler implements PropertyChangeListener
  {

    /**
     * Receives notification when any of the scrollpane's bound property
     * changes. This method calls the appropriate update method on the
     * <code>ScrollBarUI</code>.
     *
     * @param e the property change event
     *
     * @see BasicScrollPaneUI#updateColumnHeader(PropertyChangeEvent)
     * @see BasicScrollPaneUI#updateRowHeader(PropertyChangeEvent)
     * @see BasicScrollPaneUI#updateScrollBarDisplayPolicy(PropertyChangeEvent)
     * @see BasicScrollPaneUI#updateViewport(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent e)
    {
      if (e.getPropertyName().equals("viewport"))
        updateViewport(e);
      else if (e.getPropertyName().equals("rowHeader"))
        updateRowHeader(e);
      else if (e.getPropertyName().equals("columnHeader"))
        updateColumnHeader(e);
      else if (e.getPropertyName().equals("horizontalScrollBarPolicy")
          || e.getPropertyName().equals("verticalScrollBarPolicy"))
        updateScrollBarDisplayPolicy(e);
    }

  }

  /**
   * Listens for mouse wheel events and update the scrollpane accordingly.
   *
   * @author Roman Kennke (kennke@aicas.com)
   *
   * @since 1.4
   */
  protected class MouseWheelHandler implements MouseWheelListener
  {

    /**
     * Receives notification whenever the mouse wheel is moved.
     *
     * @param event the mouse wheel event
     */
    public void mouseWheelMoved(MouseWheelEvent event)
    {
    }

  }

  /** The Scrollpane for which the UI is provided by this class. */
  protected JScrollPane scrollpane;

  /**
   * The horizontal scrollbar listener.
   */
  protected ChangeListener hsbChangeListener;

  /**
   * The vertical scrollbar listener.
   */
  protected ChangeListener vsbChangeListener;

  /**
   * The viewport listener.
   */
  protected ChangeListener viewportChangeListener;

  /**
   * The scrollpane property change listener.
   */
  protected PropertyChangeListener spPropertyChangeListener;

  /**
   * The mousewheel listener for the scrollpane.
   */
  MouseWheelListener mouseWheelListener;

  public static ComponentUI createUI(final JComponent c) 
  {
    return new BasicScrollPaneUI();
  }

  protected void installDefaults(JScrollPane p)
  {
    scrollpane = p;
    UIDefaults defaults = UIManager.getLookAndFeelDefaults();
    p.setForeground(defaults.getColor("ScrollPane.foreground"));
    p.setBackground(defaults.getColor("ScrollPane.background"));
    p.setFont(defaults.getFont("ScrollPane.font"));
    p.setBorder(defaults.getBorder("ScrollPane.border"));
    p.setOpaque(true);
  }

  protected void uninstallDefaults(JScrollPane p)
  {
    p.setForeground(null);
    p.setBackground(null);
    p.setFont(null);
    p.setBorder(null);
    scrollpane = null;
  }
    
  public void installUI(final JComponent c) 
  {
    super.installUI(c);
    installDefaults((JScrollPane) c);
    installListeners((JScrollPane) c);
  }

  /**
   * Installs the listeners on the scrollbars, the viewport and the scrollpane.
   *
   * @param sp the scrollpane on which to install the listeners
   */
  protected void installListeners(JScrollPane sp)
  {
    if (spPropertyChangeListener == null)
      spPropertyChangeListener = createPropertyChangeListener();
    sp.addPropertyChangeListener(spPropertyChangeListener);

    if (hsbChangeListener == null)
      hsbChangeListener = createHSBChangeListener();
    sp.getHorizontalScrollBar().getModel().addChangeListener(hsbChangeListener);
    
    if (vsbChangeListener == null)
      vsbChangeListener = createVSBChangeListener();
    sp.getVerticalScrollBar().getModel().addChangeListener(vsbChangeListener);

    if (viewportChangeListener == null)
      viewportChangeListener = createViewportChangeListener();
    sp.getViewport().addChangeListener(viewportChangeListener);

    if (mouseWheelListener == null)
      mouseWheelListener = createMouseWheelListener();
    sp.addMouseWheelListener(mouseWheelListener);
  }

  /**
   * Creates and returns the change listener for the horizontal scrollbar.
   *
   * @return the change listener for the horizontal scrollbar
   */
  protected ChangeListener createHSBChangeListener()
  {
    return new HSBChangeListener();
  }

  /**
   * Creates and returns the change listener for the vertical scrollbar.
   *
   * @return the change listener for the vertical scrollbar
   */
  protected ChangeListener createVSBChangeListener()
  {
    return new VSBChangeListener();
  }

  /**
   * Creates and returns the change listener for the viewport.
   *
   * @return the change listener for the viewport
   */
  protected ChangeListener createViewportChangeListener()
  {
    return new ViewportChangeHandler();
  }

  /**
   * Creates and returns the property change listener for the scrollpane.
   *
   * @return the property change listener for the scrollpane
   */
  protected PropertyChangeListener createPropertyChangeListener()
  {
    return new PropertyChangeHandler();
  }

  /**
   * Creates and returns the mouse wheel listener for the scrollpane.
   *
   * @return the mouse wheel listener for the scrollpane
   */
  protected MouseWheelListener createMouseWheelListener()
  {
    return new MouseWheelHandler();
  }

  public void uninstallUI(final JComponent c) 
  {
    super.uninstallUI(c);
    this.uninstallDefaults((JScrollPane)c);
    uninstallListeners((JScrollPane) c);
  }

  /**
   * Uninstalls all the listeners that have been installed in
   * {@link #installListeners(JScrollPane)}.
   *
   * @param c the scrollpane from which to uninstall the listeners 
   */
  protected void uninstallListeners(JComponent c)
  {
    JScrollPane sp = (JScrollPane) c;
    sp.removePropertyChangeListener(spPropertyChangeListener);
    sp.getHorizontalScrollBar().getModel()
                               .removeChangeListener(hsbChangeListener);
    sp.getVerticalScrollBar().getModel()
                             .removeChangeListener(vsbChangeListener);
    sp.getViewport().removeChangeListener(viewportChangeListener);
    sp.removeMouseWheelListener(mouseWheelListener);
  }

  public Dimension getMinimumSize(JComponent c) 
  {
    JScrollPane p = (JScrollPane ) c;
    ScrollPaneLayout sl = (ScrollPaneLayout) p.getLayout();
    return sl.minimumLayoutSize(c);
  }

  public void paint(Graphics g, JComponent c)
  {      
    // do nothing; the normal painting-of-children algorithm, along with
    // ScrollPaneLayout, does all the relevant work.
  }

  /**
   * Synchronizes the scrollbars with the viewport's extents.
   */
  protected void syncScrollPaneWithViewport()
  {
    JViewport vp = scrollpane.getViewport();
    JScrollBar vsb = scrollpane.getVerticalScrollBar();
    JScrollBar hsb = scrollpane.getHorizontalScrollBar();
    hsb.getModel().setExtent(vp.getExtentSize().width);
    vsb.getModel().setExtent(vp.getExtentSize().height);
  }

  /**
   * Receives notification when the <code>columnHeader</code> property has
   * changed on the scrollpane.
   *
   * @param ev the property change event
   */
  protected void updateColumnHeader(PropertyChangeEvent ev)
  {
    // TODO: Find out what should be done here. Or is this only a hook?
  }

  /**
   * Receives notification when the <code>rowHeader</code> property has changed
   * on the scrollpane.
   *
   * @param ev the property change event
   */
  protected void updateRowHeader(PropertyChangeEvent ev)
  {
    // TODO: Find out what should be done here. Or is this only a hook?
  }

  /**
   * Receives notification when the <code>scrollBarDisplayPolicy</code>
   * property has changed on the scrollpane.
   *
   * @param ev the property change event
   */
  protected void updateScrollBarDisplayPolicy(PropertyChangeEvent ev)
  {
    // TODO: Find out what should be done here. Or is this only a hook?
  }

  /**
   * Receives notification when the <code>viewport</code> property has changed
   * on the scrollpane.
   *
   * This method sets removes the viewportChangeListener from the old viewport
   * and adds it to the new viewport.
   *
   * @param ev the property change event
   */
  protected void updateViewport(PropertyChangeEvent ev)
  {
    JViewport oldViewport = (JViewport) ev.getOldValue();
    oldViewport.removeChangeListener(viewportChangeListener);
    JViewport newViewport = (JViewport) ev.getNewValue();
    oldViewport.addChangeListener(viewportChangeListener);
  }
}












