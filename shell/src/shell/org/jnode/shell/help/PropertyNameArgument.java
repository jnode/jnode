/*
 * $Id$
 */

package org.jnode.shell.help;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author qades
 */
public class PropertyNameArgument extends Argument {

	public PropertyNameArgument(String name, String description, boolean multi) {
		super(name, description, multi);
	}

	public PropertyNameArgument(String name, String description) {
		super(name, description);
	}

	public String complete(String partial) {
		List props = new ArrayList();
		Iterator i = System.getProperties().keySet().iterator();
		while( i.hasNext() ) {
		String prop = (String)i.next();
			if( prop.startsWith(partial) )
				props.add(prop);
		}

		return complete(partial, props);
	}
	
}
