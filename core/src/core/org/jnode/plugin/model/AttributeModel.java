/*
 * $Id$
 */
package org.jnode.plugin.model;

import org.jnode.vm.BootableObject;

/**
 * @author epr
 */
public class AttributeModel implements BootableObject {

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
