package org.jnode.shell.bjorne;

import org.jnode.shell.CommandRunnable;
import org.jnode.shell.ShellException;

public abstract class BjorneSubshellRunner implements CommandRunnable {
    private int rc;
    private final BjorneContext context;
    
    public BjorneSubshellRunner(BjorneContext context) {
        super();
        this.context = context;
    }

    @Override
    public void flushStreams() {
        context.flushIOs();
    }

    @Override
    public int getRC() {
        return rc;
    }

    public final void run() {
        try {
            rc = doRun();
        } catch (ShellException ex) {
            // FIXME ... this isn't right ...
            rc = 1;
        }
    }

    protected abstract int doRun() throws ShellException;
}
