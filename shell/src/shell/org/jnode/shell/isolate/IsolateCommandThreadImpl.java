package org.jnode.shell.isolate;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.util.Properties;

import javax.isolate.Isolate;
import javax.isolate.IsolateStatus;
import javax.isolate.Link;
import javax.isolate.LinkMessage;
import javax.isolate.StreamBindings;

import org.jnode.shell.CommandRunner;
import org.jnode.shell.CommandThread;
import org.jnode.shell.ShellInvocationException;
import org.jnode.shell.ThreadExitListener;
import org.jnode.shell.io.CommandIO;
import org.jnode.util.ReaderInputStream;
import org.jnode.util.WriterOutputStream;
import org.jnode.vm.isolate.ObjectLinkMessage;

/**
 * This class implements the CommandThread API for commands run in their 
 * own private isolates.
 * 
 * @author crawley@jnode.org
 */
public class IsolateCommandThreadImpl implements CommandThread {
    
    private final Isolate isolate;
    private final CommandRunner cr;
    
    public IsolateCommandThreadImpl(CommandRunner cr) throws IOException {
        this.cr = cr;
        CommandIO[] ios = cr.getIos();
        Properties properties = System.getProperties();
        StreamBindings streamBindings = createStreamBindings(ios);
        isolate = new Isolate(streamBindings, properties, 
                "org.jnode.shell.isolate.IsolateCommandLauncher", new String[0]);
    }

    private StreamBindings createStreamBindings(CommandIO[] ios) throws IOException {
        // FIXME if there are more than 3 CommandIOs, they should be passed
        // to the isolate via a link message.
        if (ios.length > 3) {
            throw new RuntimeException("> 3 CommandIOs not implemented yet");
        }
        StreamBindings streamBindings = new StreamBindings();
        Closeable in = ios[0].findBaseStream();
        if (in instanceof FileInputStream) {
            streamBindings.setIn((FileInputStream) in);
        } else {
            streamBindings.setIn(createSocketForInput(in));
        }
        Closeable out = ios[1].findBaseStream();
        if (out instanceof FileOutputStream) {
            streamBindings.setOut((FileOutputStream) out);
        } else {
            streamBindings.setOut(createSocketForOutput(out));
        }
        Closeable err = ios[2].findBaseStream();
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
            out = new WriterOutputStream((Writer) closeable);        
        } else {
            out = (OutputStream) closeable;
        }
        return new IsolateSocket(out);
    }

    @Override
    public int getReturnCode() {
        // TODO Auto-generated method stub
        return -1;
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
            Link sl = isolate.newStatusLink();
            isolate.start(cl);
            ObjectLinkMessage msg = ObjectLinkMessage.newMessage(this.cr);
            cl.send(msg);
            while (true) {
                LinkMessage statusMsg = sl.receive();
                IsolateStatus status = statusMsg.extractStatus();
                if (status.getState().equals(IsolateStatus.State.EXITED)) {
                    System.err.println("Got the EXITED message");
                    break;
                }
            }
        } catch (Exception ex) {
            throw new ShellInvocationException("Cannot start isolate", ex);
        } 
    }

    @Override
    public void stop(ThreadDeath threadDeath) {
        // FIXME - not sure what status argument should be ... but this is moot
        // at the moment because VmIsolate.halt does nothing with it anyway.
        isolate.halt(0 /* FIXME */);
    }
    
    public void waitFor() {
        // TODO implement me
    }
}
