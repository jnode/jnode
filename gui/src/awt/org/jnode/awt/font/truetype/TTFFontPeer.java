package org.jnode.awt.font.truetype;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.text.CharacterIterator;
import java.util.Locale;
import java.util.Map;

import org.jnode.awt.font.JNodeFontPeer;

public class TTFFontPeer extends JNodeFontPeer<TTFontProvider, TTFFont> {

    public TTFFontPeer(TTFontProvider provider, String name, Map attrs) {
        super(provider, name, attrs);
    }

    @Override
    public boolean canDisplay(Font font, char c) {
        TTFFont ttfFont = getCompatibleFont(font);
        //ttfFont.getFontData().getGlyph(c);
        return false;
    }

    @Override
    public int canDisplayUpTo(Font font, CharacterIterator i, int start, int limit) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public GlyphVector createGlyphVector(Font font, FontRenderContext frc, CharacterIterator ci) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GlyphVector createGlyphVector(Font font, FontRenderContext ctx, int[] glyphCodes) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte getBaselineFor(Font font, char c) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public FontMetrics getFontMetrics(Font font) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getGlyphName(Font font, int glyphIndex) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LineMetrics getLineMetrics(Font font, CharacterIterator ci, int begin, int limit,
            FontRenderContext rc) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Rectangle2D getMaxCharBounds(Font font, FontRenderContext rc) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getMissingGlyphCode(Font font) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getNumGlyphs(Font font) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getPostScriptName(Font font) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Rectangle2D getStringBounds(Font font, CharacterIterator ci, int begin, int limit,
            FontRenderContext frc) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSubFamilyName(Font font, Locale locale) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasUniformLineMetrics(Font font) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public GlyphVector layoutGlyphVector(Font font, FontRenderContext frc, char[] chars, int start,
            int limit, int flags) {
        // TODO Auto-generated method stub
        return null;
    }

}
