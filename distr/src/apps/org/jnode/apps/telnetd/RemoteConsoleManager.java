/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.apps.telnetd;

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
