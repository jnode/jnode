/*
 * $Id: Command.java 3772 2008-02-10 15:02:53Z lsantha $
 *
 * JNode.org
 * Copyright (C) 2007-2008 JNode.org
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
package org.jnode.shell.bjorne;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.jnode.shell.CommandLine;
import org.jnode.shell.ShellException;

final class SourceBuiltin extends BjorneBuiltin {
    @SuppressWarnings("deprecation")
    public int invoke(CommandLine command, BjorneInterpreter interpreter,
            BjorneContext context) throws ShellException {
        Iterator<String> it = command.iterator();
        if (!it.hasNext()) {
            error("source: filename argument required", context);
            return 1;
        }
        String fileName = it.next();
        File file = new File(fileName);
        long size = file.length();
        String commandStr;
        FileReader fin = null;
        try {
            fin = new FileReader(file);
            if (size > 1000000) {
                // Since we are going to read the whole script into memory, we
                // need to set some limit on the script's file size ...
                error("source: " + fileName + ": file too big", context);
                return 1;
            }
            char[] buffer = new char[(int) size];
            int nosRead = fin.read(buffer);
            commandStr = new String(buffer, 0, nosRead);
        } catch (IOException ex) {
            error("source: " + fileName + ": " + ex.getMessage(), context);
            return 1;
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException ex) {
                    /* blah */
                }
            }
        }
        // TODO ... implement args.
        return interpreter.interpret(interpreter.getShell(), commandStr,
                null, true);
    }
}
