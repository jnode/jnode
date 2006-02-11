/* LabelView.java -- A view to render styled text
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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Shape;

import javax.swing.event.DocumentEvent;

/**
 * A {@link GlyphView} that caches the textattributes for most effective
 * rendering.
 *
 * @author Roman Kennke (kennke@aicas.com)
 */
public class LabelView extends GlyphView
{

  /**
   * The background color.
   */
  Color background;

  /**
   * The foreground color.
   */
  Color foreground;

  /**
   * The background color.
   */
  Font font;

  /**
   * The strikethrough flag.
   */
  boolean strikeThrough;

  /**
   * The underline flag.
   */
  boolean underline;

  /**
   * The subscript flag.
   */
  boolean subscript;

  /**
   * The superscript flag.
   */
  boolean superscript;

  /**
   * Creates a new <code>GlyphView</code> for the given <code>Element</code>.
   *
   * @param element the element that is rendered by this GlyphView
   */
  public LabelView(Element element)
  {
    super(element);
    setPropertiesFromAttributes();
  }

  /**
   * Loads the properties of this label view from the element's text
   * attributes. This method is called from the constructor and the
   * {@link #changedUpdate} method
   */
  protected void setPropertiesFromAttributes()
  {
    Element el = getElement();
    AttributeSet atts = el.getAttributes();
    // We cannot use StyleConstants.getBackground() here, because that returns
    // BLACK as default (when background == null). What we need is the
    // background setting of the text component instead, which is what we get
    // when background == null anyway.
    background = (Color) atts.getAttribute(StyleConstants.Background);
    foreground = StyleConstants.getForeground(atts);
    strikeThrough = StyleConstants.isStrikeThrough(atts);
    subscript = StyleConstants.isSubscript(atts);
    superscript = StyleConstants.isSuperscript(atts);
    underline = StyleConstants.isUnderline(atts);

    // Determine the font.
    String family = StyleConstants.getFontFamily(atts);
    int size = StyleConstants.getFontSize(atts);
    int style = Font.PLAIN;
    if (StyleConstants.isBold(atts))
        style |= Font.BOLD;
    if (StyleConstants.isItalic(atts))
      style |= Font.ITALIC;
    font = new Font(family, style, size);
  }

  /**
   * Receives notification when text attributes change in the chunk of
   * text that this view is responsible for. This simply calls
   * {@link #setPropertiesFromAttributes()}.
   *
   * @param e the document event
   * @param a the allocation of this view
   * @param vf the view factory to use for creating new views
   */
  public void changedUpdate(DocumentEvent e, Shape a, ViewFactory vf)
  {
    setPropertiesFromAttributes();
  }

  /**
   * Returns the background color for the glyphs.
   *
   * @return the background color for the glyphs
   */
  public Color getBackground()
  {
    return background;
  }

  /**
   * Sets the background color for the glyphs. A value of <code>null</code>
   * means the background of the parent view should shine through.
   *
   * @param bg the background to set or <code>null</code>
   *
   * @since 1.5
   */
  protected void setBackground(Color bg)
  {
    background = bg;
  }

  /**
   * Returns the foreground color for the glyphs.
   *
   * @return the foreground color for the glyphs
   */
  public Color getForeground()
  {
    return foreground;
  }

  /**
   * Returns the font for the glyphs.
   *
   * @return the font for the glyphs
   */
  public Font getFont()
  {
    return font;
  }

  /**
   * Returns the font metrics of the current font.
   *
   * @return the font metrics of the current font
   *
   * @deprecated this is not used anymore
   */
  protected FontMetrics getFontMetrics()
  {
    return getContainer().getGraphics().getFontMetrics(font);
  }

  /**
   * Returns <code>true</code> if the glyphs are rendered underlined,
   * <code>false</code> otherwise.
   *
   * @return <code>true</code> if the glyphs are rendered underlined,
   *         <code>false</code> otherwise
   */
  public boolean isUnderline()
  {
    return underline;
  }

  /**
   * Sets the underline flag.
   *
   * @param flag <code>true</code> if the glyphs are rendered underlined,
   *             <code>false</code> otherwise
   */
  protected void setUnderline(boolean flag)
  {
    underline = flag;
  }

  /**
   * Returns <code>true</code> if the glyphs are rendered as subscript,
   * <code>false</code> otherwise.
   *
   * @return <code>true</code> if the glyphs are rendered as subscript,
   *         <code>false</code> otherwise
   */
  public boolean isSubscript()
  {
    return subscript;
  }

  /**
   * Sets the subscript flag.
   *
   * @param flag <code>true</code> if the glyphs are rendered as subscript,
   *             <code>false</code> otherwise
   */
  protected void setSubscript(boolean flag)
  {
    subscript = flag;
  }

  /**
   * Returns <code>true</code> if the glyphs are rendered as superscript,
   * <code>false</code> otherwise.
   *
   * @return <code>true</code> if the glyphs are rendered as superscript,
   *         <code>false</code> otherwise
   */
  public boolean isSuperscript()
  {
    return superscript;
  }

  /**
   * Sets the superscript flag.
   *
   * @param flag <code>true</code> if the glyphs are rendered as superscript,
   *             <code>false</code> otherwise
   */
  protected void setSuperscript(boolean flag)
  {
    superscript = flag;
  }

  /**
   * Returns <code>true</code> if the glyphs are rendered strike-through,
   * <code>false</code> otherwise.
   *
   * @return <code>true</code> if the glyphs are rendered strike-through,
   *         <code>false</code> otherwise
   */
  public boolean isStrikeThrough()
  {
    return strikeThrough;
  }

  /**
   * Sets the strike-through flag.
   *
   * @param flag <code>true</code> if the glyphs are rendered strike-through,
   *             <code>false</code> otherwise
   */
  protected void setStrikeThrough(boolean flag)
  {
    strikeThrough = flag;
  }
}
