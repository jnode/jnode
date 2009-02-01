/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.shell.command.debug;

import gnu.classpath.jdwp.JNodeSocketTransport;
import gnu.classpath.jdwp.Jdwp;

import java.io.PrintWriter;
import java.io.Reader;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.IntegerArgument;

/**
 * Starts up a JDWP remote debugger listener for this JNode instance.
 *
 * @author Levente S\u00e1ntha
 */
public class DebugCommand extends AbstractCommand {
    private static final int DEFAULT_PORT = 6789;
    private boolean up = true;
    private final IntegerArgument ARG_PORT = 
        new IntegerArgument("port", Argument.OPTIONAL, "the port to listen to");

    public DebugCommand() {
        super("Listen for connections from a remote debugger");
        registerArguments(ARG_PORT);
    }

    public static void main(String[] args) throws Exception {
        new DebugCommand().execute(args);
    }

    @Override
    public void execute() throws Exception {
        int port = ARG_PORT.isSet() ? ARG_PORT.getValue() : DEFAULT_PORT;

        // FIXME - in the even of internal exceptions, JDWP writes to System.out.
        final String ps = "transport=dt_socket,suspend=n,address=" + port + ",server=y";
        Thread t = new Thread(new Runnable() {
            public void run() {
                while (up()) {
                    Jdwp jdwp = new Jdwp();
                    jdwp.configure(ps);
                    jdwp.run();
                    jdwp.waitToFinish();
                    jdwp.shutdown();
                }
                // workaround for the restricted capabilities of JDWP support in GNU Classpath.
                JNodeSocketTransport.ServerSocketHolder.close();
            }
        });
        t.start();

        Reader in = getInput().getReader();
        PrintWriter out = getOutput().getPrintWriter();
        while (in.read() != 'q') {
            out.println("Type 'q' to exit");
        }
        // FIXME - this just stops the 'debug' command.  The listener will keep running
        // until the remote debugger disconnects.  We should have a way to disconnect at
        // this end.
        down();
    }

    public synchronized boolean up() {
        return up;
    }

    public synchronized void down() {
        up = false;
    }
}
