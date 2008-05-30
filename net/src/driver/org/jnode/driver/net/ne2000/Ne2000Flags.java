/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.driver.net.ne2000;

import org.jnode.plugin.ConfigurationElement;

/**
 * @author epr
 */
public class Ne2000Flags {

    /**
     * Device name
     */
    private final String name;
    /**
     * Size of internal memory
     */
    private final int memSize;
    private final boolean b16;

    /**
     * Create a new instance
     */
    public Ne2000Flags(ConfigurationElement config) {
        this(config.getAttribute("name"));
    }

    /**
     * Create a new instance
     *
     * @param name Device name
     */
    public Ne2000Flags(String name) {
        this.name = name;
        this.memSize = 16 * 1024;
        this.b16 = true;
    }

    /**
     * Gets the name of the device
     */
    public String getName() {
        return name;
    }

    /**
     * Gets size of internal NIC memory in bytes
     */
    public int getMemSize() {
        return memSize;
    }

    /**
     * Use 16-bit data transfer?
     */
    public boolean is16bit() {
		return b16;
	}

}
