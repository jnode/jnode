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
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.jnode.awt.font.TextRenderer;
import org.jnode.driver.video.Surface;
import org.jnode.font.bdf.BDFFontContainer;
import org.jnode.font.bdf.BDFGlyph;
import org.jnode.font.bdf.BDFMetrics;
import org.jnode.font.bdf.BDFParser;

/**
 * @author Stephane Meslin-Weber
 * @author Fabien DUMINY (fduminy@jnode.org)
 */
public class BDFTextRenderer implements TextRenderer {
	private BDFFontContainer bdfFont;
	
    /**
     * Create a new instance
     * 
     * @param bdfFont the font used for rendering
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
     * @param surface the rendering surface
     * @param clip clipping shape
     * @param tx transformation
     * @param str the string to render
     * @param x location x
     * @param y location y
     * @param color string color
     * @see java.awt.Graphics
     */
    final public void render(Surface surface, Shape clip, AffineTransform tx,
            String str, int x, int y, Color color) {   
        if (str == null || str.length() == 0)
            return;

        BDFMetrics fm = bdfFont.getFontMetrics();
        y-=fm.getDescent();
        int charsCount = str.length();

        if ((bdfFont != null) && (charsCount > 0)) {
            int offset = 0;
            final int bdfFontDepth = bdfFont.getDepth();

            BDFParser.Rectangle b_rect = new BDFParser.Rectangle();
            final Point2D src = new Point2D.Double();
            final Point2D dst = new Point2D.Double();
            
            for(int i=0;i<charsCount;i++) {
                int base = fm.getDescent();
                BDFGlyph glyph = bdfFont.getGlyph(str.charAt(i));
                if(glyph==null) {
                    continue;
                }
                
                final int fHeight= glyph.getBbx(b_rect).height;
                final int glyphBbxY = glyph.getBbx(b_rect).y;
//                final int bdfFontBbxHeight = bdfFont.getBoundingBox().height;
                final int[] fData = glyph.getData();
                final int scan = fData.length/fHeight;
                
//                if(i == 0)
//                {
//                    System.out.println("BDF:x="+x+" y="+y+" fHeight="+fHeight+
//                    		" fData.length="+fData.length+" scan="+scan+
//                    		" bdfFontBbxHeight="+bdfFontBbxHeight+
//                    		" glyphBbxY="+glyphBbxY+" base="+base);
//                    Rectangle r = clip.getBounds();
//                    System.out.println("bounds:x="+r.x+" y="+r.y+
//                    			" width="+r.width+" height="+r.height);
//                    src.setLocation(x, y);
//                    tx.transform(src, dst);                    
//                    System.out.println("newX="+dst.getX()+" newY="+dst.getY());
//                    
//                    double[] d = new double[9];
//                    tx.getMatrix(d);
//                    for(int j = 0 ; j  < d.length ; j++) System.out.print(d[j] + " ");
//                    System.out.println();
//                }
                
                for(int k=0;k<fHeight;k++) {
                	final int offsetLine = k*scan;
                    for(int j=0;j<scan;j++) {
                        int fPixel = fData[offsetLine+j];
                        if(fPixel!=0) {
                            int r = color.getRed(); //(color & 0x00FF0000) >> 16;
                            int g = color.getGreen(); //(color & 0x0000FF00) >> 8;
                            int b = color.getBlue(); //(color & 0x000000FF);
                            
                            r = ((r * fPixel)>>bdfFontDepth) & 0xFF;
                            g = ((g * fPixel)>>bdfFontDepth) & 0xFF;
                            b = ((b * fPixel)>>bdfFontDepth) & 0xFF;
                            
                            fPixel = (((r << 16)+ (g << 8) +  b )| 0xFF000000);
    
                            int px = x+offset+j;
                            //int py = y+(bdfFontBbxHeight+base-fHeight)+k-glyphBbxY;
                            int py = y+(base-fHeight)+k-glyphBbxY;
                            
                            src.setLocation(px, py);
                            tx.transform(src, dst);
                            //if(clip.contains(dst))
                            //{
	                            px = (int) dst.getX();
	                            py = (int) dst.getY();
	                            	
	                            surface.setRGBPixel(px, py, fPixel);
                            //}
                        }
                    }
                }
                
                offset+=glyph.getDWidth().width-glyph.getBbx(b_rect).x;
            }
        }
    }    
}
	