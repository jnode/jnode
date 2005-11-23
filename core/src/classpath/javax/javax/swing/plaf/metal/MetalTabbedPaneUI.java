/* MetalTabbedPaneUI.java
   Copyright (C) 2005 Free Software Foundation, Inc.

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


package javax.swing.plaf.metal;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/**
 * A UI delegate for the {@link JTabbedPane} component.
 */
public class MetalTabbedPaneUI extends BasicTabbedPaneUI
{

  /**
   * A {@link LayoutManager} responsible for placing all the tabs and the 
   * visible component inside the {@link JTabbedPane}. This class is only used 
   * for {@link JTabbedPane#WRAP_TAB_LAYOUT}.
   *
   * @specnote Apparently this class was intended to be protected,
   *           but was made public by a compiler bug and is now
   *           public for compatibility.
   */
  public class TabbedPaneLayout 
      extends BasicTabbedPaneUI.TabbedPaneLayout
  {
    /**
     * Creates a new instance of the layout manager.
     */
    public TabbedPaneLayout()
    {
      // Nothing to do here.
    }
    
    /**
     * Overridden to do nothing, because tab runs are not rotated in the 
     * {@link MetalLookAndFeel}.
     * 
     * @param tabPlacement  the tab placement (one of {@link #TOP}, 
     *        {@link #BOTTOM}, {@link #LEFT} or {@link #RIGHT}).
     * @param selectedRun  the index of the selected run.
     */
    protected void rotateTabRuns(int tabPlacement, int selectedRun)
    {
      // do nothing, because tab runs are not rotated in the MetalLookAndFeel
    }
    
    /**
     * Overridden to do nothing, because the selected tab does not have extra
     * padding in the {@link MetalLookAndFeel}.
     * 
     * @param tabPlacement  the tab placement (one of {@link #TOP}, 
     *        {@link #BOTTOM}, {@link #LEFT} or {@link #RIGHT}).
     * @param selectedIndex  the index of the selected tab.
     */
    protected void padSelectedTab(int tabPlacement, int selectedIndex)
    {
      // do nothing, because the selected tab does not have extra padding in 
      // the MetalLookAndFeel
    }
  }

  /**
   * The minimum tab width.
   */
  protected int minTabWidth;

  /**
   * The color for the selected tab.
   */
  protected Color selectColor;

  /**
   * The color for a highlighted selected tab.
   */
  protected Color selectHighlight;

  /**
   * The background color used for the tab area.
   */
  protected Color tabAreaBackground;

  /**
   * Constructs a new instance of MetalTabbedPaneUI.
   */
  public MetalTabbedPaneUI()
  {
    super();
  }

  /**
   * Returns an instance of MetalTabbedPaneUI.
   *
   * @param component the component for which we return an UI instance
   *
   * @return an instance of MetalTabbedPaneUI
   */
  public static ComponentUI createUI(JComponent component)
  {
    return new MetalTabbedPaneUI();
  }
  
  /**
   * Creates and returns an instance of {@link TabbedPaneLayout}.
   * 
   * @return A layout manager used by this UI delegate.
   */
  protected LayoutManager createLayoutManager()
  {
    return super.createLayoutManager();
  }
  
  /**
   * Paints the border for a single tab.
   * 
   * @param g  the graphics device.
   * @param tabPlacement  the tab placement ({@link #TOP}, {@link #LEFT}, 
   *        {@link #BOTTOM} or {@link #RIGHT}).
   * @param tabIndex  the index of the tab to draw the border for.
   * @param x  the x-coordinate for the tab's bounding rectangle.
   * @param y  the y-coordinate for the tab's bounding rectangle.
   * @param w  the width for the tab's bounding rectangle.
   * @param h  the height for the tab's bounding rectangle.
   * @param isSelected  indicates whether or not the tab is selected.
   */
  protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, 
          int x, int y, int w, int h, boolean isSelected) 
  {
    if (tabPlacement == TOP)
      paintTopTabBorder(tabIndex, g, x, y, w, h, 0, 0, isSelected);
    else if (tabPlacement == LEFT) 
      paintLeftTabBorder(tabIndex, g, x, y, w, h, 0, 0, isSelected);
    else if (tabPlacement == BOTTOM)
      paintBottomTabBorder(tabIndex, g, x, y, w, h, 0, 0, isSelected);
    else if (tabPlacement == RIGHT)
      paintRightTabBorder(tabIndex, g, x, y, w, h, 0, 0, isSelected);
    else 
      throw new AssertionError("Unrecognised 'tabPlacement' argument.");
  }

  /**
   * Paints the border for a tab assuming that the tab position is at the top
   * ({@link #TOP}).
   * 
   * @param tabIndex  the tab index.
   * @param g  the graphics device.
   * @param x  the x-coordinate for the tab's bounding rectangle.
   * @param y  the y-coordinate for the tab's bounding rectangle.
   * @param w  the width for the tab's bounding rectangle.
   * @param h  the height for the tab's bounding rectangle.
   * @param btm  ???
   * @param rght  ???
   * @param isSelected  indicates whether the tab is selected.
   */
  protected void paintTopTabBorder(int tabIndex, Graphics g, int x, int y,
      int w, int h, int btm, int rght, boolean isSelected)
  {
    if (isSelected)
    {
      g.setColor(MetalLookAndFeel.getControlHighlight());
      g.drawLine(x + 1, y + h, x + 1, y + 6);      
      g.drawLine(x + 1, y + 6, x + 6, y + 1);
      g.drawLine(x + 6, y + 1, x + w - 1, y + 1);
    }
    g.setColor(MetalLookAndFeel.getControlDarkShadow());
    g.drawLine(x, y + h - 1, x, y + 6);
    g.drawLine(x, y + 6, x + 6, y);
    g.drawLine(x + 6, y, x + w, y);
    g.drawLine(x + w, y, x + w, y + h - 1);
  }
  
  /**
   * Paints the border for a tab assuming that the tab position is at the left
   * ({@link #LEFT}).
   * 
   * @param tabIndex  the tab index.
   * @param g  the graphics device.
   * @param x  the x-coordinate for the tab's bounding rectangle.
   * @param y  the y-coordinate for the tab's bounding rectangle.
   * @param w  the width for the tab's bounding rectangle.
   * @param h  the height for the tab's bounding rectangle.
   * @param btm  ???
   * @param rght  ???
   * @param isSelected  indicates whether the tab is selected.
   */
  protected void paintLeftTabBorder(int tabIndex, Graphics g, int x, int y,
      int w, int h, int btm, int rght, boolean isSelected)
  {
    if (isSelected)
    {
      g.setColor(MetalLookAndFeel.getControlHighlight());
      g.drawLine(x + 1, y + h, x + 1, y + 6);      
      g.drawLine(x + 1, y + 6, x + 6, y + 1);
      g.drawLine(x + 6, y + 1, x + w - 1, y + 1);
    }
    g.setColor(MetalLookAndFeel.getControlDarkShadow());
    g.drawLine(x, y + h, x, y + 6);
    g.drawLine(x, y + 6, x + 6, y);
    g.drawLine(x + 6, y, x + w - 1, y);
    g.drawLine(x, y + h, x + w - 1, y + h);
  }
  
  /**
   * Paints the border for a tab assuming that the tab position is at the right
   * ({@link #RIGHT}).
   * 
   * @param tabIndex  the tab index.
   * @param g  the graphics device.
   * @param x  the x-coordinate for the tab's bounding rectangle.
   * @param y  the y-coordinate for the tab's bounding rectangle.
   * @param w  the width for the tab's bounding rectangle.
   * @param h  the height for the tab's bounding rectangle.
   * @param btm  ???
   * @param rght  ???
   * @param isSelected  indicates whether the tab is selected.
   */
  protected void paintRightTabBorder(int tabIndex, Graphics g, int x, int y,
      int w, int h, int btm, int rght, boolean isSelected)
  {
    if (isSelected)
    {
      g.setColor(MetalLookAndFeel.getControlHighlight());
      g.drawLine(x, y + 1, x + w - 7, y + 1);      
      g.drawLine(x + w - 7, y + 1, x + w - 1, y + 7);
    }
    g.setColor(MetalLookAndFeel.getControlDarkShadow());
    g.drawLine(x, y, x + w - 7, y);
    g.drawLine(x + w - 7, y, x + w - 1, y + 6);
    g.drawLine(x + w - 1, y + 6, x + w - 1, y + h - 1);
    g.drawLine(x + w - 1, y + h, x, y + h);
  }
  
  /**
   * Paints the border for a tab assuming that the tab position is at the bottom
   * ({@link #BOTTOM}).
   * 
   * @param tabIndex  the tab index.
   * @param g  the graphics device.
   * @param x  the x-coordinate for the tab's bounding rectangle.
   * @param y  the y-coordinate for the tab's bounding rectangle.
   * @param w  the width for the tab's bounding rectangle.
   * @param h  the height for the tab's bounding rectangle.
   * @param btm  ???
   * @param rght  ???
   * @param isSelected  indicates whether the tab is selected.
   */
  protected void paintBottomTabBorder(int tabIndex, Graphics g, int x, int y,
      int w, int h, int btm, int rght, boolean isSelected)
  {
    if (isSelected)
    {
      g.setColor(MetalLookAndFeel.getControlHighlight());
      g.drawLine(x + 1, y, x + 1, y + h - 7);      
      g.drawLine(x + 1, y + h - 7, x + 7, y + h - 1);
    }
    g.setColor(MetalLookAndFeel.getControlDarkShadow());
    g.drawLine(x, y, x, y + h - 7);
    g.drawLine(x, y + h - 7, x + 6, y + h - 1);
    g.drawLine(x + 6, y + h - 1, x + w, y + h - 1);
    g.drawLine(x + w, y + h - 1, x + w, y);
  }

  /**
   * Paints the background for a tab.
   * 
   * @param g  the graphics device.
   * @param tabPlacement  the tab placement ({@link #TOP}, {@link #LEFT}, 
   *        {@link #BOTTOM} or {@link #RIGHT}).
   * @param tabIndex  the index of the tab to draw the border for.
   * @param x  the x-coordinate for the tab's bounding rectangle.
   * @param y  the y-coordinate for the tab's bounding rectangle.
   * @param w  the width for the tab's bounding rectangle.
   * @param h  the height for the tab's bounding rectangle.
   * @param isSelected  indicates whether or not the tab is selected.
   */
  protected void paintTabBackground(Graphics g, int tabPlacement,
      int tabIndex, int x, int y, int w, int h, boolean isSelected)
  {
    if (isSelected)
      g.setColor(UIManager.getColor("TabbedPane.selected"));
    else
      {
        // This is only present in the OceanTheme, so we must check if it
        // is actually there
        Color background = UIManager.getColor("TabbedPane.unselectedBackground");
        if (background == null)
          background = UIManager.getColor("TabbedPane.background");
        g.setColor(background);
      }
    int[] px, py;
    if (tabPlacement == TOP) 
      {
        px = new int[] {x + 6, x + w - 1, x + w -1, x + 2, x + 2};
        py = new int[] {y + 2, y + 2, y + h - 1, y + h -1, y + 6};
      }
    else if (tabPlacement == LEFT)
      {
        px = new int[] {x + 6, x + w - 1, x + w -1, x + 2, x + 2};
        py = new int[] {y + 2, y + 2, y + h - 1, y + h -1, y + 6};
      }
    else if (tabPlacement == BOTTOM)
      {
        px = new int[] {x + 2, x + w - 1, x + w -1, x + 8, x + 2};
        py = new int[] {y, y, y + h - 1, y + h -1, y + h - 7};
      }
    else if (tabPlacement == RIGHT)
      {
        px = new int[] {x + 2, x + w - 7, x + w - 1, x + w - 1, x + 2};
        py = new int[] {y + 2, y + 2, y + 7, y + h -1, y + h - 1};
      }
    else 
      throw new AssertionError("Unrecognised 'tabPlacement' argument.");
    g.fillPolygon(px, py, 5);
  }
  
  /**
   * Returns <code>true</code> if the tabs in the specified run should be 
   * padded to make the run fill the width/height of the {@link JTabbedPane}.
   * 
   * @param tabPlacement  the tab placement for the {@link JTabbedPane} (one of
   *        {@link #TOP}, {@link #BOTTOM}, {@link #LEFT} and {@link #RIGHT}).
   * @param run  the run index.
   * 
   * @return A boolean.
   */
  protected boolean shouldPadTabRun(int tabPlacement, int run)
  {
    // as far as I can tell, all runs should be padded except the last run
    // (which is drawn at the very top for tabPlacement == TOP)
    return run < this.runCount - 1;
  }
  
  /**
   * Installs the defaults for this UI. This method calls super.installDefaults
   * and then loads the Metal specific defaults for TabbedPane.
   */
  protected void installDefaults()
  {
    super.installDefaults();
    selectColor = UIManager.getColor("TabbedPane.selected");
    selectHighlight = UIManager.getColor("TabbedPane.selectHighlight");
    tabAreaBackground = UIManager.getColor("TabbedPane.tabAreaBackground");
    minTabWidth = 0;
  }
  
  /**
   * Returns the color for the gap.
   * 
   * @param currentRun - The current run to return the color for
   * @param x - The x position of the current run
   * @param y - The y position of the current run
   * 
   * @return the color for the gap in the current run.
   */
  protected Color getColorForGap(int currentRun, int x, int y)
  {
    return tabAreaBackground;
  }
  
  /**
   * Paints the highlight below the tab, if there is one.
   */
  protected void paintHighlightBelowTab()
  {
    // Nothing to do here, no highlight below the tab as far
    // as I can see.
  }
  
  /**
   * Returns true if the gap should be filled in.
   * 
   * @param currentRun - The current run
   * @param tabIndex - The current tab
   * @param x - The x position of the tab
   * @param y - The y position of the tab
   * 
   * @return true if the gap at the current run should be filled 
   */
  protected boolean shouldFillGap(int currentRun, int tabIndex,
                                  int x, int y)
  {
    return true;
  }
  
  /**
   * Returns true if we should rotate the tab runs. 
   * 
   * @param tabPlacement - The current tab placement.
   * @param selectedRun - The selected run.
   * 
   * @return true if the tab runs should be rotated.
   */
  protected boolean shouldRotateTabRuns(int tabPlacement,
                                        int selectedRun)
  {
    // false because tab runs are not rotated in the MetalLookAndFeel
    return false;
  }
  
  /**
   * This method returns the max tab height.
   *
   * @param tabPlacement The JTabbedPane's tab placement.
   *
   * @return The maximum tab height.
   */
  protected int calculateMaxTabHeight(int tabPlacement)
  {
    return super.calculateMaxTabHeight(tabPlacement);
  }
  
  /**
   * This method returns how much the label for the tab should shift in the X
   * direction.
   *
   * @param tabPlacement The JTabbedPane's tab placement.
   * @param tabIndex The tab index being painted.
   * @param isSelected Whether this tab is selected.
   *
   * @return The amount the label should shift by in the X direction.
   */
  protected int getTabLabelShiftX(int tabPlacement, int tabIndex,
                                  boolean isSelected)
  {
    // No reason to shift.
    return 0;
  }
  
  /**
   * This method returns how much the label for the tab should shift in the Y
   * direction.
   *
   * @param tabPlacement The JTabbedPane's tab placement.
   * @param tabIndex The tab index being painted.
   * @param isSelected Whether this tab is selected.
   *
   * @return The amount the label should shift by in the Y direction.
   */
  protected int getTabLabelShiftY(int tabPlacement, int tabIndex,
                                  boolean isSelected)
  {
    // No reason to shift.
    return 0;
  }
  
  /**
   * This method returns the tab run overlay.
   *
   * @param tabPlacement The JTabbedPane's tab placement.
   *
   * @return The tab run overlay.
   */
  protected int getTabRunOverlay(int tabPlacement)
  {
    tabRunOverlay = UIManager.getInt("TabbedPane.tabRunOverlay");
    return tabRunOverlay;
  }
  
  /**
   * This method paints the JTabbedPane.
   *
   * @param g The Graphics object to paint with.
   * @param c The JComponent to paint.
   */
  public void paint(Graphics g, JComponent c)
  {
    super.paint(g, c);
  }
  
  /**
   * This method paints the bottom edge of the content border.
   *
   * @param g The Graphics object to paint with.
   * @param tabPlacement The JTabbedPane's tab placement.
   * @param selectedIndex The selected tab index.
   * @param x The x coordinate for the content area.
   * @param y The y coordinate for the content area.
   * @param w The width of the content area.
   * @param h The height of the content area.
   */
  protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement,
                                              int selectedIndex, int x, int y,
                                              int w, int h)
  {
    super.paintContentBorderBottomEdge(g, tabPlacement, selectedIndex, 
                                       x, y, w, h);
  }
  
  /**
   * This method paints the left edge of the content border.
   *
   * @param g The Graphics object to paint with.
   * @param tabPlacement The JTabbedPane's tab placement.
   * @param selectedIndex The selected tab index.
   * @param x The x coordinate for the content area.
   * @param y The y coordinate for the content area.
   * @param w The width of the content area.
   * @param h The height of the content area.
   */
  protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement,
                                            int selectedIndex, int x, int y,
                                            int w, int h)
  {
    super.paintContentBorderLeftEdge(g, tabPlacement, selectedIndex, 
                                       x, y, w, h);
  }
  
  /**
   * This method paints the right edge of the content border.
   *
   * @param g The Graphics object to paint with.
   * @param tabPlacement The JTabbedPane's tab placement.
   * @param selectedIndex The selected tab index.
   * @param x The x coordinate for the content area.
   * @param y The y coordinate for the content area.
   * @param w The width of the content area.
   * @param h The height of the content area.
   */
  protected void paintContentBorderRightEdge(Graphics g, int tabPlacement,
                                             int selectedIndex, int x, int y,
                                             int w, int h)
  {
    super.paintContentBorderRightEdge(g, tabPlacement, selectedIndex, 
                                      x, y, w, h);
  }
  
  /**
   * This method paints the top edge of the content border.
   *
   * @param g The Graphics object to paint with.
   * @param tabPlacement The JTabbedPane's tab placement.
   * @param selectedIndex The selected tab index.
   * @param x The x coordinate for the content area.
   * @param y The y coordinate for the content area.
   * @param w The width of the content area.
   * @param h The height of the content area.
   */
  protected void paintContentBorderTopEdge(Graphics g, int tabPlacement,
                                           int selectedIndex, int x, int y,
                                           int w, int h)
  {
    super.paintContentBorderTopEdge(g, tabPlacement, selectedIndex, 
                                      x, y, w, h);
  }
  
  /**
   * This method paints the focus rectangle around the selected tab.
   *
   * @param g The Graphics object to paint with.
   * @param tabPlacement The JTabbedPane's tab placement.
   * @param rects The array of rectangles keeping track of size and position.
   * @param tabIndex The tab index.
   * @param iconRect The icon bounds.
   * @param textRect The text bounds.
   * @param isSelected Whether this tab is selected.
   */
  protected void paintFocusIndicator(Graphics g, int tabPlacement,
                                     Rectangle[] rects, int tabIndex,
                                     Rectangle iconRect, Rectangle textRect,
                                     boolean isSelected)
  {
    super.paintFocusIndicator(g, tabPlacement, rects, tabIndex, 
                              iconRect, textRect, isSelected);
  }
  
  /**
   * Notifies this UI delegate that it's time to paint the specified 
   * component. This method is invoked by JComponent when the specified 
   * component is being painted. By default this method will fill the 
   * specified component with its background color (if its opaque property
   * is true) and then immediately call paint. In general this method need 
   * not be overridden by subclasses; all look-and-feel rendering code should 
   * reside in the paint method.
   * 
   * @param g - the Graphics context in which to paint
   * @param c - the component being painted; this argument 
   * is often ignored, but might be used if the UI object is 
   * stateless and shared by multiple components
   */
  public void update(Graphics g, JComponent c)
  {
    super.update(g, c);
  }
}
