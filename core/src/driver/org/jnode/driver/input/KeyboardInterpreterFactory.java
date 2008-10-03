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

package org.jnode.driver.input;

import java.util.Locale;
import java.util.ResourceBundle;
import org.apache.log4j.Logger;

/**
 * The KeyboardInterpreterFactory class provides static methods for creating KeyboardInterpreter 
 * objects and forming keyboard layout identifiers.
 * <p>
 * This is an interim API.  We intend to replace it with a JNode service that includes
 * methods for managing the mapping of keyboard layout IDs to keyboard layout classes.
 *
 * @author Marc DENTY
 * @author crawley@jnode.org
 */
public class KeyboardInterpreterFactory {

    private static final Logger log = Logger
        .getLogger(KeyboardInterpreterFactory.class);

    /**
     * Load the default keyboard layout as specified in the 'org.jnode.driver.input.KeyboardLayout'
     * resource bundle.  If none is specified or the specified layout cannot be used, we use the
     * 'US_en' layout as a fallback.
     *
     * @return a valid KeyboardInterpreter
     */
    public static KeyboardInterpreter getDefaultKeyboardInterpreter() {
        String country = null;
        String region = null;
        String variant = null;
        try {
            ResourceBundle rb = ResourceBundle.getBundle("org.jnode.driver.input.KeyboardLayout", 
                    Locale.getDefault(), Thread.currentThread().getContextClassLoader());
            country = getProperty(rb, "defaultCountry");
            // FIXME the following property name should be 'defaultLanguage'
            region = getProperty(rb, "defaultRegion");
            variant = getProperty(rb, "defaultVariant");
        } catch (Exception ex) {
            log.error("Cannot find the 'org.jnode.driver.input.KeyboardLayout' resource bundle", ex);
        }
        if (country != null) {
            try {
                KeyboardInterpreter ki = createKeyboardInterpreter(country, region, variant);
                if (ki != null) {
                    return ki;
                }
            } catch (Exception ex) {
                log.error("Cannot load the default '" + 
                        makeKeyboardInterpreterID(country, region, variant) +
                        "' keyboard interpreter", ex);
            }
        }
        // Use the US_en keyboard layout as a fallback if there was no resource bundle, no
        // usable default keyboard layout or the specified default layout had no interpreter.
        log.error("Trying the 'US_en' keyboard interpreter as a fallback");
        try {
            return createKeyboardInterpreter("US", "en", null);
        } catch (Throwable ex) {
            log.error("Cannot load 'US_en' keyboard interpreter", ex);
        }
        // FIXME we should probably throw an exception ...
        return null;
    }
    
    /**
     * Get a String-valued property value from the resource bundle, dealing
     * with empty and missing values.
     * 
     * @param rb the resource bundle
     * @param key the property name 
     * @return the property value or <code>null</null> if the value is missing or empty.
     */
    private static String getProperty(ResourceBundle rb, String key) {
        try {
            String res = rb.getString(key);
            res = res.trim();
            return (res.length() == 0) ? null : res;
        } catch (RuntimeException e) { /* ClassCastException or MissingResourceException */
            return null;
        }
    }

    /**
     * Create a new keyboard interpreter object.  Note that keyboard interpreter
     * objects are stateful and therefore cannot be shared by multiple keyboards.
     *
     * @param country the country code; e.g. US, GB, FR, DE, etc.
     * @param language the language code; e.g. en, fr, de etc or <code>null</code>
     * @param variant a keyboard variant name or <code>null</code>.
     * @return a KeyboardInterpreter or <code>null</code>
     */
    public static KeyboardInterpreter createKeyboardInterpreter(
            String country, String language, String variant) {
        final String id = makeKeyboardInterpreterID(country, language, variant);
        log.debug("Looking for interpreter for keyboard layout '" + id + "'");
        final String classI10N = "org.jnode.driver.input.l10n.KeyboardInterpreter_" + id;

        try {
            final ClassLoader cl = Thread.currentThread().getContextClassLoader();
            return (KeyboardInterpreter) cl.loadClass(classI10N).newInstance();
        } catch (ClassNotFoundException ex) {
            log.error("No keyboard interpreter class found for layout id " + id +
                    ": expected class name '" + classI10N + "'.");
        } catch (Exception ex) {
            log.error("Error loading or instantiating keyboard interpreter class '" + 
                    classI10N + "'.", ex);
        }
        return null;
    }
    
    /**
     * Convert a country / language / variant keyboard triple into a keyboard
     * layout identifier.
     * 
     * @param country the country code; e.g. US, GB, FR, DE, etc.
     * @param language the language code; e.g. en, fr, de etc or <code>null</code>.
     * @param variant a keyboard variant name or <code>null</code>.
     * @return the keyboard layout identifier.
     */
    public static String makeKeyboardInterpreterID(
            String country, String language, String variant) {
        final String id;
        country = country.toUpperCase();
        if (language != null) {
            language = language.toLowerCase();
            if (variant == null) {
                id = country + "_" + language;
            } else {
                id = country + "_" + language + "_" + variant;
            }
        } else if (variant != null) {
            id = country + "_" + variant;
        } else {
            id = country;
        }
        return id;
    }
}
