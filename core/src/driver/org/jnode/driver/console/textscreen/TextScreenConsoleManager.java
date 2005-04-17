/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.driver.console.textscreen;

import java.awt.event.KeyEvent;

import javax.naming.NameNotFoundException;

import org.jnode.driver.console.Console;
import org.jnode.driver.console.ConsoleException;
import org.jnode.driver.console.spi.AbstractConsoleManager;
import org.jnode.driver.textscreen.ScrollableTextScreen;
import org.jnode.driver.textscreen.TextScreen;
import org.jnode.driver.textscreen.TextScreenManager;
import org.jnode.naming.InitialNaming;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class TextScreenConsoleManager extends AbstractConsoleManager {

    private int SCROLLABLE_HEIGHT = 500;
    
    /**
     * Initialize this instance.
     * @throws ConsoleException
     */
    public TextScreenConsoleManager()
            throws ConsoleException {
    }
    
    
    /**
     * @see org.jnode.driver.console.ConsoleManager#createConsole(String, int)
     */
    public Console createConsole(String name, int options) {
        if ((options & CreateOptions.TEXT) != 0) {
            final TextScreenManager tsm;
            try {
                tsm = InitialNaming.lookup(TextScreenManager.NAME);
            } catch (NameNotFoundException ex) {
                throw new IllegalArgumentException("TextScreenManager not found");
            }
            final Console console;
            if (name == null) {
                name = autoName();
            }
            if ((options & CreateOptions.SCROLLABLE) != 0) {
                final ScrollableTextScreen screen;
                screen = tsm.getSystemScreen().createCompatibleScrollableBufferScreen(SCROLLABLE_HEIGHT);
                console = new ScrollableTextScreenConsole(this, name, screen, options);
            } else {
                final TextScreen screen;
                screen = tsm.getSystemScreen().createCompatibleBufferScreen();
                console = new TextScreenConsole(this, name, screen, options);
            }            
            setAccelerator(console);
            registerConsole(console);
            return console;
        } else {
            throw new IllegalArgumentException("Unknown option " + options);
        }
    }
    
    /**
     * Create an automatic console name.
     * @return
     */
    private String autoName() {
        int i = 0;
        while (true) {
            final String name = "Console" + i;
            if (getConsole(name) == null) {
                return name;
            } else {
                i++;
            }
        }
    }
    
    private void setAccelerator(Console console) {
        for (int i = 0; i < 12; i++) {
            final int keyCode = KeyEvent.VK_F1 + i;
            if (getConsoleByAccelerator(keyCode) == null) {
                console.setAcceleratorKeyCode(keyCode);
                break;
            }
        }
    }
}
