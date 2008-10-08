package org.jnode.awt.font.bdf;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.text.CharacterIterator;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jnode.awt.font.JNodeFontPeer;
import org.jnode.font.bdf.BDFFontContainer;
import org.jnode.font.bdf.BDFGlyph;
import org.jnode.font.bdf.BDFMetrics;

import sun.font.CoreMetrics;
import sun.font.FontLineMetrics;
import sun.font.StandardGlyphVector;

/**
 * Specific implementation of {@link JNodeFontPeer} for BDF fonts
 * 
 * @author fabien
 *
 */
public class BDFFontPeer extends JNodeFontPeer<BDFFontProvider, BDFFont> {
    private static final Logger log = Logger.getLogger(BDFFontPeer.class);    

    /**
     * this the char used to replace missing glyphs in BDFFont
     */
    private static final char MISSING_GLYPH_CODE = '\u0020';
    
    public BDFFontPeer(BDFFontProvider provider, String name, Map attrs) {
        super(provider, name, attrs);
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#canDisplay(java.awt.Font, char)
     */
    @Override
    public boolean canDisplay(Font font, char c) {        
        BDFFont bdfFont = getCompatibleFont(font);
        
        //TODO this is a temporary workaround : we should add a method to BDFFont
        BDFGlyph spaceGlyph = bdfFont.getContainer().getGlyph(MISSING_GLYPH_CODE);
        BDFGlyph characterGlyph = bdfFont.getContainer().getGlyph(c);
        
        return (c == MISSING_GLYPH_CODE) || ((c != MISSING_GLYPH_CODE) && (characterGlyph != spaceGlyph));
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getBaselineFor(java.awt.Font,
     *      char)
     */
    @Override
    public byte getBaselineFor(Font font, char c) {
        // TODO find proper value from the BDFFontContainer 
        // it should be one of Font.CENTER_BASELINE, Font.HANGING_BASELINE, 
        // Font.ROMAN_BASELINE   
        return Font.ROMAN_BASELINE;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getFontMetrics(java.awt.Font)
     */
    @Override
    public FontMetrics getFontMetrics(Font font) {
        return getCompatibleFont(font).getFontMetrics();
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getGlyphName(java.awt.Font, int)
     */
    @Override
    public String getGlyphName(Font font, int glyphIndex) {
        return getCompatibleFont(font).getContainer().getGlyphs()[glyphIndex].getName();
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getLineMetrics(java.awt.Font,
     *      java.text.CharacterIterator, int, int,
     *      java.awt.font.FontRenderContext)
     */
    @Override
    public LineMetrics getLineMetrics(Font font, CharacterIterator ci,
                                      int begin, int limit, FontRenderContext rc) {
        BDFFont bdfFont = getCompatibleFont(font);
        BDFMetrics fm = bdfFont.getContainer().getFontMetrics();
        
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

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getMaxCharBounds(java.awt.Font,
     *      java.awt.font.FontRenderContext)
     */
    @Override
    public Rectangle2D getMaxCharBounds(Font font, FontRenderContext rc) {
        BDFFont bdfFont = getCompatibleFont(font);
                        
        final Rectangle2D bounds = provider.getMaxCharBounds(bdfFont.getContainer());
        transform(bounds, rc);
        
        return bounds;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getMissingGlyphCode(java.awt.Font)
     */
    @Override
    public int getMissingGlyphCode(Font font) {
        //TODO this is a temporary workaround : we should add a method to BDFFont
        return MISSING_GLYPH_CODE; // this the char used to replace missing glyphs in BDFFont
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getNumGlyphs(java.awt.Font)
     */
    @Override
    public int getNumGlyphs(Font font) {
        return getCompatibleFont(font).getContainer().getGlyphs().length;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getPostScriptName(java.awt.Font)
     */
    @Override
    public String getPostScriptName(Font font) {
        return getCompatibleFont(font).getContainer().getName();
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#getStringBounds(java.awt.Font,
     *      java.text.CharacterIterator, int, int,
     *      java.awt.font.FontRenderContext)
     */
    @Override
    public Rectangle2D getStringBounds(Font font, CharacterIterator ci,
                                       int begin, int limit, FontRenderContext frc) {
        BDFFont bdfFont = getCompatibleFont(font);
        BDFFontContainer container = bdfFont.getContainer();

        double width = 0;
        double height = 0;
        for (char c = ci.setIndex(begin); ci.getIndex() <= limit; c = ci.next()) {
            BDFGlyph g = container.getGlyph(c);
            if (g != null) {
                width += g.getDWidth().width;
                height = Math.max(g.getDWidth().height, height);
            }                
        }
        final Rectangle2D bounds = new Rectangle2D.Double(0, 0, width, height);
                
        transform(bounds, frc);
        return bounds;
    }

    /**
     * 
     * @see gnu.java.awt.peer.ClasspathFontPeer#getSubFamilyName(java.awt.Font,
     *      java.util.Locale)
     */
    @Override
    public String getSubFamilyName(Font font, Locale locale) {
        System.out.println("JNodeFontPeer.getSubFamilyName not implemented");
        // TODO not implemented ... remove that while moving to openjdk
        return "";
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#hasUniformLineMetrics(java.awt.Font)
     */
    @Override
    public boolean hasUniformLineMetrics(Font font) {
        // We don't have "subfonts" (terms used in GNU Classpath javadoc)
        // => returns true
        return true;
    }

    /**
     * @see gnu.java.awt.peer.ClasspathFontPeer#layoutGlyphVector(java.awt.Font,
     *      java.awt.font.FontRenderContext, char[], int, int, int)
     */
    @Override
    public GlyphVector layoutGlyphVector(Font font, FontRenderContext frc,
                                         char[] chars, int start, int limit, int flags) {
        //TODO work only for latin fonts but not for hindi, arabic ... fonts
        // see GNU Classpath javadoc
        return new StandardGlyphVector(font, chars, start, limit, frc);
    }
}
