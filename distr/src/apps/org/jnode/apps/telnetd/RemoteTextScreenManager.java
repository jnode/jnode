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

import java.util.HashMap;
import java.util.Map;

import net.wimpi.telnetd.io.TerminalIO;

import org.jnode.driver.textscreen.TextScreenManager;

/**
 * Implementation of the {@link TextScreenManager} interface for the telnet daemon.
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 * 
 */
public final class RemoteTextScreenManager implements TextScreenManager {
    private Map<TerminalIO, RemoteTextScreen> systemScreens =
            new HashMap<TerminalIO, RemoteTextScreen>();
    private TerminalIO terminalIO;

    /**
     * Define the actual terminal used to sent textscreen data. 
     * @param terminalIO
     */
    public void setTerminalIO(TerminalIO terminalIO) {
        this.terminalIO = terminalIO;
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreenManager#getSystemScreen()
     */
    public RemoteTextScreen getSystemScreen() {
        RemoteTextScreen systemScreen = systemScreens.get(terminalIO);
        if (systemScreen == null) {
            systemScreen = new RemoteTextScreen(terminalIO);
            systemScreens.put(terminalIO, systemScreen);
        }
        return systemScreen;
    }
}
