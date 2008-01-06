package org.jnode.apps.telnetd;

import org.jnode.driver.console.Console;
import org.jnode.driver.console.ConsoleException;
import org.jnode.driver.console.spi.AbstractConsoleManager;

/**
 *
 * @author Fabien DUMINY (fduminy at jnode.org)
 *
 */
public class RemoteConsoleManager extends AbstractConsoleManager {
	public RemoteConsoleManager() throws ConsoleException {
		super();
	}

	public Console createConsole(String name, int options) {
		// not used for now
		throw new UnsupportedOperationException("shouldn't be used");
	}
}
