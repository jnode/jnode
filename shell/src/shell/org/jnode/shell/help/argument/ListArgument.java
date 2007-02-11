package org.jnode.shell.help.argument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.ParsedArguments;

public abstract class ListArgument<T> extends Argument
		implements Comparator<T>
{
	private static final Logger log = Logger.getLogger(ListArgument.class);
	
	static
	{
		log.setLevel(Level.DEBUG);
	}
	
	public ListArgument(String name, String description, boolean multi) 
	{
		super(name, description, multi);
	}

	public ListArgument(String name, String description) 
	{
		this(name, description, false);
	}

	@Override
	final public String complete(String partial) {
		final List<T> result = new ArrayList<T>();
		for (T choice : getValues()) {
			if (isPartOfArgument(choice, partial)) {
				result.add(choice);
			}
		}

		Collections.sort(result, this);
		return completeFromList(partial, result);
	}
	
    final protected String completeFromList(String partial, Collection<T> list) {
    	log.debug("list.size()="+list.size());
        if (list.size() == 0) // none found
                return partial;

        if (list.size() == 1) return toStringArgument(list.iterator().next()) + " ";

        // list matching
        list(list);

        // return the common part, i.e. complete as much as possible
        return common(list);
    }	
    
    final protected String common(Collection<T> items) {
        if (items.isEmpty())
        	return "";
        
        StringBuilder result = new StringBuilder(toStringArgument(items.iterator().next()));
        for (T item : items) {
            while (!isPartOfArgument(item, result.toString())) {
                // shorten the result until it matches
                result = result.deleteCharAt(result.length());
            }
        }
        log.debug("\nresult="+result);
        return result.toString();
    }
    
    final public void list(Collection<T> items) {
    	String[] result = new String[items.size()];
    	int i = 0;
    	for(T item : items)
    	{
    		result[i] = item.toString();
    		i++;
    	}
    	list(result);        
    }
    
    final public T getArgValue(ParsedArguments cmdLine)
    {
		String value = getValue(cmdLine);
		if(value == null)
		{
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
		if ((value == null) || "".equals(value))
			return true;

		try {
			return (getArgValue(value) != null);
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

}
