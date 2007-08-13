package org.jnode.driver.console;


public interface InputCompleter {
	CompletionInfo complete(String partial);
	
	/**
     * Gets the completer's current InputHistory object.  If the completer is modal,
     * different histories may be returned at different times.
     */
    public InputHistory getInputHistory();

    
}
