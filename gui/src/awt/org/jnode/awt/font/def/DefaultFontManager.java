/*
 * $Id$
 */
package org.jnode.awt.font.def;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.jnode.awt.font.FontManager;
import org.jnode.awt.font.FontProvider;
import org.jnode.awt.font.TextRenderer;
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

    private final Logger log = Logger.getLogger(getClass());
    private final ExtensionPoint providersEP;
    private final HashMap providers = new HashMap();

    /**
     * Create a new instance
     * @param providersEP
     */
    public DefaultFontManager(ExtensionPoint providersEP) {
        this.providersEP = providersEP;
    }

    /**
     * Start this manager
     * @throws PluginException
     */
    protected void start() throws PluginException {
        providersEP.addListener(this);
        updateFontProviders();
        try {
            InitialNaming.bind(NAME, this);
        } catch (NamingException ex) {
            throw new PluginException(ex);
        }
    }

    /**
     * Start this manager
     */
    protected void stop() {
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
        final HashSet all = new HashSet();
        for (Iterator i = providers.values().iterator(); i.hasNext();) {
            final FontProvider prv = (FontProvider) i.next();
            all.addAll(prv.getAllFonts());
        }
        return (Font[]) all.toArray(new Font[all.size()]);
    }

    /**
     * Gets the font metrics for the given font.
     * @param font
     * @return The font metrics for the given font
     */
    public FontMetrics getFontMetrics(Font font) {
        final FontProvider prv = getProvider(font);
        if (prv != null) {
            return prv.getFontMetrics(font);
        } else {
            return new EmptyFontMetrics(font);
        }
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
    public void drawText(Graphics2D g, String text, Font font, int x, int y) {
        final FontProvider prv = getProvider(font);
        if (prv == null) {
        	log.error("No FontProvider for font=" + font);
        } else {
        	final TextRenderer renderer = prv.getTextRenderer(font);
        	renderer.render(g, text, x, y);
        }
    }

    /**
     * @param point
     * @param extension
     * @see org.jnode.plugin.ExtensionPointListener#extensionAdded(org.jnode.plugin.ExtensionPoint, org.jnode.plugin.Extension)
     */
    public void extensionAdded(ExtensionPoint point, Extension extension) {
        updateFontProviders();
    }

    /**
     * @param point
     * @param extension
     * @see org.jnode.plugin.ExtensionPointListener#extensionRemoved(org.jnode.plugin.ExtensionPoint, org.jnode.plugin.Extension)
     */
    public void extensionRemoved(ExtensionPoint point, Extension extension) {
        final ConfigurationElement[] elements = extension.getConfigurationElements();
        for (int j = 0; j < elements.length; j++) {
            final String className = elements[j].getAttribute("class");
            log.debug("Removed provider: class=" + className);
            if (className != null) {
                providers.remove(className);
            }
        }
    }

    /**
     * Gets the provider for a given font
     * @param font
     * @return The provider
     */
    private FontProvider getProvider(Font font) {
        for (Iterator i = providers.values().iterator(); i.hasNext();) {
            final FontProvider prv = (FontProvider) i.next();
            if (prv.provides(font)) {
                return prv;
            }
        }
        return null;
    }

    private synchronized void updateFontProviders() {
        final Extension[] extensions = providersEP.getExtensions();
        log.debug("Found " + extensions.length + " font providers");

        for (int i = 0; i < extensions.length; i++) {
            final Extension ext = extensions[i];
            final ConfigurationElement[] elements = ext.getConfigurationElements();
            for (int j = 0; j < elements.length; j++) {
                configureProvider(providers, elements[j]);
            }
        }
    }

    private void configureProvider(Map providers, ConfigurationElement element) {
        final String className = element.getAttribute("class");
        log.debug("Configure provider: class=" + className);
        if ((className != null) && !providers.containsKey(className)) {
            try {
                final Class cls = Thread.currentThread().getContextClassLoader().loadClass(className);
                final FontProvider provider = (FontProvider) cls.newInstance();
                providers.put(className, provider);
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

    private static class EmptyFontMetrics extends FontMetrics {
        /**
         * @param font
         */
        public EmptyFontMetrics(Font font) {
            super(font);
        }
    }
}
