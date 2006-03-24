/* BasicTabbedPaneUI.java --
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.PanelUI;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.plaf.UIResource;
import javax.swing.text.View;

/**
 * This is the Basic Look and Feel's UI delegate for JTabbedPane.
 */
public class BasicTabbedPaneUI extends TabbedPaneUI implements SwingConstants
{
  /**
   * A helper class that handles focus.
   *
   * @specnote Apparently this class was intended to be protected,
   *           but was made public by a compiler bug and is now
   *           public for compatibility.
   */
  public class FocusHandler extends FocusAdapter
  {
    /**
     * This method is called when the component gains focus.
     *
     * @param e The FocusEvent.
     */
    public void focusGained(FocusEvent e)
    {
      // FIXME: Implement.
    }

    /**
     * This method is called when the component loses focus.
     *
     * @param e The FocusEvent.
     */
    public void focusLost(FocusEvent e)
    {
      // FIXME: Implement.
    }
  }

  /**
   * A helper class for determining if mouse presses occur inside tabs and
   * sets the index appropriately. In SCROLL_TAB_MODE, this class also
   * handles the mouse clicks on the scrolling buttons.
   *
   * @specnote Apparently this class was intended to be protected,
   *           but was made public by a compiler bug and is now
   *           public for compatibility.
   */
  public class MouseHandler extends MouseAdapter
  {
    /**
     * This method is called when the mouse is pressed. The index cannot
     * change to a tab that is  not enabled.
     *
     * @param e The MouseEvent.
     */
    public void mousePressed(MouseEvent e)
    {
      if (tabPane.isEnabled())
        {
          int index = tabForCoordinate(tabPane, e.getX(), e.getY());
          if (index >= 0 && tabPane.isEnabledAt(index))
            {
              tabPane.setSelectedIndex(index);
            }
            }
        }

    /**
     * Receives notification when the mouse pointer has entered the tabbed
     * pane.
     *
     * @param ev the mouse event
     */
    public void mouseEntered(MouseEvent ev)
    {
      int tabIndex = tabForCoordinate(tabPane, ev.getX(), ev.getY());
      setRolloverTab(tabIndex);
    }

    /**
     * Receives notification when the mouse pointer has exited the tabbed
     * pane.
     *
     * @param ev the mouse event
     */
    public void mouseExited(MouseEvent ev)
    {
      setRolloverTab(-1);
    }

    /**
     * Receives notification when the mouse pointer has moved over the tabbed
     * pane.
     *
     * @param ev the mouse event
     */
    public void mouseMoved(MouseEvent ev)
    {
      int tabIndex = tabForCoordinate(tabPane, ev.getX(), ev.getY());
      setRolloverTab(tabIndex);
    }
  }

  /**
   * This class handles PropertyChangeEvents fired from the JTabbedPane.
   *
   * @specnote Apparently this class was intended to be protected,
   *           but was made public by a compiler bug and is now
   *           public for compatibility.
   */
  public class PropertyChangeHandler implements PropertyChangeListener
  {
    /**
     * This method is called whenever one of the properties of the JTabbedPane
     * changes.
     *
     * @param e The PropertyChangeEvent.
     */
    public void propertyChange(PropertyChangeEvent e)
    {
      if (e.getPropertyName().equals("tabLayoutPolicy"))
        {
          layoutManager = createLayoutManager();
          
          tabPane.setLayout(layoutManager);
        }
      else if (e.getPropertyName().equals("tabPlacement")
          && tabPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT)
        {
          incrButton = createIncreaseButton();
          decrButton = createDecreaseButton();
        }
      tabPane.revalidate();
      tabPane.repaint();
    }
  }

  /**
   * A LayoutManager responsible for placing all the tabs and the visible
   * component inside the JTabbedPane. This class is only used for
   * WRAP_TAB_LAYOUT.
   *
   * @specnote Apparently this class was intended to be protected,
   *           but was made public by a compiler bug and is now
   *           public for compatibility.
   */
  public class TabbedPaneLayout implements LayoutManager
  {
    /**
     * This method is called when a component is added to the JTabbedPane.
     *
     * @param name The name of the component.
     * @param comp The component being added.
     */
    public void addLayoutComponent(String name, Component comp)
    {
      // Do nothing.
    }

    /**
     * This method is called when the rectangles need to be calculated. It
     * also fixes the size of the visible component.
     */
    public void calculateLayoutInfo()
    {
      int count = tabPane.getTabCount();
      assureRectsCreated(count);
      calculateTabRects(tabPane.getTabPlacement(), count);
      tabRunsDirty = false;
    }

    /**
     * This method calculates the size of the the JTabbedPane.
     *
     * @param minimum Whether the JTabbedPane will try to be as small as it
     *        can.
     *
     * @return The desired size of the JTabbedPane.
     */
    protected Dimension calculateSize(boolean minimum)
    {
      int tabPlacement = tabPane.getTabPlacement();

      int width = 0;
      int height = 0;
      Component c;
      Dimension dims;

      // Find out the minimum/preferred size to display the largest child
      // of the tabbed pane.
      for (int i = 0; i < tabPane.getTabCount(); i++)
        {
          c = tabPane.getComponentAt(i);
          if (c == null)
            continue;
          dims = minimum ? c.getMinimumSize() : c.getPreferredSize(); 
          if (dims != null)
            {
              height = Math.max(height, dims.height);
              width = Math.max(width, dims.width);
            }
        }

      Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
      if (tabPlacement == SwingConstants.TOP
          || tabPlacement == SwingConstants.BOTTOM)
        {
          int min = calculateMaxTabWidth(tabPlacement);
          width = Math.max(min, width);
          int tabAreaHeight = preferredTabAreaHeight(tabPlacement,
                                                     width - tabAreaInsets.left
                                                     -tabAreaInsets.right);
          height += tabAreaHeight;
        }
      else
        {
          int min = calculateMaxTabHeight(tabPlacement);
          height = Math.max(min, height);
          int tabAreaWidth = preferredTabAreaWidth(tabPlacement,
                                                   height - tabAreaInsets.top
                                                   - tabAreaInsets.bottom);
          width += tabAreaWidth;
        }

      Insets tabPaneInsets = tabPane.getInsets();
      return new Dimension(width + tabPaneInsets.left + tabPaneInsets.right,
                           height + tabPaneInsets.top + tabPaneInsets.bottom);
    }

    // if tab placement is LEFT OR RIGHT, they share width.
    // if tab placement is TOP OR BOTTOM, they share height
    // PRE STEP: finds the default sizes for the labels as well as their locations.
    // AND where they will be placed within the run system.
    // 1. calls normalizeTab Runs.
    // 2. calls rotate tab runs.
    // 3. pads the tab runs.
    // 4. pads the selected tab.

    /**
     * This method is called to calculate the tab rectangles.  This method
     * will calculate the size and position of all  rectangles (taking into
     * account which ones should be in which tab run). It will pad them and
     * normalize them  as necessary.
     *
     * @param tabPlacement The JTabbedPane's tab placement.
     * @param tabCount The run the current selection is in.
     */
    protected void calculateTabRects(int tabPlacement, int tabCount)
    {
      Insets insets = tabPane.getInsets();
      Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
      Dimension size = tabPane.getSize();
      
      // The coordinates of the upper left corner of the tab area.
      int x;
      int y;
      // The location at which the runs must be broken.
      int breakAt;

      // Calculate the bounds for the tab area.
      switch (tabPlacement)
      {
        case LEFT:
          maxTabWidth = calculateMaxTabWidth(tabPlacement);
          x = insets.left + tabAreaInsets.left;
          y = insets.top + tabAreaInsets.top;
          breakAt = size.height - (insets.bottom + tabAreaInsets.bottom);
          break;
        case RIGHT:
          maxTabWidth = calculateMaxTabWidth(tabPlacement);
          x = size.width - (insets.right + tabAreaInsets.right) - maxTabWidth;
          y = insets.top + tabAreaInsets.top;
          breakAt = size.height - (insets.bottom + tabAreaInsets.bottom);
          break;
        case BOTTOM:
          maxTabHeight = calculateMaxTabHeight(tabPlacement);
          x = insets.left + tabAreaInsets.left;
          y = size.height - (insets.bottom + tabAreaInsets.bottom)
              - maxTabHeight;
          breakAt = size.width - (insets.right + tabAreaInsets.right);
          break;
        case TOP:
        default:
          maxTabHeight = calculateMaxTabHeight(tabPlacement);
          x = insets.left + tabAreaInsets.left;
          y = insets.top + tabAreaInsets.top;
          breakAt = size.width - (insets.right + tabAreaInsets.right);
          break;
      }

      if (tabCount == 0)
        return;

      FontMetrics fm = getFontMetrics();
      runCount = 0;
      selectedRun = -1;
      int selectedIndex = tabPane.getSelectedIndex();

      Rectangle rect;

      // Go through all the tabs and build the tab runs.
      if (tabPlacement == SwingConstants.TOP
          || tabPlacement == SwingConstants.BOTTOM)
        {
          for (int i = 0; i < tabCount; i++)
            {
              rect = rects[i];
              if (i > 0)
                {
                  rect.x = rects[i - 1].x + rects[i - 1].width;
                }
              else
                {
                  tabRuns[0] = 0;
                  runCount = 1;
                  maxTabWidth = 0;
                  rect.x = x;
            }
              rect.width = calculateTabWidth(tabPlacement, i, fm);
              maxTabWidth = Math.max(maxTabWidth, rect.width);

              if (rect.x != 2 + insets.left && rect.x + rect.width > breakAt)
            {
                  if (runCount > tabRuns.length - 1)
                    expandTabRunsArray();
                  tabRuns[runCount] = i;
                  runCount++;
                  rect.x = x;
            }

              rect.y = y;
              rect.height = maxTabHeight;
              if (i == selectedIndex)
                selectedRun = runCount - 1;
                
            }
        }
      else
        {
          for (int i = 0; i < tabCount; i++)
            {
              rect = rects[i];
              if (i > 0)
                {
                  rect.y = rects[i - 1].y + rects[i - 1].height;
                }
              else
                {
                  tabRuns[0] = 0;
                  runCount = 1;
                  maxTabHeight = 0;
                  rect.y = y;
            }
              rect.height = calculateTabHeight(tabPlacement, i,
                                               fm.getHeight());
              maxTabHeight = Math.max(maxTabHeight, rect.height);

              if (rect.y != 2 + insets.top && rect.y + rect.height > breakAt)
            {
                  if (runCount > tabRuns.length - 1)
                    expandTabRunsArray();
                  tabRuns[runCount] = i;
                  runCount++;
                  rect.y = y;
            }

              rect.x = x;
              rect.width = maxTabWidth;

              if (i == selectedIndex)
                selectedRun = runCount - 1;
            }
        }

      if (runCount > 1)
        {
          int start;
          if  (tabPlacement == SwingConstants.TOP
              || tabPlacement == SwingConstants.BOTTOM)
            start = y;
          else
            start = x;
          normalizeTabRuns(tabPlacement, tabCount, start, breakAt);
          selectedRun = getRunForTab(tabCount, selectedIndex);
      if (shouldRotateTabRuns(tabPlacement))
            {
        rotateTabRuns(tabPlacement, selectedRun);
            }
        }

      // Pad the runs.
      int tabRunOverlay = getTabRunOverlay(tabPlacement);
      for (int i = runCount - 1; i >= 0; --i)
        {
          int start = tabRuns[i];
          int nextIndex;
          if (i == runCount - 1)
            nextIndex = 0;
          else
            nextIndex = i + 1;
          int next = tabRuns[nextIndex];
          int end = (next != 0 ? next - 1 : tabCount - 1);
          if (tabPlacement == SwingConstants.TOP
              || tabPlacement == SwingConstants.BOTTOM)
        {
              for (int j = start; j <= end; ++j)
            {
                  rect = rects[j];
                  rect.y = y;
                  rect.x += getTabRunIndent(tabPlacement, i);
            }
              if (shouldPadTabRun(tabPlacement, i))
            {
                  padTabRun(tabPlacement, start, end, breakAt);
                }
              if (tabPlacement == BOTTOM)
                y -= (maxTabHeight - tabRunOverlay);
              else
                y += (maxTabHeight - tabRunOverlay);
            }
          else
            {
              for (int j = start; j <= end; ++j)
                {
                  rect = rects[j];
                  rect.x = x;
                  rect.y += getTabRunIndent(tabPlacement, i);
            }
              if (shouldPadTabRun(tabPlacement, i))
            {
                  padTabRun(tabPlacement, start, end, breakAt);
                }
              if (tabPlacement == RIGHT)
                x -= (maxTabWidth - tabRunOverlay);
              else
                x += (maxTabWidth - tabRunOverlay);

            }
        }
      padSelectedTab(tabPlacement, selectedIndex);
    }

    /**
     * This method is called when the JTabbedPane is laid out in
     * WRAP_TAB_LAYOUT. It calls calculateLayoutInfo to  find the positions
     * of all its components.
     *
     * @param parent The Container to lay out.
     */
    public void layoutContainer(Container parent)
    {
      calculateLayoutInfo();

      int tabPlacement = tabPane.getTabPlacement();
      Insets insets = tabPane.getInsets();
      int childCount = tabPane.getComponentCount();
      if (childCount > 0)
        {
          int compX;
          int compY;
          int tabAreaWidth = 0;
          int tabAreaHeight = 0;
          switch (tabPlacement)
          {
            case LEFT:
              tabAreaWidth = calculateTabAreaWidth(tabPlacement, runCount,
                                                   maxTabWidth);
              compX = tabAreaWidth + insets.left + contentBorderInsets.left;
              compY = insets.top + contentBorderInsets.top;
              break;
            case RIGHT:
              tabAreaWidth = calculateTabAreaWidth(tabPlacement, runCount,
                                                   maxTabWidth);
              compX = insets.left + contentBorderInsets.left;
              compY = insets.top + contentBorderInsets.top;
              break;
            case BOTTOM: 
              tabAreaHeight = calculateTabAreaHeight(tabPlacement, runCount,
                                                     maxTabHeight);
              compX = insets.left + contentBorderInsets.left;
              compY = insets.top + contentBorderInsets.top;
              break;
            case TOP:
            default:
              tabAreaHeight = calculateTabAreaHeight(tabPlacement, runCount,
                                                     maxTabHeight);
              compX = insets.left + contentBorderInsets.left;
              compY = tabAreaHeight + insets.top + contentBorderInsets.top;
          }
          Rectangle bounds = tabPane.getBounds();
          int compWidth = bounds.width - tabAreaWidth - insets.left
                          - insets.right - contentBorderInsets.left
                          - contentBorderInsets.right;
          int compHeight = bounds.height - tabAreaHeight - insets.top
                           - insets.bottom - contentBorderInsets.top
                           - contentBorderInsets.bottom;


          for (int i = 0; i < childCount; ++i)
            {
              Component c = tabPane.getComponent(i);
              c.setBounds(compX, compY, compWidth, compHeight);
            }
        }
    }

    /**
     * This method returns the minimum layout size for the given container.
     *
     * @param parent The container that is being sized.
     *
     * @return The minimum size.
     */
    public Dimension minimumLayoutSize(Container parent)
    {
      return calculateSize(false);
    }

    // If there is more free space in an adjacent run AND the tab in the run can fit in the 
    // adjacent run, move it. This method is not perfect, it is merely an approximation.
    // If you play around with Sun's JTabbedPane, you'll see that 
    // it does do some pretty strange things with regards to not moving tabs 
    // that should be moved. 
    // start = the x position where the tabs will begin
    // max = the maximum position of where the tabs can go to (tabAreaInsets.left + the width of the tab area)

    /**
     * This method tries to "even out" the number of tabs in each run based on
     * their widths.
     *
     * @param tabPlacement The JTabbedPane's tab placement.
     * @param tabCount The number of tabs.
     * @param start The x position where the tabs will begin.
     * @param max The maximum x position where the tab can run to.
     */
    protected void normalizeTabRuns(int tabPlacement, int tabCount, int start,
                                    int max)
    {
      Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
      if (tabPlacement == SwingUtilities.TOP
          || tabPlacement == SwingUtilities.BOTTOM)
        {
          // We should only do this for runCount - 1, cause we can only shift that many times between
          // runs.
          for (int i = 1; i < runCount; i++)
            {
              Rectangle currRun = rects[lastTabInRun(tabCount, i)];
              Rectangle nextRun = rects[lastTabInRun(tabCount, getNextTabRun(i))];
              int spaceInCurr = currRun.x + currRun.width;
              int spaceInNext = nextRun.x + nextRun.width;

              int diffNow = spaceInCurr - spaceInNext;
              int diffLater = (spaceInCurr - currRun.width)
              - (spaceInNext + currRun.width);
              while (Math.abs(diffLater) < Math.abs(diffNow)
                  && spaceInNext + currRun.width < max)
                {
                  tabRuns[i]--;
                  spaceInNext += currRun.width;
                  spaceInCurr -= currRun.width;
                  currRun = rects[lastTabInRun(tabCount, i)];
                  diffNow = spaceInCurr - spaceInNext;
                  diffLater = (spaceInCurr - currRun.width)
                  - (spaceInNext + currRun.width);
                }

              // Fix the bounds.
              int first = lastTabInRun(tabCount, i) + 1;
              int last = lastTabInRun(tabCount, getNextTabRun(i));
              int currX = tabAreaInsets.left;
              for (int j = first; j <= last; j++)
                {
                  rects[j].x = currX;
                  currX += rects[j].width;
                }
            }
        }
      else
        {
          for (int i = 1; i < runCount; i++)
            {
              Rectangle currRun = rects[lastTabInRun(tabCount, i)];
              Rectangle nextRun = rects[lastTabInRun(tabCount, getNextTabRun(i))];
              int spaceInCurr = currRun.y + currRun.height;
              int spaceInNext = nextRun.y + nextRun.height;

              int diffNow = spaceInCurr - spaceInNext;
              int diffLater = (spaceInCurr - currRun.height)
              - (spaceInNext + currRun.height);
              while (Math.abs(diffLater) < Math.abs(diffNow)
                  && spaceInNext + currRun.height < max)
                {
                  tabRuns[i]--;
                  spaceInNext += currRun.height;
                  spaceInCurr -= currRun.height;
                  currRun = rects[lastTabInRun(tabCount, i)];
                  diffNow = spaceInCurr - spaceInNext;
                  diffLater = (spaceInCurr - currRun.height)
                  - (spaceInNext + currRun.height);
                }

              int first = lastTabInRun(tabCount, i) + 1;
              int last = lastTabInRun(tabCount, getNextTabRun(i));
              int currY = tabAreaInsets.top;
              for (int j = first; j <= last; j++)
                {
                  rects[j].y = currY;
                  currY += rects[j].height;
                }
            }
        }
    }

    /**
     * This method pads the tab at the selected index by the  selected tab pad
     * insets (so that it looks larger).
     *
     * @param tabPlacement The placement of the tabs.
     * @param selectedIndex The selected index.
     */
    protected void padSelectedTab(int tabPlacement, int selectedIndex)
    {
      Insets insets = getSelectedTabPadInsets(tabPlacement);
      rects[selectedIndex].x -= insets.left;
      rects[selectedIndex].y -= insets.top;
      rects[selectedIndex].width += insets.left + insets.right;
      rects[selectedIndex].height += insets.top + insets.bottom;
    }

    // If the tabs on the run don't fill the width of the window, make it fit now.
    // start = starting index of the run
    // end = last index of the run
    // max = tabAreaInsets.left + width (or equivalent)
    // assert start <= end.

    /**
     * This method makes each tab in the run larger so that the  tabs expand
     * to fill the runs width/height (depending on tabPlacement).
     *
     * @param tabPlacement The placement of the tabs.
     * @param start The index of the first tab.
     * @param end The last index of the tab
     * @param max The amount of space in the run (width for TOP and BOTTOM
     *        tabPlacement).
     */
    protected void padTabRun(int tabPlacement, int start, int end, int max)
    {
      if (tabPlacement == SwingConstants.TOP
          || tabPlacement == SwingConstants.BOTTOM)
        {
          int runWidth = rects[end].x + rects[end].width;
          int spaceRemaining = max - runWidth;
          int numTabs = end - start + 1;

          // now divvy up the space.
          int spaceAllocated = spaceRemaining / numTabs;
          int currX = rects[start].x;
          for (int i = start; i <= end; i++)
            {
              rects[i].x = currX;
              rects[i].width += spaceAllocated;
              currX += rects[i].width;
              // This is used because since the spaceAllocated 
              // variable is an int, it rounds down. Sometimes,
              // we don't fill an entire row, so we make it do
              // so now.
              if (i == end && rects[i].x + rects[i].width != max)
                rects[i].width = max - rects[i].x;
            }
        }
      else
        {
          int runHeight = rects[end].y + rects[end].height;
          int spaceRemaining = max - runHeight;
          int numTabs = end - start + 1;

          int spaceAllocated = spaceRemaining / numTabs;
          int currY = rects[start].y;
          for (int i = start; i <= end; i++)
            {
              rects[i].y = currY;
              rects[i].height += spaceAllocated;
              currY += rects[i].height;
              if (i == end && rects[i].y + rects[i].height != max)
                rects[i].height = max - rects[i].y;
            }
        }
    }

    /**
     * This method returns the preferred layout size for the given container.
     *
     * @param parent The container to size.
     *
     * @return The preferred layout size.
     */
    public Dimension preferredLayoutSize(Container parent)
    {
      return calculateSize(false);
    }

    /**
     * This method returns the preferred tab height given a tabPlacement and
     * width.
     *
     * @param tabPlacement The JTabbedPane's tab placement.
     * @param width The expected width.
     *
     * @return The preferred tab area height.
     */
    protected int preferredTabAreaHeight(int tabPlacement, int width)
    {
      if (tabPane.getTabCount() == 0)
        return calculateTabAreaHeight(tabPlacement, 0, 0);

      int runs = 0;
      int runWidth = 0;
      int tabWidth = 0;

      FontMetrics fm = getFontMetrics();

      Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
      Insets insets = tabPane.getInsets();

      // Only interested in width, this is a messed up rectangle now.
      width -= tabAreaInsets.left + tabAreaInsets.right + insets.left
      + insets.right;

      // The reason why we can't use runCount:
      // This method is only called to calculate the size request
      // for the tabbedPane. However, this size request is dependent on 
      // our desired width. We need to find out what the height would
      // be IF we got our desired width.
      for (int i = 0; i < tabPane.getTabCount(); i++)
        {
          tabWidth = calculateTabWidth(tabPlacement, i, fm);
          if (runWidth + tabWidth > width)
            {
              runWidth = tabWidth;
              runs++;
            }
          else
            runWidth += tabWidth;
        }
      runs++;

      int maxTabHeight = calculateMaxTabHeight(tabPlacement);
      int tabAreaHeight = calculateTabAreaHeight(tabPlacement, runs,
                                                 maxTabHeight);
      return tabAreaHeight;
    }

    /**
     * This method calculates the preferred tab area width given a tab
     * placement and height.
     *
     * @param tabPlacement The JTabbedPane's tab placement.
     * @param height The expected height.
     *
     * @return The preferred tab area width.
     */
    protected int preferredTabAreaWidth(int tabPlacement, int height)
    {
      if (tabPane.getTabCount() == 0)
        return calculateTabAreaHeight(tabPlacement, 0, 0);

      int runs = 0;
      int runHeight = 0;
      int tabHeight = 0;

      FontMetrics fm = getFontMetrics();

      Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
      Insets insets = tabPane.getInsets();

      height -= tabAreaInsets.top + tabAreaInsets.bottom + insets.top
      + insets.bottom;
      int fontHeight = fm.getHeight();

      for (int i = 0; i < tabPane.getTabCount(); i++)
        {
          tabHeight = calculateTabHeight(tabPlacement, i, fontHeight);
          if (runHeight + tabHeight > height)
            {
              runHeight = tabHeight;
              runs++;
            }
          else
            runHeight += tabHeight;
        }
      runs++;

      int maxTabWidth = calculateMaxTabWidth(tabPlacement);
      int tabAreaWidth = calculateTabAreaWidth(tabPlacement, runs, maxTabWidth);
      return tabAreaWidth;
    }

    /**
     * This method rotates the places each run in the correct place  the
     * tabRuns array. See the comment for tabRuns for how the runs are placed
     * in the array.
     *
     * @param tabPlacement The JTabbedPane's tab placement.
     * @param selectedRun The run the current selection is in.
     */
    protected void rotateTabRuns(int tabPlacement, int selectedRun)
    {
      if (runCount == 1 || selectedRun == 1 || selectedRun == -1)
        return;
      int[] newTabRuns = new int[tabRuns.length];
      int currentRun = selectedRun;
      int i = 1;
      do
        {
          newTabRuns[i] = tabRuns[currentRun];
          currentRun = getNextTabRun(currentRun);
          i++;
        }
      while (i < runCount);
      if (runCount > 1)
        newTabRuns[0] = tabRuns[currentRun];

      tabRuns = newTabRuns;
      BasicTabbedPaneUI.this.selectedRun = 1;
    }

    /**
     * This method is called when a component is removed  from the
     * JTabbedPane.
     *
     * @param comp The component removed.
     */
    public void removeLayoutComponent(Component comp)
    {
      // Do nothing.
    }
  }

  /**
   * This class acts as the LayoutManager for the JTabbedPane in
   * SCROLL_TAB_MODE.
   */
  private class TabbedPaneScrollLayout extends TabbedPaneLayout
  {
    /**
     * This method returns the preferred layout size for the given container.
     *
     * @param parent The container to calculate a size for.
     *
     * @return The preferred layout size.
     */
    public Dimension preferredLayoutSize(Container parent)
    {
      return super.calculateSize(true);
    }

    /**
     * This method returns the minimum layout size for the given container.
     *
     * @param parent The container to calculate a size for.
     *
     * @return The minimum layout size.
     */
    public Dimension minimumLayoutSize(Container parent)
    {
      return super.calculateSize(true);
    }

    /**
     * This method calculates the tab area height given  a desired width.
     *
     * @param tabPlacement The JTabbedPane's tab placement.
     * @param width The expected width.
     *
     * @return The tab area height given the width.
     */
    protected int preferredTabAreaHeight(int tabPlacement, int width)
    {
      if (tabPane.getTabCount() == 0)
        return calculateTabAreaHeight(tabPlacement, 0, 0);

      int runs = 1;

      int maxTabHeight = calculateMaxTabHeight(tabPlacement);
      int tabAreaHeight = calculateTabAreaHeight(tabPlacement, runs,
                                                 maxTabHeight);
      return tabAreaHeight;
    }

    /**
     * This method calculates the tab area width given a desired height.
     *
     * @param tabPlacement The JTabbedPane's tab placement.
     * @param height The expected height.
     *
     * @return The tab area width given the height.
     */
    protected int preferredTabAreaWidth(int tabPlacement, int height)
    {
      if (tabPane.getTabCount() == 0)
        return calculateTabAreaHeight(tabPlacement, 0, 0);

      int runs = 1;

      int maxTabWidth = calculateMaxTabWidth(tabPlacement);
      int tabAreaWidth = calculateTabAreaWidth(tabPlacement, runs, maxTabWidth);
      return tabAreaWidth;
    }

    /**
     * This method is called to calculate the tab rectangles.  This method
     * will calculate the size and position of all  rectangles (taking into
     * account which ones should be in which tab run). It will pad them and
     * normalize them  as necessary.
     *
     * @param tabPlacement The JTabbedPane's tab placement.
     * @param tabCount The number of tabs.
     */
    protected void calculateTabRects(int tabPlacement, int tabCount)
    {
      if (tabCount == 0)
        return;

      FontMetrics fm = getFontMetrics();
      SwingUtilities.calculateInnerArea(tabPane, calcRect);
      Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
      Insets insets = tabPane.getInsets();
      int runs = 1;
      int start = 0;
      int top = 0;
      if (tabPlacement == SwingConstants.TOP
          || tabPlacement == SwingConstants.BOTTOM)
        {
          int maxHeight = calculateMaxTabHeight(tabPlacement);
          calcRect.width -= tabAreaInsets.left + tabAreaInsets.right;
          start = tabAreaInsets.left + insets.left;
          int width = 0;
          int runWidth = start;
          top = insets.top + tabAreaInsets.top;
          for (int i = 0; i < tabCount; i++)
            {
              width = calculateTabWidth(tabPlacement, i, fm);

              rects[i] = new Rectangle(runWidth, top, width, maxHeight);
              runWidth += width;
            }
          tabAreaRect.width = tabPane.getWidth() - insets.left - insets.right;
          tabAreaRect.height = runs * maxTabHeight
          - (runs - 1) * tabRunOverlay
          + tabAreaInsets.top + tabAreaInsets.bottom;
          contentRect.width = tabAreaRect.width;
          contentRect.height = tabPane.getHeight() - insets.top
          - insets.bottom - tabAreaRect.height;
          contentRect.x = insets.left;
          tabAreaRect.x = insets.left;
          if (tabPlacement == SwingConstants.BOTTOM)
            {
              contentRect.y = insets.top;
              tabAreaRect.y = contentRect.y + contentRect.height;
            }
          else
            {
              tabAreaRect.y = insets.top;
              contentRect.y = tabAreaRect.y + tabAreaRect.height;
            }
        }
      else
        {
          int maxWidth = calculateMaxTabWidth(tabPlacement);

          calcRect.height -= tabAreaInsets.top + tabAreaInsets.bottom;
          int height = 0;
          start = tabAreaInsets.top + insets.top;
          int runHeight = start;
          int fontHeight = fm.getHeight();
          top = insets.left + tabAreaInsets.left;
          for (int i = 0; i < tabCount; i++)
            {
              height = calculateTabHeight(tabPlacement, i, fontHeight);
              rects[i] = new Rectangle(top, runHeight, maxWidth, height);
              runHeight += height;
            }
          tabAreaRect.width = runs * maxTabWidth - (runs - 1) * tabRunOverlay
          + tabAreaInsets.left + tabAreaInsets.right;
          tabAreaRect.height = tabPane.getHeight() - insets.top
          - insets.bottom;
          tabAreaRect.y = insets.top;
          contentRect.width = tabPane.getWidth() - insets.left - insets.right
          - tabAreaRect.width;
          contentRect.height = tabAreaRect.height;
          contentRect.y = insets.top;
          if (tabPlacement == SwingConstants.LEFT)
            {
              tabAreaRect.x = insets.left;
              contentRect.x = tabAreaRect.x + tabAreaRect.width;
            }
          else
            {
              contentRect.x = insets.left;
              tabAreaRect.x = contentRect.x + contentRect.width;
            }
        }
      runCount = runs;
      if (runCount > tabRuns.length)
        expandTabRunsArray();

      padSelectedTab(tabPlacement, tabPane.getSelectedIndex());
    }

    /**
     * This method is called when the JTabbedPane is laid out in
     * SCROLL_TAB_LAYOUT. It finds the position for all components in the
     * JTabbedPane.
     *
     * @param pane The JTabbedPane to be laid out.
     */
    public void layoutContainer(Container pane)
    {
      super.layoutContainer(pane);
      int tabCount = tabPane.getTabCount();
      Point p = null;
      if (tabCount == 0)
        return;
      int tabPlacement = tabPane.getTabPlacement();
      incrButton.setVisible(false);
      decrButton.setVisible(false);
      if (tabPlacement == SwingConstants.TOP
          || tabPlacement == SwingConstants.BOTTOM)
        {
          if (tabAreaRect.x + tabAreaRect.width < rects[tabCount - 1].x
              + rects[tabCount - 1].width)
            {
              Dimension incrDims = incrButton.getPreferredSize();
              Dimension decrDims = decrButton.getPreferredSize();

              decrButton.setBounds(tabAreaRect.x + tabAreaRect.width
                                   - incrDims.width - decrDims.width,
                                   tabAreaRect.y, decrDims.width,
                                   tabAreaRect.height);
              incrButton.setBounds(tabAreaRect.x + tabAreaRect.width
                                   - incrDims.width, tabAreaRect.y,
                                   decrDims.width, tabAreaRect.height);

              tabAreaRect.width -= decrDims.width + incrDims.width;
              incrButton.setVisible(true);
              decrButton.setVisible(true);
            }
        }

      if (tabPlacement == SwingConstants.LEFT
          || tabPlacement == SwingConstants.RIGHT)
        {
          if (tabAreaRect.y + tabAreaRect.height < rects[tabCount - 1].y
              + rects[tabCount - 1].height)
            {
              Dimension incrDims = incrButton.getPreferredSize();
              Dimension decrDims = decrButton.getPreferredSize();

              decrButton.setBounds(tabAreaRect.x,
                                   tabAreaRect.y + tabAreaRect.height
                                   - incrDims.height - decrDims.height,
                                   tabAreaRect.width, decrDims.height);
              incrButton.setBounds(tabAreaRect.x,
                                   tabAreaRect.y + tabAreaRect.height
                                   - incrDims.height, tabAreaRect.width,
                                   incrDims.height);

              tabAreaRect.height -= decrDims.height + incrDims.height;
              incrButton.setVisible(true);
              decrButton.setVisible(true);
            }
        }
      viewport.setBounds(tabAreaRect.x, tabAreaRect.y, tabAreaRect.width,
                         tabAreaRect.height);
      int tabC = tabPane.getTabCount() - 1;
      if (tabCount > 0)
        {
          int w = Math.max(rects[tabC].width + rects[tabC].x, tabAreaRect.width);
          int h = Math.max(rects[tabC].height, tabAreaRect.height);
          p = findPointForIndex(currentScrollLocation);
          
          // we want to cover that entire space so that borders that run under
          // the tab area don't show up when we move the viewport around.
          panel.setSize(w + p.x, h + p.y);
        }
      viewport.setViewPosition(p);
      viewport.repaint();
    }
  }

  /**
   * This class handles ChangeEvents from the JTabbedPane.
   *
   * @specnote Apparently this class was intended to be protected,
   *           but was made public by a compiler bug and is now
   *           public for compatibility.
   */
  public class TabSelectionHandler implements ChangeListener
  {
    /**
     * This method is called whenever a ChangeEvent is fired from the
     * JTabbedPane.
     *
     * @param e The ChangeEvent fired.
     */
    public void stateChanged(ChangeEvent e)
    {
      selectedRun = getRunForTab(tabPane.getTabCount(),
                                 tabPane.getSelectedIndex());
      tabPane.revalidate();
      tabPane.repaint();
    }
  }

  /**
   * This helper class is a JPanel that fits inside the ScrollViewport. This
   * panel's sole job is to paint the tab rectangles inside the  viewport so
   * that it's clipped correctly.
   */
  private class ScrollingPanel extends JPanel
  {
    /**
     * This is a private UI class for our panel.
     */
    private class ScrollingPanelUI extends BasicPanelUI
    {
      /**
       * This method overrides the default paint method. It paints the tab
       * rectangles for the JTabbedPane in the panel.
       *
       * @param g The Graphics object to paint with.
       * @param c The JComponent to paint.
       */
      public void paint(Graphics g, JComponent c)
      {
        paintTabArea(g, tabPane.getTabPlacement(), tabPane.getSelectedIndex());
      }
    }

    /**
     * This method overrides the updateUI method. It makes the default UI for
     * this ScrollingPanel to be  a ScrollingPanelUI.
     */
    public void updateUI()
    {
      setUI((PanelUI) new ScrollingPanelUI());
    }
  }

  /**
   * This is a helper class that paints the panel that paints tabs. This
   * custom JViewport is used so that the tabs painted in the panel will be
   * clipped. This class implements UIResource so tabs are not added when
   * this objects of this class are added to the  JTabbedPane.
   */
  private class ScrollingViewport extends JViewport implements UIResource
  {
    // TODO: Maybe remove this inner class.
  }

  /**
   * This is a helper class that implements UIResource so it is not added as a
   * tab when an object of this class is added to the JTabbedPane.
   */
  private class ScrollingButton extends BasicArrowButton implements UIResource
  {
    /**
     * Creates a ScrollingButton given the direction.
     *
     * @param dir The direction to point in.
     */
    public ScrollingButton(int dir)
    {
      super(dir);
    }
  }

  /** The button that increments the current scroll location.
   * This is package-private to avoid an accessor method.  */
  transient ScrollingButton incrButton;

  /** The button that decrements the current scroll location.
   * This is package-private to avoid an accessor method.  */
  transient ScrollingButton decrButton;

  /** The viewport used to display the tabs.
   * This is package-private to avoid an accessor method.  */
  transient ScrollingViewport viewport;

  /** The panel inside the viewport that paints the tabs.
   * This is package-private to avoid an accessor method.  */
  transient ScrollingPanel panel;

  /** The starting visible tab in the run in SCROLL_TAB_MODE.
   * This is package-private to avoid an accessor method.  */
  transient int currentScrollLocation;

  /** A reusable rectangle. */
  protected Rectangle calcRect;

  /** An array of Rectangles keeping track of the tabs' area and position. */
  protected Rectangle[] rects;

  /** The insets around the content area. */
  protected Insets contentBorderInsets;

  /** The extra insets around the selected tab. */
  protected Insets selectedTabPadInsets;

  /** The insets around the tab area. */
  protected Insets tabAreaInsets;

  /** The insets around each and every tab. */
  protected Insets tabInsets;

  /**
   * The outer bottom and right edge color for both the tab and content
   * border.
   */
  protected Color darkShadow;

  /** The color of the focus outline on the selected tab. */
  protected Color focus;

  /** FIXME: find a use for this. */
  protected Color highlight;

  /** The top and left edge color for both the tab and content border. */
  protected Color lightHighlight;

  /** The inner bottom and right edge color for the tab and content border. */
  protected Color shadow;

  /** The maximum tab height. */
  protected int maxTabHeight;

  /** The maximum tab width. */
  protected int maxTabWidth;

  /** The number of runs in the JTabbedPane. */
  protected int runCount;

  /** The index of the run that the selected index is in. */
  protected int selectedRun;

  /** The amount of space each run overlaps the previous by. */
  protected int tabRunOverlay;

  /** The gap between text and label */
  protected int textIconGap;

  // Keeps track of tab runs.
  // The organization of this array is as follows (lots of experimentation to
  // figure this out)
  // index 0 = furthest away from the component area (aka outer run)
  // index 1 = closest to component area (aka selected run)
  // index > 1 = listed in order leading from selected run to outer run.
  // each int in the array is the tab index + 1 (counting starts at 1)
  // for the last tab in the run. (same as the rects array)

  /** This array keeps track of which tabs are in which run. See above. */
  protected int[] tabRuns;

  /**
   * Indicates if the layout of the tab runs is ok or not. This is package
   * private to avoid a synthetic accessor method.
   */
  boolean tabRunsDirty;

  /**
   * This is the keystroke for moving down.
   *
   * @deprecated 1.3
   */
  protected KeyStroke downKey;

  /**
   * This is the keystroke for moving left.
   *
   * @deprecated 1.3
   */
  protected KeyStroke leftKey;

  /**
   * This is the keystroke for moving right.
   *
   * @deprecated 1.3
   */
  protected KeyStroke rightKey;

  /**
   * This is the keystroke for moving up.
   *
   * @deprecated 1.3
   */
  protected KeyStroke upKey;

  /** The listener that listens for focus events. */
  protected FocusListener focusListener;

  /** The listener that listens for mouse events. */
  protected MouseListener mouseListener;

  /** The listener that listens for property change events. */
  protected PropertyChangeListener propertyChangeListener;

  /** The listener that listens for change events. */
  protected ChangeListener tabChangeListener;

  /** The tab pane that this UI paints. */
  protected JTabbedPane tabPane;

  /** The current layout manager for the tabPane.
   * This is package-private to avoid an accessor method.  */
  transient LayoutManager layoutManager;

  /** The rectangle that describes the tab area's position and size.
   * This is package-private to avoid an accessor method.  */
  transient Rectangle tabAreaRect;

  /** The rectangle that describes the content area's position and
   * size.  This is package-private to avoid an accessor method.  */
  transient Rectangle contentRect;

  /**
   * The index over which the mouse is currently moving.
   */
  private int rolloverTab;

  /**
   * Determines if tabs are painted opaque or not. This can be adjusted using
   * the UIManager property 'TabbedPane.tabsOpaque'.
   */
  private boolean tabsOpaque;

  /**
   * Creates a new BasicTabbedPaneUI object.
   */
  public BasicTabbedPaneUI()
  {
    super();
    rects = new Rectangle[0];
    tabRuns = new int[10];
  }

  /**
   * This method creates a ScrollingButton that  points in the appropriate
   * direction for an increasing button.
   * This is package-private to avoid an accessor method.
   *
   * @return The increase ScrollingButton.
   */
  ScrollingButton createIncreaseButton()
  {
    if (incrButton == null)
      incrButton = new ScrollingButton(SwingConstants.NORTH);
    if (tabPane.getTabPlacement() == SwingConstants.TOP
        || tabPane.getTabPlacement() == SwingConstants.BOTTOM)
      incrButton.setDirection(SwingConstants.EAST);
    else
      incrButton.setDirection(SwingConstants.SOUTH);
    return incrButton;
  }

  /**
   * This method creates a ScrollingButton that points in the appropriate
   * direction for a decreasing button.
   * This is package-private to avoid an accessor method.
   *
   * @return The decrease ScrollingButton.
   */
  ScrollingButton createDecreaseButton()
  {
    if (decrButton == null)
      decrButton = new ScrollingButton(SwingConstants.SOUTH);
    if (tabPane.getTabPlacement() == SwingConstants.TOP
        || tabPane.getTabPlacement() == SwingConstants.BOTTOM)
      decrButton.setDirection(SwingConstants.WEST);
    else
      decrButton.setDirection(SwingConstants.NORTH);
    return decrButton;
  }

  /**
   * This method finds the point to set the view  position at given the index
   * of a tab. The tab will be the first visible tab in the run.
   * This is package-private to avoid an accessor method.
   *
   * @param index The index of the first visible tab.
   *
   * @return The position of the first visible tab.
   */
  Point findPointForIndex(int index)
  {
    int tabPlacement = tabPane.getTabPlacement();
    int selectedIndex = tabPane.getSelectedIndex();
    Insets insets = getSelectedTabPadInsets(tabPlacement);
    int w = 0;
    int h = 0;

    if (tabPlacement == TOP || tabPlacement == BOTTOM)
      {
        if (index > 0)
          {
            w += rects[index - 1].x + rects[index - 1].width;
            if (index > selectedIndex)
              w -= insets.left + insets.right;
          }
      }

    else
      {
        if (index > 0)
          {
            h += rects[index - 1].y + rects[index - 1].height;
            if (index > selectedIndex)
              h -= insets.top + insets.bottom;
          }
      }

    Point p = new Point(w, h);
    return p;
  }

  /**
   * This method creates a new BasicTabbedPaneUI.
   *
   * @param c The JComponent to create a UI for.
   *
   * @return A new BasicTabbedPaneUI.
   */
  public static ComponentUI createUI(JComponent c)
  {
    return new BasicTabbedPaneUI();
  }

  /**
   * This method installs the UI for the given JComponent.
   *
   * @param c The JComponent to install the UI for.
   */
  public void installUI(JComponent c)
  {
    super.installUI(c);
    if (c instanceof JTabbedPane)
      {
        tabPane = (JTabbedPane) c;
        
        installComponents();
        installDefaults();
        installListeners();
        installKeyboardActions();
        
        layoutManager = createLayoutManager();
        tabPane.setLayout(layoutManager);
      }
  }

  /**
   * This method uninstalls the UI for the  given JComponent.
   *
   * @param c The JComponent to uninstall the UI for.
   */
  public void uninstallUI(JComponent c)
  {
    layoutManager = null;

    uninstallKeyboardActions();
    uninstallListeners();
    uninstallDefaults();
    uninstallComponents();

    tabPane = null;
  }

  /**
   * This method creates the appropriate layout manager for the JTabbedPane's
   * current tab layout policy. If the tab layout policy is
   * SCROLL_TAB_LAYOUT, then all the associated components that need to be
   * created will be done so now.
   *
   * @return A layout manager given the tab layout policy.
   */
  protected LayoutManager createLayoutManager()
  {
    if (tabPane.getTabLayoutPolicy() == JTabbedPane.WRAP_TAB_LAYOUT)
      return new TabbedPaneLayout();
    else
      {
        incrButton = createIncreaseButton();
        decrButton = createDecreaseButton();
        viewport = new ScrollingViewport();
        viewport.setLayout(null);
        panel = new ScrollingPanel();
        viewport.setView(panel);
        tabPane.add(incrButton);
        tabPane.add(decrButton);
        tabPane.add(viewport);
        currentScrollLocation = 0;
        decrButton.setEnabled(false);
        panel.addMouseListener(mouseListener);
        incrButton.addMouseListener(mouseListener);
        decrButton.addMouseListener(mouseListener);
        viewport.setBackground(Color.LIGHT_GRAY);

        return new TabbedPaneScrollLayout();
      }
  }

  /**
   * This method installs components for this JTabbedPane.
   */
  protected void installComponents()
  {
    // Nothing to be done.
  }

  /**
   * This method uninstalls components for this JTabbedPane.
   */
  protected void uninstallComponents()
  {
    // Nothing to be done.
  }

  /**
   * This method installs defaults for the Look and Feel.
   */
  protected void installDefaults()
  {
    LookAndFeel.installColorsAndFont(tabPane, "TabbedPane.background",
                                     "TabbedPane.foreground",
                                     "TabbedPane.font");
    tabPane.setOpaque(false);

    highlight = UIManager.getColor("TabbedPane.highlight");
    lightHighlight = UIManager.getColor("TabbedPane.lightHighlight");

    shadow = UIManager.getColor("TabbedPane.shadow");
    darkShadow = UIManager.getColor("TabbedPane.darkShadow");

    focus = UIManager.getColor("TabbedPane.focus");

    textIconGap = UIManager.getInt("TabbedPane.textIconGap");
    tabRunOverlay = UIManager.getInt("TabbedPane.tabRunOverlay");

    tabInsets = UIManager.getInsets("TabbedPane.tabInsets");
    selectedTabPadInsets = UIManager.getInsets("TabbedPane.tabbedPaneTabPadInsets");
    tabAreaInsets = UIManager.getInsets("TabbedPane.tabAreaInsets");
    contentBorderInsets = UIManager.getInsets("TabbedPane.tabbedPaneContentBorderInsets");
    tabsOpaque = UIManager.getBoolean("TabbedPane.tabsOpaque");

    calcRect = new Rectangle();
    tabRuns = new int[10];
    tabAreaRect = new Rectangle();
    contentRect = new Rectangle();
  }

  /**
   * This method uninstalls defaults for the Look and Feel.
   */
  protected void uninstallDefaults()
  {
    calcRect = null;
    tabAreaRect = null;
    contentRect = null;
    tabRuns = null;

    contentBorderInsets = null;
    tabAreaInsets = null;
    selectedTabPadInsets = null;
    tabInsets = null;

    focus = null;
    darkShadow = null;
    shadow = null;
    lightHighlight = null;
    highlight = null;

    // Install UI colors and fonts.
    LookAndFeel.installColorsAndFont(tabPane, "TabbedPane.background",
                                     "TabbedPane.foreground",
                                     "TabbedPane.font");
  }

  /**
   * This method creates and installs the listeners for this UI.
   */
  protected void installListeners()
  {
    mouseListener = createMouseListener();
    tabChangeListener = createChangeListener();
    propertyChangeListener = createPropertyChangeListener();
    focusListener = createFocusListener();

    tabPane.addMouseListener(mouseListener);
    tabPane.addChangeListener(tabChangeListener);
    tabPane.addPropertyChangeListener(propertyChangeListener);
    tabPane.addFocusListener(focusListener);
  }

  /**
   * This method removes and nulls the listeners for this UI.
   */
  protected void uninstallListeners()
  {
    tabPane.removeFocusListener(focusListener);
    tabPane.removePropertyChangeListener(propertyChangeListener);
    tabPane.removeChangeListener(tabChangeListener);
    tabPane.removeMouseListener(mouseListener);

    focusListener = null;
    propertyChangeListener = null;
    tabChangeListener = null;
    mouseListener = null;
  }

  /**
   * This method creates a new MouseListener.
   *
   * @return A new MouseListener.
   */
  protected MouseListener createMouseListener()
  {
    return new MouseHandler();
  }

  /**
   * This method creates a new FocusListener.
   *
   * @return A new FocusListener.
   */
  protected FocusListener createFocusListener()
  {
    return new FocusHandler();
  }

  /**
   * This method creates a new ChangeListener.
   *
   * @return A new ChangeListener.
   */
  protected ChangeListener createChangeListener()
  {
    return new TabSelectionHandler();
  }

  /**
   * This method creates a new PropertyChangeListener.
   *
   * @return A new PropertyChangeListener.
   */
  protected PropertyChangeListener createPropertyChangeListener()
  {
    return new PropertyChangeHandler();
  }

  /**
   * This method installs keyboard actions for the JTabbedPane.
   */
  protected void installKeyboardActions()
  {
    // FIXME: Implement.
  }

  /**
   * This method uninstalls keyboard actions for the JTabbedPane.
   */
  protected void uninstallKeyboardActions()
  {
    // FIXME: Implement.
  }

  /**
   * This method returns the minimum size of the JTabbedPane.
   *
   * @param c The JComponent to find a size for.
   *
   * @return The minimum size.
   */
  public Dimension getMinimumSize(JComponent c)
  {
    return layoutManager.minimumLayoutSize(tabPane);
  }

  /**
   * This method returns the maximum size of the JTabbedPane.
   *
   * @param c The JComponent to find a size for.
   *
   * @return The maximum size.
   */
  public Dimension getMaximumSize(JComponent c)
  {
    return new Dimension(Short.MAX_VALUE, Short.MAX_VALUE);
  }

  /**
   * This method paints the JTabbedPane.
   *
   * @param g The Graphics object to paint with.
   * @param c The JComponent to paint.
   */
  public void paint(Graphics g, JComponent c)
  {
    if (!tabPane.isValid())
      tabPane.validate();

    if (tabPane.getTabCount() == 0)
      return;
    if (tabPane.getTabLayoutPolicy() == JTabbedPane.WRAP_TAB_LAYOUT)
      paintTabArea(g, tabPane.getTabPlacement(), tabPane.getSelectedIndex());
    paintContentBorder(g, tabPane.getTabPlacement(), tabPane.getSelectedIndex());
  }

  /**
   * This method paints the tab area. This includes painting the rectangles
   * that make up the tabs.
   *
   * @param g The Graphics object to paint with.
   * @param tabPlacement The JTabbedPane's tab placement.
   * @param selectedIndex The selected index.
   */
  protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex)
  {
    Rectangle ir = new Rectangle();
    Rectangle tr = new Rectangle();

    boolean isScroll = tabPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT;

    // Please note: the ordering of the painting is important. 
    // we WANT to paint the outermost run first and then work our way in.
    int tabCount = tabPane.getTabCount();
    for (int i = runCount - 1; i >= 0; --i)
          {
        int start = tabRuns[i];
        int next;
        if (i == runCount - 1)
          next = tabRuns[0];
        else
          next = tabRuns[i + 1];
        int end = (next != 0 ? next - 1 : tabCount - 1);
        for (int j = start; j <= end; ++j)
              {
            if (j != selectedIndex)
                  {
                paintTab(g, tabPlacement, rects, j, ir, tr);
                  }
              }
          }

    // Paint selected tab in front of every other tab.
    if (selectedIndex >= 0)
      paintTab(g, tabPlacement, rects, selectedIndex, ir, tr);
  }

  /**
   * This method paints an individual tab.
   *
   * @param g The Graphics object to paint with.
   * @param tabPlacement The JTabbedPane's tab placement.
   * @param rects The array of rectangles that keep the size and position of
   *        the tabs.
   * @param tabIndex The tab index to paint.
   * @param iconRect The rectangle to use for the icon.
   * @param textRect The rectangle to use for the text.
   */
  protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects,
                          int tabIndex, Rectangle iconRect, Rectangle textRect)
  {
    Rectangle rect = rects[tabIndex];
    boolean isSelected = tabIndex == tabPane.getSelectedIndex();
    // Paint background if necessary.
    if (tabsOpaque || tabPane.isOpaque())
      {
        paintTabBackground(g, tabPlacement, tabIndex, rect.x, rect.y,
                           rect.width, rect.height, isSelected);
      }

    // Paint border.
    paintTabBorder(g, tabPlacement, tabIndex, rect.x, rect.y, rect.width,
                   rect.height, isSelected);


    // Layout label.
    FontMetrics fm = getFontMetrics();
    Icon icon = getIconForTab(tabIndex);
    String title = tabPane.getTitleAt(tabIndex);
    layoutLabel(tabPlacement, fm, tabIndex, title, icon, rect, iconRect,
                textRect, isSelected);
    // Paint the text.
      paintText(g, tabPlacement, tabPane.getFont(), fm, tabIndex, title,
                textRect, isSelected);
    // Paint icon if necessary.
    paintIcon(g, tabPlacement, tabIndex, icon, iconRect, isSelected);
    // Paint focus indicator.
    paintFocusIndicator(g, tabPlacement, rects, tabIndex, iconRect, textRect,
                        isSelected);
  }

  /**
   * This method lays out the tab and finds the location to paint the  icon
   * and text.
   *
   * @param tabPlacement The JTabbedPane's tab placement.
   * @param metrics The font metrics for the font to paint with.
   * @param tabIndex The tab index to paint.
   * @param title The string painted.
   * @param icon The icon painted.
   * @param tabRect The tab bounds.
   * @param iconRect The calculated icon bounds.
   * @param textRect The calculated text bounds.
   * @param isSelected Whether this tab is selected.
   */
  protected void layoutLabel(int tabPlacement, FontMetrics metrics,
                             int tabIndex, String title, Icon icon,
                             Rectangle tabRect, Rectangle iconRect,
                             Rectangle textRect, boolean isSelected)
  {
    SwingUtilities.layoutCompoundLabel(metrics, title, icon,
                                       SwingConstants.CENTER,
                                       SwingConstants.CENTER,
                                       SwingConstants.CENTER,
                                       SwingConstants.RIGHT, tabRect,
                                       iconRect, textRect, textIconGap);

    int shiftX = getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
    int shiftY = getTabLabelShiftY(tabPlacement, tabIndex, isSelected);

    iconRect.x += shiftX;
    iconRect.y += shiftY;

    textRect.x += shiftX;
    textRect.y += shiftY;
  }

  /**
   * This method paints the icon.
   *
   * @param g The Graphics object to paint.
   * @param tabPlacement The JTabbedPane's tab placement.
   * @param tabIndex The tab index to paint.
   * @param icon The icon to paint.
   * @param iconRect The bounds of the icon.
   * @param isSelected Whether this tab is selected.
   */
  protected void paintIcon(Graphics g, int tabPlacement, int tabIndex,
                           Icon icon, Rectangle iconRect, boolean isSelected)
  {
    if (icon != null)
    icon.paintIcon(tabPane, g, iconRect.x, iconRect.y);
  }

  /**
   * This method paints the text for the given tab.
   *
   * @param g The Graphics object to paint with.
   * @param tabPlacement The JTabbedPane's tab placement.
   * @param font The font to paint with.
   * @param metrics The fontmetrics of the given font.
   * @param tabIndex The tab index.
   * @param title The string to paint.
   * @param textRect The bounds of the string.
   * @param isSelected Whether this tab is selected.
   */
  protected void paintText(Graphics g, int tabPlacement, Font font,
                           FontMetrics metrics, int tabIndex, String title,
                           Rectangle textRect, boolean isSelected)
  {
    g.setFont(font);
    View textView = getTextViewForTab(tabIndex);
    if (textView != null)
      {
        textView.paint(g, textRect);
        return;
      }

    int mnemIndex = tabPane.getDisplayedMnemonicIndexAt(tabIndex);
    if (tabPane.isEnabled() && tabPane.isEnabledAt(tabIndex))
      {
    Color fg = tabPane.getForegroundAt(tabIndex);
        if (isSelected && (fg instanceof UIResource))
      {
            Color selectionForeground =
              UIManager.getColor("TabbedPane.selectionForeground");
            if (selectionForeground != null)
              fg = selectionForeground;
          }
        g.setColor(fg);

        if (mnemIndex != -1)
          BasicGraphicsUtils.drawStringUnderlineCharAt(g, title, mnemIndex,
                                                       textRect.x,
                                                       textRect.y
                                                       + metrics.getAscent());
        else
          g.drawString(title, textRect.x, textRect.y + metrics.getAscent());
      }
    else
      {
        Color bg = tabPane.getBackgroundAt(tabIndex);
        g.setColor(bg.brighter());
        if (mnemIndex != -1)
          BasicGraphicsUtils.drawStringUnderlineCharAt(g, title, mnemIndex,
                                                       textRect.x, textRect.y);
        else
          g.drawString(title, textRect.x, textRect.y);

        g.setColor(bg.darker());
        if (mnemIndex != -1)
          BasicGraphicsUtils.drawStringUnderlineCharAt(g, title, mnemIndex,
                                                       textRect.x + 1,
                                                       textRect.y + 1);
        else
          g.drawString(title, textRect.x + 1, textRect.y + 1);
      }
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
    if (tabPane.hasFocus() && isSelected)
      {
        Rectangle rect = rects[tabIndex];
        // The focus rectangle.
        int x;
        int y;
        int w;
        int h;

    g.setColor(focus);
        switch (tabPlacement)
        {
        case LEFT:
          x = rect.x + 3;
          y = rect.y + 3;
          w = rect.width - 5;
          h = rect.height - 6;
          break;
        case RIGHT:
          x = rect.x + 2;
          y = rect.y + 3;
          w = rect.width - 6;
          h = rect.height - 5;
          break;
        case BOTTOM:
          x = rect.x + 3;
          y = rect.y + 2;
          w = rect.width - 6;
          h = rect.height - 5;
          break;
        case TOP:
        default:
          x = rect.x + 3;
        y = rect.y + 3;
        w = rect.width - 6;
        h = rect.height - 5;
        }
        BasicGraphicsUtils.drawDashedRect(g, x, y, w, h);
      }
  }

  /**
   * This method paints the border for an individual tab.
   *
   * @param g The Graphics object to paint with.
   * @param tabPlacement The JTabbedPane's tab placement.
   * @param tabIndex The tab index.
   * @param x The x position of the tab.
   * @param y The y position of the tab.
   * @param w The width of the tab.
   * @param h The height of the tab.
   * @param isSelected Whether the tab is selected.
   */
  protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                int x, int y, int w, int h, boolean isSelected)
  {
    Color saved = g.getColor();

    if (! isSelected || tabPlacement != SwingConstants.TOP)
      {
        g.setColor(shadow);
        g.drawLine(x + 1, y + h - 1, x + w - 1, y + h - 1);
        g.setColor(darkShadow);
        g.drawLine(x, y + h, x + w, y + h);
      }

    if (! isSelected || tabPlacement != SwingConstants.LEFT)
      {
        g.setColor(darkShadow);
        g.drawLine(x + w, y, x + w, y + h);
        g.setColor(shadow);
        g.drawLine(x + w - 1, y + 1, x + w - 1, y + h - 1);
      }

    if (! isSelected || tabPlacement != SwingConstants.RIGHT)
      {
        g.setColor(lightHighlight);
        g.drawLine(x, y, x, y + h);
      }

    if (! isSelected || tabPlacement != SwingConstants.BOTTOM)
      {
        g.setColor(lightHighlight);
        g.drawLine(x, y, x + w, y);
      }

    g.setColor(saved);
  }

  /**
   * This method paints the background for an individual tab.
   *
   * @param g The Graphics object to paint with.
   * @param tabPlacement The JTabbedPane's tab placement.
   * @param tabIndex The tab index.
   * @param x The x position of the tab.
   * @param y The y position of the tab.
   * @param w The width of the tab.
   * @param h The height of the tab.
   * @param isSelected Whether the tab is selected.
   */
  protected void paintTabBackground(Graphics g, int tabPlacement,
                                    int tabIndex, int x, int y, int w, int h,
                                    boolean isSelected)
  {
    Color saved = g.getColor();
    if (isSelected)
      g.setColor(Color.LIGHT_GRAY);
    else
      {
        Color bg = tabPane.getBackgroundAt(tabIndex);
        if (bg == null)
          bg = Color.GRAY;
        g.setColor(bg);
      }

    g.fillRect(x, y, w, h);

    g.setColor(saved);
  }

  /**
   * This method paints the border around the content area.
   *
   * @param g The Graphics object to paint with.
   * @param tabPlacement The JTabbedPane's tab placement.
   * @param selectedIndex The index of the selected tab.
   */
  protected void paintContentBorder(Graphics g, int tabPlacement,
                                    int selectedIndex)
  {
    int width = tabPane.getWidth();
    int height = tabPane.getHeight();
    Insets insets = tabPane.getInsets();
    Insets tabAreaInsets = getTabAreaInsets(tabPlacement);

    // Calculate coordinates of content area.
    int x = insets.left;
    int y = insets.top;
    int w = width - insets.left - insets.right;
    int h = height - insets.top - insets.bottom;

    switch (tabPlacement)
    {
    case LEFT:
      x += calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
      w -= (x - insets.left);
      break;
    case RIGHT:
      w -= calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
      break;
    case BOTTOM:
      h -= calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
      break;
    case TOP:
    default:
      y += calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
      h -= (y - insets.top);
    }

    // Fill background if necessary.
    if (tabPane.isOpaque())
      {
        Color bg = UIManager.getColor("TabbedPane.contentAreaColor");
        g.setColor(bg);
        g.fillRect(x, y, w, h);
      }

    // Paint border.
    paintContentBorderTopEdge(g, tabPlacement, selectedIndex, x, y, w, h);
    paintContentBorderLeftEdge(g, tabPlacement, selectedIndex, x, y, w, h);
    paintContentBorderBottomEdge(g, tabPlacement, selectedIndex, x, y, w, h);
    paintContentBorderRightEdge(g, tabPlacement, selectedIndex, x, y, w, h);
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
    Color saved = g.getColor();
    g.setColor(lightHighlight);

    int startgap = rects[selectedIndex].x;
    int endgap = rects[selectedIndex].x + rects[selectedIndex].width;

    int diff = 0;

    if (tabPlacement == SwingConstants.TOP)
      {
        if (tabPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT)
          {
            Point p = findPointForIndex(currentScrollLocation);
            diff = p.x;
          }

        g.drawLine(x, y, startgap - diff, y);
        g.drawLine(endgap - diff, y, x + w, y);
      }
    else
      g.drawLine(x, y, x + w, y);

    g.setColor(saved);
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
    Color saved = g.getColor();
    g.setColor(lightHighlight);

    int startgap = rects[selectedIndex].y;
    int endgap = rects[selectedIndex].y + rects[selectedIndex].height;

    int diff = 0;

    if (tabPlacement == SwingConstants.LEFT)
      {
        if (tabPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT)
          {
            Point p = findPointForIndex(currentScrollLocation);
            diff = p.y;
          }

        g.drawLine(x, y, x, startgap - diff);
        g.drawLine(x, endgap - diff, x, y + h);
      }
    else
      g.drawLine(x, y, x, y + h);

    g.setColor(saved);
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
    Color saved = g.getColor();

    int startgap = rects[selectedIndex].x;
    int endgap = rects[selectedIndex].x + rects[selectedIndex].width;

    int diff = 0;

    if (tabPlacement == SwingConstants.BOTTOM)
      {
        if (tabPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT)
          {
            Point p = findPointForIndex(currentScrollLocation);
            diff = p.x;
          }

        g.setColor(shadow);
        g.drawLine(x + 1, y + h - 1, startgap - diff, y + h - 1);
        g.drawLine(endgap - diff, y + h - 1, x + w - 1, y + h - 1);

        g.setColor(darkShadow);
        g.drawLine(x, y + h, startgap - diff, y + h);
        g.drawLine(endgap - diff, y + h, x + w, y + h);
      }
    else
      {
        g.setColor(shadow);
        g.drawLine(x + 1, y + h - 1, x + w - 1, y + h - 1);
        g.setColor(darkShadow);
        g.drawLine(x, y + h, x + w, y + h);
      }

    g.setColor(saved);
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
    Color saved = g.getColor();
    int startgap = rects[selectedIndex].y;
    int endgap = rects[selectedIndex].y + rects[selectedIndex].height;

    int diff = 0;

    if (tabPlacement == SwingConstants.RIGHT)
      {
        if (tabPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT)
          {
            Point p = findPointForIndex(currentScrollLocation);
            diff = p.y;
          }

        g.setColor(shadow);
        g.drawLine(x + w - 1, y + 1, x + w - 1, startgap - diff);
        g.drawLine(x + w - 1, endgap - diff, x + w - 1, y + h - 1);

        g.setColor(darkShadow);
        g.drawLine(x + w, y, x + w, startgap - diff);
        g.drawLine(x + w, endgap - diff, x + w, y + h);
      }
    else
      {
        g.setColor(shadow);
        g.drawLine(x + w - 1, y + 1, x + w - 1, y + h - 1);
        g.setColor(darkShadow);
        g.drawLine(x + w, y, x + w, y + h);
      }

    g.setColor(saved);
  }

  /**
   * This method returns the tab bounds for the given index.
   *
   * @param pane The JTabbedPane.
   * @param i The index to look for.
   *
   * @return The bounds of the tab with the given index.
   */
  public Rectangle getTabBounds(JTabbedPane pane, int i)
  {
    return rects[i];
  }

  /**
   * This method returns the number of runs.
   *
   * @param pane The JTabbedPane.
   *
   * @return The number of runs.
   */
  public int getTabRunCount(JTabbedPane pane)
  {
    return runCount;
  }

  /**
   * This method returns the tab index given a coordinate.
   *
   * @param pane The JTabbedPane.
   * @param x The x coordinate.
   * @param y The y coordinate.
   *
   * @return The tab index that the coordinate lands in.
   */
  public int tabForCoordinate(JTabbedPane pane, int x, int y)
  {
    if (! tabPane.isValid())
      tabPane.validate();

    int tabCount = tabPane.getTabCount();
    int index = -1;
    for (int i = 0; i < tabCount; ++i)
      {
        if (rects[i].contains(x, y))
          {
            index = i;
            break;
          }
      }

    // FIXME: Handle scrollable tab layout.

    return index;
  }

  /**
   * This method returns the tab bounds in the given rectangle.
   *
   * @param tabIndex The index to get bounds for.
   * @param dest The rectangle to store bounds in.
   *
   * @return The rectangle passed in.
   */
  protected Rectangle getTabBounds(int tabIndex, Rectangle dest)
  {
    dest.setBounds(getTabBounds(tabPane, tabIndex));
    return dest;
  }

  /**
   * This method returns the component that is shown in  the content area.
   *
   * @return The component that is shown in the content area.
   */
  protected Component getVisibleComponent()
  {
    return tabPane.getComponentAt(tabPane.getSelectedIndex());
  }

  /**
   * This method sets the visible component.
   *
   * @param component The component to be set visible.
   */
  protected void setVisibleComponent(Component component)
  {
    component.setVisible(true);
    tabPane.setSelectedComponent(component);
  }

  /**
   * This method assures that enough rectangles are created given the
   * tabCount. The old array is copied to the  new one.
   *
   * @param tabCount The number of tabs.
   */
  protected void assureRectsCreated(int tabCount)
  {
    if (rects.length < tabCount)
      {
        Rectangle[] old = rects;
        rects = new Rectangle[tabCount];
        System.arraycopy(old, 0, rects, 0, old.length);
        for (int i = old.length; i < rects.length; i++)
          rects[i] = new Rectangle();
      }
  }

  /**
   * This method expands the tabRuns array to give it more room. The old array
   * is copied to the new one.
   */
  protected void expandTabRunsArray()
  {
    // This method adds another 10 index positions to the tabRuns array.
    if (tabRuns == null)
      tabRuns = new int[10];
    else
      {
        int[] newRuns = new int[tabRuns.length + 10];
        System.arraycopy(tabRuns, 0, newRuns, 0, tabRuns.length);
        tabRuns = newRuns;
      }
  }

  /**
   * This method returns which run a particular tab belongs to.
   *
   * @param tabCount The number of tabs.
   * @param tabIndex The tab to find.
   *
   * @return The tabRuns index that it belongs to.
   */
  protected int getRunForTab(int tabCount, int tabIndex)
  {
    if (runCount == 1 && tabIndex < tabCount && tabIndex >= 0)
      return 1;
    for (int i = 0; i < runCount; i++)
      {
        int first = lastTabInRun(tabCount, getPreviousTabRun(i)) + 1;
        if (first == tabCount)
          first = 0;
        int last = lastTabInRun(tabCount, i);
        if (last >= tabIndex && first <= tabIndex)
          return i;
      }
    return -1;
  }

  /**
   * This method returns the index of the last tab in  a run.
   *
   * @param tabCount The number of tabs.
   * @param run The run to check.
   *
   * @return The last tab in the given run.
   */
  protected int lastTabInRun(int tabCount, int run)
  {
    if (tabRuns[run] == 0)
      return tabCount - 1;
    else
      return tabRuns[run] - 1;
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
    return tabRunOverlay;
  }

  /**
   * This method returns the tab run indent. It is used in WRAP_TAB_LAYOUT and
   * makes each tab run start indented by a certain amount.
   *
   * @param tabPlacement The JTabbedPane's tab placement.
   * @param run The run to get indent for.
   *
   * @return The amount a run should be indented.
   */
  protected int getTabRunIndent(int tabPlacement, int run)
  {
    return 0;
  }

  /**
   * This method returns whether a tab run should be padded.
   *
   * @param tabPlacement The JTabbedPane's tab placement.
   * @param run The run to check.
   *
   * @return Whether the given run should be padded.
   */
  protected boolean shouldPadTabRun(int tabPlacement, int run)
  {
    return true;
  }

  /**
   * This method returns whether the tab runs should be rotated.
   *
   * @param tabPlacement The JTabbedPane's tab placement.
   *
   * @return Whether runs should be rotated.
   */
  protected boolean shouldRotateTabRuns(int tabPlacement)
  {
    return true;
  }

  /**
   * This method returns an icon for the tab. If the tab is disabled, it
   * should return the disabledIcon. If it is enabled, then it should return
   * the default icon.
   *
   * @param tabIndex The tab index to get an icon for.
   *
   * @return The icon for the tab index.
   */
  protected Icon getIconForTab(int tabIndex)
  {
    if (tabPane.isEnabledAt(tabIndex))
      return tabPane.getIconAt(tabIndex);
    else
      return tabPane.getDisabledIconAt(tabIndex);
  }

  /**
   * This method returns a view that can paint the text for the label.
   *
   * @param tabIndex The tab index to get a view for.
   *
   * @return The view for the tab index.
   */
  protected View getTextViewForTab(int tabIndex)
  {
    return null;
  }

  /**
   * This method returns the tab height, including insets, for the given index
   * and fontheight.
   *
   * @param tabPlacement The JTabbedPane's tab placement.
   * @param tabIndex The index of the tab to calculate.
   * @param fontHeight The font height.
   *
   * @return This tab's height.
   */
  protected int calculateTabHeight(int tabPlacement, int tabIndex,
                                   int fontHeight)
  {
    // FIXME: Handle HTML somehow.

    int height = fontHeight;
    Icon icon = getIconForTab(tabIndex);
    Insets tabInsets = getTabInsets(tabPlacement, tabIndex);
    if (icon != null)
      height = Math.max(height, icon.getIconHeight());
    height += tabInsets.top + tabInsets.bottom + 2;
    return height;
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
    maxTabHeight = 0;

    FontMetrics fm = getFontMetrics();
    int fontHeight = fm.getHeight();

    for (int i = 0; i < tabPane.getTabCount(); i++)
      maxTabHeight = Math.max(calculateTabHeight(tabPlacement, i, fontHeight),
                              maxTabHeight);

    return maxTabHeight;
  }

  /**
   * This method calculates the tab width, including insets, for the given tab
   * index and font metrics.
   *
   * @param tabPlacement The JTabbedPane's tab placement.
   * @param tabIndex The tab index to calculate for.
   * @param metrics The font's metrics.
   *
   * @return The tab width for the given index.
   */
  protected int calculateTabWidth(int tabPlacement, int tabIndex,
                                  FontMetrics metrics)
  {
    Icon icon = getIconForTab(tabIndex);
    Insets insets = getTabInsets(tabPlacement, tabIndex);

    int width = 0;
    if (icon != null)
      {
        Rectangle vr = new Rectangle();
        Rectangle ir = new Rectangle();
        Rectangle tr = new Rectangle();
        layoutLabel(tabPlacement, getFontMetrics(), tabIndex,
                    tabPane.getTitleAt(tabIndex), icon, vr, ir, tr,
                    tabIndex == tabPane.getSelectedIndex());
        width = tr.union(ir).width;
      }
    else
      width = metrics.stringWidth(tabPane.getTitleAt(tabIndex));

    width += insets.left + insets.right;
    return width;
  }

  /**
   * This method calculates the max tab width.
   *
   * @param tabPlacement The JTabbedPane's tab placement.
   *
   * @return The maximum tab width.
   */
  protected int calculateMaxTabWidth(int tabPlacement)
  {
    maxTabWidth = 0;

    FontMetrics fm = getFontMetrics();

    for (int i = 0; i < tabPane.getTabCount(); i++)
      maxTabWidth = Math.max(calculateTabWidth(tabPlacement, i, fm),
                             maxTabWidth);

    return maxTabWidth;
  }

  /**
   * This method calculates the tab area height, including insets, for the
   * given amount of runs and tab height.
   *
   * @param tabPlacement The JTabbedPane's tab placement.
   * @param horizRunCount The number of runs.
   * @param maxTabHeight The max tab height.
   *
   * @return The tab area height.
   */
  protected int calculateTabAreaHeight(int tabPlacement, int horizRunCount,
                                       int maxTabHeight)
  {
    Insets insets = getTabAreaInsets(tabPlacement);
    int tabAreaHeight = horizRunCount * maxTabHeight
                        - (horizRunCount - 1) * tabRunOverlay;

    tabAreaHeight += insets.top + insets.bottom;

    return tabAreaHeight;
  }

  /**
   * This method calculates the tab area width, including insets, for the
   * given amount of runs and tab width.
   *
   * @param tabPlacement The JTabbedPane's tab placement.
   * @param vertRunCount The number of runs.
   * @param maxTabWidth The max tab width.
   *
   * @return The tab area width.
   */
  protected int calculateTabAreaWidth(int tabPlacement, int vertRunCount,
                                      int maxTabWidth)
  {
    Insets insets = getTabAreaInsets(tabPlacement);
    int tabAreaWidth = vertRunCount * maxTabWidth
                       - (vertRunCount - 1) * tabRunOverlay;

    tabAreaWidth += insets.left + insets.right;

    return tabAreaWidth;
  }

  /**
   * This method returns the tab insets appropriately rotated.
   *
   * @param tabPlacement The JTabbedPane's tab placement.
   * @param tabIndex The tab index.
   *
   * @return The tab insets for the given index.
   */
  protected Insets getTabInsets(int tabPlacement, int tabIndex)
  {
    return tabInsets;
  }

  /**
   * This method returns the selected tab pad insets appropriately rotated.
   *
   * @param tabPlacement The JTabbedPane's tab placement.
   *
   * @return The selected tab pad insets.
   */
  protected Insets getSelectedTabPadInsets(int tabPlacement)
  {
    Insets target = new Insets(0, 0, 0, 0);
    rotateInsets(selectedTabPadInsets, target, tabPlacement);
    return target;
  }

  /**
   * This method returns the tab area insets appropriately rotated.
   *
   * @param tabPlacement The JTabbedPane's tab placement.
   *
   * @return The tab area insets.
   */
  protected Insets getTabAreaInsets(int tabPlacement)
  {
    Insets target = new Insets(0, 0, 0, 0);
    rotateInsets(tabAreaInsets, target, tabPlacement);
    return target;
  }

  /**
   * This method returns the content border insets appropriately rotated.
   *
   * @param tabPlacement The JTabbedPane's tab placement.
   *
   * @return The content border insets.
   */
  protected Insets getContentBorderInsets(int tabPlacement)
  {
    Insets target = new Insets(0, 0, 0, 0);
    rotateInsets(contentBorderInsets, target, tabPlacement);
    return target;
  }

  /**
   * This method returns the fontmetrics for the font of the JTabbedPane.
   *
   * @return The font metrics for the JTabbedPane.
   */
  protected FontMetrics getFontMetrics()
  {
    FontMetrics fm = tabPane.getFontMetrics(tabPane.getFont());
    return fm;
  }

  /**
   * This method navigates from the selected tab into the given direction. As
   * a result, a new tab will be selected (if possible).
   *
   * @param direction The direction to navigate in.
   */
  protected void navigateSelectedTab(int direction)
  {
    int tabPlacement = tabPane.getTabPlacement();
    if (tabPlacement == SwingConstants.TOP
        || tabPlacement == SwingConstants.BOTTOM)
      {
        if (direction == SwingConstants.WEST)
          selectPreviousTabInRun(tabPane.getSelectedIndex());
        else if (direction == SwingConstants.EAST)
          selectNextTabInRun(tabPane.getSelectedIndex());

        else
          {
            int offset = getTabRunOffset(tabPlacement, tabPane.getTabCount(),
                                         tabPane.getSelectedIndex(),
                                         (tabPlacement == SwingConstants.RIGHT)
                                         ? true : false);
            selectAdjacentRunTab(tabPlacement, tabPane.getSelectedIndex(),
                                 offset);
          }
      }
    if (tabPlacement == SwingConstants.LEFT
        || tabPlacement == SwingConstants.RIGHT)
      {
        if (direction == SwingConstants.NORTH)
          selectPreviousTabInRun(tabPane.getSelectedIndex());
        else if (direction == SwingConstants.SOUTH)
          selectNextTabInRun(tabPane.getSelectedIndex());
        else
          {
            int offset = getTabRunOffset(tabPlacement, tabPane.getTabCount(),
                                         tabPane.getSelectedIndex(),
                                         (tabPlacement == SwingConstants.RIGHT)
                                         ? true : false);
            selectAdjacentRunTab(tabPlacement, tabPane.getSelectedIndex(),
                                 offset);
          }
      }
  }

  /**
   * This method selects the next tab in the run.
   *
   * @param current The current selected index.
   */
  protected void selectNextTabInRun(int current)
  {
    tabPane.setSelectedIndex(getNextTabIndexInRun(tabPane.getTabCount(),
                                                  current));
  }

  /**
   * This method selects the previous tab in the run.
   *
   * @param current The current selected index.
   */
  protected void selectPreviousTabInRun(int current)
  {
    tabPane.setSelectedIndex(getPreviousTabIndexInRun(tabPane.getTabCount(),
                                                      current));
  }

  /**
   * This method selects the next tab (regardless of runs).
   *
   * @param current The current selected index.
   */
  protected void selectNextTab(int current)
  {
    tabPane.setSelectedIndex(getNextTabIndex(current));
  }

  /**
   * This method selects the previous tab (regardless of runs).
   *
   * @param current The current selected index.
   */
  protected void selectPreviousTab(int current)
  {
    tabPane.setSelectedIndex(getPreviousTabIndex(current));
  }

  /**
   * This method selects the correct tab given an offset from the current tab
   * index. If the tab placement is TOP or BOTTOM, the offset will be in the
   * y direction, otherwise, it will be in the x direction. A new coordinate
   * will be found by adding the offset to the current location of the tab.
   * The tab that the new location will be selected.
   *
   * @param tabPlacement The JTabbedPane's tab placement.
   * @param tabIndex The tab to start from.
   * @param offset The coordinate offset.
   */
  protected void selectAdjacentRunTab(int tabPlacement, int tabIndex,
                                      int offset)
  {
    int x = rects[tabIndex].x + rects[tabIndex].width / 2;
    int y = rects[tabIndex].y + rects[tabIndex].height / 2;

    switch (tabPlacement)
    {
    case SwingConstants.TOP:
    case SwingConstants.BOTTOM:
      y += offset;
      break;
    case SwingConstants.RIGHT:
    case SwingConstants.LEFT:
      x += offset;
      break;
    }

    int index = tabForCoordinate(tabPane, x, y);
    if (index != -1)
      tabPane.setSelectedIndex(index);
  }

  // This method is called when you press up/down to cycle through tab runs.
  // it returns the distance (between the two runs' x/y position.
  // where one run is the current selected run and the other run is the run in the
  // direction of the scroll (dictated by the forward flag)
  // the offset is an absolute value of the difference

  /**
   * This method calculates the offset distance for use in
   * selectAdjacentRunTab. The offset returned will be a difference in the y
   * coordinate between the run in  the desired direction and the current run
   * (for tabPlacement in TOP or BOTTOM). Use x coordinate for LEFT and
   * RIGHT.
   *
   * @param tabPlacement The JTabbedPane's tab placement.
   * @param tabCount The number of tabs.
   * @param tabIndex The starting index.
   * @param forward If forward, the run in the desired direction will be the
   *        next run.
   *
   * @return The offset between the two runs.
   */
  protected int getTabRunOffset(int tabPlacement, int tabCount, int tabIndex,
                                boolean forward)
  {
    int currRun = getRunForTab(tabCount, tabIndex);
    int offset;
    int nextRun = (forward) ? getNextTabRun(currRun) : getPreviousTabRun(currRun);
    if (tabPlacement == SwingConstants.TOP
        || tabPlacement == SwingConstants.BOTTOM)
      offset = rects[lastTabInRun(tabCount, nextRun)].y
               - rects[lastTabInRun(tabCount, currRun)].y;
    else
      offset = rects[lastTabInRun(tabCount, nextRun)].x
               - rects[lastTabInRun(tabCount, currRun)].x;
    return offset;
  }

  /**
   * This method returns the previous tab index.
   *
   * @param base The index to start from.
   *
   * @return The previous tab index.
   */
  protected int getPreviousTabIndex(int base)
  {
    base--;
    if (base < 0)
      return tabPane.getTabCount() - 1;
    return base;
  }

  /**
   * This method returns the next tab index.
   *
   * @param base The index to start from.
   *
   * @return The next tab index.
   */
  protected int getNextTabIndex(int base)
  {
    base++;
    if (base == tabPane.getTabCount())
      return 0;
    return base;
  }

  /**
   * This method returns the next tab index in the run. If the next index is
   * out of this run, it will return the starting tab index for the run.
   *
   * @param tabCount The number of tabs.
   * @param base The index to start from.
   *
   * @return The next tab index in the run.
   */
  protected int getNextTabIndexInRun(int tabCount, int base)
  {
    int index = getNextTabIndex(base);
    int run = getRunForTab(tabCount, base);
    if (index == lastTabInRun(tabCount, run) + 1)
      index = lastTabInRun(tabCount, getPreviousTabRun(run)) + 1;
    return getNextTabIndex(base);
  }

  /**
   * This method returns the previous tab index in the run. If the previous
   * index is out of this run, it will return the last index for the run.
   *
   * @param tabCount The number of tabs.
   * @param base The index to start from.
   *
   * @return The previous tab index in the run.
   */
  protected int getPreviousTabIndexInRun(int tabCount, int base)
  {
    int index = getPreviousTabIndex(base);
    int run = getRunForTab(tabCount, base);
    if (index == lastTabInRun(tabCount, getPreviousTabRun(run)))
      index = lastTabInRun(tabCount, run);
    return getPreviousTabIndex(base);
  }

  /**
   * This method returns the index of the previous run.
   *
   * @param baseRun The run to start from.
   *
   * @return The index of the previous run.
   */
  protected int getPreviousTabRun(int baseRun)
  {
    if (getTabRunCount(tabPane) == 1)
      return 1;

    int prevRun = --baseRun;
    if (prevRun < 0)
      prevRun = getTabRunCount(tabPane) - 1;
    return prevRun;
  }

  /**
   * This method returns the index of the next run.
   *
   * @param baseRun The run to start from.
   *
   * @return The index of the next run.
   */
  protected int getNextTabRun(int baseRun)
  {
    if (getTabRunCount(tabPane) == 1)
      return 1;

    int nextRun = ++baseRun;
    if (nextRun == getTabRunCount(tabPane))
      nextRun = 0;
    return nextRun;
  }

  /**
   * This method rotates the insets given a direction to rotate them in.
   * Target placement should be one of TOP, LEFT, BOTTOM, RIGHT. The  rotated
   * insets will be stored in targetInsets. Passing in TOP as  the direction
   * does nothing. Passing in LEFT switches top and left, right and bottom.
   * Passing in BOTTOM switches top and bottom. Passing in RIGHT switches top
   * for left, left for bottom, bottom for right, and right for top.
   *
   * @param topInsets The reference insets.
   * @param targetInsets An Insets object to store the new insets.
   * @param targetPlacement The rotation direction.
   */
  protected static void rotateInsets(Insets topInsets, Insets targetInsets,
                                     int targetPlacement)
  {
    // Sun's version will happily throw an NPE if params are null,
    // so I won't check it either.
    switch (targetPlacement)
    {
    case SwingConstants.TOP:
      targetInsets.top = topInsets.top;
      targetInsets.left = topInsets.left;
      targetInsets.right = topInsets.right;
      targetInsets.bottom = topInsets.bottom;
      break;
    case SwingConstants.LEFT:
      targetInsets.left = topInsets.top;
      targetInsets.top = topInsets.left;
      targetInsets.right = topInsets.bottom;
      targetInsets.bottom = topInsets.right;
      break;
    case SwingConstants.BOTTOM:
      targetInsets.top = topInsets.bottom;
      targetInsets.bottom = topInsets.top;
      targetInsets.left = topInsets.left;
      targetInsets.right = topInsets.right;
      break;
    case SwingConstants.RIGHT:
      targetInsets.top = topInsets.left;
      targetInsets.left = topInsets.bottom;
      targetInsets.bottom = topInsets.right;
      targetInsets.right = topInsets.top;
      break;
    }
  }

  /**
   * Sets the tab which should be highlighted when in rollover mode. And
   * <code>index</code> of <code>-1</code> means that the rollover tab
   * is deselected (i.e. the mouse is outside of the tabarea).
   *
   * @param index the index of the tab that is under the mouse, <code>-1</code>
   *        for no tab
   *
   * @since 1.5
   */
  protected void setRolloverTab(int index)
  {
    rolloverTab = index;
  }

  /**
   * Retunrs the index of the tab over which the mouse is currently moving,
   * or <code>-1</code> for no tab.
   *
   * @return the index of the tab over which the mouse is currently moving,
   *         or <code>-1</code> for no tab
   *
   * @since 1.5
   */
  protected int getRolloverTab()
  {
    return rolloverTab;
  }
}
