/**
 * 
 */
package org.jnode.shell.bjorne;

import java.util.Iterator;

import org.jnode.shell.CommandLine;
import org.jnode.shell.ShellException;

final class ContinueBuiltin extends BjorneBuiltin {
    @SuppressWarnings("deprecation")
    public int invoke(CommandLine command, BjorneInterpreter interpreter,
            BjorneContext context) throws ShellException {
        Iterator<String> it = command.iterator();
        if (!it.hasNext()) {
            throw new BjorneControlException(BjorneInterpreter.BRANCH_CONTINUE,
                    1);
        } else {
            String arg = it.next();
            try {
                int count = Integer.parseInt(arg);
                if (count > 0) {
                    throw new BjorneControlException(
                            BjorneInterpreter.BRANCH_CONTINUE, count);
                }
                error("continue: " + arg + ": loop count out of range", context);
            } catch (NumberFormatException ex) {
                error("continue: " + arg + ": numeric argument required",
                        context);
            }
        }
        return 1;
    }
}