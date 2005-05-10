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
 
package org.jnode.plugin.model;

import org.jnode.vm.BootableObject;

/**
 * @author epr
 */
final class AttributeModel implements BootableObject {

	private final String name;
	private final String value;
	
	public AttributeModel(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Gets the name of this attribute
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the value of this attribute
	 */
	public String getValue() {
		return value;
	}
}
