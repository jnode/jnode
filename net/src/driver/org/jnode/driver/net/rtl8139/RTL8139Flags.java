/*
 * $Id$
 */

package org.jnode.driver.net.rtl8139;

import org.jnode.driver.net.ethernet.spi.Flags;
import org.jnode.plugin.ConfigurationElement;

/**
 * @author Martin Husted Hartvig
 */

public class RTL8139Flags implements Flags {

    private final String name;

    /**
     * Create a new instance of the flags
     */
    public RTL8139Flags(ConfigurationElement config) {
        final String name = config.getAttribute("name");
        if (name != null) {
            this.name = name;
        } else {
            this.name = "Unknown RTL8139";
        }
    }

    /**
     * Create a new instance of the flags
     * 
     * @param name
     */

    public RTL8139Flags(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the device
     */

    public String getName() {
        return name;
    }
}

