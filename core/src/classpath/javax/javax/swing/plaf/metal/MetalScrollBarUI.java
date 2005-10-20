/* MetalScrollBarUI.java
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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 * A UI delegate for the {@link JScrollBar} component.
 */
public class MetalScrollBarUI extends BasicScrollBarUI
{
  
  /**
   * A property change handler for the UI delegate that monitors for
   * changes to the "JScrollBar.isFreeStanding" property, and updates
   * the buttons and track rendering as appropriate.
   */
  class MetalScrollBarPropertyChangeHandler 
    extends BasicScrollBarUI.PropertyChangeHandler
  {
    /**
     * Creates a new handler.
     * 
     * @see #createPropertyChangeListener()
     */
    public MetalScrollBarPropertyChangeHandler()
    {
      // Nothing to do here.
    }
    
    /**
     * Handles a property change event.  If the event name is
     * <code>JSlider.isFreeStanding</code>, this method updates the 
     * delegate, otherwise the event is passed up to the super class.
     * 
     * @param e  the property change event.
     */
    public void propertyChange(PropertyChangeEvent e)
    {
      if (e.getPropertyName().equals(FREE_STANDING_PROP))
        {
          Boolean prop = (Boolean) e.getNewValue();
          isFreeStanding = (prop == null ? true : prop.booleanValue());
	  if (increaseButton != null)
          increaseButton.setFreeStanding(isFreeStanding);
	  if (decreaseButton != null)
          decreaseButton.setFreeStanding(isFreeStanding);
        }
      else
	super.propertyChange(e);
    }
  }
  
  /** The name for the 'free standing' property. */
  public static final String FREE_STANDING_PROP = "JScrollBar.isFreeStanding";

  /** The minimum thumb size */
  private static final Dimension MIN_THUMB_SIZE = new Dimension(17, 17);

  /** The button that increases the value in the scroll bar. */
  protected MetalScrollButton increaseButton;
  
  /** The button that decreases the value in the scroll bar. */
  protected MetalScrollButton decreaseButton;
  
  /** The scroll bar width. */
  protected int scrollBarWidth;
  
  /** 
   * A flag that indicates whether the scroll bar is "free standing", which 
   * means it has complete borders and can be used anywhere in the UI.  A 
   * scroll bar which is not free standing has borders missing from one
   * side, and relies on being part of another container with its own borders
   * to look right visually. */
  protected boolean isFreeStanding;
  
  /**
   * Constructs a new instance of MetalScrollBarUI.
   */
  public MetalScrollBarUI()
  {
    super();
  }

  /**
   * Returns an instance of MetalScrollBarUI.
   *
   * @param component the component for which we return an UI instance
   *
   * @return an instance of MetalScrollBarUI
   */
  public static ComponentUI createUI(JComponent component)
  {
    return new MetalScrollBarUI();
  }

  /**
   * Installs the defaults.
   */
  protected void installDefaults()
  {    
    // need to initialise isFreeStanding before calling the super class, 
    // so that the value is set when createIncreaseButton() and 
    // createDecreaseButton() are called (unless there is somewhere earlier
    // that we can do this).
    Boolean prop = (Boolean) scrollbar.getClientProperty(FREE_STANDING_PROP);
    isFreeStanding = (prop == null ? true : prop.booleanValue());
    super.installDefaults();
  }
    
  /**
   * Creates a property change listener for the delegate to use.  This
   * overrides the method to provide a custom listener for the 
   * {@link MetalLookAndFeel} that can handle the 
   * <code>JScrollBar.isFreeStanding</code> property.
   * 
   * @return A property change listener.
   */
  protected PropertyChangeListener createPropertyChangeListener()
  {
    return new MetalScrollBarPropertyChangeHandler();
  }
  
  /**
   * Creates a new button to use as the control at the lower end of the
   * {@link JScrollBar}.
   * 
   * @param orientation  the orientation of the button ({@link #NORTH},
   *                     {@link #SOUTH}, {@link #EAST} or {@link #WEST}).
   * 
   * @return The button.
   */
  protected JButton createDecreaseButton(int orientation)
  {
    UIDefaults defaults = UIManager.getLookAndFeelDefaults();
    scrollBarWidth = defaults.getInt("ScrollBar.width");
    return new MetalScrollButton(orientation, scrollBarWidth, isFreeStanding);
  }

  /**
   * Creates a new button to use as the control at the upper end of the
   * {@link JScrollBar}.
   * 
   * @param orientation  the orientation of the button ({@link #NORTH},
   *                     {@link #SOUTH}, {@link #EAST} or {@link #WEST}).
   * 
   * @return The button.
   */
  protected JButton createIncreaseButton(int orientation)
  {
    UIDefaults defaults = UIManager.getLookAndFeelDefaults();
    scrollBarWidth = defaults.getInt("ScrollBar.width");
    return new MetalScrollButton(orientation, scrollBarWidth, isFreeStanding);
  }
  
  /**
   * Paints the track for the scrollbar.
   * 
   * @param g  the graphics device.
   * @param c  the component.
   * @param trackBounds  the track bounds.
   */
  protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds)
  {
    g.setColor(MetalLookAndFeel.getControl());
    g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, 
            trackBounds.height);
    if (scrollbar.getOrientation() == HORIZONTAL) 
      paintTrackHorizontal(g, c, trackBounds.x, trackBounds.y, 
          trackBounds.width, trackBounds.height);
    else 
      paintTrackVertical(g, c, trackBounds.x, trackBounds.y, 
          trackBounds.width, trackBounds.height);
    
  }
  
  /**
   * Paints the track for a horizontal scrollbar.
   * 
   * @param g  the graphics device.
   * @param c  the component.
   * @param x  the x-coordinate for the track bounds.
   * @param y  the y-coordinate for the track bounds.
   * @param w  the width for the track bounds.
   * @param h  the height for the track bounds.
   */
  private void paintTrackHorizontal(Graphics g, JComponent c, 
      int x, int y, int w, int h)
  {
    if (c.isEnabled())
      {
        g.setColor(MetalLookAndFeel.getControlDarkShadow());
        g.drawLine(x, y, x, y + h - 1);
        g.drawLine(x, y, x + w - 1, y);
        g.drawLine(x + w - 1, y, x + w - 1, y + h - 1);
        
        g.setColor(MetalLookAndFeel.getControlShadow());
        g.drawLine(x + 1, y + 1, x + 1, y + h - 1);
        g.drawLine(x + 1, y + 1, x + w - 2, y + 1);
        
        if (isFreeStanding) 
          {
            g.setColor(MetalLookAndFeel.getControlDarkShadow());
            g.drawLine(x, y + h - 2, x + w - 1, y + h - 2);
            g.setColor(MetalLookAndFeel.getControlShadow());
            g.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
          }
      }
    else
      {
        g.setColor(MetalLookAndFeel.getControlDisabled());
        g.drawRect(x, y, w - 1, h - 1);
      }
  }
    
  /**
   * Paints the track for a vertical scrollbar.
   * 
   * @param g  the graphics device.
   * @param c  the component.
   * @param x  the x-coordinate for the track bounds.
   * @param y  the y-coordinate for the track bounds.
   * @param w  the width for the track bounds.
   * @param h  the height for the track bounds.
   */
  protected void paintTrackVertical(Graphics g, JComponent c, 
      int x, int y, int w, int h)
  {
    if (c.isEnabled())
      {
        g.setColor(MetalLookAndFeel.getControlDarkShadow());
        g.drawLine(x, y, x, y + h - 1);
        g.drawLine(x, y, x + w - 1, y);
        g.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
        
        g.setColor(MetalLookAndFeel.getControlShadow());
        g.drawLine(x + 1, y + 1, x + w - 1, y + 1);
        g.drawLine(x + 1, y + 1, x + 1, y + h - 2);
        g.drawLine(x + 1, y + h - 2, x + w - 1, y + h - 2);
        
        if (isFreeStanding) 
          {
            g.setColor(MetalLookAndFeel.getControlDarkShadow());
            g.drawLine(x + w - 2, y, x + w - 2, y + h - 1);
            g.setColor(MetalLookAndFeel.getControlHighlight());
            g.drawLine(x + w - 1, y, x + w - 1, y + h - 1);
          }
      }
    else
      {
        g.setColor(MetalLookAndFeel.getControlDisabled());
        g.drawRect(x, y, w - 1, h - 1);
      }
  }

  /**
   * Paints the slider button of the ScrollBar.
   *
   * @param g the Graphics context to use
   * @param c the JComponent on which we paint
   * @param thumbBounds the rectangle that is the slider button
   */
  protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds)
  {
    // a disabled scrollbar has no thumb in the metal look and feel
    if (!c.isEnabled())
      return;
    
    // first we fill the background
    g.setColor(thumbColor);
    g.fillRect(thumbBounds.x, thumbBounds.y, thumbBounds.width,
               thumbBounds.height);

    // draw the outer dark line
    int hAdj = 1;
    int wAdj = 1;
    if (scrollbar.getOrientation() == HORIZONTAL)
      hAdj++;
    else
      wAdj++;
    
    g.setColor(new Color(102, 102, 153));
    g.drawRect(thumbBounds.x, thumbBounds.y, thumbBounds.width - wAdj,
               thumbBounds.height - hAdj);

    // draw the inner light line
    g.setColor(thumbHighlightColor);
    g.drawLine(thumbBounds.x + 1, thumbBounds.y + 1,
               thumbBounds.x + thumbBounds.width - 2,
               thumbBounds.y + 1);
    g.drawLine(thumbBounds.x + 1, thumbBounds.y + 1,
               thumbBounds.x + 1,
               thumbBounds.y + thumbBounds.height - 2);

    // draw the shadow line
    UIDefaults def = UIManager.getLookAndFeelDefaults();
    g.setColor(def.getColor("ScrollBar.shadow"));
    g.drawLine(thumbBounds.x + 1, thumbBounds.y + thumbBounds.height,
               thumbBounds.x + thumbBounds.width,
               thumbBounds.y + thumbBounds.height);

    // draw the pattern
    MetalUtils.fillMetalPattern(c, g, thumbBounds.x + 3, thumbBounds.y + 3,
                                thumbBounds.width - 6, thumbBounds.height - 6,
                                thumbHighlightColor, new Color(102, 102, 153));
  }

  /**
   * This method returns the minimum thumb size.
   *
   * @return The minimum thumb size.
   */
  protected Dimension getMinimumThumbSize()
  {
    return MIN_THUMB_SIZE;
  }
  
}

