/*
 * $Id$
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
                rb = ResourceBundle.getBundle(
                        "org.jnode.driver.input.KeyboardLayout", Locale
                                .getDefault(), Thread.currentThread()
                                .getContextClassLoader());
                defaultCountry = rb.getString("defaultCountry");
                if (defaultCountry.trim().length() == 0) {
                    defaultCountry = null;
                }
            } catch (Exception e) {
                log
                        .warn("Cannot load default keyboard layout, loading US layout instead");
                return getKeyboardInterpreter("US", null);
            }
            try {
                defaultRegion = rb.getString("defaultRegion");
                if (defaultRegion.trim().length() == 0) {
                    defaultRegion = null;
                }
            } catch (Exception e) {
            }

            KeyboardInterpreter ki = getKeyboardInterpreter(defaultCountry,
                    defaultRegion);
            if (ki == null) {
                throw new NullPointerException("KeyboardInterpreter for "
                        + defaultCountry + " not found");
            } else {
                return ki;
            }
        } catch (Exception e) {
            try {
                return getKeyboardInterpreter("US", null);
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
     * @param country
     *            a String
     * @param region
     *            a String
     * 
     * @return a KeyboardInterpreter
     * @version 2/8/2004
     */
    public static KeyboardInterpreter getKeyboardInterpreter(String country,
            String region) throws InstantiationException,
            IllegalAccessException {
        
        final String id;
        country = country.toUpperCase();
        if (region != null) {
            region = region.toLowerCase();
            id = country + "_" + region;
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
