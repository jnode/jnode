/**
 * 
 */
package org.jnode.shell.bjorne;

import java.util.Iterator;

import org.jnode.shell.CommandLine;
import org.jnode.shell.ShellException;

final class ExitBuiltin extends BjorneBuiltin {
    public int invoke(CommandLine command, BjorneInterpreter interpreter,
            BjorneContext context) throws ShellException {
        Iterator<String> args = command.iterator();
        if (!args.hasNext()) {
            throw new BjorneControlException(BjorneInterpreter.BRANCH_EXIT,
                    context.getLastReturnCode());
        } else {
            String arg = args.next();
            try {
                throw new BjorneControlException(BjorneInterpreter.BRANCH_EXIT,
                        Integer.parseInt(arg));
            } catch (NumberFormatException ex) {
                error("exit: " + arg + ": numeric argument required", context);
            }
        }
        return 1;
    }
}