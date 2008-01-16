package org.jnode.apps.jpartition.consoleview.components;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.jnode.apps.jpartition.Context;

public class Component {
	protected final Context context;
	
	protected Component(Context context) {
		this.context = context;
	}

	final protected void print(String s) {
		context.getOut().print(s);
	}
	
	final protected void println(String s) {
		context.getOut().println(s);
	}

	final protected int read() throws IOException {
		return context.getIn().read();
	}

	final protected boolean readBoolean(boolean defaultValue) throws IOException {
		String line = context.getIn().readLine();
		
		boolean value = defaultValue;
		try
		{
			if(defaultValue)
			{
				value = ("no".equals(line)) ? false : true;
			}
			else
			{				
				value = ("yes".equals(line)) ? true : false;
			}
		}
		catch(Exception e)
		{
			value = defaultValue;
		}
		
		return value;
	}

	final protected int readInt(int defaultValue) throws IOException {
		String line = context.getIn().readLine();
		int value = defaultValue;
		try
		{
			value = Integer.valueOf(line);
		}
		catch(NumberFormatException e)
		{
			value = defaultValue;
		}
		return value;
	}
	
	final protected void reportError(Logger log, Object source, Throwable t)
	{
		context.getErrorReporter().reportError(log, source, t);
	}

	final protected void reportError(Logger log, Object source, String message)
	{
		context.getErrorReporter().reportError(log, source, message);
	}	
	
	final protected void checkNonNull(String paramName, Object param)
	{
		if(param == null)
		{
			throw new NullPointerException("parameter "+paramName+" can't be null");
		}
	}

	final protected void checkNonEmpty(String paramName, Collection<?> param)
	{
		checkNonNull(paramName, param);
		
		if(param.isEmpty())
		{
			throw new IllegalArgumentException("parameter "+paramName+" can't be empty");
		}
	}
	
}
