package org.jnode.apps.jpartition.consoleview.components;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.jnode.apps.jpartition.Context;

public class NumberField extends Component {
	private static final Logger log = Logger.getLogger(NumberField.class);
		
	public NumberField(Context context) {
		super(context);
	}

	public long show(String question) throws IOException {
		return show(question, Long.MIN_VALUE, Long.MAX_VALUE);
	}
	
	public long show(String question, long min, long max) throws IOException {
		checkNonNull("question", question);
		
		print(question);
		
		long value = readInt(-1);
		while((value < min) || (value > max)) 
		{
			reportError(log, null, "invalid choice");
			value = readInt(-1);
		}
		
		return value;
	}
}
