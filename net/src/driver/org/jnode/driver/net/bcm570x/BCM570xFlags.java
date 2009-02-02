/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.driver.net.bcm570x;

import org.jnode.driver.net.ethernet.spi.Flags;
import org.jnode.plugin.ConfigurationElement;

/**
 * @author Martin Husted Hartvig
 */
public class BCM570xFlags implements Flags {

    private final String name;

    /**
     * Create a new instance of the flags
     */
    public BCM570xFlags(ConfigurationElement config) {
        final String name = config.getAttribute("name");
        if (name != null) {
            this.name = name;
        } else {
            this.name = "Unknown BCM570x";
        }
    }

    /**
     * Create a new instance of the flags
     * 
     * @param name
     */
    public BCM570xFlags(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the device
     */
    public String getName() {
        return name;
    }
}

