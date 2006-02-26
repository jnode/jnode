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
 
package org.jnode.awt.font.bdf;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import org.jnode.awt.font.TextRenderer;
import org.jnode.awt.font.renderer.RenderCache;
import org.jnode.awt.font.spi.AbstractTextRenderer;
import org.jnode.awt.font.spi.FontData;
import org.jnode.driver.video.Surface;
import org.jnode.font.bdf.BDFFontContainer;
import org.jnode.font.bdf.BDFGlyph;
import org.jnode.font.bdf.BDFMetrics;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 */
public class BDFTextRenderer implements TextRenderer {
	private BDFFontContainer bdfFont;
	
    /**
     * Create a new instance
     * 
     * @param fontData
     * @param fontSize
     */
    public BDFTextRenderer(BDFFontContainer bdfFont) {
    	this.bdfFont = bdfFont;
    }
    
    /**
     * Strings are drawn using .pjaf font files read in <code>PJAFontData</code>
     * objects by <code>PJAGraphicsManager</code>.
     * <p>
     * NOTE: This method derived from PJA rendering code.
     * 
     * @see com.eteks.awt.PJAGraphicsManager
     * @see com.eteks.awt.PJAFontData
     * @see java.awt.Graphics
     */
    final public void render(Surface surface, Shape clip, AffineTransform tx,
            String str, int x, int y, Color color) {   
        if (str == null || str.length() == 0) {
            System.err.println("empty string!");
            return;
        }
        
        BDFMetrics fm = (BDFMetrics)bdfFont.getFontMetrics();
        int charsCount = str.length();

        if ((bdfFont != null) && (charsCount > 0)) {
            int offset = 0;
            char[] chars = str.toCharArray();

            for(int i=0;i<charsCount;i++) {
                int base = fm.getDescent();
                BDFGlyph glyph = bdfFont.getGlyph(chars[i]);
                if(glyph==null) {
                    continue;
                }
                
                int fHeight= glyph.getBbx().height;
                int[] fData = glyph.getData();
                int scan = fData.length/fHeight;
                
                for(int k=0;k<fHeight;k++) {
                    for(int j=0;j<scan;j++) {
                        int fPixel = fData[(k*scan)+j];
                        if(fPixel!=0) {
                            int r = color.getRed(); //(color & 0x00FF0000) >> 16;
                            int g = color.getGreen(); //(color & 0x0000FF00) >> 8;
                            int b = color.getBlue(); //(color & 0x000000FF);
                            
                            r = ((r * fPixel)>>bdfFont.getDepth()) & 0xFF;
                            g = ((g * fPixel)>>bdfFont.getDepth()) & 0xFF;
                            b = ((b * fPixel)>>bdfFont.getDepth()) & 0xFF;
                            
                            fPixel = (((r << 16)+ (g << 8) +  b )| 0xFF000000);
    
                            int px = x+offset+j;
                            int py = y+(bdfFont.getBoundingBox().height+base-fHeight)+k-glyph.getBbx().y; 
                            surface.setRGBPixel(px, py, fPixel);
                        }
                    }
                }
                
                offset+=glyph.getDWidth().width-glyph.getBbx().x;
            }
        }
    }    
}
	