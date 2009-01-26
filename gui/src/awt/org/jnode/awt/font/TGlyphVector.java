package org.jnode.awt.font;

import sun.font.StandardGlyphVector;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.text.CharacterIterator;

/**
 * Temporary hack to support SurfaceGraphics2D.drawGlyphVector(GlyphVector g, float x, float y).
 */
public class TGlyphVector extends StandardGlyphVector {
    private char[] chars;
    public static TGlyphVector create(Font font, FontRenderContext frc, CharacterIterator ci) {
        StringBuilder sb = new StringBuilder(80);
        for (char c = ci.first(); (ci.getIndex() <= ci.getEndIndex()) && (c != CharacterIterator.DONE); c = ci.next())
            sb.append(c);

        return new TGlyphVector(font, sb.toString().toCharArray(), frc);
    }

    public TGlyphVector(Font font, char[] chars, int start, int limit, FontRenderContext frc) {
        super(font, chars, start, limit, frc);
        this.chars = new char[limit];
        System.arraycopy(chars, start, this.chars, 0, limit);
    }

    public TGlyphVector(Font font, char[] chars, FontRenderContext frc) {
        super(font, chars, 0, chars.length, frc);
        this.chars = chars;
    }

    public char[] getChars() {
        return chars;
    }
}
