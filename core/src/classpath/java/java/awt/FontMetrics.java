/* FontMetrics.java -- Information about about a fonts display characteristics
   Copyright (C) 1999, 2002, 2005  Free Software Foundation, Inc.

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


package java.awt;

import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.text.CharacterIterator;

// FIXME: I leave many methods basically unimplemented.  This
// should be reviewed.

/**
  * This class returns information about the display characteristics of
  * a font.  It is abstract, and concrete subclasses should implement at
  * least the following methods:
  *
  * <ul>
  * <li>getAscent()</li>
  * <li>getDescent()</li>
  * <li>getLeading()</li>
  * <li>getMaxAdvance()</li>
  * <li>charWidth(char)</li>
  * <li>charsWidth(char[], int, int)</li>
  * </ul>
  *
  * @author Aaron M. Renn (arenn@urbanophile.com)
  */
public abstract class FontMetrics implements java.io.Serializable
{
  // Serialization constant.
  private static final long serialVersionUID = 1681126225205050147L;

  /**
	  * This is the font for which metrics will be returned.
	  */
  protected Font font;

  /**
	  * Initializes a new instance of <code>FontMetrics</code> for the
	  * specified font.
	  *
	  * @param font The font to return metric information for.
	  */
  protected FontMetrics(Font font)
  {
		this.font = font;
  }

  /**
	  * Returns the font that this object is creating metric information fo.
	  *
	  * @return The font for this object.
	  */
  public Font getFont()
  {
    return font;
  }

  /**
	  * Returns the leading, or spacing between lines, for this font.
	  *
	  * @return The font leading.
	  */
  public int getLeading()
  {
    return 0;
  }

  /**
	  * Returns the ascent of the font, which is the distance from the base
	  * to the top of the majority of characters in the set.  Some characters
	  * can exceed this value however.
	  *
	  * @return The font ascent.
	  */
  public int getAscent()
  {
    return 1;
  }

  /**
	  * Returns the descent of the font, which is the distance from the base
	  * to the bottom of the majority of characters in the set.  Some characters
	  * can exceed this value however.
	  *
	  * @return The font descent.
	  */
  public int getDescent()
  {
    return 1;
  }

  /**
	  * Returns the height of a line in this font.  This will be the sum
	  * of the leading, the ascent, and the descent.
	  *
	  * @return The height of the font.
	  */
  public int getHeight()
  {
    return getAscent() + getDescent() + getLeading();
  }

  /**
	  * Returns the maximum ascent value.  This is the maximum distance any
	  * character in the font rised above the baseline.
	  *
	  * @return The maximum ascent for this font.
	  */
  public int getMaxAscent()
  {
    return getAscent();
  }

  /**
	  * Returns the maximum descent value.  This is the maximum distance any
	  * character in the font extends below the baseline.
	  *
	  * @return The maximum descent for this font.
	  */
  public int getMaxDescent()
  {
    return getMaxDecent();
  }

  /**
	  * Returns the maximum descent value.  This is the maximum distance any
	  * character in the font extends below the baseline.
	  *
	  * @return The maximum descent for this font.
	  *
	  * @deprecated This method is deprecated in favor of
	  * <code>getMaxDescent()</code>.
	  */
  public int getMaxDecent()
  {
    return getDescent();
  }

  /**
	  * Returns the width of the widest character in the font.
	  *
	  * @return The width of the widest character in the font.
	  */
  public int getMaxAdvance()
  {
    return -1;
  }

  /**
	  * Returns the width of the specified character.
	  *
	  * @param ch The character to return the width of.
	  *
	  * @return The width of the specified character.
	  */
  public int charWidth(int ch)
  {
    return charWidth((char) ch);
  }

  /**
	  * Returns the width of the specified character.
	  *
	  * @param ch The character to return the width of.
	  *
	  * @return The width of the specified character.
	  */
  public int charWidth(char ch)
  {
    return 1;
  }

  /**
	  * Returns the total width of the specified string
	  *
	  * @param str The string to return the width of.
	  *
	  * @return The width of the string.
	  */
  public int stringWidth(String str)
  {
		char[] buf = new char[str.length()];
		str.getChars(0, str.length(), buf, 0);

    return charsWidth(buf, 0, buf.length);
  }

  /**
	  * Returns the total width of the specified character array.
	  *
	  * @param buf The character array containing the data.
	  * @param offset The offset into the array to start calculating from.
	  * @param len The total number of bytes to process.
	  *
	  * @return The width of the requested characters.
	  */
  public int charsWidth(char[] buf, int offset, int len)
  {
		int total_width = 0;
    int endOffset = offset + len;
    for (int i = offset; i < endOffset; i++)
    total_width += charWidth(buf[i]);
    return total_width;
  }

  /**
	  * Returns the total width of the specified byte array.
	  *
	  * @param buf The byte array containing the data.
	  * @param offset The offset into the array to start calculating from.
	  * @param len The total number of bytes to process.
	  *
	  * @return The width of the requested characters.
	  */
  public int bytesWidth(byte[] buf, int offset, int len)
  {
		int total_width = 0;
		for (int i = offset; i < len; i++)
      total_width = charWidth((char) buf[i]);

    return total_width;
  }

  /**
	  * Returns the widths of the first 256 characters in the font.
	  *
	  * @return The widths of the first 256 characters in the font.
	  */
  public int[] getWidths()
  {
    int[] result = new int[256];
    for (char i = 0; i < 256; i++)
      result[i] = charWidth(i);
    return result;
	}

  /**
	  * Returns a string representation of this object.
	  *
	  * @return A string representation of this object.
	  */
  public String toString()
  {
  return (this.getClass() + "[font=" + font + ",ascent=" + getAscent() 
	  + ",descent=" + getDescent() + ",height=" + getHeight() + "]");
  }

  // Generic FontRenderContext used when getLineMetrics is called with a
  // plain Graphics object.
  private static final FontRenderContext gRC = new FontRenderContext(null,
								   false,
								   false);

  /**
  * Returns a {@link LineMetrics} object constructed with the
  * specified text and the {@link FontRenderContext} of the Graphics
  * object when it is an instance of Graphics2D or a generic
  * FontRenderContext with a null transform, not anti-aliased and not
  * using fractional metrics.
  *
  * @param text The string to calculate metrics from.
  * @param g The Graphics object that will be used.
  *
  * @return A new {@link LineMetrics} object.
  */
  public LineMetrics getLineMetrics(String text, Graphics g)
  {
  return getLineMetrics(text, 0, text.length(), g);
  }

  /**
 * Returns a {@link LineMetrics} object constructed with the
 * specified text and the {@link FontRenderContext} of the Graphics
 * object when it is an instance of Graphics2D or a generic
 * FontRenderContext with a null transform, not anti-aliased and not
 * using fractional metrics.
 *
 * @param text The string to calculate metrics from.
 * @param begin Index of first character in <code>text</code> to measure.
 * @param limit Index of last character in <code>text</code> to measure.
 * @param g The Graphics object that will be used.
 *
 * @return A new {@link LineMetrics} object.
 *
 * @throws IndexOutOfBoundsException if the range [begin, limit] is
 * invalid in <code>text</code>.
 */
  public LineMetrics getLineMetrics(String text, int begin, int limit,
                                    Graphics g)
  {
  FontRenderContext rc;
  if (g instanceof Graphics2D)
    rc = ((Graphics2D) g).getFontRenderContext();
  else
    rc = gRC;
  return font.getLineMetrics(text, begin, limit, rc);
  }

  /**
 * Returns a {@link LineMetrics} object constructed with the
 * specified text and the {@link FontRenderContext} of the Graphics
 * object when it is an instance of Graphics2D or a generic
 * FontRenderContext with a null transform, not anti-aliased and not
 * using fractional metrics.
 *
 * @param chars The string to calculate metrics from.
 * @param begin Index of first character in <code>text</code> to measure.
 * @param limit Index of last character in <code>text</code> to measure.
 * @param g The Graphics object that will be used.
 *
 * @return A new {@link LineMetrics} object.
 *
 * @throws IndexOutOfBoundsException if the range [begin, limit] is
 * invalid in <code>text</code>.
 */
  public LineMetrics getLineMetrics(char[] chars, int begin, int limit,
                                    Graphics g)
  {
  FontRenderContext rc;
  if (g instanceof Graphics2D)
    rc = ((Graphics2D) g).getFontRenderContext();
  else
    rc = gRC;
  return font.getLineMetrics(chars, begin, limit, rc);
  }

  /**
 * Returns a {@link LineMetrics} object constructed with the
 * specified text and the {@link FontRenderContext} of the Graphics
 * object when it is an instance of Graphics2D or a generic
 * FontRenderContext with a null transform, not anti-aliased and not
 * using fractional metrics.
 *
 * @param ci An iterator over the string to calculate metrics from.
 * @param begin Index of first character in <code>text</code> to measure.
 * @param limit Index of last character in <code>text</code> to measure.
 * @param g The Graphics object that will be used.
 *
 * @return A new {@link LineMetrics} object.
 *
 * @throws IndexOutOfBoundsException if the range [begin, limit] is
 * invalid in <code>text</code>.
 */
  public LineMetrics getLineMetrics(CharacterIterator ci, int begin,
				  int limit, Graphics g)
  {
  FontRenderContext rc;
  if (g instanceof Graphics2D)
    rc = ((Graphics2D) g).getFontRenderContext();
  else
    rc = gRC;
  return font.getLineMetrics(ci, begin, limit, rc);
  }

  public Rectangle2D getStringBounds(String str, Graphics context)
  {
    return font.getStringBounds(str, getFontRenderContext(context));
  }

  public Rectangle2D getStringBounds(String str, int beginIndex, int limit,
                                     Graphics context)
  {
    return font.getStringBounds(str, beginIndex, limit,
                                getFontRenderContext(context));
  }

  public Rectangle2D getStringBounds(char[] chars, int beginIndex, int limit,
                                     Graphics context)
  {
    return font.getStringBounds(chars, beginIndex, limit,
                                getFontRenderContext(context));
  }

  public Rectangle2D getStringBounds(CharacterIterator ci, int beginIndex,
                                     int limit, Graphics context)
  {
    return font.getStringBounds(ci, beginIndex, limit,
                                getFontRenderContext(context));
  }

  private FontRenderContext getFontRenderContext(Graphics context)
  {
    if (context instanceof Graphics2D)
      return ((Graphics2D) context).getFontRenderContext();

    return gRC;
  }
}
