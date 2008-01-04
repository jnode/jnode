package org.jnode.apps.jpartition;

import org.apache.log4j.Logger;

public interface ErrorReporter {
	void reportError(Logger log, Object source, Throwable t); 
	
	void reportError(Logger log, Object source, String message); 
}
