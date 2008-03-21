package org.jnode.shell.help.argument;

import java.util.Collection;
import java.util.Comparator;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.ParsedArguments;

public abstract class ListArgument<T> extends Argument
		implements Comparator<T>
{
	private static final Logger log = Logger.getLogger(ListArgument.class);
	
	static {
		log.setLevel(Level.DEBUG);
	}
	
	public ListArgument(String name, String description, boolean multi) {
		super(name, description, multi);
	}

	public ListArgument(String name, String description) {
		this(name, description, false);
	}

	@Override
	final public void complete(CompletionInfo completion, String partial) {
		for (T choice : getValues()) {
			if (isPartOfArgument(choice, partial)) {
				completion.addCompletion(choice.toString());
			}
		}
	}
    
    final public T getArgValue(ParsedArguments cmdLine) {
		String value = getValue(cmdLine);
		if (value == null) {
			return null;
		}
		return getArgValue(value);
    }

    abstract protected Collection<T> getValues(); 
    abstract public T getArgValue(String value);
	abstract protected boolean isPartOfArgument(T argument, String part);
	abstract protected String toStringArgument(T arg);
	abstract public int compare(T choice1, T choice2);

	@Override
	final protected boolean isValidValue(String value) {
		if ((value == null) || "".equals(value)) {
			return true;
		}
		try {
			return (getArgValue(value) != null);
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

}
