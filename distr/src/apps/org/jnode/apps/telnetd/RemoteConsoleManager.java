package org.jnode.apps.telnetd;

import net.wimpi.telnetd.io.TerminalIO;

import org.jnode.driver.console.ConsoleException;
import org.jnode.driver.console.textscreen.TextScreenConsoleManager;

/**
 *
 * @author Fabien DUMINY (fduminy at jnode.org)
 *
 */
public class RemoteConsoleManager extends TextScreenConsoleManager {
    private final RemoteTextScreenManager textScreenManager;

	public RemoteConsoleManager() throws ConsoleException {
		super();
		this.textScreenManager = new RemoteTextScreenManager();
	}

    public void setTerminalIO(TerminalIO terminalIO)
    {
    	this.textScreenManager.setTerminalIO(terminalIO);
    }

	@Override
    protected RemoteTextScreenManager getTextScreenManager() {
        return textScreenManager;
    }
}
