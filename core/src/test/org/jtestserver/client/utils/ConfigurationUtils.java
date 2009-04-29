/**
 * 
 */
package org.jtestserver.client.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.LogManager;

/**
 * This is a utility class to manage directory hierarchy of JTestServer.
 * 
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class ConfigurationUtils {
    private static final String HOME_DIRECTORY_PROPERTY = "jtestserver.home";
    private static final String DEFAULT_HOME_DIRECTORY_NAME = "home";
    
    private static final String CONFIG_DIRECTORY_NAME = "config";
    private static final String CONFIG_FILE_NAME = "config.properties";

    private static boolean INIT = false;
    private static File HOME = null;
    
    /**
     * Try to find the home directory defined as a directory that contains a sub-directory named config, 
     * which contains a file named config.properties.<br> 
     * It's searched in the following order : 
     * <ul>
     * <li>Get the system property <code>jtestserver.home</code> and, if it gives a valid directory, use it.</li>
     * <li>If the current directory contains a sub-directory named config, 
     * which contains a file named config.properties, then use it.</li>
     * <li>Ultimately, try to get it from the classpath of this class and walking up to the 
     * package <code>org.jtestserver.home</code></li>
     * </ul>
     * @return the home directory.
     * @throws RuntimeException if the home directory can't be found.
     */
    public static File getHomeDirectory() {        
        return HOME;
    }
    
    /**
     * Get the configuration file.
     * @return
     */
    public static File getConfigurationFile() {
        return new File(getConfigurationDirectory(), CONFIG_FILE_NAME);
    }

    /**
     * Initialize everything (search for home directory, init logging ...).
     * <b>That method must be called before anything else (especially the other 
     * public methods of that class, but not only that).
     * @throw RuntimeException if something is wrong (typically the home directory can't be found).
     */
    public static void init() {
        if (!INIT) {
            // search home directory 
            HOME = searchHomeDirectory();
            
            // init logging
            File loggingConfigFile = new File(getConfigurationDirectory(), "logging.config.properties");
            
            System.setProperty("java.util.logging.config.file", loggingConfigFile.getAbsolutePath());
            try {
                LogManager.getLogManager().readConfiguration();
            } catch (SecurityException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
    
    /**
     * Indicates if the given file is a valid home directory.
     * @param f file to test
     * @return true if the given file is a valid home directory.
     */
    private static boolean isValidHomeDirectory(File f) {
        boolean valid = false;
        if (isValidDirectory(f)) {
            File configDir = new File(f, CONFIG_DIRECTORY_NAME);
            if (isValidDirectory(configDir)) {
                File configFile = new File(configDir, CONFIG_FILE_NAME);
                valid = configFile.exists() && configFile.isFile() && configFile.canRead();
            }
        }
        
        return valid;
    }
    
    /**
     * Indicates if the given file is a valid directory.
     * @param f file to test
     * @return true if the given file is a valid directory.
     */
    private static boolean isValidDirectory(File f) {
        return f.exists() && f.isDirectory() && f.canRead();
    }

    /**
     * Get the configuration directory.
     * @return configuration directory.
     */
    private static File getConfigurationDirectory() {
        return new File(getHomeDirectory(), CONFIG_DIRECTORY_NAME);
    }

    /**
     * Do the actual search of the home directory, as specified in {@link #getHomeDirectory()}.
     * @return the home directory.
     * @throws RuntimeException if the home directory can't be found.
     */
    private static File searchHomeDirectory() {
        File home = null;
        
        // try from the system property
        String value = System.getProperty(HOME_DIRECTORY_PROPERTY);
        if ((value != null) && !value.trim().isEmpty()) {
            home = new File(value);
            if (!isValidHomeDirectory(home)) {
                home = null;
            } else {
                System.out.println("using home directory from system property : " + home.getAbsolutePath());
            }
        }
        
        if (home == null) {
            home = new File(DEFAULT_HOME_DIRECTORY_NAME); 
            if (!isValidHomeDirectory(home)) {
                home = null;
            } else {
                System.out.println("using current directory as home : " + home.getAbsolutePath());
            }
        }
        
        // try from the classpath
        if (home == null) {
            URL myURL = ConfigurationUtils.class.getResource(ConfigurationUtils.class.getSimpleName() + ".class");
            
            File f = new File(myURL.getFile());
            while (!f.getAbsolutePath().endsWith("/jtestserver") && !f.getAbsolutePath().isEmpty()) {
                f = f.getParentFile();
            }
            home = new File(f, "home");
            
            if (!isValidHomeDirectory(home)) {
                home = null;
            } else {
                System.out.println("using home directory from classpath : " + home.getAbsolutePath());
            }
        }
        
        if (home == null) {
            throw new RuntimeException("unable to find a valid home directory");
        }
            
        return home;
    }
}
