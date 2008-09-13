package org.jnode.apps.telnetd;

import java.io.InputStream;
import java.io.Reader;

import net.wimpi.telnetd.io.TerminalIO;

import org.jnode.driver.console.ConsoleException;
import org.jnode.driver.console.textscreen.KeyboardHandler;
import org.jnode.driver.console.textscreen.KeyboardReader;
import org.jnode.driver.console.textscreen.TextScreenConsole;
import org.jnode.driver.console.textscreen.TextScreenConsoleManager;

/**
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 * 
 */
public class RemoteConsoleManager extends TextScreenConsoleManager {
    private final RemoteTextScreenManager textScreenManager;
    private TerminalIO terminalIO;

    public RemoteConsoleManager() throws ConsoleException {
        super();
        this.textScreenManager = new RemoteTextScreenManager();
    }

    public void setTerminalIO(TerminalIO terminalIO) {
        this.terminalIO = terminalIO;
    }

    @Override
    protected Reader getReader(int options, TextScreenConsole console) {
        // InputStream in = System.in;
        // if ((options & CreateOptions.NO_LINE_EDITTING) == 0) {
        // KeyboardHandler kbHandler = new DefaultKeyboardHandler(null);
        // in = new KeyboardInputStream(kbHandler, console);
        // }
        //        
        // return in;

        KeyboardHandler kbHandler = new RemoteKeyboardHandler(terminalIO);
        return new KeyboardReader(kbHandler, console);
    }

    @Override
    protected RemoteTextScreenManager getTextScreenManager() {
        this.textScreenManager.setTerminalIO(terminalIO);
        return textScreenManager;
    }
}
