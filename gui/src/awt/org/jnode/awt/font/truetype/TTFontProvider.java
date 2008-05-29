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
import java.awt.FontMetrics;
import java.io.IOException;
import java.net.URL;
import org.apache.log4j.Logger;
import org.jnode.awt.font.TextRenderer;
import org.jnode.awt.font.renderer.RenderCache;
import org.jnode.awt.font.spi.AbstractFontProvider;

/**
 * @author epr
 * @author Fabien DUMINY (fduminy@jnode.org)
 */
public class TTFontProvider extends AbstractFontProvider {
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
        super("ttf");
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
     * Gets the font data for the given font
     *
     * @param font
     * @return The font data
     */
    private TTFFontData getFontData(Font font) {
        if (font instanceof TTFFont) {
            return ((TTFFont) font).getFontData();
        } else {
            final TTFFont ttf = (TTFFont) getCompatibleFont(font);
            if (ttf != null) {
                return ttf.getFontData();
            } else {
                log.warn("Font not instanceof TTFFont: " + font.getClass().getName());
                return null;
            }
        }
    }

    protected Font loadFont(URL url) throws IOException {
        log.debug("<<< loadFont(" + url + ") >>>");
        final TTFFontData fontData = new TTFFontDataFile(url);
        return new TTFFont(fontData, 10);
    }

    protected String[] getSystemFonts() {
        return SYSTEM_FONTS;
    }
}
