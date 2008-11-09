package org.jnode.net.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.HostNameArgument;
import org.jnode.shell.syntax.PortNumberArgument;

/**
 * This command establishes a TCP connection to a remote machine, either by
 * connecting to it or accepting a remote connection.  Once the connection has
 * been set up, it sends the command's standard input to the remote connection and
 * sends the output from the connection to the command's standard output.
 * 
 * @author quades
 * @author crawley@jnode.org
 */
public class TcpInoutCommand extends AbstractCommand {
    // FIXME this command is only useful for testing. What we Really need is
    // implementations of TELNET, RSH and SSH protocols (client and
    // server-side).

    private final HostNameArgument ARG_HOST = new HostNameArgument(
            "host", Argument.OPTIONAL, "the hostname of the server to contact");

    private final PortNumberArgument ARG_PORT = new PortNumberArgument(
            "port", Argument.OPTIONAL, "the port the server is listening to");

    private final PortNumberArgument ARG_LOCAL_PORT = new PortNumberArgument(
            "localPort", Argument.OPTIONAL, "the local port we should listen to");

    private Socket socket;
    private CopyThread toThread;
    private CopyThread fromThread;

    public TcpInoutCommand() {
        super("Set up an interactive TCP connection to a remote machine");
        registerArguments(ARG_HOST, ARG_LOCAL_PORT, ARG_PORT);
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        new TcpInoutCommand().execute(args);
    }

    public void execute() throws IOException {
        Socket socket;
        if (ARG_LOCAL_PORT.isSet()) {
            int port = ARG_LOCAL_PORT.getValue();
            ServerSocket ss = ServerSocketFactory.getDefault().createServerSocket(port);
            socket = ss.accept();
        } else {
            InetAddress host = ARG_HOST.getAsInetAddress();
            int port = ARG_PORT.getValue();
            socket = SocketFactory.getDefault().createSocket(host, port);
        }
        InputStream in = getInput().getInputStream();
        OutputStream out = getOutput().getOutputStream();
        PrintWriter err = getError().getPrintWriter();
        toThread = new CopyThread(in, socket.getOutputStream(), err);
        fromThread = new CopyThread(socket.getInputStream(), out, err);

        synchronized (this) {
            toThread.start();
            fromThread.start();
            try {
                wait();
            } catch (InterruptedException e) {
                close(null);
            }
        }
    }

    private synchronized void close(CopyThread source) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                // We don't care ...
            }
            socket = null;
            notifyAll();
        }
        if (source != toThread) {
            toThread.terminate();
        }
        if (source != fromThread) {
            fromThread.terminate();
        }
    }

    private class CopyThread extends Thread {
        private final InputStream in;
        private final OutputStream out;
        private final PrintWriter err;
        private boolean terminated;

        CopyThread(InputStream in, OutputStream out, PrintWriter err) {
            this.in = in;
            this.out = out;
            this.err = err;
        }

        synchronized void terminate() {
            if (!this.terminated) {
                interrupt();
                this.terminated = true;
            }
        }

        public void run() {
            try {
                while (socket != null) {
                    int b = in.read();
                    if (b == -1) {
                        break;
                    }
                    out.write(b);
                }
            } catch (IOException ex) {
                synchronized (this) {
                    if (!terminated) {
                        err.println(ex.getLocalizedMessage());
                    }
                }
            } finally {
                close(this);
            }
        }
    }
}
