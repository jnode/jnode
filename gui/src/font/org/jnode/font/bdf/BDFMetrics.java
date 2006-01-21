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
 along with Odonata; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
 */
package org.jnode.font.bdf;

import java.awt.FontMetrics;
import java.awt.Rectangle;


/**
 * Represents a FontMetrics for BDF fonts.
 * @author Stephane Meslin-Weber
 */
public class BDFMetrics extends FontMetrics {
    private static final long serialVersionUID = -4874492191748367800L;

    private BDFFont font;

    protected BDFMetrics(BDFFont font) {
        super(font);
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

    public int charWidth(char ch) {
        BDFGlyph g = font.getGlyph(ch);
        if (g != null) {
            Rectangle r = g.getBbx();
            r.width = (r.width + 1) & ~1;
            return r.width;
        }
        return 0;
    }
    
    public int[] charsWidths(char[] chars, final int start, final int end) {
        int[] advances = new int[chars.length];
        int adv_idx = 0;
        int last = (advances.length-1);

        for(int i=start;i<start+end;i++) {
            BDFGlyph glyph = font.getGlyph(chars[i]);
            if(adv_idx==0) {
                advances[adv_idx++] = font.getGlyph(chars[i]).getBbx().x;
            } else if(adv_idx!=last) {
                advances[adv_idx++] = (advances[adv_idx-1] + glyph.getDWidth().width) - glyph.getBbx().x;
            } else {
                // FIXME: what's this 12 doing here?
                advances[adv_idx++] = (advances[adv_idx-1] + glyph.getDWidth().width)+12;
            }
        }
        
        return advances;
    }

    public int charsWidth(char[] chars, int start, int end) {
        int total = 0;

        int[] lengths = charsWidths(chars,start,end);
        for(int i=0;i<lengths.length;i++)
            total+=lengths[i];
        
        return total;
    }
}