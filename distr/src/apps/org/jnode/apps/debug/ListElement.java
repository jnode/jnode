/* $Id$
 * Created on Jun 2, 2004
 */
package org.jnode.apps.debug;

import charvax.swing.JLabel;

/**
 * @author blind
 */
public class ListElement extends JLabel {
	String name;
	Object value;
	public ListElement(Object value, String name) {
		super(name);
		this.value = value;
		this.name = name;
	}
	
	public String toString() {
		return name;
	}
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return Returns the value.
	 */
	public Object getValue() {
		return value;
	}
}

