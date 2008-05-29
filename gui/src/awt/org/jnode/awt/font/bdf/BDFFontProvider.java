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

import java.awt.Font;
import java.awt.FontMetrics;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jnode.awt.font.TextRenderer;
import org.jnode.awt.font.renderer.RenderCache;
import org.jnode.awt.font.spi.AbstractFontProvider;
import org.jnode.font.bdf.BDFFontContainer;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 */
public class BDFFontProvider extends AbstractFontProvider {
    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(BDFFontProvider.class);

    static {
        log.setLevel(Level.DEBUG);
    }

    /**
     * All system fonts
     */
    private static final String SYSTEM_FONTS[] = {"Vera-10.bdf", "Vera-12.bdf", "Vera-14.bdf", "VeraMono-12-8.bdf"};

    public BDFFontProvider() {
        super("bdf");
        log.debug("new BDFFontProvider");
    }

    protected TextRenderer createTextRenderer(RenderCache renderCache, Font font) {
        final BDFFont bdfFont = getBDFFont(font);
        final TextRenderer renderer = new BDFTextRenderer(bdfFont.getContainer());
        log.debug("created TextRenderer for BDF");
        return renderer;
    }


    protected FontMetrics createFontMetrics(Font font) throws IOException {
        final BDFFont bdfFont = getBDFFont(font);
        return bdfFont.getFontMetrics();
    }

    protected Font loadFont(URL url) throws IOException {
        log.debug("<<< loadFont(" + url + ") >>>");
        Reader reader = new InputStreamReader(url.openStream());
        try {
            log.debug("loadFont: before BDFFontContainer.createFont");
            BDFFontContainer container = BDFFontContainer.createFont(reader);
            log.debug("loadFont: before new BDFFont");
            return new BDFFont(container);
        } catch (Exception e) {
            IOException ioe = new IOException("can't load BDFFont from " + url);
            ioe.initCause(e);
            throw ioe;
        }
    }

    protected String[] getSystemFonts() {
        return SYSTEM_FONTS;
    }

    private BDFFont getBDFFont(Font font) {
        if (font instanceof BDFFont) {
            return (BDFFont) font;
        } else {
            final BDFFont bdf = (BDFFont) getCompatibleFont(font);
            if (bdf != null) {
                return bdf;
            } else {
                log.warn("Font not instanceof BDFFont: " + font.getClass().getName());
                return null;
            }
        }
    }
}
