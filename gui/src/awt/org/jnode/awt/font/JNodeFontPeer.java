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

import sun.font.StandardGlyphVector;

/**
 * @author vali
 *         <p/>
 *         To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Generation - Code and Comments
 */
public abstract class JNodeFontPeer<FP extends FontProvider<F>, F extends Font> 
        extends ClasspathFontPeer implements FontPeer {
    protected final FP provider;
    
    /**
     * @param name
     * @param attrs
     */
    public JNodeFontPeer(FP provider, String name, Map attrs) {
        super(name, attrs);
        this.provider = provider;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#canDisplay(java.awt.Font, char)
     */
    public abstract boolean canDisplay(Font font, char c);

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#canDisplayUpTo(java.awt.Font,
     *      java.text.CharacterIterator, int, int)
     */
    @Override
    public final int canDisplayUpTo(Font font, CharacterIterator i, int start, int limit) {
        int upTo = -1;
        
        for (char c = i.setIndex(start); i.getIndex() <= limit; c = i.next()) {
            if (!canDisplay(font, c)) {
                upTo = i.getIndex();
                break;
            }
        }
    
        return upTo;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#createGlyphVector(java.awt.Font,
     *      java.awt.font.FontRenderContext, java.text.CharacterIterator)
     */
    @Override
    public final GlyphVector createGlyphVector(Font font, FontRenderContext frc,
                                         CharacterIterator ci) {
        return new StandardGlyphVector(font, ci, frc);
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#createGlyphVector(java.awt.Font,
     *      java.awt.font.FontRenderContext, int[])
     */
    @Override
    public final GlyphVector createGlyphVector(Font font, FontRenderContext ctx,
                                         int[] glyphCodes) {
        return new StandardGlyphVector(font, glyphCodes, ctx);
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getBaselineFor(java.awt.Font,
     *      char)
     */
    public abstract byte getBaselineFor(Font font, char c);

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getFontMetrics(java.awt.Font)
     */
    public abstract FontMetrics getFontMetrics(Font font);

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getGlyphName(java.awt.Font, int)
     */
    public abstract String getGlyphName(Font font, int glyphIndex);

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getLineMetrics(java.awt.Font,
     *      java.text.CharacterIterator, int, int,
     *      java.awt.font.FontRenderContext)
     */
    public abstract LineMetrics getLineMetrics(Font font, CharacterIterator ci,
                                      int begin, int limit, FontRenderContext rc);

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getMaxCharBounds(java.awt.Font,
     *      java.awt.font.FontRenderContext)
     */
    public abstract Rectangle2D getMaxCharBounds(Font font, FontRenderContext rc);

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getMissingGlyphCode(java.awt.Font)
     */
    public abstract int getMissingGlyphCode(Font font);

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getNumGlyphs(java.awt.Font)
     */
    public abstract int getNumGlyphs(Font font);

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getPostScriptName(java.awt.Font)
     */
    public abstract String getPostScriptName(Font font);

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getStringBounds(java.awt.Font,
     *      java.text.CharacterIterator, int, int,
     *      java.awt.font.FontRenderContext)
     */
    public abstract Rectangle2D getStringBounds(Font font, CharacterIterator ci,
                                       int begin, int limit, FontRenderContext frc);

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getSubFamilyName(java.awt.Font,
     *      java.util.Locale)
     */
    public abstract String getSubFamilyName(Font font, Locale locale);

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#hasUniformLineMetrics(java.awt.Font)
     */
    public abstract boolean hasUniformLineMetrics(Font font);

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#layoutGlyphVector(java.awt.Font,
     *      java.awt.font.FontRenderContext, char[], int, int, int)
     */
    public abstract GlyphVector layoutGlyphVector(Font font, FontRenderContext frc,
                                         char[] chars, int start, int limit, int flags);
    
    /**
     * Convert the given font to a Font whose type is F.
     * The font given as input might not be an instance of F 
     * since {@link Font} class is public, not abstract and has a public constructor.
     * If that's the case, then we are trying to find the closest font that 
     * this peer's provider provides.
     *   
     * @param font any instance of {@link Font} (might not be an instance of F)
     * @return
     */
    protected final F getCompatibleFont(Font font) {
        return provider.getCompatibleFont(font);
    }
    
    protected final void transform(Rectangle2D bounds, FontRenderContext frc) {
        if (frc.getTransform() != null) {
            double[] srcPoints =
                    new double[] {bounds.getMinX(), bounds.getMinY(), 
                        bounds.getMinX(), bounds.getMaxY(), 
                        bounds.getMaxX(), bounds.getMaxY(), 
                        bounds.getMaxX(), bounds.getMinY()};
            double[] dstPoints = new double[srcPoints.length]; 
            frc.getTransform().transform(srcPoints, 0, dstPoints, 0, srcPoints.length / 2);
            
            // compute the bounding box of the result
            double minX = dstPoints[0];
            double minY = dstPoints[1];
            double maxX = minX;
            double maxY = minY;
            for (int i = 2; i < dstPoints.length; i += 2) {
                double x = dstPoints[i];
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
                
                double y = dstPoints[i + 1];
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
            bounds.setRect(minX, minY, maxX - minX + 1, maxY - minY + 1);
        }        
    }    
}
