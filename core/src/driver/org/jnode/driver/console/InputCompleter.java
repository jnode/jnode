package org.jnode.driver.console;


public interface InputCompleter {
	CompletionInfo complete(String partial);
	
	CommandHistory getCommandHistory();
}
