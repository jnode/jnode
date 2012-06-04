/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.shell.isolate;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.jnode.shell.AsyncCommandInvoker;
import org.jnode.shell.CommandInvoker;
import org.jnode.shell.CommandLine;
import org.jnode.shell.CommandRunner;
import org.jnode.shell.CommandShell;
import org.jnode.shell.CommandThread;
import org.jnode.shell.ShellException;
import org.jnode.shell.ShellInvocationException;
import org.jnode.shell.SimpleCommandInvoker;

/**
 * This command invoker runs commands in their own isolates.
 * 
 * @author crawley@jnode.org
 */
public class IsolateCommandInvoker extends AsyncCommandInvoker implements CommandInvoker {

    public static final Factory FACTORY = new Factory() {
        public SimpleCommandInvoker create(CommandShell shell) {
            return new IsolateCommandInvoker(shell);
        }

        public String getName() {
            return "isolate";
        }
    };

    public IsolateCommandInvoker(CommandShell commandShell) {
        super(commandShell);
    }

    public String getName() {
        return "isolate";
    }
    
    public int invoke(CommandLine commandLine, Properties sysProps, Map<String, String> env)
        throws ShellException {
        CommandRunner cr = setup(commandLine, sysProps, env);
        return runIt(commandLine, cr);
    }

    public CommandThread invokeAsynchronous(CommandLine commandLine, Properties sysProps,
            Map<String, String> env) throws ShellException {
        CommandRunner cr = setup(commandLine, sysProps, env);
        return forkIt(commandLine, cr);
    }

    @Override
    protected CommandThread createThread(CommandRunner cr) throws ShellInvocationException {
        try {
            return new IsolateCommandThreadImpl(cr);
        } catch (IOException ex) {
            throw new ShellInvocationException(ex);
        }
    }
}
