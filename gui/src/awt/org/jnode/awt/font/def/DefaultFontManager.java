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

package org.jnode.awt.font.def;

import gnu.java.security.action.GetPropertyAction;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.jnode.awt.font.FontManager;
import org.jnode.awt.font.FontProvider;
import org.jnode.awt.font.JNodeFontPeer;
import org.jnode.awt.font.TextRenderer;
import org.jnode.driver.video.Surface;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.ConfigurationElement;
import org.jnode.plugin.Extension;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.ExtensionPointListener;
import org.jnode.plugin.PluginException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DefaultFontManager implements FontManager, ExtensionPointListener {

    private static final Logger log = Logger.getLogger(DefaultFontManager.class);    
    private final ExtensionPoint providersEP;
    
    /**
     * Note : For now, we have only 2 providers (bdf, ttf) and we will probably 
     * never have more than 5 ones. So, a {@link List} is enough for our usage. 
     */
    private final List<FontProvider<?>> providers = new ArrayList<FontProvider<?>>(2);

    public final Map<Integer, String> fontTypeToProviderName = (Map<Integer, String>)
        Collections.singletonMap(Font.TRUETYPE_FONT, "ttf");

    /**
     * Create a new instance
     *
     * @param providersEP
     */
    public DefaultFontManager(ExtensionPoint providersEP) {
        this.providersEP = providersEP;
    }

    /**
     * Start this manager
     *
     * @throws PluginException
     */
    public void start() throws PluginException {
        providersEP.addListener(this);
        try {
            InitialNaming.bind(NAME, this);
        } catch (NamingException ex) {
            throw new PluginException(ex);
        }
        updateFontProviders();
    }

    /**
     * Start this manager
     */
    public void stop() {
        InitialNaming.unbind(NAME);
        providersEP.removeListener(this);
    }

    /**
     * Returns an array containing a one-point size instance of all fonts
     * available in this provider.
     * Typical usage would be to allow a user to select a particular font.
     * Then, the application can size the font and set various font
     * attributes by calling the deriveFont method on the choosen instance.
     * This method provides for the application the most precise control
     * over which Font instance is used to render text.
     * If a font in this provider has multiple programmable variations,
     * only one instance of that Font is returned in the set,
     * and other variations must be derived by the application.
     * If a font in this provider has multiple programmable variations,
     * such as Multiple-Master fonts, only one instance of that font
     * is returned in the Font array.
     * The other variations must be derived by the application.
     *
     * @return All fonts
     */
    public synchronized Font[] getAllFonts() {
        final HashSet<Font> all = new HashSet<Font>();
        for (FontProvider<?> prv : providers) {
            all.addAll(prv.getAllFonts());
        }
        return (Font[]) all.toArray(new Font[all.size()]);
    }

    /**
     * Gets the font metrics for the given font.
     *
     * @param font
     * @return The font metrics for the given font
     */
    public FontMetrics getFontMetrics(Font font) {
        Font txFont = getTranslatedFont(font);
        return getProvider(txFont).getFontMetrics(txFont);
    }

    /**
     * Draw the given text to the given graphics at the given location,
     * using the given font.
     *
     * @param g
     * @param text
     * @param font
     * @param x
     * @param y
     */
    public void drawText(Surface g, Shape clip, AffineTransform tx, CharSequence text, Font font, int x, int y,
                         Color color) {
        Font txFont = getTranslatedFont(font);
        final TextRenderer renderer = getProvider(txFont).getTextRenderer(txFont);
        renderer.render(g, clip, tx, text, x, y, color);
    }

    /**
     * @param point
     * @param extension
     * @see org.jnode.plugin.ExtensionPointListener#extensionAdded(org.jnode.plugin.ExtensionPoint,
     * org.jnode.plugin.Extension)
     */
    public void extensionAdded(ExtensionPoint point, Extension extension) {
        updateFontProviders();
    }

    /**
     * @param point
     * @param extension
     * @see org.jnode.plugin.ExtensionPointListener#extensionRemoved(org.jnode.plugin.ExtensionPoint,
     * org.jnode.plugin.Extension)
     */
    public void extensionRemoved(ExtensionPoint point, Extension extension) {
        final ConfigurationElement[] elements = extension.getConfigurationElements();
        for (int j = 0; j < elements.length; j++) {
            final String className = elements[j].getAttribute("class");
            
            if (log.isDebugEnabled()) {
                log.debug("Removed provider: class=" + className);
            }
            
            if (className != null) {
                int idx = getProviderIndexByClass(className);
                if (idx >= 0) {
                    providers.remove(idx);
                }
            }
        }
    }

    public Font createFont(int format, InputStream stream) throws FontFormatException, IOException {
        String name = fontTypeToProviderName.get(format);
        if (name == null) throw new IllegalArgumentException("unknown format " + name);

        for (FontProvider<?> prv : getProviders()) {
            if (prv.getName().equals(name)) {
                return prv.createFont(stream);
            }
        }

        throw new FontFormatException("can't create font with format " + name);
    }

    @Override
    public JNodeFontPeer<?, ?> createFontPeer(String name, Map attrs) {
        for (FontProvider<?> prv : getProviders()) {
            JNodeFontPeer<?, ?> peer = prv.createFontPeer(name, attrs);
            if (peer != null) {
                return peer;
            }
        }

        throw new IllegalArgumentException("can't create font peer from name " + name);
    }

    /**
     * Gets the provider for a given font
     *
     * @param font
     * @return The provider
     */
    private FontProvider<?> getProvider(Font font) {
        for (FontProvider<?> prv : getProviders()) {
            if (prv.provides(font)) {
                return prv;
            }
        }
        
        if (log.isDebugEnabled()) {
            log.debug("font=" + font + " NO PROVIDER");
        }
        
        return null;
    }

    /**
     * Get the list of providers by taking care that the preferred provider 
     * (specified by the jnode.font.renderer system property) is always at the 
     * first position.
     * 
     * @return
     */
    private synchronized List<FontProvider<?>> getProviders() {
// TODO fix true type font
//        final String firstProviderName = (String)AccessController.
// doPrivileged(new GetPropertyAction("jnode.font.renderer", "ttf"));
        final String firstProviderName =
            (String) AccessController.doPrivileged(new GetPropertyAction("jnode.font.renderer", "bdf"));
        if ((providers.size() > 1) && !firstProviderName.equals(providers.get(0).getName())) {
            for (int i = 1; i < providers.size(); i++) {
                if (firstProviderName.equals(providers.get(i).getName())) {
                    
                    // exchange the providers so that firstProvider is always at index 0
                    FontProvider<?> firstProvider = providers.get(i);
                    providers.set(i, providers.get(0));
                    providers.set(0, firstProvider);
                    
                    break;
                }
            }
        }
        return providers;
    }

    /**
     * Translates the font into a font that is provided by a provider.
     *
     * @param font
     * @return
     */
    private Font getTranslatedFont(Font font) {
        Font txFont = font;
        
        if (getProvider(font) == null) {
            txFont = getCompatibleFont(font);
        }
        
        return txFont;
    }

    /**
     * Translates the font into a font that is provided by a provider.
     *
     * @param font
     * @return
     */
    private Font getCompatibleFont(Font font) {
        return getProviders().get(0).getCompatibleFont(font);
    }
    
    private synchronized void updateFontProviders() {
        final Extension[] extensions = providersEP.getExtensions();
        
        if (log.isDebugEnabled()) {
            log.debug("Found " + extensions.length + " font providers");
        }

        for (int i = 0; i < extensions.length; i++) {
            final ConfigurationElement[] elements = extensions[i].getConfigurationElements();
            for (int j = 0; j < elements.length; j++) {
                configureProvider(providers, elements[j]);
            }
        }
    }

    private void configureProvider(List<FontProvider<?>> providers, ConfigurationElement element) {
        final String className = element.getAttribute("class");
        
        if (log.isDebugEnabled()) {
            log.debug("Configure provider: class=" + className);
        }
        
        if ((className != null) && (getProviderIndexByClass(className) < 0)) {
            
            try {
                final Class<?> cls = Thread.currentThread().getContextClassLoader().loadClass(className);
                final FontProvider<?> provider = (FontProvider<?>) cls.newInstance();
                providers.add(provider);
            } catch (ClassNotFoundException ex) {
                log.error("Cannot find provider class " + className);
            } catch (IllegalAccessException ex) {
                log.error("Cannot access provider class " + className);
            } catch (InstantiationException ex) {
                log.error("Cannot instantiate provider class " + className);
            } catch (ClassCastException ex) {
                log.error("Provider class " + className + " does not implement the FontProvider interface");
            }
        }
    }
    
    private int getProviderIndexByClass(String className) {
        int idx = -1;
        
        for (int i = 0; i < providers.size(); i++) {
            if (providers.get(i).getClass().getName().equals(className)) {
                idx = i;
                break;
            }
        }
        
        return idx;
    }
}
