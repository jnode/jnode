package org.jnode.net.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;
import org.jnode.shell.help.argument.InetAddressArgument;
import org.jnode.shell.help.argument.IntegerArgument;

public class NetCommand {

	private static final InetAddressArgument ARG_HOST = new InetAddressArgument(
			"ip-address", "the IP-address of the server to contact");

	private static final IntegerArgument ARG_PORT = new IntegerArgument("port",
			"the port the server is listening to");

	private static final IntegerArgument ARG_LISTENPORT = new IntegerArgument(
			"port", "the Port the server should listen to");

	private static final Parameter PARAM_LISTEN = new Parameter(ARG_LISTENPORT);

	public static Help.Info HELP_INFO = new Help.Info("tcp", new Syntax(
			"Connect to a remote server", new Parameter(ARG_HOST),
			new Parameter(ARG_PORT)), new Syntax("Listens to a specified port",
			PARAM_LISTEN));

	/**
	 * @param args
	 */
	public static void main(String[] sArgs) throws Exception {
		new NetCommand().execute(new CommandLine(sArgs), System.in, System.out, System.err);
	}
	
	
	public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
		ParsedArguments args = HELP_INFO.parse(commandLine.toStringArray());

		Socket socket = null;
		if (PARAM_LISTEN.isSet(args)) {
			int port = ARG_LISTENPORT.getInteger(args);
			ServerSocket ss = ServerSocketFactory.getDefault()
					.createServerSocket(port);
			socket = ss.accept();
		} else {
			InetAddress host = ARG_HOST.getAddress(args);
			int port = ARG_PORT.getInteger(args);
			socket = SocketFactory.getDefault().createSocket(host, port);
		}

		new NetConnection(socket).start();
	}

	private static class NetConnection {
		private Socket socket;

		private CopyThread toThread;

		private CopyThread fromThread;

		private NetConnection(Socket socket) {
			this.socket = socket;
		}

		private void start() throws IOException {
			toThread = new CopyThread(System.in, socket.getOutputStream());
			fromThread = new CopyThread(socket.getInputStream(), System.out);
			toThread.start();
			fromThread.start();
			synchronized(this) {
				try {
					wait();
				} catch (Exception e) {
				}
			}
		}

		private void close(CopyThread source) {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
				}
				socket = null;
				synchronized(this) {
					notifyAll();
				}
			}
			if (source == fromThread)
				toThread.breakout();
			else
				fromThread.breakout();
		}

		private class CopyThread extends Thread {

			private final InputStream in;

			private final OutputStream out;

			public CopyThread(InputStream in, OutputStream out) {
				this.in = in;
				this.out = out;
			}

			void breakout() {
				interrupt();
			}

			public void run() {
				try {
					while (socket != null) {
						int b = in.read();
						if (b == -1)
							break;

						out.write(b);
					}
				} catch (Exception iex) {
				} finally {
					close(this);
				}
			}

		}
	}
}
