/* SwingUtilities.java --
   Copyright (C) 2002 Free Software Foundation, Inc.

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

package javax.swing;

import java.applet.Applet;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleStateSet;


/**
 * This class contains a number of static utility functions which are
 * useful when drawing swing components, dispatching events, or calculating
 * regions which need painting.
 *
 * @author Graydon Hoare (graydon&064;redhat.com)
 */
public class SwingUtilities implements SwingConstants
{

  /**
   * Calculates the portion of the base rectangle which is inside the
   * insets.
   *
   * @param base The rectangle to apply the insets to
   * @param insets The insets to apply to the base rectangle
   * @param ret A rectangle to use for storing the return value, or
   * <code>null</code>
   *
   * @return The calculated area inside the base rectangle and its insets,
   * either stored in ret or a new Rectangle if ret is <code>null</code>
   *
   * @see #calculateInnerArea
   */
  public static Rectangle calculateInsetArea(Rectangle base, Insets insets,
                                             Rectangle ret)
  {
    if (ret == null)
      ret = new Rectangle();
    ret.setBounds(base.x + insets.left, base.y + insets.top,
                  base.width - (insets.left + insets.right),
                  base.height - (insets.top + insets.bottom));
    return ret;
  }

  /**
   * Calculates the portion of the component's bounds which is inside the
   * component's border insets. This area is usually the area a component
   * should confine its painting to. The coordinates are returned in terms
   * of the <em>component's</em> coordinate system, where (0,0) is the
   * upper left corner of the component's bounds.
   *
   * @param c The component to measure the bounds of
   * @param r A Rectangle to store the return value in, or
   * <code>null</code>
   *
   * @return The calculated area inside the component and its border
   * insets
   *
   * @see #calculateInsetArea
   */
  public static Rectangle calculateInnerArea(JComponent c, Rectangle r)
  {
    Rectangle b = getLocalBounds(c);
    return calculateInsetArea(b, c.getInsets(), r);
  }

  /**
   * Calculates the bounds of a component in the component's own coordinate
   * space. The result has the same height and width as the component's
   * bounds, but its location is set to (0,0).
   *
   * @param aComponent The component to measure
   *
   * @return The component's bounds in its local coordinate space
   */
  public static Rectangle getLocalBounds(Component aComponent)
  {
    Rectangle bounds = aComponent.getBounds();
    return new Rectangle(0, 0, bounds.width, bounds.height);
  }

  /**
   * Returns the font metrics object for a given font. The metrics can be
   * used to calculate crude bounding boxes and positioning information,
   * for laying out components with textual elements.
   *
   * @param font The font to get metrics for
   *
   * @return The font's metrics
   *
   * @see java.awt.font.GlyphMetrics
   */
  public static FontMetrics getFontMetrics(Font font)
  {
    return Toolkit.getDefaultToolkit().getFontMetrics(font);
  }

  /**
   * If <code>comp</code> is a RootPaneContainer, return its JRootPane.
   * Otherwise call <code>getAncestorOfClass(JRootPane.class, a)</code>.
   *
   * @param comp The component to get the JRootPane of
   *
   * @return a suitable JRootPane for <code>comp</code>, or <code>null</code>
   * 
   * @see javax.swing.RootPaneContainer#getRootPane
   * @see #getAncestorOfClass
   */
  public static JRootPane getRootPane(Component comp)
  {
    if (comp instanceof RootPaneContainer)
      return ((RootPaneContainer)comp).getRootPane();
    else
      return (JRootPane) getAncestorOfClass(JRootPane.class, comp);
  }

  /**
   * Returns the least ancestor of <code>comp</code> which has the
   * specified name.
   *
   * @param name The name to search for
   * @param comp The component to search the ancestors of
   *
   * @return The nearest ancestor of <code>comp</code> with the given
   * name, or <code>null</code> if no such ancestor exists
   *
   * @see java.awt.Component#getName
   * @see #getAncestorOfClass
   */
  public static Container getAncestorNamed(String name, Component comp)
  {
    while (comp != null && (comp.getName() != name))
      comp = comp.getParent();
    return (Container) comp;
  }

  /**
   * Returns the least ancestor of <code>comp</code> which is an instance
   * of the specified class.
   *
   * @param c The class to search for
   * @param comp The component to search the ancestors of
   *
   * @return The nearest ancestor of <code>comp</code> which is an instance
   * of the given class, or <code>null</code> if no such ancestor exists
   *
   * @see #getAncestorOfClass
   * @see #windowForComponent
   * @see 
   * 
   */
  public static Container getAncestorOfClass(Class c, Component comp)
  {
    while (comp != null && (! c.isInstance(comp)))
      comp = comp.getParent();
    return (Container) comp;
  }

  /**
   * Equivalent to calling <code>getAncestorOfClass(Window, comp)</code>.
   *
   * @param comp The component to search for an ancestor window 
   *
   * @return An ancestral window, or <code>null</code> if none exists
   */
  public static Window windowForComponent(Component comp)
  {
    return (Window) getAncestorOfClass(Window.class, comp);
  }

  /**
   * Returns the "root" of the component tree containint <code>comp</code>
   * The root is defined as either the <em>least</em> ancestor of
   * <code>comp</code> which is a {@link Window}, or the <em>greatest</em>
   * ancestor of <code>comp</code> which is a {@link Applet} if no {@link
   * Window} ancestors are found.
   *
   * @param comp The component to search for a root
   *
   * @return The root of the component's tree, or <code>null</code>
   */
  public static Component getRoot(Component comp)
  {
    Applet app = null;
    Window win = null;

    while (comp != null)
      {
        if (win == null && comp instanceof Window)
          win = (Window) comp;
        else if (comp instanceof Applet)
          app = (Applet) comp;
        comp = comp.getParent();
      }

    if (win != null)
      return win;
    else
      return app;
  }

  /**
   * Return true if a descends from b, in other words if b is an
   * ancestor of a.
   *
   * @param a The child to search the ancestry of
   * @param b The potential ancestor to search for
   *
   * @return true if a is a descendent of b, false otherwise
   */
  public static boolean isDescendingFrom(Component a, Component b)
  {
    while (true)
      {
        if (a == null || b == null)
          return false;
        if (a == b)
          return true;
        a = a.getParent();
      }
  }

  /**
   * Returns the deepest descendent of parent which is both visible and
   * contains the point <code>(x,y)</code>. Returns parent when either
   * parent is not a container, or has no children which contain
   * <code>(x,y)</code>. Returns <code>null</code> when either
   * <code>(x,y)</code> is outside the bounds of parent, or parent is
   * <code>null</code>.
   * 
   * @param parent The component to search the descendents of
   * @param x Horizontal coordinate to search for
   * @param y Vertical coordinate to search for
   *
   * @return A component containing <code>(x,y)</code>, or
   * <code>null</code>
   *
   * @see java.awt.Container#findComponentAt
   */
  public static Component getDeepestComponentAt(Component parent, int x, int y)
  {
    if (parent == null || (! parent.contains(x, y)))
      return null;

    if (! (parent instanceof Container))
      return parent;

    Container c = (Container) parent;
    return c.findComponentAt(x, y);
  }

  /**
   * Converts a point from a component's local coordinate space to "screen"
   * coordinates (such as the coordinate space mouse events are delivered
   * in). This operation is equivalent to translating the point by the
   * location of the component (which is the origin of its coordinate
   * space).
   *
   * @param p The point to convert
   * @param c The component which the point is expressed in terms of
   *
   * @see convertPointFromScreen
   */
  public static void convertPointToScreen(Point p, Component c)
  {
    Point c0 = c.getLocationOnScreen();
    p.translate(c0.x, c0.y);
  }

  /**
   * Converts a point from "screen" coordinates (such as the coordinate
   * space mouse events are delivered in) to a component's local coordinate
   * space. This operation is equivalent to translating the point by the
   * negation of the component's location (which is the origin of its
   * coordinate space).
   *
   * @param p The point to convert
   * @param c The component which the point should be expressed in terms of
   */
  public static void convertPointFromScreen(Point p, Component c)
  {
    Point c0 = c.getLocationOnScreen();
    p.translate(-c0.x, -c0.y);
  }

  /**
   * Converts a point <code>(x,y)</code> from the coordinate space of one
   * component to another. This is equivalent to converting the point from
   * <code>source</code> space to screen space, then back from screen space
   * to <code>destination</code> space. If exactly one of the two
   * Components is <code>null</code>, it is taken to refer to the root
   * ancestor of the other component. If both are <code>null</code>, no
   * transformation is done.
   *
   * @param source The component which the point is expressed in terms of
   * @param x Horizontal coordinate of point to transform
   * @param y Vertical coordinate of point to transform
   * @param destination The component which the return value will be
   * expressed in terms of
   *
   * @return The point <code>(x,y)</code> converted from the coordinate space of the
   * source component to the coordinate space of the destination component
   *
   * @see #convertPointToScreen
   * @see #convertPointFromScreen
   * @see #convertRectangle
   * @see #getRoot
   */
  public static Point convertPoint(Component source, int x, int y,
                                   Component destination)
  {
    Point pt = new Point(x, y);

    if (source == null && destination == null)
      return pt;

    if (source == null)
      source = getRoot(destination);

    if (destination == null)
      destination = getRoot(source);

    convertPointToScreen(pt, source);
    convertPointFromScreen(pt, destination);

    return pt;
  }

  
  /**
   * Converts a rectangle from the coordinate space of one component to
   * another. This is equivalent to converting the rectangle from
   * <code>source</code> space to screen space, then back from screen space
   * to <code>destination</code> space. If exactly one of the two
   * Components is <code>null</code>, it is taken to refer to the root
   * ancestor of the other component. If both are <code>null</code>, no
   * transformation is done.
   *
   * @param source The component which the rectangle is expressed in terms of
   * @param rect The rectangle to convert
   * @param destination The component which the return value will be
   * expressed in terms of
   *
   * @return A new rectangle, equal in size to the input rectangle, but
   * with its position converted from the coordinate space of the source
   * component to the coordinate space of the destination component
   *
   * @see #convertPointToScreen
   * @see #convertPointFromScreen
   * @see #convertPoint
   * @see #getRoot
   */
  public static Rectangle convertRectangle(Component source,
                                           Rectangle rect,
                                           Component destination)
  {
    Point pt = convertPoint(source, rect.x, rect.y, destination);
    return new Rectangle(pt.x, pt.y, rect.width, rect.height);
  }

  /**
   * Convert a mouse event which refrers to one component to another.  This
   * includes changing the mouse event's coordinate space, as well as the
   * source property of the event. If <code>source</code> is
   * <code>null</code>, it is taken to refer to <code>destination</code>'s
   * root component. If <code>destination</code> is <code>null</code>, the
   * new event will remain expressed in <code>source</code>'s coordinate
   * system.
   *
   * @param source The component the mouse event currently refers to
   * @param sourceEvent The mouse event to convert
   * @param destination The component the new mouse event should refer to
   *
   * @return A new mouse event expressed in terms of the destination
   * component's coordinate space, and with the destination component as
   * its source
   *
   * @see #convertPoint
   */
  public static MouseEvent convertMouseEvent(Component source,
                                             MouseEvent sourceEvent,
                                             Component destination)
  {
    Point newpt = convertPoint(source, sourceEvent.getX(), sourceEvent.getY(),
                               destination);

    return new MouseEvent(destination, sourceEvent.getID(),
                          sourceEvent.getWhen(), sourceEvent.getModifiers(),
                          newpt.x, newpt.y, sourceEvent.getClickCount(),
                          sourceEvent.isPopupTrigger(), sourceEvent.getButton());
  }

  /**
   * Recursively walk the component tree under <code>comp</code> calling
   * <code>updateUI</code> on each {@link JComponent} found. This causes
   * the entire tree to re-initialize its UI delegates.
   *
   * @param comp The component to walk the children of, calling <code>updateUI</code>
   */
  public static void updateComponentTreeUI(Component comp)
  {
    if (comp == null)
      return;
    
    if (comp instanceof Container)
      {
        Component[] children = ((Container)comp).getComponents();
        for (int i = 0; i < children.length; ++i)
          updateComponentTreeUI(children[i]);
      }

    if (comp instanceof JComponent)
      ((JComponent)comp).updateUI();
  }


  /**
   * <p>Layout a "compound label" consisting of a text string and an icon
   * which is to be placed near the rendered text. Once the text and icon
   * are laid out, the text rectangle and icon rectangle parameters are
   * altered to store the calculated positions.</p>
   *
   * <p>The size of the text is calculated from the provided font metrics
   * object.  This object should be the metrics of the font you intend to
   * paint the label with.</p>
   *
   * <p>The position values control where the text is placed relative to
   * the icon. The horizontal position value should be one of the constants
   * <code>LEADING</code>, <code>TRAILING</code>, <code>LEFT</code>,
   * <code>RIGHT</code> or <code>CENTER</code>. The vertical position value
   * should be one fo the constants <code>TOP</code>, <code>BOTTOM</code>
   * or <code>CENTER</code>.</p>
   *
   * <p>The text-icon gap value controls the number of pixels between the
   * icon and the text.</p>
   *
   * <p>The alignment values control where the text and icon are placed, as
   * a combined unit, within the view rectangle. The horizontal alignment
   * value should be one of the constants <code>LEADING</code>,
   * <code>TRAILING</code>, <code>LEFT</code>, <code>RIGHT</code> or
   * <code>CENTER</code>. The vertical alignment valus should be one of the
   * constants <code>TOP</code>, <code>BOTTOM</code> or
   * <code>CENTER</code>.</p>
   *
   * <p>If the <code>LEADING</code> or <code>TRAILING</code> constants are
   * given for horizontal alignment or horizontal text position, they are
   * interpreted relative to the provided component's orientation property,
   * a constant in the {@link java.awt.ComponentOrientation} class. For
   * example, if the component's orientation is <code>LEFT_TO_RIGHT</code>,
   * then the <code>LEADING</code> value is a synonym for <code>LEFT</code>
   * and the <code>TRAILING</code> value is a synonym for
   * <code>RIGHT</code></p>
   *
   * <p>If the text and icon are equal to or larger than the view
   * rectangle, the horizontal and vertical alignment values have no
   * affect.</p>
   *
   * @param c A component used for its orientation value
   * @param fm The font metrics used to measure the text
   * @param text The text to place in the compound label
   * @param icon The icon to place next to the text
   * @param verticalAlignment The vertical alignment of the label relative
   * to its component
   * @param horizontalAlignment The horizontal alignment of the label
   * relative to its component
   * @param verticalTextPosition The vertical position of the label's text
   * relative to its icon
   * @param horizontalTextPosition The horizontal position of the label's
   * text relative to its icon
   * @param viewR The view rectangle, specifying the area which layout is
   * constrained to
   * @param iconR A rectangle which is modified to hold the laid-out
   * position of the icon
   * @param textR A rectangle which is modified to hold the laid-out
   * position of the text
   * @param textIconGap The distance between text and icon
   *
   * @return The string of characters, possibly truncated with an elipsis,
   * which is laid out in this label
   */

  public static String layoutCompoundLabel(JComponent c, 
                                           FontMetrics fm,
                                           String text, 
                                           Icon icon, 
                                           int verticalAlignment,
                                           int horizontalAlignment, 
                                           int verticalTextPosition,
                                           int horizontalTextPosition, 
                                           Rectangle viewR,
                                           Rectangle iconR, 
                                           Rectangle textR, 
                                           int textIconGap)
  {

    // Fix up the orientation-based horizontal positions.

    if (horizontalTextPosition == LEADING)
      {
        if (c.getComponentOrientation() == ComponentOrientation.RIGHT_TO_LEFT)
          horizontalTextPosition = RIGHT;
        else
          horizontalTextPosition = LEFT;
      }
    else if (horizontalTextPosition == TRAILING)
      {
        if (c.getComponentOrientation() == ComponentOrientation.RIGHT_TO_LEFT)
          horizontalTextPosition = LEFT;
        else
          horizontalTextPosition = RIGHT;
      }

    // Fix up the orientation-based alignments.

    if (horizontalAlignment == LEADING)
      {
        if (c.getComponentOrientation() == ComponentOrientation.RIGHT_TO_LEFT)
          horizontalAlignment = RIGHT;
        else
          horizontalAlignment = LEFT;
      }
    else if (horizontalAlignment == TRAILING)
      {
        if (c.getComponentOrientation() == ComponentOrientation.RIGHT_TO_LEFT)
          horizontalAlignment = LEFT;
        else
          horizontalAlignment = RIGHT;
      }
    
    return layoutCompoundLabel(fm, text, icon,
                               verticalAlignment,
                               horizontalAlignment,
                               verticalTextPosition,
                               horizontalTextPosition,
                               viewR, iconR, textR, textIconGap);
  }

  /**
   * <p>Layout a "compound label" consisting of a text string and an icon
   * which is to be placed near the rendered text. Once the text and icon
   * are laid out, the text rectangle and icon rectangle parameters are
   * altered to store the calculated positions.</p>
   *
   * <p>The size of the text is calculated from the provided font metrics
   * object.  This object should be the metrics of the font you intend to
   * paint the label with.</p>
   *
   * <p>The position values control where the text is placed relative to
   * the icon. The horizontal position value should be one of the constants
   * <code>LEFT</code>, <code>RIGHT</code> or <code>CENTER</code>. The
   * vertical position value should be one fo the constants
   * <code>TOP</code>, <code>BOTTOM</code> or <code>CENTER</code>.</p>
   *
   * <p>The text-icon gap value controls the number of pixels between the
   * icon and the text.</p>
   *
   * <p>The alignment values control where the text and icon are placed, as
   * a combined unit, within the view rectangle. The horizontal alignment
   * value should be one of the constants <code>LEFT</code>, <code>RIGHT</code> or
   * <code>CENTER</code>. The vertical alignment valus should be one of the
   * constants <code>TOP</code>, <code>BOTTOM</code> or
   * <code>CENTER</code>.</p>
   *
   * <p>If the text and icon are equal to or larger than the view
   * rectangle, the horizontal and vertical alignment values have no
   * affect.</p>
   *
   * <p>Note that this method does <em>not</em> know how to deal with
   * horizontal alignments or positions given as <code>LEADING</code> or
   * <code>TRAILING</code> values. Use the other overloaded variant of this
   * method if you wish to use such values.
   *
   * @param fm The font metrics used to measure the text
   * @param text The text to place in the compound label
   * @param icon The icon to place next to the text
   * @param verticalAlignment The vertical alignment of the label relative
   * to its component
   * @param horizontalAlignment The horizontal alignment of the label
   * relative to its component
   * @param verticalTextPosition The vertical position of the label's text
   * relative to its icon
   * @param horizontalTextPosition The horizontal position of the label's
   * text relative to its icon
   * @param viewR The view rectangle, specifying the area which layout is
   * constrained to
   * @param iconR A rectangle which is modified to hold the laid-out
   * position of the icon
   * @param textR A rectangle which is modified to hold the laid-out
   * position of the text
   * @param textIconGap The distance between text and icon
   *
   * @return The string of characters, possibly truncated with an elipsis,
   * which is laid out in this label
   */

  public static String layoutCompoundLabel(FontMetrics fm,
                                           String text,
                                           Icon icon,
                                           int verticalAlignment,
                                           int horizontalAlignment,
                                           int verticalTextPosition,
                                           int horizontalTextPosition,
                                           Rectangle viewR,
                                           Rectangle iconR,
                                           Rectangle textR,
                                           int textIconGap)
  {

    // Work out basic height and width.

    if (icon == null)
      {
        textIconGap = 0;
        iconR.width = 0;
        iconR.height = 0;
      }
    else
      {
        iconR.width = icon.getIconWidth();
        iconR.height = icon.getIconWidth();
      }
    textR.width = fm.stringWidth(text);
    textR.height = fm.getHeight(); 

    // Work out the position of text and icon, assuming the top-left coord
    // starts at (0,0). We will fix that up momentarily, after these
    // "position" decisions are made and we look at alignment.

    switch (horizontalTextPosition)
      {
      case LEFT:
        textR.x = 0;
        iconR.x = textR.width + textIconGap;
        break;
      case RIGHT:
        iconR.x = 0;
        textR.x = iconR.width + textIconGap;
        break;
      case CENTER:
        int centerLine = Math.max(textR.width, iconR.width) / 2;
        textR.x = centerLine - textR.width/2;
        iconR.x = centerLine - iconR.width/2;
        break;
      }

    switch (verticalTextPosition)
      {
      case TOP:
        textR.y = 0;
        iconR.y = textR.height + textIconGap;
        break;
      case BOTTOM:
        iconR.y = 0;
        textR.y = iconR.height + textIconGap;
        break;
      case CENTER:
        int centerLine = Math.max(textR.height, iconR.height) / 2;
        textR.y = centerLine - textR.height/2;
        iconR.y = centerLine - iconR.height/2;
        break;
      }

    // The two rectangles are laid out correctly now, but only assuming
    // that their upper left corner is at (0,0). If we have any alignment other
    // than TOP and LEFT, we need to adjust them.

    Rectangle u = textR.union(iconR);
    int horizontalAdjustment = viewR.x;
    int verticalAdjustment = viewR.y;
    switch (verticalAlignment)
      {
      case TOP:
        break;
      case BOTTOM:
        verticalAdjustment += (viewR.height - u.height);
        break;
      case CENTER:
        verticalAdjustment += ((viewR.height/2) - (u.height/2));
        break;
      }
    switch (horizontalAlignment)
      {
      case LEFT:
        break;
      case RIGHT:
        horizontalAdjustment += (viewR.width - u.width);
        break;
      case CENTER:
        horizontalAdjustment += ((viewR.width/2) - (u.width/2));
        break;
      }

    iconR.x += horizontalAdjustment;
    iconR.y += verticalAdjustment;

    textR.x += horizontalAdjustment;
    textR.y += verticalAdjustment;

    return text;
  }

  /** 
   * Calls {@link java.awt.EventQueue.invokeLater} with the
   * specified {@link Runnable}. 
   */
  public static void invokeLater(Runnable doRun)
  {
    java.awt.EventQueue.invokeLater(doRun);
  }

  /** 
   * Calls {@link java.awt.EventQueue.invokeAndWait} with the
   * specified {@link Runnable}. 
   */
  public static void invokeAndWait(Runnable doRun)
    throws InterruptedException,
    InvocationTargetException
  {
    java.awt.EventQueue.invokeAndWait(doRun);
  }

  /** 
   * Calls {@link java.awt.EventQueue.isEventDispatchThread}.
   */
  public static boolean isEventDispatchThread()
  {
    return java.awt.EventQueue.isDispatchThread();
  }

}
