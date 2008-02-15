package org.jnode.apps.telnetd;

import java.util.HashMap;
import java.util.Map;

import net.wimpi.telnetd.io.TerminalIO;

import org.jnode.driver.textscreen.TextScreenManager;

/**
*
* @author Fabien DUMINY (fduminy at jnode.org)
*
*/
public final class RemoteTextScreenManager implements TextScreenManager {
    private Map<TerminalIO, RemoteTextScreen> systemScreens = new HashMap<TerminalIO, RemoteTextScreen>();
    private TerminalIO terminalIO;

    public void setTerminalIO(TerminalIO terminalIO)
    {
    	this.terminalIO = terminalIO;
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreenManager#getSystemScreen()
     */
    public RemoteTextScreen getSystemScreen() {
    	RemoteTextScreen systemScreen = systemScreens.get(terminalIO);
        if(systemScreen == null){
            systemScreen = new RemoteTextScreen(terminalIO);
            systemScreens.put(terminalIO, systemScreen);
        }
        return systemScreen;
    }
}
