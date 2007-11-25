/**
 * 
 */
package org.jnode.shell.bjorne;

import java.util.Iterator;

import org.jnode.shell.CommandLine;
import org.jnode.shell.ShellException;

final class ReturnBuiltin extends BjorneBuiltin {
    public int invoke(CommandLine command, BjorneInterpreter interpreter,
            BjorneContext context) throws ShellException {
        Iterator<String> it = command.iterator();
        if (!it.hasNext()) {
            throw new BjorneControlException(BjorneInterpreter.BRANCH_RETURN,
                    context.getLastReturnCode());
        } else {
            String arg = it.next();
            try {
                throw new BjorneControlException(
                        BjorneInterpreter.BRANCH_RETURN, Integer.parseInt(arg));
            } catch (NumberFormatException ex) {
                error("return: " + arg + ": numeric argument required", context);
            }
        }
        return 1;
    }
}