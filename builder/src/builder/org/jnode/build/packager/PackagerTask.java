package org.jnode.build.packager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Abstract class used for common stuff among packager tasks
 * 
 * @author fabien
 *
 */
public class PackagerTask extends Task {    
    protected static final String PROPERTIES_FILE = "plugins.properties";
    
    /**
     * The default properties for the packager tool
     */
    private static final Properties DEFAULT_PROPERTIES;
    
    static {
        Properties props;
        try {
            props = readProperties(PackagerTask.class.getResourceAsStream(PROPERTIES_FILE), null);
        } catch (Throwable t) {
            t.printStackTrace();
            props = new Properties();
        }
        DEFAULT_PROPERTIES = props;
    }
    
    // properties names
    protected static final String USER_PLUGIN_IDS = "user.plugin.ids";
    protected static final String PLUGIN_LIST_NAME = "plugin.list.name";
    protected static final String FORCE_OVERWRITE_SCRIPTS = "force.overwrite.scripts";
    
    /**
     * Directory for user plugins/resources
     */
    protected File userApplicationsDir = null;
    
    /**
     * actual properties for the packager
     */
    private Properties properties = null;

    /**
     * Define the directory where user put its own plugins/resources to add in jnode cdrom
     * @param file
     */
    public final void setUserApplicationsDir(File file) {
        if ((file != null) && file.exists() && file.isDirectory()) {
            userApplicationsDir = file;
        } else {
            userApplicationsDir = null;
        }
    }      
    
    /**
     * Is that task enabled ?
     * @return
     */
    protected final boolean isEnabled() {
        return (userApplicationsDir != null);
    }
    
    /**
     * Get properties file used to configure the packager tool
     * @return
     */
    protected final File getPropertiesFile() {
        return isEnabled() ? new File(userApplicationsDir, PROPERTIES_FILE) : null;
    }
    
    /**
     * Get the properties and if necessary read it from the file
     * @return
     */
    protected final synchronized Properties getProperties() {
        if (properties == null) {
            properties = readProperties();
        }
        
        return properties;
    }

    /**
     * Read the properties file used to configure the packager tool
     * @return
     */
    private final Properties readProperties() {
        try {
            final Properties properties;
            
            final File file = getPropertiesFile();
            if (file.exists()) {
                properties = readProperties(new FileInputStream(file), DEFAULT_PROPERTIES);
            } else {
                properties = new Properties(DEFAULT_PROPERTIES);
            }
            
            return properties; 
        } catch (FileNotFoundException e) {
            throw new BuildException("failed to read properties file", e);
        }
    }
    
    /**
     * Read the properties from the given {@link InputStream}
     * 
     * @param input
     * @param defaultProps
     * @return
     */
    private static final Properties readProperties(InputStream input, Properties defaultProps) {
        Properties properties = (defaultProps == null) ? new Properties() : new Properties(defaultProps);
        
        try {
            properties.load(input);
        } catch (IOException ioe) {
            throw new BuildException("failed to read properties file", ioe);
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {
                throw new BuildException("failed to close input stream", ioe);
            }
        }
        
        return properties;
    }    
}
