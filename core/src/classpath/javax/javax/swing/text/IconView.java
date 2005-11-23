/* IconView.java -- A view to render icons
   Copyright (C) 2005  Free Software Foundation, Inc.

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


package javax.swing.text;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.Icon;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

/**
 * A View that can render an icon. This view is created by the
 * {@link StyledEditorKit}'s view factory for all elements that have name
 * {@link StyleConstants#IconElementName}. This is usually created by
 * inserting an icon into <code>JTextPane</code> using
 * {@link JTextPane#insertIcon(Icon)}
 *
 * The icon is determined using the attribute
 * {@link StyleConstants#IconAttribute}, which's value must be an {@link Icon}.
 *
 * @author Roman Kennke (kennke@aicas.com)
 */
public class IconView
  extends View
{

  /**
   * Creates a new <code>IconView</code> for the given <code>Element</code>.
   *
   * @param element the element that is rendered by this IconView
   */
  public IconView(Element element)
  {
    super(element);
  }

  /**
   * Renders the <code>Element</code> that is associated with this
   * <code>View</code>.
   *
   * @param g the <code>Graphics</code> context to render to
   * @param a the allocated region for the <code>Element</code>
   */
  public void paint(Graphics g, Shape a)
  {
    Icon icon = StyleConstants.getIcon(getElement().getAttributes());
    Rectangle b = a.getBounds();
    icon.paintIcon(getContainer(), g, b.x, b.y);
  }

  /**
   * Returns the preferred span of the content managed by this
   * <code>View</code> along the specified <code>axis</code>.
   *
   * @param axis the axis
   *
   * @return the preferred span of this <code>View</code>.
   */
  public float getPreferredSpan(int axis)
  {
    Icon icon = StyleConstants.getIcon(getElement().getAttributes());
    float span;
    if (axis == X_AXIS)
      span = icon.getIconWidth();
    else if (axis == Y_AXIS)
      span = icon.getIconHeight();
    else
      throw new IllegalArgumentException();
    return span;
  }

  /**
   * Maps a position in the document into the coordinate space of the View.
   * The output rectangle usually reflects the font height but has a width
   * of zero.
   *
   * @param pos the position of the character in the model
   * @param a the area that is occupied by the view
   * @param b either {@link Position.Bias#Forward} or
   *        {@link Position.Bias#Backward} depending on the preferred
   *        direction bias. If <code>null</code> this defaults to
   *        <code>Position.Bias.Forward</code>
   *
   * @return a rectangle that gives the location of the document position
   *         inside the view coordinate space
   *
   * @throws BadLocationException if <code>pos</code> is invalid
   * @throws IllegalArgumentException if b is not one of the above listed
   *         valid values
   */
  public Shape modelToView(int pos, Shape a, Position.Bias b)
    throws BadLocationException
  {
    Element el = getElement();
    if (pos < el.getStartOffset() || pos >= el.getEndOffset())
      throw new BadLocationException("Illegal offset for this view", pos);
    Rectangle r = a.getBounds();
    Icon icon = StyleConstants.getIcon(el.getAttributes());
    return new Rectangle(r.x, r.y, icon.getIconWidth(), icon.getIconHeight());
  }

  /**
   * Maps coordinates from the <code>View</code>'s space into a position
   * in the document model.
   *
   * @param x the x coordinate in the view space
   * @param y the y coordinate in the view space
   * @param a the allocation of this <code>View</code>
   * @param b the bias to use
   *
   * @return the position in the document that corresponds to the screen
   *         coordinates <code>x, y</code>
   */
  public int viewToModel(float x, float y, Shape a, Position.Bias[] b)
  {
    // The element should only have one character position and it is clear
    // that this position is the position that best matches the given screen
    // coordinates, simply because this view has only this one position.
    Element el = getElement();
    return el.getStartOffset();
  }

  /**
   * Returns the document position that is (visually) nearest to the given
   * document position <code>pos</code> in the given direction <code>d</code>.
   *
   * @param c the text component
   * @param pos the document position
   * @param b the bias for <code>pos</code>
   * @param d the direction, must be either {@link SwingConstants#NORTH},
   *        {@link SwingConstants#SOUTH}, {@link SwingConstants#WEST} or
   *        {@link SwingConstants#EAST}
   * @param biasRet an array of {@link Position.Bias} that can hold at least
   *        one element, which is filled with the bias of the return position
   *        on method exit
   *
   * @return the document position that is (visually) nearest to the given
   *         document position <code>pos</code> in the given direction
   *         <code>d</code>
   *
   * @throws BadLocationException if <code>pos</code> is not a valid offset in
   *         the document model
   */
  public int getNextVisualPositionFrom(JTextComponent c, int pos,
                                       Position.Bias b, int d,
                                       Position.Bias[] biasRet)
    throws BadLocationException
  {
    // TODO: Implement this properly.
    throw new AssertionError("Not implemented yet.");
  }

}
