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
 
package org.jnode.driver.net.eepro100;

import org.jnode.driver.net.ethernet.spi.Flags;
import org.jnode.plugin.ConfigurationElement;


/**
 * @author flesire
 */
public class EEPRO100Flags implements Flags {

    private final String name;

    /**
     * Create a new instance
     */
    public EEPRO100Flags(ConfigurationElement config) {
        this(config.getAttribute("name"));
    }

    public EEPRO100Flags(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
    * @see org.jnode.driver.net.ethernet.Flags#getName()
    */
    public String getName() {
        return this.name;
    }

}
