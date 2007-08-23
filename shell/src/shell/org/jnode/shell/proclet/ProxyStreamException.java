package org.jnode.shell.proclet;

import java.io.IOException;


/**
 * This exception indicates an error in a proxy stream mechanism
 * 
 * @author crawley@jnode.org
 */
public class ProxyStreamException extends IOException {

	private static final long serialVersionUID = 1L;

	public ProxyStreamException(String message) {
		super(message);
	}
	
	public ProxyStreamException(String message, Throwable ex) {
		super(message);
		this.initCause(ex);
	}

}
