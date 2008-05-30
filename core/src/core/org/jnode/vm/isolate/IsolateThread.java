/*
 * $Id$
 */
package org.jnode.vm.isolate;

import java.io.InputStream;
import java.io.PrintStream;
import org.jnode.plugin.PluginManager;

/**
 * Thread type that is used to start new isolated.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class IsolateThread extends Thread {

    private final VmIsolate isolate;

    private final PluginManager piManager;

    private final PrintStream stdout;

    private final PrintStream stderr;

    private final InputStream stdin;

    /**
     * Default constructor.
     *
     * @param isolate
     */
    IsolateThread(ThreadGroup group, VmIsolate isolate,
                  PluginManager piManager, PrintStream stdout, PrintStream stderr,
                  InputStream stdin) {
        super(group, null, isolate.getMainClassName(), isolate
            .getIsolatedStaticsTable());
        this.isolate = isolate;
        this.piManager = piManager;
        this.stdout = stdout;
        this.stderr = stderr;
        this.stdin = stdin;

        //TODO crawley, review this, looks like a serious side effect for creating a thread
        System.setIn(stdin);
        System.setOut(stdout);
        System.setErr(stderr);
    }

    /**
     * @see java.lang.Thread#run()
     */
    @Override
    public final void run() {
        isolate.run(this);
    }

    /**
     * @return the piManager
     */
    final PluginManager getPluginManager() {
        return piManager;
    }

    /**
     * @return the stderr
     */
    final PrintStream getStderr() {
        return stderr;
    }

    /**
     * @return the stdin
     */
    final InputStream getStdin() {
        return stdin;
    }

    /**
     * @return the stdout
     */
    final PrintStream getStdout() {
        return stdout;
    }
}
