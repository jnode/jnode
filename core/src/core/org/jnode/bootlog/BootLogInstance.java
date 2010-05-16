package org.jnode.bootlog;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.jnode.naming.InitialNaming;


/**
 * Class holding the {@link BootLog} instance used by the system.  
 * 
 * @author Fabien DUMINY
 *
 */
public final class BootLogInstance {
	private BootLogInstance () {		
	}
	
	/**
	 * Get the system's {@link BootLog}.
	 * @return the system's {@link BootLog}.
	 */
	public static BootLog get() {
		try {
			return InitialNaming.lookup(BootLog.class);
		} catch (NameNotFoundException e) {
			throw new Error("unable to find a BootLog instance", e);
		}
	}

	/**
	 * Set the system's {@link BootLog}.
	 * @param bootLog the system's {@link BootLog}.
	 * @throws NamingException 
	 * @throws NameAlreadyBoundException 
	 */
	public static void set(BootLog bootLog) throws NameAlreadyBoundException, NamingException {
		InitialNaming.bind(BootLog.class, bootLog);
	}
}
