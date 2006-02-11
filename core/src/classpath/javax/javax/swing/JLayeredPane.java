/* JLayeredPane.java -- 
   Copyright (C) 2002, 2004 Free Software Foundation, Inc.

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
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Hashtable;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;

/**
 * A container that adds depth to the usual <code>Container</code> semantics.
 * Each child component of a <code>Layered Pane</code> is placed within one
 * of several layers. <code>JLayeredPane</code> defines a set of standard
 * layers. The pre-defined sets are (in the order from button to top):
 *
 *  <dl>
 *    <dt>{@link #DEFAULT_LAYER}</dt>
 *    <dd>The layer where most of the normal components are placed. This
 *      is the bottommost layer.</dd>
 *
 *    <dt>{@link #PALETTE_LAYER}</dt>
 *    <dd>Palette windows are placed in this layer.</dd>
 *
 *    <dt>{@link #MODAL_LAYER}</dt>
 *    <dd>The layer where internal modal dialog windows are placed.</dd>
 *
 *    <dt>{@link #POPUP_LAYER}</dt>
 *    <dd>The layer for popup menus</dd>
 *
 *    <dt>{@link #DRAG_LAYER}</dt>
 *    <dd>Components that are beeing dragged are temporarily placed in
 *       this layer.</dd>
 *  </dl>
 *
 * <p>A child is in exactly one of these layers at any time, though there may
 * be other layers if someone creates them.</p>
 *
 * <p>You can add a component to a specific layer using the
 * {@link Container#add(Component, Object)} method. I.e.
 * <code>layeredPane.add(comp, JLayeredPane.MODAL_LAYER)</code> will add the
 * component <code>comp</code> to the modal layer of <code>layeredPane</code>.
 * </p>
 *
 * <p>To change the layer of a component that is already a child of
 * a <code>JLayeredPane</code>, use the {@link #setLayer(Component, int)} 
 * method.</p>
 *
 * <p>The purpose of this class is to translate this view of "layers" into a
 * contiguous array of components: the one held in our ancestor,
 * {@link java.awt.Container}.</p>
 *
 * <p>There is a precise set of words we will use to refer to numbers within
 * this class:</p>
 * 
 * <dl>
 * <dt>Component Index:</dt> 
 * <dd>An offset into the <code>component</code> array held in our ancestor,
 * {@link java.awt.Container}, from <code>[0 .. component.length)</code>. The drawing
 * rule with indices is that 0 is drawn last.</dd>
 *
 * <dt>Layer Number:</dt>
 * <dd>A general <code>int</code> specifying a layer within this component.  Negative
 * numbers are drawn first, then layer 0, then positive numbered layers, in
 * ascending order.</dd>
 *
 * <dt>Position:</dt> 
 * <dd>An offset into a layer's "logical drawing order". Layer position 0
 * is drawn last. Layer position -1 is a synonym for the first layer
 * position (the logical "bottom").</dd>
 * </dl>
 *
 * <p><b>Note:</b> the layer numbering order is the <em>reverse</em> of the
 * component indexing and position order</p>
 *
 * @author Graydon Hoare (graydon@redhat.com)
 * @author Roman Kennke (kennke@aicas.com)
 */
public class JLayeredPane extends JComponent implements Accessible
{
  
  /**
   * Provides accessibility support for <code>JLayeredPane</code>.
   */
  protected class AccessibleJLayeredPane extends AccessibleJComponent
  {
    /**
     * Creates a new instance of <code>AccessibleJLayeredPane</code>.
     */
    protected AccessibleJLayeredPane()
    {
      // Nothing to do here.
    }

    /**
     * Returns the accessble role of <code>JLayeredPane</code>,
     * {@link AccessibleRole#LAYERED_PANE}. 
     */
    public AccessibleRole getAccessibleRole()
    {
      return AccessibleRole.LAYERED_PANE;
    }
  }

  private static final long serialVersionUID = 5534920399324590459L;
  
  public static final String LAYER_PROPERTY = "layeredContainerLayer";

  public static final Integer FRAME_CONTENT_LAYER = new Integer (-30000);

  public static final Integer DEFAULT_LAYER = new Integer (0);
  public static final Integer PALETTE_LAYER = new Integer (100);
  public static final Integer MODAL_LAYER   = new Integer (200);
  public static final Integer POPUP_LAYER   = new Integer (300);
  public static final Integer DRAG_LAYER    = new Integer (400);

  private Hashtable componentToLayer;   // Component -> Layer Number (Integer)
  
  public JLayeredPane()
  {
    componentToLayer = new Hashtable ();
    setLayout(null);
  }

  /** 
   * Looks up the layer a child component is currently assigned to.
   *
   * If <code>c</code> is an instance of {@link JComponent}, then the layer
   * is fetched from the client property with the key {@link #LAYER_PROPERTY}.
   * Otherwise it is looked up in an internal hashtable that maps
   * non-JComponent components to layers. If the components cannot be found
   * in either way, the {@link #DEFAULT_LAYER} is returned.
   *
   * @param c the component to look up.
   *
   * @return the layer the component is currently assigned to; if the component
   *         is not in this layered pane, then 0 (DEFAULT_LAYER) is returned
   */
  public int getLayer(Component c)
  {
    Integer layerObj;
    if (c instanceof JComponent)
      {
        JComponent jc = (JComponent) c;
        layerObj = (Integer) jc.getClientProperty(LAYER_PROPERTY);
      }
    else
      layerObj = (Integer) componentToLayer.get(c);

    if (layerObj == null)
      layerObj = DEFAULT_LAYER;

    return layerObj.intValue();
  }

  /**
   * Looks up the layer in the client property with the key
   * {@link #LAYER_PROPERTY} of <code>comp</code>. If no such property can be
   * found, we return <code>0</code> ({@link #DEFAULT_LAYER}).
   *
   * @param comp the component for which the layer is looked up
   *
   * @return the layer of <code>comp</code> as stored in the corresponding
   *         client property, or <code>0</code> if there is no such property
   */
  public static int getLayer(JComponent comp)
  {
    Integer layerObj = (Integer) comp.getClientProperty(LAYER_PROPERTY);
    if (layerObj == null)
      layerObj = DEFAULT_LAYER;
    return layerObj.intValue();
  }

  /**
   * Returns the first JLayeredPane that contains the Component
   * <code>comp</code> or <code>null</code> if <code>comp</code> is
   * not contained in a JLayeredPane.
   *
   * @param comp the component for which we are searching the JLayeredPane
   *     ancestor
   *
   * @return the first JLayeredPane that contains the Component
   *     <code>comp</code> or <code>null</code> if <code>comp</code> is
   *     not contained in a JLayeredPane
   */
  public static JLayeredPane getLayeredPaneAbove(Component comp)
  {
    JLayeredPane lp = (JLayeredPane) SwingUtilities.getAncestorOfClass
      (JLayeredPane.class, comp);
    return lp;
  }

  /**
   * Return the greatest layer number currently in use, in this container.
   * This number may legally be positive <em>or</em> negative.
   *
   * @return the highest layer number
   *
   * @see #lowestLayer()
   */
  public int highestLayer()
  {
    Component[] components = getComponents();
    int highest;
    if (components.length == 0)
      highest = 0;
    else
      {
        highest = Integer.MIN_VALUE;
        for (int i = 0; i < components.length; i++)
          highest = Math.max(highest, getLayer(components[i]));
      }
    return highest;
  }

  /**
   * Return the least layer number currently in use, in this container.
   * This number may legally be positive <em>or</em> negative.
   *
   * @return the least layer number
   *
   * @see #highestLayer()
   */
  public int lowestLayer()
  {
    Component[] components = getComponents();
    int lowest;
    if (components.length == 0)
      lowest = 0;
    else
      {
        lowest = Integer.MAX_VALUE;
        for (int i = 0; i < components.length; i++)
          lowest = Math.max(lowest, getLayer(components[i]));
      }
    return lowest;
  }

  /**
   * Moves a component to the "front" of its layer. The "front" is a
   * synonym for position 0, which is also the last position drawn in each
   * layer, so is usually the component which occludes the most other
   * components in its layer.
   *
   * @param c the component to move to the front of its layer
   *
   * @see #moveToBack
   */
  public void moveToFront(Component c)
  {
    setPosition (c, 0);
  }

  /**
   * <p>Moves a component to the "back" of its layer. The "back" is a
   * synonym for position N-1 (also known as position -1), where N is the
   * size of the layer.</p>
   *
   * <p>The "back" of a layer is the first position drawn, so the component at
   * the "back" is usually the component which is occluded by the most
   * other components in its layer.</p>
   *
   * @param c the component to move to the back of its layer.
   *
   * @see #moveToFront
   */
  public void moveToBack(Component c)
  {
    setPosition (c, -1);
  }

  /**
   * Return the position of a component within its layer. Positions are assigned
   * from the "front" (position 0) to the "back" (position N-1), and drawn from 
   * the back towards the front.
   *
   * @param c the component to get the position of
   *
   * @return the position of <code>c</code> within its layer or -1 if
   *         <code>c</code> is not a child of this layered pane
   *
   * @see #setPosition
   */
  public int getPosition(Component c)
  {
    int pos = -1;
    int index = getIndexOf(c);
    Component[] components = getComponents();
    int layer = getLayer(c);
    if (index >= 0)
	    {
        for (int i = index; i >= 0; --i)
          {
            if (layer == getLayer(components[i]))
              pos++;
            else
              break;
          }
	    }
    return pos;
  }

  /**
   * Change the position of a component within its layer. Positions are assigned
   * from the "front" (position 0) to the "back" (position N-1), and drawn from 
   * the back towards the front.
   *
   * @param c the component to change the position of
   * @param position the position to assign the component to
   *
   * @see #getPosition
   */
  public void setPosition(Component c, int position)
  {
    int layer = getLayer(c);
    int index = insertIndexForLayer(layer, position);
    setComponentZOrder(c, index);
  }
    
  /**
   * Return an array of all components within a layer of this
   * container. Components are ordered front-to-back, with the "front"
   * element (which draws last) at position 0 of the returned array.
   *
   * @param layer the layer to return components from
   *
   * @return the components in the layer
   */
  public Component[] getComponentsInLayer(int layer)
  {
    Component[] inLayer = new Component[getComponentCountInLayer(layer)];
    Component[] components = getComponents();
    int j = 0;
    for (int i = 0; i < components.length; ++i)
      {
        if (layer == getLayer(components[i]))
	    {
            inLayer[j] = components[i];
            j++;
          }
	    }
    return inLayer;
  }

  /**
   * Return the number of components within a layer of this
   * container.
   *
   * @param layer the layer count components in
   *
   * @return the number of components in the layer
   */
  public int getComponentCountInLayer(int layer)
  {
    Component[] components = getComponents();
    int count = 0;
    for (int i = components.length - 1; i >= 0; --i)
      {
        if (getLayer(components[i]) == layer)
          count++;
      }
    return count;
  }

  /**
   * Return a hashtable mapping child components of this container to
   * Integer objects representing the component's layer assignments.
   */
  protected Hashtable getComponentToLayer()
  {
    return componentToLayer;
  }

  /**
   * Return the index of a component within the underlying (contiguous)
   * array of children. This is a "raw" number which does not represent the
   * child's position in a layer, but rather its position in the logical
   * drawing order of all children of the container.
   *
   * @param c the component to look up.
   *
   * @return the external index of the component or <code>-1</code> if
   *         <code>c</code> is not a child of this layered pane 
   */
  public int getIndexOf(Component c) 
  {
    return getComponentZOrder(c);
  }    

  /**
   * Return an Integer object which holds the same int value as the
   * parameter. This is strictly an optimization to minimize the number of
   * identical Integer objects which we allocate.
   *
   * @param layer the layer number as an int.
   *
   * @return the layer number as an Integer, possibly shared.
   */
  protected Integer getObjectForLayer(int layer)
  {
    switch (layer)
	    {
	    case -30000:
        return FRAME_CONTENT_LAYER;

	    case 0:
        return DEFAULT_LAYER;

	    case 100:
        return PALETTE_LAYER;

	    case 200:
        return MODAL_LAYER;

	    case 300:
        return POPUP_LAYER;

	    case 400:
        return DRAG_LAYER;

	    default:
        break;
	    }

    return new Integer(layer);
  }

  /**
   * Computes an index at which to request the superclass {@link
   * java.awt.Container} inserts a component, given an abstract layer and
   * position number.
   *
   * @param layer the layer in which to insert a component.
   * @param position the position in the layer at which to insert a component.
   *
   * @return the index at which to insert the component.
   */
  protected int insertIndexForLayer(int layer, int position)
  {
    // position < 0 means insert at greatest position within layer.
    if (position < 0)
      position = Integer.MAX_VALUE;

    Component[] components = getComponents();
    int index = 0;

    // Try to find the start index of the specified layer.
    int p = -1;
    for (int i = 0; i < components.length; i++)
      {
        int l = getLayer(components[i]);
        if (l > layer)
          index++;
        // If we are in the layer we look for, try to find the position.
        else if (l == layer)
          {
            p++;
            if (p < position)
              index++;
    else
              break;
          }
        // No need to look further if the layer at i is smaller than layer.
        else
          break;
      }
    return index;
  }

  /**
   * Removes a child from this container. The child is specified by
   * index. After removal, the child no longer occupies a layer.
   *
   * @param index the index of the child component to remove.
   */
  public void remove(int index)
  {
    Component c = getComponent(index);
    if (! (c instanceof JComponent))
    componentToLayer.remove(c);
    super.remove(index);
  }

  /**
   * <p>Set the layer property for a component, within this container. The
   * component will be implicitly mapped to the bottom-most position in the
   * layer, but only if added <em>after</em> calling this method.</p>
   *
   * <p>Read that carefully: this method should be called <em>before</em> the
   * component is added to the container.</p>
   *
   * @param c the component to set the layer property for.
   * @param layer the layer number to assign to the component.
   */
  public void setLayer(Component c, int layer)
  {
    setLayer(c, layer, -1);
  }

  /**
   * Set the layer and position of a component, within this container.
   *
   * @param c the child component to set the layer property for.
   * @param layer the layer number to assign to the component.
   * @param position the position number to assign to the component.
   */
  public void setLayer(Component c, int layer, int position)
  {
    Integer layerObj = getObjectForLayer(layer);
    if (c instanceof JComponent)
      {
        JComponent jc = (JComponent) c;
        jc.putClientProperty(LAYER_PROPERTY, layerObj);
      }
    else
      componentToLayer.put (c, layerObj);

    // Set position only of component is already added to this layered pane.
    if (getIndexOf(c) != -1)
    setPosition(c, position);
  }

  /**
   * Overrides the default implementation from {@link java.awt.Container}
   * such that <code>layerConstraint</code> is interpreted as an {@link
   * Integer}, specifying the layer to which the component will be added
   * (at the bottom position).
   *
   * The argument <code>index</code> specifies the position within the layer
   * at which the component should be added, where <code>0</code> is the top
   * position greater values specify positions below that and <code>-1</code>
   * specifies the bottom position.
   *
   * @param comp the component to add
   * @param layerConstraint an integer specifying the layer to add the
   *        component to
   * @param index the position within the layer
   */
  protected void addImpl(Component comp, Object layerConstraint, int index) 
  {        	
    int layer;
    if (layerConstraint != null && layerConstraint instanceof Integer)
      layer = ((Integer) layerConstraint).intValue();
    else
	  layer = getLayer(comp);
	
    int newIdx = insertIndexForLayer(layer, index);
    setLayer(comp, layer);
    super.addImpl(comp, layerConstraint, newIdx);
  }     

  /**
   * Sets the layer property for a JComponent.
   *
   * @param component the component for which to set the layer
   * @param layer the layer property to set
   */
  public static void putLayer(JComponent component, int layer)
  {
    component.putClientProperty(LAYER_PROPERTY, new Integer(layer));
  }

  /**
   * Returns the accessible context for this <code>JLayeredPane</code>.
   *
   * @return the accessible context for this <code>JLayeredPane</code>
   */
  public AccessibleContext getAccessibleContext()
  {
    if (accessibleContext == null)
      accessibleContext = new AccessibleJLayeredPane();
    return accessibleContext;
  }

  /**
   * This method is overridden order to provide a reasonable painting
   * mechanism for <code>JLayeredPane</code>. This is necessary since
   * <code>JLayeredPane</code>'s do not have an own UI delegate.
   *
   * Basically this method clears the background for the
   * <code>JLayeredPane</code> and then calls <code>super.paint(g)</code>.
   *
   * @param g the graphics context to use
   */
  public void paint(Graphics g)
  {
    if (isOpaque())
      {
        Color oldColor = g.getColor();
        Rectangle clip = g.getClipBounds();
    g.setColor(getBackground());
        g.fillRect(clip.x, clip.y, clip.width, clip.height);
        g.setColor(oldColor);
      }
    super.paint(g);
  }

  /**
   * Returns <code>false</code> if components in this layered pane can overlap,
   * otherwise <code>true</code>.
   *
   * @return <code>false</code> if components in this layered pane can overlap,
   *         otherwise <code>true</code>
   */
  public boolean isOptimizedDrawingEnabled()
  {
    int numChildren = getComponentCount();
    boolean result = false;
    // We implement a heuristic here in order to return a quick result:
    // - For 0 or 1 children it is clear that they do not overlap, return true.
    // - For 2 children we check their bounding rectangles and if they don't
    //   interect, return true.
    // - For more than 2 children we return false. Comparing all the bounding
    //   rectangles costs too much time and in most cases this will return
    //   false anyway, since JLayeredPane are mostly used in JRootPane and then
    //   have at least one child (the contentPane) that takes up the whole
    //   area of the JLayeredPane.
    switch (numChildren)
    {
      case 0:
      case 1:
        result = true;
        break;
      case 2:
        Rectangle r1 = getComponent(0).getBounds();
        Rectangle r2 = getComponent(1).getBounds();
        result = !r1.intersects(r2);
        break;
      default:
        result = false;
    }
    return result;
  }
}
