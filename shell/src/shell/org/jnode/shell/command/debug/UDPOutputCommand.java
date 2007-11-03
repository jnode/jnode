/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.log4j.Logger;
import org.jnode.debug.UDPAppender;
import org.jnode.debug.UDPOutputStream;
import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.argument.InetAddressArgument;
import org.jnode.shell.help.argument.IntegerArgument;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */
public class UDPOutputCommand  implements Command {

  private static final InetAddressArgument ARG_ADDRESS = new InetAddressArgument(
          "host", "connect host to the udpreceiver");

  private static final IntegerArgument ARG_PORT = new IntegerArgument(
          "port", "port to connect on");


  private static final Parameter PARAM_ADDRESS = new Parameter(
          ARG_ADDRESS, Parameter.MANDATORY);

  private static final Parameter PARAM_PORT = new Parameter(
          ARG_PORT, Parameter.OPTIONAL);

  public static Help.Info HELP_INFO = new Help.Info("udpout",
          "send data from System.out and System.err to remote host",
          new Parameter[] { PARAM_ADDRESS , PARAM_PORT });


  public void execute(CommandLine commandLine, InputStream in,
                      PrintStream out, PrintStream err) throws Exception {

    final ParsedArguments args = HELP_INFO.parse(commandLine);

    final int port;

    if (PARAM_PORT.isSet(args)) {
        port = ARG_PORT.getInteger(args);
    }
    else
    {
      port = 5612;
    }

    final SocketAddress address = new InetSocketAddress(ARG_ADDRESS.getAddress(args), port);
    UDPOutputStream udpOut = new UDPOutputStream(address);

    DupOutputStream dupOut = new DupOutputStream(System.out, udpOut);
    PrintStream ps = new PrintStream(dupOut);
    System.setOut(ps);
    System.setErr(ps);

    final Logger root = Logger.getRootLogger();
    root.addAppender(new UDPAppender(udpOut, null));
  }

  static class DupOutputStream extends OutputStream {

    private final OutputStream os1;
    private final OutputStream os2;

    public DupOutputStream(OutputStream os1, OutputStream os2) {
      this.os1 = os1;
      this.os2 = os2;
    }

      /**
     * @see java.io.OutputStream#close()
     * @throws IOException
       */
    public void close() throws IOException {
      os1.close();
      os2.close();
    }

    /**
     * @see java.io.OutputStream#flush()
     * @throws IOException
     */
    public void flush() throws IOException {
      os1.flush();
      os2.flush();
    }

    /**
     * @param b
     * @param off
     * @param len
     * @see java.io.OutputStream#write(byte[], int, int)
     * @throws IOException
     * @throws NullPointerException
     * @throws IndexOutOfBoundsException
     */
    public void write(byte[] b, int off, int len) throws IOException, NullPointerException, IndexOutOfBoundsException {
      os1.write(b, off, len);
      os2.write(b, off, len);
    }

    /**
     * @param b
     * @see java.io.OutputStream#write(byte[])
     * @throws IOException
     * @throws NullPointerException
     */
    public void write(byte[] b) throws IOException, NullPointerException {
      os1.write(b);
      os2.write(b);
    }

    /**
     * @param b
     * @see java.io.OutputStream#write(int)
     * @throws IOException
     */
    public void write(int b) throws IOException {
      os1.write(b);
      os2.write(b);
    }
  }
}
