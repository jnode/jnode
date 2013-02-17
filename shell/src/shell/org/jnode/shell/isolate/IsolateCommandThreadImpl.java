/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.util.Properties;

import javax.isolate.Isolate;
import javax.isolate.IsolateStatus;
import javax.isolate.Link;
import javax.isolate.LinkMessage;
import javax.isolate.StreamBindings;

import org.jnode.shell.Command;
import org.jnode.shell.CommandRunner;
import org.jnode.shell.CommandThread;
import org.jnode.shell.ShellInvocationException;
import org.jnode.shell.ThreadExitListener;
import org.jnode.shell.io.CommandIO;
import org.jnode.shell.io.ShellConsoleWriter;
import org.jnode.util.ReaderInputStream;
import org.jnode.util.WriterOutputStream;
import org.jnode.vm.isolate.ObjectLinkMessage;

/**
 * This class implements the CommandThread API for running commands in a new isolates.
 * It takes care of assembling the Stream bindings for the new isolate, creating it
 * and passing it the CommandRunner that holds the command class and arguments.
 * 
 * @author crawley@jnode.org
 */
public class IsolateCommandThreadImpl implements CommandThread {
    
    private final Isolate isolate;
    private final CommandRunner cr;
    private Link sl;
    private int rc;
    
    public IsolateCommandThreadImpl(CommandRunner cr) throws IOException {
        this.cr = cr;
        CommandIO[] ios = cr.getIOs();
        Properties properties = System.getProperties();
        StreamBindings streamBindings = createStreamBindings(ios);
        isolate = new Isolate(streamBindings, properties, 
                "org.jnode.shell.isolate.IsolateCommandLauncher", new String[0]);
    }

    private StreamBindings createStreamBindings(CommandIO[] ios) throws IOException {
        // FIXME if there are more than 4 streams, they should be passed
        // to the isolate via a link message.  Note that the 4th one is the command
        // shell's error stream!!
//        if (ios.length > 3) {
//            throw new RuntimeException("> 3 CommandIOs not implemented yet");
//        }
        StreamBindings streamBindings = new StreamBindings();
        Closeable in = ios[Command.STD_IN].findBaseStream();
        if (in instanceof FileInputStream) {
            streamBindings.setIn((FileInputStream) in);
        } else {
            streamBindings.setIn(createSocketForInput(in));
        }
        Closeable out = ios[Command.STD_OUT].findBaseStream();
        if (out instanceof FileOutputStream) {
            streamBindings.setOut((FileOutputStream) out);
        } else {
            streamBindings.setOut(createSocketForOutput(out));
        }
        Closeable err = ios[Command.STD_ERR].findBaseStream();
        if (err instanceof FileOutputStream) {
            streamBindings.setErr((FileOutputStream) err);
        } else {
            streamBindings.setErr(createSocketForOutput(err));
        }
        return streamBindings;
    }

    private Socket createSocketForInput(Closeable closeable) throws IOException {
        InputStream in;
        if (closeable instanceof Reader) {
            in = new ReaderInputStream((Reader) closeable);        
        } else {
            in = (InputStream) closeable;
        }
        return new IsolateSocket(in);
    }

    private Socket createSocketForOutput(Closeable closeable) throws IOException {
        OutputStream out;
        if (closeable instanceof Writer) {
            boolean isConsole = closeable instanceof ShellConsoleWriter;
            out = new WriterOutputStream((Writer) closeable, !isConsole);        
        } else {
            out = (OutputStream) closeable;
        }
        return new IsolateSocket(out);
    }

    @Override
    public int getReturnCode() {
        // FIXME ... maybe we should check that the thread has terminated.
        return rc;
    }
    
    @Override
    public Throwable getTerminatingException() {
        // FIXME ... implement this
        return null;
    }

    @Override
    public boolean isAlive() {
        final IsolateStatus.State state = isolate.getState();
        return IsolateStatus.State.STARTED.equals(state);
    }

    @Override
    public void start(ThreadExitListener listener) throws ShellInvocationException {
        try {
            Link cl = Link.newLink(Isolate.currentIsolate(), isolate);
            sl = isolate.newStatusLink();
            isolate.start(cl);
            ObjectLinkMessage msg = ObjectLinkMessage.newMessage(this.cr);
            cl.send(msg);
        } catch (Exception ex) {
            throw new ShellInvocationException("Error starting isolate", ex);
        } 
    }

    @Override
    public void stop(ThreadDeath threadDeath) {
        isolate.halt(-1);
    }
    
    public void waitFor() throws ShellInvocationException {
        try {
            while (true) {
                LinkMessage statusMsg = sl.receive();
                IsolateStatus status = statusMsg.extractStatus();
                if (status.getState().equals(IsolateStatus.State.EXITED)) {
                    rc = status.getExitCode();
                    break;
                }
            }
        } catch (Exception ex) {
            throw new ShellInvocationException("Error waiting for isolate", ex);
        } 
    }
}
