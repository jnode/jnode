package org.jnode.shell.bjorne;

import org.jnode.shell.CommandLine;
import org.jnode.shell.ShellException;

abstract class BjorneBuiltin {

    abstract int invoke(CommandLine command, BjorneInterpreter interpreter,
            BjorneContext context) throws ShellException;

    void error(String msg, BjorneContext context) {
        context.resolvePrintStream(context.getStream(2)).println(msg);
    }

}
