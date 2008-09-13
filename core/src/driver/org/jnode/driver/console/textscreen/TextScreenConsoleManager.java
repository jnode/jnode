/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 
package org.jnode.driver.console.textscreen;

import java.io.InputStream;
import java.io.Reader;

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
public class TextScreenConsoleManager extends AbstractConsoleManager {

    private int SCROLLABLE_HEIGHT = 500;
    
    /**
     * Initialize this instance.
     *
     * @throws ConsoleException
     */
    public TextScreenConsoleManager()
        throws ConsoleException {
    }
    
    
    /**
     * @see org.jnode.driver.console.ConsoleManager#createConsole(String, int)
     */
    public TextScreenConsole createConsole(String name, int options) {
        if ((options & CreateOptions.TEXT) != 0) {
            final TextScreenManager tsm = getTextScreenManager();
            final TextScreenConsole console;
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
            console.setIn(getReader(options, console));
            if ((options & CreateOptions.STACKED) != 0) {
                stackConsole(console);
            } else {
                setAccelerator(console);
            }

            registerConsole(console);
            return console;
        } else {
            throw new IllegalArgumentException("Unknown option " + options);
        }
    }
    
    protected Reader getReader(int options, TextScreenConsole console) {
        Reader in = null;
        if ((options & CreateOptions.NO_LINE_EDITTING) == 0) {
            KeyboardHandler kbHandler = new DefaultKeyboardHandler(getKeyboardApi());
            in = new KeyboardReader(kbHandler, console);
        }
        return in;
    }
    
    protected TextScreenManager getTextScreenManager() {
        TextScreenManager tsm;
        try {
            tsm = InitialNaming.lookup(TextScreenManager.NAME);
        } catch (NameNotFoundException ex) {
            throw new IllegalArgumentException("TextScreenManager not found");
        }
        return tsm;
    }

    /**
     * Create an automatic console name.
     *
     * @return the generated name
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


    @Override
    public void textScreenManagerChanged() {
        final TextScreen systemScreen = getTextScreenManager().getSystemScreen();
                
        for (Console c : getConsoles()) {
            TextScreenConsole console = (TextScreenConsole) c;
            
            final TextScreen screen;            
            if ((console.getOptions() & CreateOptions.SCROLLABLE) != 0) {
                screen = systemScreen.createCompatibleScrollableBufferScreen(SCROLLABLE_HEIGHT);
            } else {
                screen = systemScreen.createCompatibleBufferScreen();
            }
            console.systemScreenChanged(screen);
        }
    }

    /*
    protected void setAccelerator(Console console) {
        for (int i = 0; i < 12; i++) {
            final int keyCode = KeyEvent.VK_F1 + i;
            if (getConsoleByAccelerator(keyCode) == null) {
                console.setAcceleratorKeyCode(keyCode);
                break;
            }
        }        
    }
    */
}
