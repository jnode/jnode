/*
 * $Id$
 */

package org.jnode.shell.help;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qades
 */
public class OptionArgument extends Argument {

	private final Option[] options;

	public OptionArgument(String name, String description, Option[] options, boolean multi) {
		super(name, description, multi);
		this.options = options;
	}

	public OptionArgument(String name, String description, Option[] options) {
		this(name, description, options, SINGLE);
	}

	public String format() {
		if (options.length == 0)
			return "";
		String result = options[0].getName();
		for (int i = 1; i < options.length; i++)
			result += "|" + options[i].getName();
		return result;
	}

	public void describe(Help help) {
		for (int i = 0; i < options.length; i++)
			options[i].describe(help);
	}

	public String complete(String partial) {
		List opts = new ArrayList();
		for( int i = 0; i < options.length; i++ ) {
			if( options[i].getName().startsWith(partial) )
				opts.add(options[i].getName());
		}

                return complete(partial, opts);
	}


	public static class Option extends Parameter {
		public Option(String name, String description) {
			super(name, description, MANDATORY);
		}
	}

}
