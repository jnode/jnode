/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
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
