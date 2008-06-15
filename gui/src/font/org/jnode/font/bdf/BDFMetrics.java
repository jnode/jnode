/*
 Copyright (C) 2002-2006 Stephane Meslin-Weber <steph@tangency.co.uk>
 All rights reserved.
 
 This file is part of Odonata.
 
 Odonata is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 Odonata is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with GNU Classpath; see the file COPYING.  If not, write to
 the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 Boston, MA 02110-1301 USA.

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
 exception statement from your version. 
 */
package org.jnode.font.bdf;

/**
 * Represents a FontMetrics for BDF fonts.
 * 
 * @author Stephane Meslin-Weber
 */
public class BDFMetrics {
    private static final long serialVersionUID = -4874492191748367800L;

    private BDFFontContainer font;

    protected BDFMetrics(BDFFontContainer font) {

        this.font = font;

    }

    public int getAscent() {
        return font.getBoundingBox().height + getDescent();
    }

    public int getDescent() {
        return font.getBoundingBox().y;
    }

    public int getLeading() {
        return font.getBoundingBox().x;
    }

    public int getMaxAdvance() {
        return font.getBoundingBox().width;
    }

    public int getHeight() {
        return font.getBoundingBox().height;
    }

    public int charWidth(char ch) {
        BDFGlyph g = font.getGlyph(ch);
        if (g != null) {
            BDFParser.Rectangle r = g.getBbx();
            r.width = (r.width + 1) & ~1;
            return r.width;
        }
        return 0;
    }

    public int[] charsWidths(char[] chars, final int start, final int end) {
        int[] advances = new int[chars.length];
        int adv_idx = 0;
        int last = (advances.length - 1);

        BDFParser.Rectangle b_rec = new BDFParser.Rectangle();
        for (int i = start; i < start + end; i++) {
            BDFGlyph glyph = font.getGlyph(chars[i]);
            if (adv_idx == 0) {
                advances[adv_idx++] = glyph.getBbx(b_rec).x;
            } else if (adv_idx != last) {
                advances[adv_idx++] =
                        (advances[adv_idx - 1] + glyph.getDWidth().width) - glyph.getBbx(b_rec).x;
            } else {
                // FIXME: what's this 12 doing here?
                advances[adv_idx++] = (advances[adv_idx - 1] + glyph.getDWidth().width) + 12;
            }
        }

        return advances;
    }

    public int charsWidth(char[] chars, int start, int end) {
        int total = 0;

        int[] lengths = charsWidths(chars, start, end);
        for (int i = 0; i < lengths.length; i++)
            total += lengths[i];

        return total;
    }
}
