/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
