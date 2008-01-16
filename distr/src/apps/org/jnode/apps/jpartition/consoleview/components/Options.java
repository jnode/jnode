package org.jnode.apps.jpartition.consoleview.components;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.jnode.apps.jpartition.Context;

public class Options extends Component {
	private static final Logger log = Logger.getLogger(Options.class);
		
	public Options(Context context) {
		super(context);
	}

	public int show(String question, String[] options) throws IOException {
		return show(question, Arrays.asList(options));
	}
	
	public int show(String question, Collection<?> options) throws IOException {
		checkNonNull("question", question);
		checkNonEmpty("options", options);
		
		println(question);
		int i = 1;
		for(Object option : options)
		{
			println("  " + i + " - "+option);
			i++;
		}
		print("Choice : ");
		
		int choice = readInt(-1);
		while((choice < 1) || (choice > options.size())) 
		{
			reportError(log, null, "invalid choice");
			choice = readInt(-1);
		}
		
		return choice;
	}
}
