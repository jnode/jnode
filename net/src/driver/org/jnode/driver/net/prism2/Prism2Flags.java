/*
 * $Id$
 *
 * JNode.org
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
