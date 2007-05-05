/*
 * $Id$
 */
package org.jnode.driver.net.via_rhine;

import org.jnode.driver.net.ethernet.spi.Flags;
import org.jnode.plugin.ConfigurationElement;

/**
 * @author Levente Sántha
 */
class ViaRhineFlags implements Flags {
    private final String name;

    public ViaRhineFlags(ConfigurationElement config) {
        this(config.getAttribute("name"));
    }
    
	/**
	 * Create a new instance
	 * @param name
	 */
	public ViaRhineFlags(String name) {
		this.name = name;
	}

    public String getName() {
        return name;
    }
}
