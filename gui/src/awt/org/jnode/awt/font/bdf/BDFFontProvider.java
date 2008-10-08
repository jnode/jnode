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
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jnode.awt.JNodeToolkit;
import org.jnode.awt.font.FontProvider;
import org.jnode.awt.font.JNodeFontPeer;
import org.jnode.awt.font.TextRenderer;
import org.jnode.awt.font.renderer.RenderCache;
import org.jnode.awt.font.spi.AbstractFontProvider;
import org.jnode.font.bdf.BDFFontContainer;
import org.jnode.font.bdf.BDFGlyph;

/**
 * {@link org.jnode.awt.font.FontProvider} for {@link BDFFont}s.
 * 
 * @author Fabien DUMINY (fduminy@jnode.org)
 */
public class BDFFontProvider extends AbstractFontProvider<BDFFont, BDFFontContainer> {
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
        super(BDFFont.class, "bdf");        
    }

    protected TextRenderer createTextRenderer(RenderCache renderCache, Font font) {
        final BDFFont bdfFont = getCompatibleFont(font);
        final TextRenderer renderer = new BDFTextRenderer(bdfFont.getContainer());
        return renderer;
    }


    protected FontMetrics createFontMetrics(Font font) throws IOException {
        final BDFFont bdfFont = getCompatibleFont(font);
        return bdfFont.getFontMetrics();
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
    public BDFFontPeer createFontPeer(String name, Map attrs) {
        BDFFontPeer peer = null;

        List<BDFFontContainer> datas = getUserFontDatas();
        for (BDFFontContainer container : datas) {
            if (match(container, name, attrs)) {
                peer = new BDFFontPeer(this, name, attrs);
                datas.remove(container);
                break;
            }
        }
        
        for (BDFFontContainer container : getContainers()) {
            if (match(container, name, attrs)) {
                peer = new BDFFontPeer(this, name, attrs);
                break;
            }
        }
        
        //Unsafe.debug("BDFFontProvider: name=" + name + "fontPeer=" + peer);
        return peer;
    }

    /**
     * Read an create a Font from the given InputStream
     *
     * @param stream
     * @return
     */
    @Override
    public BDFFont createFont(InputStream stream) throws FontFormatException, IOException {
        try {
            Reader reader = new InputStreamReader(stream);
            BDFFontContainer container = BDFFontContainer.createFont(reader);
            addUserFontData(container);
            return new BDFFont(container);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            FontFormatException ffe = new FontFormatException("bad bdf format");
            ffe.initCause(e);
            throw ffe;
        }
    }
    
    /**
     * Load all default fonts.
     */
    @Override
    protected final void loadFontsImpl() {
        for (BDFFontContainer container : getContainers()) {
            addFont(new BDFFont(container));
        }
    }
        
    private List<BDFFontContainer> getContainers() {
        if (containers == null) {
            containers = new ArrayList<BDFFontContainer>();

            for (String fontResource : SYSTEM_FONTS) {
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

    @Override
    protected final Size getMaxCharSize(BDFFontContainer container) {        
        Size size = new Size();
        
        for (BDFGlyph g : container.getGlyphs()) {
            if (g != null) {
                size.maxCharWidth += g.getDWidth().width;
                size.maxCharHeight = Math.max(g.getDWidth().height, size.maxCharHeight);
            }
        }
        
        return size;
    }

    private boolean match(BDFFontContainer container, String name, Map attrs) {
        // it's a temporary workaround taking first font found
        //FIXME : find the proper way for matching the font name
        //if (container.getFamily().equals(name) || container.getName().equals(name)) {
        return true;
    }
}
