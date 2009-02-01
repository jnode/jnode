/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
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
 
package org.jnode.emu;

import java.io.File;
import org.jnode.apps.editor.TextEditor;
import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.swing.SwingTextScreenConsoleManager;
import org.jnode.driver.console.textscreen.TextScreenConsole;

/**
 * @author Levente S\u00e1ntha
 */
public class EditEmu extends Emu {
    
    public EditEmu(File root) throws EmuException {
        super(root);
    }
    
    public static void main(String[] argv) throws Exception {
        if (argv.length == 0 || argv[0].startsWith("-")) {
            System.err.println("Usage: editEmu <file> [<jnode-home>]");
            return;
        }

        new EditEmu(argv.length > 1 ? new File(argv[1]) : null).run(new File(argv[0]));
    }
    
    private void run(File file) throws Exception {
        SwingTextScreenConsoleManager cm = new SwingTextScreenConsoleManager();
        final TextScreenConsole console = cm.createConsole(
            null,
            (ConsoleManager.CreateOptions.TEXT |
                ConsoleManager.CreateOptions.NO_SYSTEM_OUT_ERR |
                ConsoleManager.CreateOptions.NO_LINE_EDITTING));

        TextEditor te = new TextEditor(console);
        te.loadFile(file);
    }
}
