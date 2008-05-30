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
 * KeyboardInterpreterFactory.java
 *
 * @author Created by Marc DENTY
 * @since 0.15
 */
public class KeyboardInterpreterFactory {

    private static final Logger log = Logger
        .getLogger(KeyboardInterpreterFactory.class);

    /**
     * Method loadDefaultKeyboardInterpreter
     *
     * @return a valid KeyboardInterpreter
     * @version 2/8/2004
     */
    public static KeyboardInterpreter getDefaultKeyboardInterpreter() {
        try {
            ResourceBundle rb = null;
            String defaultCountry = null;
            String defaultRegion = null;

            try {
                rb = ResourceBundle.getBundle("org.jnode.driver.input.KeyboardLayout", Locale.getDefault(),
                    Thread.currentThread().getContextClassLoader());
                defaultCountry = rb.getString("defaultCountry");
                if (defaultCountry.trim().length() == 0) {
                    defaultCountry = null;
                }
            } catch (Exception ex) {
                log.warn("Cannot load default keyboard layout, loading US layout instead", ex);
                return getKeyboardInterpreter("US", null, null);
            }
            try {
                defaultRegion = rb.getString("defaultRegion");
                if (defaultRegion.trim().length() == 0) {
                    defaultRegion = null;
                }
            } catch (Exception e) {
                //todo empty?
            }

            KeyboardInterpreter ki = getKeyboardInterpreter(defaultCountry,
                defaultRegion, null);
            if (ki == null) {
                throw new NullPointerException("KeyboardInterpreter for "
                    + defaultCountry + " not found");
            } else {
                return ki;
            }
        } catch (Exception e) {
            try {
                return getKeyboardInterpreter("US", null, null);
            } catch (Exception ex) {
                log.error("Cannot load US keyboard interpreter", ex);
                //FIXME : this should never happen
                return null;
            } catch (Error ex) {
                log.error("Cannot load US keyboard interpreter", ex);
                //FIXME : this should never happen
                return null;
            }
        }
    }

    /**
     * Method getKeyboardInterpreter this method
     *
     * @param country  a String
     * @param language a String
     * @param variant
     * @return a KeyboardInterpreter
     * @version 2/8/2004
     */
    public static KeyboardInterpreter getKeyboardInterpreter(String country,
                                                             String language, String variant)
        throws InstantiationException,
        IllegalAccessException {

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

        log.debug("Searching for " + id);
        final String classI10N = "org.jnode.driver.input.l10n.KeyboardInterpreter_" + id;

        try {
            final ClassLoader cl = Thread.currentThread().getContextClassLoader();
            return (KeyboardInterpreter) cl.loadClass(classI10N).newInstance();
        } catch (ClassNotFoundException e) {
            log.error("Keyboard interpreter for " + id + " not found.");
        }

        return null;
    }
}
