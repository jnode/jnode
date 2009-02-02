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
 
package org.jnode.driver.block.floppy;

import org.jnode.driver.Bus;

/**
 * @author epr
 */
public class FloppyControllerBus extends Bus {

    private final FloppyControllerDriver controller;

    /**
     * Create a new instance
     *
     * @param controller
     */
    public FloppyControllerBus(FloppyControllerDriver controller) {
        super(controller.getDevice());
        this.controller = controller;
    }

    /**
     * @return The controller
     */
    public final FloppyControllerDriver getController() {
        return this.controller;
    }
}
