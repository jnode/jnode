/*
 * $Id$
 */
package org.jnode.driver.net.prism2;

import org.jnode.driver.net.ethernet.spi.Flags;
import org.jnode.plugin.ConfigurationElement;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class Prism2Flags implements Flags {
    
    private final String name;

    /**
     * Create a new instance of the flags
     */
    public Prism2Flags(ConfigurationElement config) {
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

    public Prism2Flags(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the device
     */

    public String getName() {
        return name;
    }

}
