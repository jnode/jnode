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

package org.jnode.awt.font.spi;

import java.awt.Font;
import java.awt.FontMetrics;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jnode.awt.font.FontProvider;
import org.jnode.awt.font.TextRenderer;
import org.jnode.awt.font.renderer.RenderCache;
import org.jnode.awt.font.renderer.RenderContext;

/**
 * @author epr
 * @author Fabien DUMINY (fduminy@jnode.org)
 */
public abstract class AbstractFontProvider<F extends Font, FD> implements FontProvider<F> {

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(AbstractFontProvider.class);

    static {
        log.setLevel(Level.DEBUG);
    }

    /**
     * Cache font renderers
     */
    private final HashMap<Font, TextRenderer> renderers = new HashMap<Font, TextRenderer>();
    /**
     * Cache font metrics
     */
    private final HashMap<Font, FontMetrics> metrics = new HashMap<Font, FontMetrics>();
    /**
     * All loaded fonts (name, Font)
     */
    private final HashMap<String, F> fontsByName = new HashMap<String, F>();
    /**
     * Have the system fonts been loaded yet
     */
    private boolean fontsLoaded = false;
    private final RenderContext context = new RenderContext();
    /**
     * The render cache
     */
    private final RenderCache renderCache = new RenderCache(context);

    private final String name;
    
    private final Class<F> fontClass;
    
    private final List<FD> userFontDatas = new Vector<FD>();

    protected AbstractFontProvider(Class<F> fontClass, String name) {
        this.name = name;
        this.fontClass = fontClass;
    }


    /**
     * Give the name of the font (used for setting the first provider to use
     * among all available ones)
     */
    public final String getName() {
        return name;
    }


    /**
     * Does this provides provide the given font?
     *
     * @param font
     * @return True if this provider provides the given font, false otherwise
     */
    public final boolean provides(Font font) {
        if (font == null) return false; // don't provide default (null) fonts

        loadFonts();
        return (getCompatibleFont(font) != null);
    }

    /**
     * Returns a set containing a one-point size instance of all fonts available in this provider.
     * Typical usage would be to allow a user to select a particular font. Then, the application can size
     * the font and set various font attributes by calling the deriveFont method on the choosen instance.
     * This method provides for the application the most precise control over which Font instance is
     * used to render text. If a font in this provider has multiple programmable variations, only one instance of that
     * Font is returned in the set, and other variations must be derived by the application. If a font in this provider
     * has multiple programmable variations, such as Multiple-Master fonts, only one instance of that font is returned
     * in the Font set. The other variations must be derived by the application.
     *
     * @return The set containing all fonts provides by this provider.
     */
    public final Set<F> getAllFonts() {
        loadFonts();
        return new HashSet<F>(fontsByName.values());
    }

    /**
     * Gets a text renderer for the given font.
     *
     * @param font
     * @return The renderer
     */
    @Override
    public final TextRenderer getTextRenderer(Font font) {
        TextRenderer r = renderers.get(font);
        if (r == null) {
            r = createTextRenderer(renderCache, font);
            renderers.put(font, r);
        }
        return r;
    }

    protected abstract TextRenderer createTextRenderer(RenderCache renderCache, Font font);

    /**
     * Gets the font metrics for the given font.
     *
     * @param font
     * @return The metrics
     */
    public final FontMetrics getFontMetrics(Font font) {
        FontMetrics fm = metrics.get(font);
        
        if (fm == null) {
            try {
                fm = createFontMetrics(font);
                metrics.put(font, fm);
            } catch (IOException ex) {
                log.error("Cannot create font metrics for " + font, ex);
            }
        }
        
        return fm;
    }
    
   /*
    * Load all default fonts.
    */
    private final void loadFonts() {
        if (!fontsLoaded) {
            loadFontsImpl();
            fontsLoaded = true;
        }
    }

    protected abstract FontMetrics createFontMetrics(Font font) throws IOException;
    
    /**
     * Load all default fonts.
     */
    protected abstract void loadFontsImpl();

    /**
     * Translates the font into a font that is provided by this provider.
     *
     * @param font
     * @return
     */
    @Override
    public final F getCompatibleFont(Font font) {
        F f = null;
        
        if (fontClass.isInstance(font)) {
            f = fontClass.cast(font);
        }
        
        if (f == null) {
            f = fontsByName.get(font.getFamily());
        }
        
        if (f == null) {
            f = fontsByName.get(font.getName());
        }

        if (f == null) {
            f = fontsByName.get(font.getFontName());
        }

        if ((f == null) && (fontsByName.size() > 0)) {
            f = fontsByName.values().iterator().next();
        }
        
        return f;
    }
    
    protected void addUserFontData(FD data) {
        userFontDatas.add(data);
    }
    
    protected List<FD> getUserFontDatas() {
        return userFontDatas;
    }
    
    protected void addFont(F font) {
        //fontsByName.put(font.getName(), font);
        fontsByName.put(font.getFamily(), font);
        //fontsByName.put(font.getFontName(), font);        
    }
    
//    /**
//     * Load all default fonts.
//     */
//    private final void loadFonts() {
//        for (String font : getSystemFonts()) {
//            loadFont(font);
//        }
//        fontsLoaded = true;
//    }
//
//    private final void loadFont(String resName) {
//        try {
//            final ClassLoader cl = Thread.currentThread().getContextClassLoader();
//            final URL url = cl.getResource(resName);
//            if (url != null) {
//                final F font = loadFont(url);
//                //fontsByName.put(font.getName(), font);
//                fontsByName.put(font.getFamily(), font);
//                //fontsByName.put(font.getFontName(), font);
//            } else {
//                log.error("Cannot find font resource " + resName);
//            }
//        } catch (IOException ex) {
//            log.error("Cannot find font " + resName + ": " + ex.getMessage());
//        } catch (Throwable ex) {
//            log.error("Cannot find font " + resName, ex);
//        }
//    }
}
