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
 
package org.jnode.apps.console;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.naming.NameNotFoundException;
import javax.swing.JFrame;

import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.TextConsole;
import org.jnode.driver.console.swing.SwingTextScreenConsoleManager;
import org.jnode.driver.console.textscreen.TextScreenConsoleManager;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.CommandShell;
import org.jnode.shell.ShellManager;

/**
 * Swing based text console.
 * @author Levente S\u00e1ntha
 */
public class SwingConsole {
    private static JFrame frame;

    /**
     * Startup method.
     */
    public static void main(String[] argv) throws Exception {
        synchronized (SwingConsole.class) {
            if (frame != null) {
                System.out.println("SwingConsole is running. Only one SwingConsole can run at this time.");
                return;
            }
        }
        SwingTextScreenConsoleManager cm = new SwingTextScreenConsoleManager();
        
        TextScreenConsoleManager manager = getParentManager();
        if (manager != null) {
            cm.setParent(manager);
        }
        
        TextConsole console = cm.createConsole(
            null,
            (ConsoleManager.CreateOptions.TEXT |
                ConsoleManager.CreateOptions.SCROLLABLE));
        new Thread(new CommandShell(console), "SwingConsoleCommandShell").start();

        synchronized (SwingConsole.class) {
            frame = cm.getFrame();
        }

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                synchronized (SwingConsole.class) {
                    frame = null;
                }
            }
        });
    }
    
    private static TextScreenConsoleManager getParentManager() throws NameNotFoundException {
        TextScreenConsoleManager manager = null;
        
        ShellManager sm = InitialNaming.lookup(ShellManager.NAME);
        if (sm != null) {
            // current shell is null when JNode boot directly in GUI mode, without going 
            // through command line
            if (sm.getCurrentShell() != null) {
                manager = (TextScreenConsoleManager) sm.getCurrentShell().getConsole().getManager();
            }
        }
        
        return manager;
    }
}
