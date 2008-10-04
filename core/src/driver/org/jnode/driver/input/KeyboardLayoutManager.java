package org.jnode.driver.input;

import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

/**
 * The KeyboardManager provides methods for creating KeyboardInterpreter objects, and managing
 * the mapping from various kinds of identifiers to keyboard layout classes.
 *
 * @author Marc DENTY
 * @author crawley@jnode.org
 */
public class KeyboardLayoutManager {
    
    private final Logger log = Logger.getLogger(KeyboardLayoutManager.class);

    /**
     * The name used to bind this manager in the InitialNaming namespace.
     */
    public static Class<KeyboardLayoutManager> NAME = KeyboardLayoutManager.class;
    
    private HashMap<String, KeyboardInterpreter.Factory> map = 
        new HashMap<String, KeyboardInterpreter.Factory>();
    
    /**
     * Load the default keyboard layout as specified in the 'org.jnode.driver.input.KeyboardLayout'
     * resource bundle.  If none is specified or the specified layout cannot be used, we use the
     * 'US_en' layout as a fallback.
     *
     * @return a valid KeyboardInterpreter
     * @throws KeyboardInterpreterException 
     */
    public KeyboardInterpreter createDefaultKeyboardInterpreter() throws KeyboardInterpreterException {
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
            return createKeyboardInterpreter("US_en");
        } catch (Throwable ex) {
            log.error("Cannot load 'US_en' keyboard interpreter", ex);
            throw new KeyboardInterpreterException("Cannot create a keyboard interpreter", ex);
        }
    }
    
    /**
     * Get a String-valued property value from the resource bundle, dealing
     * with empty and missing values.
     * 
     * @param rb the resource bundle
     * @param key the property name 
     * @return the property value or <code>null</null> if the value is missing or empty.
     */
    private String getProperty(ResourceBundle rb, String key) {
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
     * @param a keyboard layout name or identifier.
     * @return a KeyboardInterpreter
     * @throws KeyboardInterpreterException 
     */
    public KeyboardInterpreter createKeyboardInterpreter(String id) 
        throws KeyboardInterpreterException {
        log.debug("Looking for interpreter for keyboard layout '" + id + "'");
        KeyboardInterpreter.Factory factory = null;
        synchronized (this) {
            factory = map.get(id);
        }
        if (factory != null) {
            return factory.create();
        } else {
            // As a fall-back look for a hard-coded keyboard interpreter class in the
            // org.jnode.driver.input.l10n plugin.
            final String classI10N = "org.jnode.driver.input.l10n.KeyboardInterpreter_" + id;
            try {
                return new KIClassWrapper(classI10N).create();
            } catch (MissingKeyboardInterpreterClassException ex) {
                // We tried and failed the fall-back, so report original problem:
                // that 'id' is not a registered keyboard interpreter.
                throw new KeyboardInterpreterException(
                        "No keyboard interpreter registered with id '" + id + "'");
            } 
        }
    }
    
    /**
     * Create a new keyboard interpreter object.  Note that keyboard interpreter
     * objects are stateful and therefore cannot be shared by multiple keyboards.
     *
     * @param country the country code; e.g. US, GB, FR, DE, etc.
     * @param language the language code; e.g. en, fr, de etc or <code>null</code>
     * @param variant a keyboard variant name or <code>null</code>.
     * @return a KeyboardInterpreter
     * @throws KeyboardInterpreterException 
     */
    public KeyboardInterpreter createKeyboardInterpreter(
            String country, String language, String variant) 
        throws KeyboardInterpreterException {
        return createKeyboardInterpreter(
                makeKeyboardInterpreterID(country, language, variant));
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
    public String makeKeyboardInterpreterID(
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
    
    /**
     * Register a keyboard interpreter factory object.
     * 
     * @param name the keyboard layout identifier.
     * @param factory the factory to be registered.
     */
    public synchronized void registerKeyboardLayout(
            String name, KeyboardInterpreter.Factory factory) {
        map.put(name, factory);
    }
    
    /**
     * Register a keyboard interpreter class.  The class is
     * 
     * @param name the keyboard layout identifier.
     * @param factory the name of the class to be registered.
     */
    public void registerKeyboardLayout(String name, String className) {
        registerKeyboardLayout(name, new KIClassWrapper(className));
    }
    
    /**
     * This wrapper class allows us to treat a class name as a keyboard interpreter
     * factory.
     */
    private static class KIClassWrapper implements KeyboardInterpreter.Factory {
        private final String className;
        
        /**
         * Create a wrapper object for a class.  
         * 
         * @param className the FQN of a class that should implement the KeyboardInterpreter
         *      interface and provide a public no-args constructor. 
         */
        public KIClassWrapper(String className) {
            this.className = className;
        }
        
        /**
         * Create an instance corresponding to the wrapped class name.
         * 
         * @throws MissingKeyboardInterpreterClassException if the class was not found
         * @throws KeyboardInterpreterException for some other error; e.g. if there is
         *     not a public no-args constructor, if an exception is thrown by the constructor
         *     or if the resulting object is not a KeyboardInterpreter.
         */
        @Override
        public KeyboardInterpreter create() throws KeyboardInterpreterException {
            try {
                // FIXME ... think about whether using the current thread's class
                // loader might present a security issue.
                final ClassLoader cl = Thread.currentThread().getContextClassLoader();
                return (KeyboardInterpreter) cl.loadClass(className).newInstance();
            } catch (ClassNotFoundException ex) {
                throw new MissingKeyboardInterpreterClassException(
                        "Keyboard interpreter class not found: " + className);
            } catch (Exception ex) {
                // Could be an access, and instantiation or a typecast exception ...
                throw new KeyboardInterpreterException(
                        "Error instantiating keyboard interpreter class:" +
                        className, ex);
            }
        }
    }
}
