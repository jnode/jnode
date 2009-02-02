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
 
package sun.font;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D.Float;
import java.util.HashMap;
import java.util.Locale;

/**
 * @see sun.font.FontManager
 * @author Levente S\u00e1ntha
 */
class NativeFontManager {
    /**
     * @see sun.font.FontManager#initIDs()
     */
    private static void initIDs() {
        //todo implement it
    }
    /**
     * @see sun.font.FontManager#setFont2D(java.awt.Font, sun.font.Font2DHandle)
     */
    private static void setFont2D(Font arg1, Font2DHandle arg2) {
        //todo implement it
    }
    /**
     * @see sun.font.FontManager#isCreatedFont(java.awt.Font)
     */
    private static boolean isCreatedFont(Font arg1) {
        //todo implement it
        return false;
    }
    /**
     * @see sun.font.FontManager#setCreatedFont(java.awt.Font)
     */
    private static void setCreatedFont(Font arg1) {
        //todo implement it
    }
    /**
     * @see sun.font.FontManager#populateFontFileNameMap(java.util.HashMap, java.util.HashMap, java.util.HashMap, java.util.Locale)
     */
    private static void populateFontFileNameMap(HashMap arg1, HashMap arg2, HashMap arg3, Locale arg4) {
        //todo implement it
    }
    /**
     * @see sun.font.FontManager#getFont2D(java.awt.Font)
     */
    private static Font2D getFont2D(Font arg1) {
        //todo implement it
        return new Font2D() {

            @Override
            FontStrike createStrike(FontStrikeDesc desc) {
                final FontStrike strike = new FontStrike() {

                    @Override
                    Float getCharMetrics(char ch) {
                        // TODO Auto-generated method stub
                        return new Float(10, 10);
                    }

                    @Override
                    float getCodePointAdvance(int cp) {
                        // TODO Auto-generated method stub
                        return 0;
                    }

                    @Override
                    StrikeMetrics getFontMetrics() {
                        // TODO Auto-generated method stub
                        return new StrikeMetrics();
                    }

                    @Override
                    float getGlyphAdvance(int glyphCode) {
                        // TODO Auto-generated method stub
                        return 0;
                    }

                    @Override
                    void getGlyphImageBounds(int glyphcode, Float pt, Rectangle result) {
                        // TODO Auto-generated method stub
                        result.setBounds((int) pt.getX(), (int) pt.getY(), 10, 10);
                    }

                    @Override
                    long getGlyphImagePtr(int glyphcode) {
                        // TODO Auto-generated method stub
                        return 0;
                    }

                    @Override
                    void getGlyphImagePtrs(int[] glyphCodes, long[] images, int len) {
                        // TODO Auto-generated method stub
                        
                    }

                    @Override
                    Float getGlyphMetrics(int glyphcode) {
                        // TODO Auto-generated method stub
                        return new Float(10, 10);
                    }

                    @Override
                    GeneralPath getGlyphOutline(int glyphCode, float x, float y) {
                        return new GeneralPath(getGlyphOutlineBounds(glyphCode));
                    }

                    @Override
                    java.awt.geom.Rectangle2D.Float getGlyphOutlineBounds(int glyphCode) {
                        // TODO Auto-generated method stub
                        return new java.awt.geom.Rectangle2D.Float(0, 0, 10, 10);
                    }

                    @Override
                    GeneralPath getGlyphVectorOutline(int[] glyphs, float x, float y) {
                        // TODO Auto-generated method stub
                        GeneralPath path = getGlyphOutline(glyphs[0], x, y);
                        
                        for(int i = 1; i < glyphs.length; i++) {
                            path.append(getGlyphOutline(glyphs[i], x, y), false);
                        }
                        
                        return path;
                    }

                    @Override
                    public int getNumGlyphs() {
                        // TODO Auto-generated method stub
                        return 0;
                    }
                    
                };
                
                return strike;
            }

            @Override
            CharToGlyphMapper getMapper() {
                final CharToGlyphMapper mapper = new CharToGlyphMapper() {

                    @Override
                    public void charsToGlyphs(int count, char[] unicodes, int[] glyphs) {
                        // TODO Auto-generated method stub
                        
                    }

                    @Override
                    public void charsToGlyphs(int count, int[] unicodes, int[] glyphs) {
                        // TODO Auto-generated method stub
                        
                    }

                    @Override
                    public boolean charsToGlyphsNS(int count, char[] unicodes, int[] glyphs) {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public int getNumGlyphs() {
                        // TODO Auto-generated method stub
                        return 0;
                    }
                    
                };
                
                return mapper;
            }
            
        };
    }
    /**
     * @see sun.font.FontManager#getPlatformFontVar()
     */
    private static boolean getPlatformFontVar() {
        //todo implement it
        return false;
    }
    /**
     * @see sun.font.FontManager#getFontPath(boolean)
     */
    private static String getFontPath(boolean arg1) {
        //todo implement it
        return null;
    }
    /**
     * @see sun.font.FontManager#setNativeFontPath(java.lang.String)
     */
    private static void setNativeFontPath(String arg1) {
        //todo implement it
    }
    /**
     * @see sun.font.FontManager#getFontConfigAASettings(java.lang.String, java.lang.String)
     */
    private static int getFontConfigAASettings(String arg1, String arg2) {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.font.FontManager#getFontConfig(java.lang.String, sun.font.FontManager.FontConfigInfo[])
     */
    private static void getFontConfig(String arg1, FontManager.FontConfigInfo[] arg2) {
        //todo implement it
    }
}
