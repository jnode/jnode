/* JTableHeader.java
   Copyright (C) 2003, 2004  Free Software Foundation, Inc.

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


package javax.swing.table;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleAction;
import javax.accessibility.AccessibleComponent;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleSelection;
import javax.accessibility.AccessibleStateSet;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleText;
import javax.accessibility.AccessibleValue;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.plaf.TableHeaderUI;

public class JTableHeader extends JComponent
{
  protected class AccessibleJTableHeader extends AccessibleJComponent
  {
    protected class AccessibleJTableHeaderEntry extends AccessibleContext
      implements Accessible, AccessibleComponent
    {
      public void addFocusListener(FocusListener l)
      {
        throw new Error("not implemented");
      }
      
      public void addPropertyChangeListener(PropertyChangeListener l)
      {
        throw new Error("not implemented");
      }
      
      public boolean contains(Point p)
      {
        throw new Error("not implemented");
      }
      
      public AccessibleAction getAccessibleAction()
      {
        throw new Error("not implemented");
      }
      
      public Accessible getAccessibleAt(Point p)
      {
        throw new Error("not implemented");
      }
      
      public Accessible getAccessibleChild(int i)
      {
        throw new Error("not implemented");
      }
      
      public int getAccessibleChildrenCount()
      {
        throw new Error("not implemented");
      }
      
      public AccessibleComponent getAccessibleComponent()
      {
        throw new Error("not implemented");
      }
      
      public AccessibleContext getAccessibleContext()
      {
        throw new Error("not implemented");
      }
      
      public String getAccessibleDescription()
      {
        throw new Error("not implemented");
      }
      
      public int getAccessibleIndexInParent()
      {
        throw new Error("not implemented");
      }
      
      public String getAccessibleName()
      {
        throw new Error("not implemented");
      }
      
      public AccessibleRole getAccessibleRole()
      {
        throw new Error("not implemented");
      }
      
      public AccessibleSelection getAccessibleSelection()
      {
        throw new Error("not implemented");
      }
      
      public AccessibleStateSet getAccessibleStateSet()
      {
        throw new Error("not implemented");
      }
      
      public AccessibleText getAccessibleText()
      {
        throw new Error("not implemented");
      }
      
      public AccessibleValue getAccessibleValue()
      {
        throw new Error("not implemented");
      }
      
      public Color getBackground()
      {
        throw new Error("not implemented");
      }
      
      public Rectangle getBounds()
      {
        throw new Error("not implemented");
      }
      
      public Cursor getCursor()
      {
        throw new Error("not implemented");
      }
      
      public Font getFont()
      {
        throw new Error("not implemented");
      }
      
      public FontMetrics getFontMetrics(Font f)
      {
        throw new Error("not implemented");
      }
      
      public Color getForeground()
      {
        throw new Error("not implemented");
      }
      
      public Locale getLocale()
      {
        throw new Error("not implemented");
      }
      
      public Point getLocation()
      {
        throw new Error("not implemented");
      }
      
      public Point getLocationOnScreen()
      {
        throw new Error("not implemented");
      }
      
      public Dimension getSize()
      {
        throw new Error("not implemented");
      }
      
      public boolean isEnabled()
      {
        throw new Error("not implemented");
      }
      
      public boolean isFocusTraversable()
  {
        throw new Error("not implemented");
      }
      
      public boolean isShowing()
      {
        throw new Error("not implemented");
      }
      
      public boolean isVisible()
    {
        throw new Error("not implemented");
    }
      
      public void removeFocusListener(FocusListener l)
      {
        throw new Error("not implemented");
      }
      
      public void removePropertyChangeListener(PropertyChangeListener l)
      {
        throw new Error("not implemented");
      }
      
      public void requestFocus()
      {
        throw new Error("not implemented");
      }
      
      public void setAccessibleDescription(String s)
      {
        throw new Error("not implemented");
      }
      
      public void setAccessibleName(String s)
      {
        throw new Error("not implemented");
      }
      
      public void setBackground(Color c)
      {
        throw new Error("not implemented");
      }
      
      public void setBounds(Rectangle r)
      {
        throw new Error("not implemented");
      }
      
      public void setCursor(Cursor c)
      {
        throw new Error("not implemented");
      }
      
      public void setEnabled(boolean b)
      {
        throw new Error("not implemented");
      }
      
      public void setFont(Font f)
      {
        throw new Error("not implemented");
      }
      
      public void setForeground(Color c)
      {
        throw new Error("not implemented");
      }
      
      public void setLocation(Point p)
      {
        throw new Error("not implemented");
      }
      
      public void setSize(Dimension d)
      {
        throw new Error("not implemented");
      }
      
      public void setVisible(boolean b)
      {
        throw new Error("not implemented");
      }
    };
  }

  private static final long serialVersionUID = 5144633983372967710L;

  /**
   * The accessibleContext property.
   */
  AccessibleContext accessibleContext;

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

  public JTableHeader()
  {
    this(null);
  }

  public JTableHeader(TableColumnModel cm)
  {
    accessibleContext = new AccessibleJTableHeader();
    columnModel = cm == null ? createDefaultColumnModel() : cm; 
    draggedColumn = null;
    draggedDistance = 0;
    opaque = true;
    reorderingAllowed = true;
    resizingAllowed = true;
    resizingColumn = null;
    table = null;
    updateTableInRealTime = true;
    cellRenderer = createDefaultRenderer();
    updateUI();
  }

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
   * Get the value of the {@link #draggedColumn} property.
   *
   * @return The current value of the property
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
   * Get the value of the {@link #reorderingAllowed} property.
   *
   * @return The current value of the property
   */
  public boolean getReorderingAllowed()
  {
    return reorderingAllowed;
  }

  /**
   * Get the value of the {@link #resizingAllowed} property.
   *
   * @return The current value of the property
   */
  public boolean getResizingAllowed()
  {
    return resizingAllowed;
  }

  /**
   * Get the value of the {@link #resizingColumn} property.
   *
   * @return The current value of the property
   */
  public TableColumn getResizingColumn()
  {
    return resizingColumn;
  }

  /**
   * Get the value of the {@link #table} property.
   *
   * @return The current value of the property
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
    columnModel = c;
  }

  /**
   * Set the value of the {@link #draggedColumn} property.
   *
   * @param d The new value of the property
   */ 
  public void setDraggedColumn(TableColumn d)
  {
    draggedColumn = d;
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
   * Set the value of the {@link #reorderingAllowed} property.
   *
   * @param r The new value of the property
   */ 
  public void setReorderingAllowed(boolean r)
  {
    reorderingAllowed = r;
  }

  /**
   * Set the value of the {@link #resizingAllowed} property.
   *
   * @param r The new value of the property
   */ 
  public void setResizingAllowed(boolean r)
  {
    resizingAllowed = r;
  }

  /**
   * Set the value of the {@link #resizingColumn} property.
   *
   * @param r The new value of the property
   */ 
  public void setResizingColumn(TableColumn r)
  {
    resizingColumn = r;
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

  protected TableCellRenderer createDefaultRenderer()
  {
    return new DefaultTableCellRenderer();
  }

  public TableCellRenderer getDefaultRenderer()
  {
    return cellRenderer;
  }

  public void setDefaultRenderer(TableCellRenderer cellRenderer)
  {
    this.cellRenderer = cellRenderer;
  }

  public Rectangle getHeaderRect(int column)
  {
    Rectangle r = getTable().getCellRect(-1, column, true);
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

}
