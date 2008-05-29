/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.awt.font;

import gnu.java.awt.peer.ClasspathFontPeer;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.peer.FontPeer;
import java.text.CharacterIterator;
import java.util.Locale;
import java.util.Map;

/**
 * @author vali
 *         <p/>
 *         To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Generation - Code and Comments
 */
public class JNodeFontPeer extends ClasspathFontPeer implements FontPeer {

    /**
     * @param name
     * @param style
     * @param size
     */
    public JNodeFontPeer(String name, int style, int size) {
        super(name, style, size);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param name
     * @param attrs
     */
    public JNodeFontPeer(String name, Map attrs) {
        super(name, attrs);
        // TODO Auto-generated constructor stub
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#canDisplay(java.awt.Font, char)
     */
    public boolean canDisplay(Font font, char c) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#canDisplayUpTo(java.awt.Font,
     *      java.text.CharacterIterator, int, int)
     */
    public int canDisplayUpTo(Font font, CharacterIterator i, int start,
                              int limit) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#createGlyphVector(java.awt.Font,
     *      java.awt.font.FontRenderContext, java.text.CharacterIterator)
     */
    public GlyphVector createGlyphVector(Font font, FontRenderContext frc,
                                         CharacterIterator ci) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#createGlyphVector(java.awt.Font,
     *      java.awt.font.FontRenderContext, int[])
     */
    public GlyphVector createGlyphVector(Font font, FontRenderContext ctx,
                                         int[] glyphCodes) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getBaselineFor(java.awt.Font,
     *      char)
     */
    public byte getBaselineFor(Font font, char c) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getFontMetrics(java.awt.Font)
     */
    public FontMetrics getFontMetrics(Font font) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getGlyphName(java.awt.Font, int)
     */
    public String getGlyphName(Font font, int glyphIndex) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getLineMetrics(java.awt.Font,
     *      java.text.CharacterIterator, int, int,
     *      java.awt.font.FontRenderContext)
     */
    public LineMetrics getLineMetrics(Font font, CharacterIterator ci,
                                      int begin, int limit, FontRenderContext rc) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getMaxCharBounds(java.awt.Font,
     *      java.awt.font.FontRenderContext)
     */
    public Rectangle2D getMaxCharBounds(Font font, FontRenderContext rc) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getMissingGlyphCode(java.awt.Font)
     */
    public int getMissingGlyphCode(Font font) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getNumGlyphs(java.awt.Font)
     */
    public int getNumGlyphs(Font font) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getPostScriptName(java.awt.Font)
     */
    public String getPostScriptName(Font font) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getStringBounds(java.awt.Font,
     *      java.text.CharacterIterator, int, int,
     *      java.awt.font.FontRenderContext)
     */
    public Rectangle2D getStringBounds(Font font, CharacterIterator ci,
                                       int begin, int limit, FontRenderContext frc) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getSubFamilyName(java.awt.Font,
     *      java.util.Locale)
     */
    public String getSubFamilyName(Font font, Locale locale) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#hasUniformLineMetrics(java.awt.Font)
     */
    public boolean hasUniformLineMetrics(Font font) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#layoutGlyphVector(java.awt.Font,
     *      java.awt.font.FontRenderContext, char[], int, int, int)
     */
    public GlyphVector layoutGlyphVector(Font font, FontRenderContext frc,
                                         char[] chars, int start, int limit, int flags) {
        // TODO Auto-generated method stub
        return null;
    }
}
