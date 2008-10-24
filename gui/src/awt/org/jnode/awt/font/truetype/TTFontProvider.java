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

package org.jnode.awt.font.truetype;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jnode.awt.font.TextRenderer;
import org.jnode.awt.font.renderer.RenderCache;
import org.jnode.awt.font.spi.AbstractFontProvider;

/**
 * @author epr
 * @author Fabien DUMINY (fduminy@jnode.org)
 */
public class TTFontProvider extends AbstractFontProvider<TTFFont, TTFFontData> {
    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(TTFontProvider.class);

    /**
     * All system fonts
     */
    private static final String SYSTEM_FONTS[] = {
        "bhm.ttf", "bhmbd.ttf", "bhmbi.ttf", "bhmi.ttf", "luxisr.ttf", "roman.ttf", "times.ttf", "velehrad.ttf"
    };

    public TTFontProvider() {
        super(TTFFont.class, "ttf");
        log.debug("new TTFontProvider");
    }

    protected TextRenderer createTextRenderer(RenderCache renderCache, Font font) {
        TextRenderer r;
//        if (false || renderer.equals("new")) {
        r = new TTFTextRenderer(renderCache, getFontMetrics(font), getFontData(font));
//        } else {
//            r = new TTFSimpleTextRenderer(getFontData(font), font.getSize());                
//        }
        log.debug("created TextRenderer for TTF");
        return r;
    }

    protected FontMetrics createFontMetrics(Font font) throws IOException {
        return new TTFFontMetrics(font, getFontData(font));
    }

    
    /**
     * Creates a font peer from the given name or return null if not supported/provided.
     * As said in {@link org.jnode.awt.JNodeToolkit#getClasspathFontPeer(String, java.util.Map)} javadoc :
     * "We don't know what kind of "name" the user requested (logical, face, family)".
     * 
     * @param name
     * @param attrs
     * @return
     */
    @Override
    public TTFFontPeer createFontPeer(String name, Map attrs) {
        //TODO implement me
//        TTFFontPeer peer = null;
//
//        List<BDFFontContainer> datas = getUserFontDatas();
//        for (BDFFontContainer container : datas) {
//            if (match(container, name, attrs)) {
//                peer = new TTFFontPeer(this, name, attrs);
//                datas.remove(container);
//                break;
//            }
//        }
//        
//        for (BDFFontContainer container : getContainers()) {
//            if (match(container, name, attrs)) {
//                peer = new TTFFontPeer(this, name, attrs);
//                break;
//            }
//        }
//        
//        return peer;
        
        return new TTFFontPeer(this, name, attrs);
    }

    /**
     * Read an create a Font from the given InputStream
     *
     * @param stream
     * @return
     */
    @Override
    public TTFFont createFont(InputStream stream) throws FontFormatException, IOException {
        try {
            TTFFontDataFile data = new TTFFontDataFile(new TTFMemoryInput(stream));
            addUserFontData(data);
            return new TTFFont(data, 10);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            FontFormatException ffe = new FontFormatException("bad ttf format");
            ffe.initCause(e);
            throw ffe;
        }        
    }
    
    /**
     * Gets the font data for the given font
     *
     * @param font
     * @return The font data
     */
    private TTFFontData getFontData(Font font) {
        if (font instanceof TTFFont) {
            return ((TTFFont) font).getFontData();
        } else {
            final TTFFont ttf = getCompatibleFont(font);
            if (ttf != null) {
                return ttf.getFontData();
            } else {
                log.warn("Font not instanceof TTFFont: " + font.getClass().getName());
                return null;
            }
        }
    }

    @Override
    protected void loadFontsImpl() {
        for (String fontResource : SYSTEM_FONTS) {
            try {
                final ClassLoader cl = Thread.currentThread().getContextClassLoader();
                final URL url = cl.getResource(fontResource);
                if (url != null) {
                    final TTFFontData fontData = new TTFFontDataFile(url);
                    addFont(new TTFFont(fontData, 10));
                } else {
                    log.error("Cannot find font resource " + fontResource);
                }
            } catch (IOException ex) {
                log.error("Cannot find font " + fontResource + ": " + ex.getMessage());
            } catch (Throwable ex) {
                log.error("Cannot find font " + fontResource, ex);
            }
        }            
    }

    @Override
    protected Size getMaxCharSize(TTFFontData fontData) {
        Size size = new Size();
        
        //TODO implement it
//        for (TTFGlyph g : fontData.getGlyph(c)) {
//            if (g != null) {
//                size.maxCharWidth += g.getDWidth().width;
//                size.maxCharHeight = Math.max(g.getDWidth().height, size.maxCharHeight);
//            }
//        }
        // temporary workaround
        size.maxCharWidth = 15;
        size.maxCharHeight = 15;
        
        
        return size;
    }
}
