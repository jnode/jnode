package org.jnode.test.shell.harness;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.jnode.shell.CommandLine;
import org.jnode.shell.CommandShell;
import org.jnode.shell.ShellException;
import org.jnode.shell.io.CommandIO;
import org.jnode.shell.io.CommandInput;
import org.jnode.shell.io.CommandInputOutput;
import org.jnode.shell.io.CommandOutput;
import org.jnode.shell.io.NullInputStream;
import org.jnode.shell.io.NullOutputStream;

/**
 * This class modify the shell's stream resolution mechanism so that
 * in/out/err resolve to the streams supplied in the constructor. 
 * 
 * @author crawley@jnode.org
 */
public class TestCommandShell extends CommandShell {
    
    private final CommandInput cin;
    private final CommandOutput cout;
    private final CommandOutput cerr;

    public TestCommandShell(InputStream in, PrintStream out, PrintStream err) 
    throws ShellException {
        super();
        this.cin = new CommandInput(in);
        this.cout = new CommandOutput(out);
        this.cerr = new CommandOutput(err);
    }

    @Override
    public PrintWriter getErr() {
        return cerr.getPrintWriter(true);
    }

    @Override
    public PrintWriter getOut() {
        return cout.getPrintWriter(false);
    }

    @Override
    protected CommandIO resolveStream(CommandIO stream) {
        if (stream == CommandLine.DEFAULT_STDIN) {
            return cin;
        } else if (stream == CommandLine.DEFAULT_STDOUT) {
            return cout;
        } else if (stream == CommandLine.DEFAULT_STDERR) {
            return cerr;
        } else if (stream == CommandLine.DEVNULL || stream == null) {
            return new CommandInputOutput(new NullInputStream(), new NullOutputStream());
        } else {
            return stream;
        }
    }
}
