/* JTableHeader.java --
   Copyright (C) 2003, 2004, 2005  Free Software Foundation, Inc.

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


package javax.swing.table;

import gnu.classpath.NotImplementedException;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeListener;
import java.util.Locale;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleAction;
import javax.accessibility.AccessibleComponent;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleSelection;
import javax.accessibility.AccessibleStateSet;
import javax.accessibility.AccessibleText;
import javax.accessibility.AccessibleValue;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.plaf.TableHeaderUI;

/**
 * Represents the table header. The header displays the column header values,
 * is always visible event if the rest of the table scrolls up and down and
 * supports column reordering and resizing with mouse.
 */
public class JTableHeader extends JComponent
  implements TableColumnModelListener, Accessible
{
  protected class AccessibleJTableHeader extends AccessibleJComponent
  {
    protected class AccessibleJTableHeaderEntry extends AccessibleContext
      implements Accessible, AccessibleComponent
    {
      public AccessibleJTableHeaderEntry(int c, JTableHeader p, JTable t) 
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public void addFocusListener(FocusListener l)
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public void addPropertyChangeListener(PropertyChangeListener l)
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public boolean contains(Point p)
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public AccessibleAction getAccessibleAction()
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public Accessible getAccessibleAt(Point p)
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public Accessible getAccessibleChild(int i)
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public int getAccessibleChildrenCount()
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public AccessibleComponent getAccessibleComponent()
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public AccessibleContext getAccessibleContext()
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public String getAccessibleDescription()
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public int getAccessibleIndexInParent()
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public String getAccessibleName()
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public AccessibleRole getAccessibleRole()
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public AccessibleSelection getAccessibleSelection()
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public AccessibleStateSet getAccessibleStateSet()
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public AccessibleText getAccessibleText()
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public AccessibleValue getAccessibleValue()
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public Color getBackground()
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public Rectangle getBounds()
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public Cursor getCursor()
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public Font getFont()
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public FontMetrics getFontMetrics(Font f)
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public Color getForeground()
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public Locale getLocale()
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public Point getLocation()
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public Point getLocationOnScreen()
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public Dimension getSize()
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public boolean isEnabled()
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public boolean isFocusTraversable()
        throws NotImplementedException
  {
        throw new Error("not implemented");
      }
      
      public boolean isShowing()
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public boolean isVisible()
        throws NotImplementedException
    {
        throw new Error("not implemented");
    }
      
      public void removeFocusListener(FocusListener l)
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public void removePropertyChangeListener(PropertyChangeListener l)
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public void requestFocus()
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public void setAccessibleDescription(String s)
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public void setAccessibleName(String s)
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public void setBackground(Color c)
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public void setBounds(Rectangle r)
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public void setCursor(Cursor c)
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public void setEnabled(boolean b)
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public void setFont(Font f)
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public void setForeground(Color c)
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public void setLocation(Point p)
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public void setSize(Dimension d)
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
      
      public void setVisible(boolean b)
        throws NotImplementedException
      {
        throw new Error("not implemented");
      }
    };
  }

  /**
   * Use serialVersionUid for interoperability.
   */
  private static final long serialVersionUID = 5144633983372967710L;

  /**
   * The columnModel property.
   */
  protected TableColumnModel columnModel;

  /**
   * The draggedColumn property.
   */
  protected TableColumn draggedColumn;

  /**
   * The draggedDistance property.
   */
  protected int draggedDistance;

  /**
   * The opaque property.
   */
  boolean opaque;

  /**
   * The reorderingAllowed property.
   */
  protected boolean reorderingAllowed;

  /**
   * The resizingAllowed property.
   */
  protected boolean resizingAllowed = true;

  /**
   * The resizingColumn property.
   */
  protected TableColumn resizingColumn;

  /**
   * The table property.
   */
  protected JTable table;

  /**
   * The updateTableInRealTime property.
   */
  protected boolean updateTableInRealTime;

  TableCellRenderer cellRenderer; 

  /**
   * Creates a new default instance.
   */
  public JTableHeader()
  {
    this(null);
  }

  /**
   * Creates a new header.  If <code>cm</code> is <code>null</code>, a new
   * table column model is created by calling 
   * {@link #createDefaultColumnModel()}.
   * 
   * @param cm  the table column model (<code>null</code> permitted).
   */
  public JTableHeader(TableColumnModel cm)
  {
    columnModel = cm == null ? createDefaultColumnModel() : cm; 
    initializeLocalVars();
    updateUI();
  }

  /**
   * Creates a default table column model.
   * 
   * @return A default table column model.
   */
  protected TableColumnModel createDefaultColumnModel()
  {
    return new DefaultTableColumnModel();
  }

  /**
   * Get the value of the {@link #accessibleContext} property.
   *
   * @return The current value of the property
   */
  public AccessibleContext getAccessibleContext()
  {
    return accessibleContext;
  }

  /**
   * Get the value of the {@link #columnModel} property.
   *
   * @return The current value of the property
   */
  public TableColumnModel getColumnModel()
  {
    return columnModel;
  }

  /**
   * Get the column that is currently being dragged. This is used when
   * handling the column reordering with mouse.
   *
   * @return the column being dragged, null if none.
   */
  public TableColumn getDraggedColumn()
  {
    return draggedColumn;
  }

  /**
   * Get the value of the {@link #draggedDistance} property.
   *
   * @return The current value of the property
   */
  public int getDraggedDistance()
  {
    return draggedDistance;
  }

  /**
   * Check if it is possible to reorder the table columns by dragging column
   * header with mouse. The table reordering is enabled by default, but can be
   * disabled with {@link #setReorderingAllowed(boolean)}.
   *
   * @return true if reordering is allowed, false otherwise.
   */
  public boolean getReorderingAllowed()
  {
    return reorderingAllowed;
  }

  /**
   * Check if it is possible to resize the table columns by dragging the column
   * boundary in the table header with mouse. The resizing is enabled
   * by default, but can be disabled with {@link #setResizingAllowed(boolean)}.
   *
   * @return true if resizing is allowed, false otherwise.
   */
  public boolean getResizingAllowed()
  {
    return resizingAllowed;
  }

  /**
   * Get the column that is currently being resized. This is used when
   * handling the column resizing with mouse.
   *
   * @return the column being currently resized, null if none.
   */
  public TableColumn getResizingColumn()
  {
    return resizingColumn;
  }

  /**
   * Get the table, having this header.
   *
   * @return the table, having this header.
   */
  public JTable getTable()
  {
    return table;
  }

  /**
   * Get the value of the {@link #updateTableInRealTime} property.
   *
   * @return The current value of the property
   */
  public boolean getUpdateTableInRealTime()
  {
    return updateTableInRealTime;
  }

  /**
   * Get the value of the {@link #opaque} property.
   *
   * @return The current value of the property
   */
  public boolean isOpaque()
  {
    return opaque;
  }

  /**
   * Set the value of the {@link #columnModel} property.
   *
   * @param c The new value of the property
   */ 
  public void setColumnModel(TableColumnModel c)
  {
    columnModel.removeColumnModelListener(this);
    columnModel = c;
    columnModel.addColumnModelListener(this);
  }

  /**
   * Set the column that is currently being dragged. This is used when
   * dragging the column with mouse. Setting to null will stop the 
   * dragging session immediately.
   *
   * @param draggingIt the column being currently dragged, null if none.
   */ 
  public void setDraggedColumn(TableColumn draggingIt)
  {
    draggedColumn = draggingIt;
  }

  /**
   * Set the value of the {@link #draggedDistance} property.
   *
   * @param d The new value of the property
   */ 
  public void setDraggedDistance(int d)
  {
    draggedDistance = d;
  }

  /**
   * Set the value of the {@link #opaque} property.
   *
   * @param o The new value of the property
   */ 
  public void setOpaque(boolean o)
  {
    opaque = o;
  }

  /**
   * Set the table ability to reorder columns by dragging column header
   * with mouse. The table reordering is enabled by default, but can be
   * disabled with this method.
   *
   * @param allowed true if reordering is allowed, false otherwise.
   */ 
  public void setReorderingAllowed(boolean allowed)
  {
    reorderingAllowed = allowed;
  }

  /**
   * Set the table ability to resize columns by dragging the column
   * boundary in the table header with mouse. The resizing is enabled
   * by default, but can be disabled using this method.
   *
   * @param allowed true if resizing is allowed, false otherwise.
   */ 
  public void setResizingAllowed(boolean allowed)
  {
    resizingAllowed = allowed;
  }

  /**
   * The the column that is currently being resized. This property is used
   * when handling table resizing with mouse. Setting to null would stop
   * the resizing session immediately.
   *
   * @param resizingIt the column being currently resized
   */ 
  public void setResizingColumn(TableColumn resizingIt)
  {
    resizingColumn = resizingIt;
  }

  /**
   * Set the value of the {@link #table} property.
   *
   * @param t The new value of the property
   */ 
  public void setTable(JTable t)
  {
    table = t;
  }

  /**
   * Set the value of the {@link #updateTableInRealTime} property.
   *
   * @param u The new value of the property
   */ 
  public void setUpdateTableInRealTime(boolean u)
  {
    updateTableInRealTime = u;
  }

  /**
   * Creates a default renderer.
   * 
   * @return A default renderer.
   */
  protected TableCellRenderer createDefaultRenderer()
  {
    return new DefaultTableCellRenderer();
  }

  /**
   * Returns the default table cell renderer.
   * 
   * @return The default table cell renderer.
   */
  public TableCellRenderer getDefaultRenderer()
  {
    return cellRenderer;
  }

  /**
   * Sets the default table cell renderer.
   * 
   * @param cellRenderer  the renderer.
   */
  public void setDefaultRenderer(TableCellRenderer cellRenderer)
  {
    this.cellRenderer = cellRenderer;
  }

  /**
   * Get the rectangle, occupied by the header of the given column.
   * 
   * @param column the column, for that the header area is requested.
   * 
   * @return the column header area.
   */
  public Rectangle getHeaderRect(int column)
  {
    Rectangle r = getTable().getCellRect(-1, column, false);
    r.height = getHeight();
    return r;
  }

  protected String paramString()
  {
    return "JTableHeader";
  }

  // UI support

  public String getUIClassID()
  {
    return "TableHeaderUI";
  }

  public TableHeaderUI getUI()
  {
    return (TableHeaderUI) ui;
  }

  public void setUI(TableHeaderUI u)
  {
    super.setUI(u);
  }

  public void updateUI()
  {
    setUI((TableHeaderUI) UIManager.getUI(this));
  }

  /**
   * Returns the index of the column at the specified point.
   * 
   * @param point  the point.
   * 
   * @return The column index, or -1.
   */
  public int columnAtPoint(Point point)
  {
    if (getBounds().contains(point))
      return columnModel.getColumnIndexAtX(point.x);
    
    return -1;
  }

  /**
   * Receives notification when a column is added to the column model.
   *
   * @param event the table column model event
   */
  public void columnAdded(TableColumnModelEvent event)
  {
    // TODO: What else to do here (if anything)?
    resizeAndRepaint();
  }

  /**
   * Receives notification when a column margin changes in the column model.
   *
   * @param event the table column model event
   */
  public void columnMarginChanged(ChangeEvent event)
  {
    // TODO: What else to do here (if anything)?
    resizeAndRepaint();
  }

  /**
   * Receives notification when a column is moved within the column model.
   *
   * @param event the table column model event
   */
  public void columnMoved(TableColumnModelEvent event)
  {
    // TODO: What else to do here (if anything)?
    resizeAndRepaint();
  }

  /**
   * Receives notification when a column is removed from the column model.
   *
   * @param event the table column model event
   */
  public void columnRemoved(TableColumnModelEvent event)
  {
    // TODO: What else to do here (if anything)?
    resizeAndRepaint();
  }

  /**
   * Receives notification when the column selection has changed.
   *
   * @param event the table column model event
   */
  public void columnSelectionChanged(ListSelectionEvent event)
  {
    // TODO: What else to do here (if anything)?
    resizeAndRepaint();
  }

  /**
   * Validates the layout of this table header and repaints it. This is
   * equivalent to <code>revalidate()</code> followed by
   * <code>repaint()</code>.
   */
  public void resizeAndRepaint()
  {
    revalidate();
    repaint();
  }

  /**
   * Initializes the fields and properties of this class with default values.
   * This is called by the constructors.
   */
  protected void initializeLocalVars()
  {
    accessibleContext = new AccessibleJTableHeader();
    draggedColumn = null;
    draggedDistance = 0;
    opaque = true;
    reorderingAllowed = true;
    resizingAllowed = true;
    resizingColumn = null;
    table = null;
    updateTableInRealTime = true;
    cellRenderer = createDefaultRenderer();
  }
}
