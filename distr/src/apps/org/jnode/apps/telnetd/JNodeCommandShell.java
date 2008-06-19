package org.jnode.apps.telnetd;

import org.jnode.driver.console.TextConsole;
import org.jnode.shell.CommandShell;
import org.jnode.shell.ShellException;

/**
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 * 
 */
public class JNodeCommandShell extends CommandShell {
    private final JNodeShell shell;

    // public JNodeCommandShell(JNodeShell shell, TextConsole console,
    // InputStream in, PrintStream out, PrintStream err)
    // throws ShellException {
    // super(console, in, out, err);
    // this.shell = shell;
    // }

    public JNodeCommandShell(TextConsole cons, JNodeShell shell) throws ShellException {
        super(cons);
        this.shell = shell;
        System.err.println("JNodeCommandShell");
    }

    @Override
    public void exit() {
        shell.close();
        super.exit();
    }
}
