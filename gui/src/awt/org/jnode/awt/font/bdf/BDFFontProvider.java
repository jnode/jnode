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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jnode.awt.JNodeToolkit;
import org.jnode.awt.font.FontProvider;
import org.jnode.awt.font.TextRenderer;
import org.jnode.awt.font.renderer.RenderCache;
import org.jnode.awt.font.spi.AbstractFontProvider;
import org.jnode.font.bdf.BDFFontContainer;
import org.jnode.vm.Unsafe;

/**
 * {@link FontProvider} for {@link BDFFont}s.
 * 
 * @author Fabien DUMINY (fduminy@jnode.org)
 */
public class BDFFontProvider extends AbstractFontProvider<BDFFont> {
    static final String NAME = "bdf";
    
    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(BDFFontProvider.class);

    /**
     * All system fonts
     */
    private static final String SYSTEM_FONTS[] = {
        "Vera-10.bdf", "Vera-12.bdf", "Vera-14.bdf", "VeraMono-12-8.bdf", "6x12_FixedMedium-12.bdf"
    };

    private List<BDFFontContainer> containers; 
    
    public BDFFontProvider() {
        super(NAME);        
    }

    protected TextRenderer createTextRenderer(RenderCache renderCache, Font font) {
        final BDFFont bdfFont = getBDFFont(font);
        final TextRenderer renderer = new BDFTextRenderer(bdfFont.getContainer());
        return renderer;
    }


    protected FontMetrics createFontMetrics(Font font) throws IOException {
        final BDFFont bdfFont = getBDFFont(font);
        return bdfFont.getFontMetrics();
    }
    
    /**
     * Creates a font peer from the given name or return null if not supported/provided.
     * As said in {@link JNodeToolkit#getClasspathFontPeer(String, java.util.Map)} javadoc : 
     * "We don't know what kind of "name" the user requested (logical, face, family)".
     * 
     * @param name
     * @param attrs
     * @return
     */
    @Override
    public BDFFontPeer createFontPeer(String name, Map attrs) {
        BDFFontPeer peer = null;

        for (BDFFontContainer container : getContainers()) {
            // it's a temporary workaround taking first font found
            //FIXME : find the proper way for matching the font name
            //if (container.getFamily().equals(name) || container.getName().equals(name)) {
            peer = new BDFFontPeer(name, attrs);
            break;
            //}
        }
        
        //Unsafe.debug("BDFFontProvider: name=" + name + "fontPeer=" + peer);
        return peer;
    }
    
    @Override
    protected BDFFont loadFont(URL url) throws IOException {
        Reader reader = new InputStreamReader(url.openStream());
        try {
            BDFFontContainer container = BDFFontContainer.createFont(reader);
            return new BDFFont(container);
        } catch (Exception e) {
            IOException ioe = new IOException("can't load BDFFont from " + url);
            ioe.initCause(e);
            throw ioe;
        }
    }
        
    private List<BDFFontContainer> getContainers() {
        if (containers == null) {
            containers = new ArrayList<BDFFontContainer>();

            for (String fontResource : getSystemFonts()) {
                try {
                    final ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    final URL url = cl.getResource(fontResource);
                    if (url != null) {
                        Reader reader = new InputStreamReader(url.openStream()); 
                        containers.add(BDFFontContainer.createFont(reader));
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
        
        return containers;
    }

    protected String[] getSystemFonts() {
        return SYSTEM_FONTS;
    }

    private BDFFont getBDFFont(Font font) {
        final BDFFont bdfFont;
        
        if (font instanceof BDFFont) {
            bdfFont = (BDFFont) font;
        } else {
            bdfFont = getCompatibleFont(font);
        }
        
        if (bdfFont == null) {
            log.warn("Font not instanceof BDFFont: " + font.getClass().getName());
        }
        
        return bdfFont;
    }
}
