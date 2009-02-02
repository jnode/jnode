/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
