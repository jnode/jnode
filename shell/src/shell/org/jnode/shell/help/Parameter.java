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
 
package org.jnode.shell.help;

/**
 * @author qades
 */
public class Parameter extends CommandLineElement {

	public static final String ANONYMOUS = "";
	public static final String NO_DESCRIPTION = "";
	public static final Argument NO_ARGUMENT = null;
	public static final boolean OPTIONAL = true;
	public static final boolean MANDATORY = false;

	private final Argument argument;
	private final boolean optional;

	public Parameter(String name, String description, Argument argument, boolean optional) {
		super(name, description);
		this.argument = argument;
		this.optional = optional;
	}

	public Parameter(String name, String description, boolean optional) {
		this(name, description, NO_ARGUMENT, optional);
	}

	public Parameter(String name, String description, Argument argument) {
		this(name, description, argument, OPTIONAL);
	}

	public Parameter(Argument argument, boolean optional) {
		this(ANONYMOUS, NO_DESCRIPTION, argument, optional);
	}

	/**
	 * A non-optional parameter
	 * @param argument
	 */
	public Parameter(Argument argument) {
		this(ANONYMOUS, NO_DESCRIPTION, argument, MANDATORY);
	}

	public final boolean isAnonymous() {
		return ANONYMOUS.equals(getName());
	}

	public final String getDescription() {
		if( isAnonymous() )
			return NO_DESCRIPTION;
		return super.getDescription();
	}

	public final boolean hasArgument() {
		return argument != NO_ARGUMENT;
	}

	public final Argument getArgument() {
		return argument;
	}

	public final boolean isOptional() {
		return optional;
	}

	public String format() {
		String result = "";
		if( !isAnonymous() ) {
			result += "-" + getName();
			if( hasArgument() && (argument.format().length() != 0))	// be aware of trailing space
				result += " ";
		}
		if( hasArgument() )
			result += argument.format();
		return (optional
			? "[" + result + "]"
			: result);
	}

	public void describe(Help help) {
		if( !isAnonymous() )
			help.describeParameter(this);
		if( hasArgument() )
			argument.describe(help);
	}

	public final String complete(String partial) {
		// delegate to argument, merely close the parameter if no argument exists
		if (hasArgument()) {
			if (Syntax.DEBUG) Syntax.LOGGER.debug("Parameter.complete: argument is " + 
											      argument.format());
			return argument.complete(partial);
		}
		else {
			// FIXME - this assumes that the partial string can never legitimately
			// have leading/trailing whitespace.
			if (Syntax.DEBUG) Syntax.LOGGER.debug("Parameter.complete: no argument");
		return partial.trim() + " ";
	}
	}

	public final boolean isSatisfied() {
		if( !hasArgument() )
			return true;
		return argument.isSatisfied();
	}

	public final boolean isSet(ParsedArguments args) {
		return args.isSet(this);
	}

}
