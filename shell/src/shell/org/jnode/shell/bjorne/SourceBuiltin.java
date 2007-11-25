/**
 * 
 */
package org.jnode.shell.bjorne;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.jnode.shell.CommandLine;
import org.jnode.shell.ShellException;

final class SourceBuiltin extends BjorneBuiltin {
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