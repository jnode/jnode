/**
 * 
 */
package org.jnode.shell.bjorne;

import java.util.Iterator;

import org.jnode.shell.CommandLine;
import org.jnode.shell.ShellException;

final class BreakBuiltin extends BjorneBuiltin {
    @SuppressWarnings("deprecation")
    public int invoke(CommandLine command, BjorneInterpreter interpreter,
            BjorneContext context) throws ShellException {
        Iterator<String> it = command.iterator();
        if (!it.hasNext()) {
            throw new BjorneControlException(BjorneInterpreter.BRANCH_BREAK, 1);
        } else {
            String arg = it.next();
            try {
                int count = Integer.parseInt(arg);
                if (count > 0) {
                    throw new BjorneControlException(
                            BjorneInterpreter.BRANCH_BREAK, count);
                }
                error("break: " + arg + ": loop count out of range", context);
            } catch (NumberFormatException ex) {
                error("break: " + arg + ": numeric argument required", context);
            }
        }
        return 1;
    }
}