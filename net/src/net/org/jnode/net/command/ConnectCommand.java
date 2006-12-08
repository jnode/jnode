package org.jnode.net.command;
  
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.InetAddress;
  
import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.InetAddressArgument;
import org.jnode.shell.help.IntegerArgument;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
  
/**
 * A simply Command to connect to a socket
 * 
 * TODO add CTRL+D to close stream
 * 
 * @author peda
 */
public class ConnectCommand implements Command {
  
	static final InetAddressArgument ARG_HOST = new InetAddressArgument("IP", "the IP Address to connect to");
	static final IntegerArgument ARG_PORT = new IntegerArgument("port", "the port to connect to");
      
	public static Help.Info HELP_INFO = new Help.Info(
			"connect",
			"Try to connect to the given port on the given system",
			new Parameter[]{
					new Parameter(ARG_HOST, Parameter.MANDATORY),
					new Parameter(ARG_PORT, Parameter.MANDATORY)
			}
	);
  
	private static boolean active = true;

	/**
	 * (non-Javadoc)
	 * @see org.jnode.shell.Command#execute(org.jnode.shell.CommandLine, java.io.InputStream, java.io.PrintStream, java.io.PrintStream)
	 */
	public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {

		active = true;
		
		ParsedArguments cmdLine = HELP_INFO.parse(commandLine.toStringArray());
		
		InetAddress host = ARG_HOST.getAddress(cmdLine);
		int port = ARG_PORT.getInteger(cmdLine);
		
		try {
			Socket client = new Socket(host, port);
			System.out.println("Connected to " + host + ":" + port);
			
			InStream is = new InStream(client.getInputStream(), out);
			is.start();
			
			//System.out.println("InStream Thread started..");
			
			OutputStream os = client.getOutputStream();
			int r;
			
			//System.out.println("Entering while(true)-loop for reading from stdin");
			
			while (active) {
				try {
					r = in.read();
					if (r == -1) 
						break;
					os.write(r);
				} catch (IOException e) {
					System.out.println("Failed to read from stdin");
					e.printStackTrace();
					break;
				}
			}
			
			//System.out.println("while(true)-loop finished");
			
			is.active2 = false;

			if (!client.isClosed())
				client.close();
			
		} catch (Exception e) {
			System.out.println("Failed in execute!");
			e.printStackTrace();
		}
	}

	/**
	 * Simple inner class to read from the Socket and print to stdout
	 */
	private class InStream extends Thread {

		private boolean active2 = true;
		private InputStream is;
		private PrintStream out;
    	 
		public InStream(InputStream is, PrintStream out) {
			this.is = is;
			this.out = out;
		}
  
		public void run() {
			int r;
			while (active2) {
				try {
					r = is.read();
					if (r == -1)
						break;
					out.print((char) r);
				} catch (IOException e) {
					System.out.println("Failed to read from Socket");
					e.printStackTrace();
					break;
				}
			}
			active = false;
		}
	}
}
