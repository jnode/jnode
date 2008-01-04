package org.jnode.apps.jpartition.model;

import org.apache.log4j.Logger;
import org.jnode.apps.jpartition.ErrorReporter;

public class DummyErrorReporter implements ErrorReporter {

	public void reportError(Logger log, Object source, Throwable t) {
		// TODO Auto-generated method stub
		t.printStackTrace();
		System.err.println(String.valueOf(source) + " : " + t.getMessage());
	}

	public void reportError(Logger log, Object source, String message) {
		// TODO Auto-generated method stub
		System.err.println(String.valueOf(source) + " : " + message);
	}

}
