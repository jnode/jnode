package org.jnode.awt.font.truetype;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.text.CharacterIterator;
import java.util.Locale;
import java.util.Map;

import org.jnode.awt.font.JNodeFontPeer;
import org.jnode.awt.font.spi.Glyph;

import sun.font.CoreMetrics;
import sun.font.FontLineMetrics;
import sun.font.StandardGlyphVector;

public class TTFFontPeer extends JNodeFontPeer<TTFontProvider, TTFFont> {

    public TTFFontPeer(TTFontProvider provider, String name, Map attrs) {
        super(provider, name, attrs);
    }

    @Override
    public boolean canDisplay(Font font, char c) {
        //TODO implement me 
        //TTFFont ttfFont = getCompatibleFont(font);
        //ttfFont.getFontData().getGlyph(c);
        return true;
    }

    @Override
    public byte getBaselineFor(Font font, char c) {
        // TODO find proper value from the TTFFontData 
        // it should be one of Font.CENTER_BASELINE, Font.HANGING_BASELINE, 
        // Font.ROMAN_BASELINE   
        return Font.ROMAN_BASELINE;
    }

    @Override
    public TTFFontMetrics getFontMetrics(Font font) {        
        try {
            //TODO we shouldn't create a new instance each time : use the cache in AbstractFontProvider ?
            return new TTFFontMetrics(font, getCompatibleFont(font).getFontData());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getGlyphName(Font font, int glyphIndex) {
        //TODO how do we get the glyph name ?
        return "";
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getLineMetrics(java.awt.Font,
     *      java.text.CharacterIterator, int, int,
     *      java.awt.font.FontRenderContext)
     */
    @Override
    public LineMetrics getLineMetrics(Font font, CharacterIterator ci,
                                      int begin, int limit, FontRenderContext rc) {
        TTFFontMetrics fm = getFontMetrics(font);
        
        float ascent = fm.getAscent();
        float descent = fm.getDescent();
        float leading = fm.getLeading();
        float height = fm.getHeight();
        
        // TODO find these metrics
        int baselineIndex = 0;
        float[] baselineOffsets = new float[]{0f};
        float strikethroughOffset = 0;
        float strikethroughThickness = 0;
        float underlineOffset = 0;
        float underlineThickness = 0;
        float ssOffset = 0;
        //
        
        float italicAngle = getItalicAngle(font);
        CoreMetrics cm = new CoreMetrics(ascent, descent, leading, height, baselineIndex, baselineOffsets,
                        strikethroughOffset, strikethroughThickness, underlineOffset,
                        underlineThickness, ssOffset, italicAngle);
        
        return new FontLineMetrics(limit - begin + 1, cm, rc);
    }

    @Override
    public Rectangle2D getMaxCharBounds(Font font, FontRenderContext rc) {
        TTFFont ttfFont = getCompatibleFont(font);
        
        final Rectangle2D bounds = provider.getMaxCharBounds(ttfFont.getFontData());
        transform(bounds, rc);
        
        return bounds;
    }

    @Override
    public int getMissingGlyphCode(Font font) {
        // TODO implement it (look at canDisplay(Font, char))
        return 0;
    }

    @Override
    public int getNumGlyphs(Font font) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getPostScriptName(Font font) {
        return getCompatibleFont(font).getPSName();
    }

    @Override
    public Rectangle2D getStringBounds(Font font, CharacterIterator ci, int begin, int limit,
            FontRenderContext frc) {
        TTFFont ttfFont = getCompatibleFont(font);
        TTFFontData container = ttfFont.getFontData();

        double width = 0;
        double height = 0;
        for (char c = ci.setIndex(begin); ci.getIndex() <= limit; c = ci.next()) {
            try {
                Glyph g = container.getGlyph(c);
                if (g != null) {
                    width += g.getBBox().getWidth();
                    height = Math.max(g.getBBox().getHeight(), height);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }                
        }
        final Rectangle2D bounds = new Rectangle2D.Double(0, 0, width, height);
                
        transform(bounds, frc);
        return bounds;
    }

    @Override
    public String getSubFamilyName(Font font, Locale locale) {
        //TODO implement me
        return "";
    }

    @Override
    public boolean hasUniformLineMetrics(Font font) {
        // We don't have "subfonts" (terms used in GNU Classpath javadoc)
        // => returns true
        return true;
    }

    @Override
    public GlyphVector layoutGlyphVector(Font font, FontRenderContext frc, char[] chars, int start,
            int limit, int flags) {
        //TODO work only for latin fonts but not for hindi, arabic ... fonts
        // see GNU Classpath javadoc
        return new StandardGlyphVector(font, chars, start, limit, frc);
    }

}
