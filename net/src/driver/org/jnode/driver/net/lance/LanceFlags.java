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

package org.jnode.driver.net.lance;

import org.jnode.driver.net.ethernet.spi.Flags;
import org.jnode.plugin.ConfigurationElement;

/**
 * @author epr
 */
public class LanceFlags implements Flags {

    private final String name;
    private String chipName;
    private boolean fullDuplex;
    private boolean autoSelectEnabled;
    private boolean mustUnreset;

    public LanceFlags(ConfigurationElement config) {
        this(config.getAttribute("name"));
    }

    /**
     * Create a new instance
     */
    public LanceFlags(String name) {
        this.name = name;
        this.chipName = "Unknown";
        this.fullDuplex = false;
        this.autoSelectEnabled = false;
        this.mustUnreset = false;
    }

    public void setForVersion(int chipVersion) {
        // TODO the flags for the different versions are not completed and the LanceCore doesn't use these
        switch (chipVersion) {
            case 0x2420:
                chipName = "PCnet/PCI 79C970";
                break;
            case 0x2430:
                chipName = "PCnet/PCI 79C970 or PCnet/32 79C965";
                break;
            case 0x2621:
                chipName = "PCnet/PCI II 79C970A";
                fullDuplex = true;
                break;
            case 0x2623:
                chipName = "PCnet/FAST 79C971";
                fullDuplex = true;
                break;
            case 0x2624:
                chipName = "PCnet/FAST+ 79C972";
                fullDuplex = true;
                break;
            case 0x2625:
                chipName = "PCnet/FAST III 79C973";
                fullDuplex = true;
                break;
            case 0x2626:
                chipName = "PCnet/Home 79C978";
                fullDuplex = true;
                break;
            case 0x2627:
                chipName = "PCnet/FAST III 79C970";
                fullDuplex = true;
                break;
            default:
                chipName = "no device !";
        }

    }

    /**
     * Gets the name of the device
     */
    public String getName() {
        return name;
    }

    public String getChipName() {
        return chipName;
    }

    public boolean isFullDuplex() {
        return fullDuplex;
    }

    public boolean isAutoSelectEnabled() {
        return autoSelectEnabled;
    }

    public boolean isMustUnreset() {
        return mustUnreset;
    }

}
