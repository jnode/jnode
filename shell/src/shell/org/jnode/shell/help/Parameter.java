/*
 * $Id$
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
		if( hasArgument() )
			return argument.complete(partial);
		return partial.trim() + " ";
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
