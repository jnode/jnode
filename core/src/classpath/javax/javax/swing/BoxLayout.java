/* BoxLayout.java -- A layout for swing components.
   Copyright (C) 2002, 2003, 2005 Free Software Foundation, Inc.

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

import java.awt.AWTError;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.io.Serializable;

/**
 * A layout that stacks the children of a container in a Box, either
 * horizontally or vertically.
 *
 * @author Ronald Veldema (rveldema@cs.vu.nl)
 * @author Roman Kennke (roman@kennke.org)
 */
public class BoxLayout implements LayoutManager2, Serializable
{

  /**
   * Specifies that components are laid out left to right.
   */
  public static final int X_AXIS = 0;

  /**
   * Specifies that components are laid out top to bottom.
   */
  public static final int Y_AXIS = 1;

  /**
   * Specifies that components are laid out in the direction of a line of text.
   */
  public static final int LINE_AXIS = 2;

  /**
   * Sepcifies that components are laid out in the direction of the line flow.
   */
  public static final int PAGE_AXIS = 3;

  /*
   * Needed for serialization.
   */
  private static final long serialVersionUID = -2474455742719112368L;

  /*
   * The container given to the constructor.
   */
  private Container container;
  
  /*
   * Current type of component layouting. Defaults to X_AXIS.
   */
  private int way = X_AXIS;

  /**
   * Constructs a <code>BoxLayout</code> object.
   *
   * @param container The container that needs to be laid out.
   * @param way The orientation of the components.
   *
   * @exception AWTError If way has an invalid value.
   */
  public BoxLayout(Container container, int way)
  {
    int width = 0;
    int height = 0;
    this.container = container;
    this.way = way;
  }

  /**
   * Adds a component to the layout. Not used in BoxLayout.
   *
   * @param name The name of the component to add.
   * @param component the component to add to the layout.
   */
  public void addLayoutComponent(String name, Component component)
  {
  }

  /**
   * Removes a component from the layout. Not used in BoxLayout.
   *
   * @param component The component to remove from the layout.
   */
  public void removeLayoutComponent(Component component)
  {
  }

  private boolean isHorizontalIn(Container parent)
  {
    ComponentOrientation orientation = parent.getComponentOrientation();
    return this.way == X_AXIS 
      || (this.way == LINE_AXIS 
          && orientation.isHorizontal())
      || (this.way == PAGE_AXIS
          && (!orientation.isHorizontal()));
  }

  

  /**
   * Returns the preferred size of the layout.
   *
   * @param parent The container that needs to be laid out.
   *
   * @return The dimension of the layout.
   */
  public Dimension preferredLayoutSize(Container parent)
  {
    if (parent != container)
      throw new AWTError("invalid parent");

    // Setup the SizeRequirements for both the X and Y axis.
    Component[] children = container.getComponents();
    SizeRequirements[] hSizeReqs = new SizeRequirements[children.length];
    SizeRequirements[] vSizeReqs = new SizeRequirements[children.length];
    getSizeRequirements(hSizeReqs, vSizeReqs);
    SizeRequirements hReq;
    SizeRequirements vReq;
    if (isHorizontalIn(container))
      {
        hReq = SizeRequirements.getTiledSizeRequirements(hSizeReqs);
        vReq = SizeRequirements.getAlignedSizeRequirements(vSizeReqs);
      }
    else
      {
        hReq = SizeRequirements.getAlignedSizeRequirements(hSizeReqs);
        vReq = SizeRequirements.getTiledSizeRequirements(vSizeReqs);
      }
    return new Dimension(hReq.preferred, vReq.preferred);
  }

  /**
   * Returns the minimum size of the layout.
   *
   * @param parent The container that needs to be laid out.
   *
   * @return The dimension of the layout.
   */
  public Dimension minimumLayoutSize(Container parent)
  {
    if (parent != container)
      throw new AWTError("invalid parent");

    // Setup the SizeRequirements for both the X and Y axis.
    Component[] children = container.getComponents();
    SizeRequirements[] hSizeReqs = new SizeRequirements[children.length];
    SizeRequirements[] vSizeReqs = new SizeRequirements[children.length];
    getSizeRequirements(hSizeReqs, vSizeReqs);
    SizeRequirements hReq;
    SizeRequirements vReq;
    if (isHorizontalIn(container))
      {
        hReq = SizeRequirements.getTiledSizeRequirements(hSizeReqs);
        vReq = SizeRequirements.getAlignedSizeRequirements(vSizeReqs);
      }
    else
      {
        hReq = SizeRequirements.getAlignedSizeRequirements(hSizeReqs);
        vReq = SizeRequirements.getTiledSizeRequirements(vSizeReqs);
      }
    return new Dimension(hReq.minimum, vReq.minimum);
  }

  /**
   * Lays out the specified container using this layout.
   *
   * @param parent The container that needs to be laid out.
   */
  public void layoutContainer(Container parent)
  {
    // Setup the SizeRequirements for both the X and Y axis.
    Component[] children = container.getComponents();
    SizeRequirements[] hSizeReqs = new SizeRequirements[children.length];
    SizeRequirements[] vSizeReqs = new SizeRequirements[children.length];
    getSizeRequirements(hSizeReqs, vSizeReqs);

    int[] hSpans = new int[children.length];
    int[] hOffsets = new int[children.length];
    int[] vSpans = new int[children.length];
    int[] vOffsets = new int[children.length];

    Insets insets = container.getInsets();
    int width = container.getWidth() - insets.left - insets.right - 1;
    int height = container.getHeight() - insets.top - insets.bottom - 1;
    if (isHorizontalIn(container))
      {
        SizeRequirements.calculateTiledPositions(width, null,
                                                 hSizeReqs, hOffsets, hSpans);
        SizeRequirements.calculateAlignedPositions(height, null,
                                                 vSizeReqs, vOffsets, vSpans);
      }
    else
      {
        SizeRequirements.calculateTiledPositions(height, null,
                                                 vSizeReqs, vOffsets, vSpans);
        SizeRequirements.calculateAlignedPositions(width, null,
                                                 hSizeReqs, hOffsets, hSpans);
      }

    // Set positions and widths of child components.
    for (int i = 0; i < children.length; i++)
      {
        Component child = children[i];
        child.setBounds(hOffsets[i] + insets.left, vOffsets[i] + insets.top,
                        hSpans[i], vSpans[i]);
      }
  }

  /**
   * Adds a component to the layout. Not used in BoxLayout
   *
   * @param child The component to add to the layout.
   * @param constraints The constraints for the component in the layout.
   */
  public void addLayoutComponent(Component child, Object constraints)
  {
  }

  /**
   * Returns the alignment along the X axis for the container.
   *
   * @param parent The container that needs to be laid out.
   *
   * @return The alignment.
   */
  public float getLayoutAlignmentX(Container parent)
  {
    if (parent != container)
      throw new AWTError("invalid parent");
    
    return 0;
  }

  /**
   * Returns the alignment along the Y axis for the container.
   *
   * @param parent The container that needs to be laid out.
   *
   * @return The alignment.
   */
  public float getLayoutAlignmentY(Container parent)
  {
    if (parent != container)
      throw new AWTError("invalid parent");
    
    return 0;
  }

  /**
   * Invalidates the layout.
   *
   * @param parent The container that needs to be laid out.
   */
  public void invalidateLayout(Container parent)
  {
    if (parent != container)
      throw new AWTError("invalid parent");
  }

  /**
   * Returns the maximum size of the layout gived the components
   * in the given container.
   *
   * @param parent The container that needs to be laid out.
   *
   * @return The dimension of the layout.
   */
  public Dimension maximumLayoutSize(Container parent)
  {
    if (parent != container)
      throw new AWTError("invalid parent");

    // Setup the SizeRequirements for both the X and Y axis.
    Component[] children = container.getComponents();
    SizeRequirements[] hSizeReqs = new SizeRequirements[children.length];
    SizeRequirements[] vSizeReqs = new SizeRequirements[children.length];
    getSizeRequirements(hSizeReqs, vSizeReqs);
    SizeRequirements hReq;
    SizeRequirements vReq;
    if (isHorizontalIn(container))
      {
        hReq = SizeRequirements.getTiledSizeRequirements(hSizeReqs);
        vReq = SizeRequirements.getAlignedSizeRequirements(vSizeReqs);
      }
    else
      {
        hReq = SizeRequirements.getAlignedSizeRequirements(hSizeReqs);
        vReq = SizeRequirements.getTiledSizeRequirements(vSizeReqs);
      }
    return new Dimension(hReq.maximum, vReq.maximum);
  }

  /**
   * Fills arrays of SizeRequirements for the horizontal and vertical
   * requirements of the children of component.
   *
   * @param hSizeReqs the horizontal requirements to be filled by this method
   * @param vSizeReqs the vertical requirements to be filled by this method
   */
  private void getSizeRequirements(SizeRequirements[] hSizeReqs,
                                   SizeRequirements[] vSizeReqs)
  {
    Component[] children = container.getComponents();
    for (int i = 0; i < children.length; i++)
      {
        Component child = children[i];
        if (! child.isVisible())
          {
            SizeRequirements req = new SizeRequirements();
            hSizeReqs[i] = req;
            vSizeReqs[i] = req;
          }
        else
          {
            SizeRequirements hReq =
              new SizeRequirements(child.getMinimumSize().width,
                                   child.getPreferredSize().width,
                                   child.getMaximumSize().width,
                                   child.getAlignmentX());
            hSizeReqs[i] = hReq;
            SizeRequirements vReq =
              new SizeRequirements(child.getMinimumSize().height,
                                   child.getPreferredSize().height,
                                   child.getMaximumSize().height,
                                   child.getAlignmentY());
            vSizeReqs[i] = vReq;
          }
      }
  }
}
