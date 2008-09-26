/*
 * $Id$
 */
package org.jnode.shell.command;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.vm.isolate.VmIsolate;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * The IsolateCommand provides information about the current isolates in the system.
 * @author Levente S\u00e1ntha
 */
public class IsolateCommand extends AbstractCommand {
    /**
     * The default implementation of the 'execute(...)' entry point complains that
     * you haven't implemented it.  A command class must override either this method
     * or (ideally) the 'execute()' entry point method.
     */
    @Override
    public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
        out.println("      Id " + " Creator " + "State    " + "Main class");
        VmIsolate root = VmIsolate.getRoot();
        if(root != null)
            out.println(format(String.valueOf(root.getId()), 8, false) + " " +
                format("0", 8, false) + " "  +
                format(String.valueOf(root.getState()), 8, true));

        for (VmIsolate iso : VmIsolate.getVmIsolates()) {
            out.println(format(String.valueOf(iso.getId()), 8, false) + " " +
                format(String.valueOf(iso.getCreator().getId()), 8, false) + " " +
                format(String.valueOf(iso.getState()), 8, true) + " " +
                iso.getMainClassName());
        }
    }

    public String format(String value, int width, boolean left) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < width - value.length(); i++) {
            sb.append(' ');
        }
        if(left)
            sb.insert(0, value);
        else
            sb.append(value);

        return sb.toString();
    }
}
