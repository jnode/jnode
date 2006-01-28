/* SaslOutputStream.java -- 
   Copyright (C) 2003, 2006 Free Software Foundation, Inc.

This file is a part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or (at
your option) any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
USA

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.  */


package gnu.javax.crypto.sasl;

import gnu.java.security.util.Util;

import java.io.OutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslServer;

/**
 * An output stream that uses either a {@link SaslClient} or a {@link SaslServer}
 * to process the data through these entities' security layer filter(s).
 */
public class SaslOutputStream extends OutputStream
{

  // Debugging methods and variables
  // -------------------------------------------------------------------------

  private static final String NAME = "SaslOutputStream";

  //   private static final String ERROR = "ERROR";
  //   private static final String WARN =  " WARN";
  //   private static final String INFO =  " INFO";
  private static final String TRACE = "DEBUG";

  private static final boolean DEBUG = true;

  private static final int debuglevel = 3;

  private static final PrintWriter err = new PrintWriter(System.out, true);

  private static void debug(String level, Object obj)
  {
    err.println("[" + level + "] " + NAME + ": " + String.valueOf(obj));
  }

  // Constants and variables
  // -------------------------------------------------------------------------

  private SaslClient client;

  private SaslServer server;

  private int maxRawSendSize;

  private OutputStream dest;

  // Constructor(s)
  // -------------------------------------------------------------------------

  public SaslOutputStream(SaslClient client, OutputStream dest)
      throws IOException
  {
    super();

    this.client = client;
    maxRawSendSize = Integer.parseInt((String) client.getNegotiatedProperty(Sasl.RAW_SEND_SIZE));
    server = null;
    this.dest = dest;
  }

  public SaslOutputStream(SaslServer server, OutputStream dest)
      throws IOException
  {
    super();

    this.server = server;
    maxRawSendSize = Integer.parseInt((String) server.getNegotiatedProperty(Sasl.RAW_SEND_SIZE));
    client = null;
    this.dest = dest;
  }

  // Class methods
  // -------------------------------------------------------------------------

  // Overloaded java.io.OutputStream methods
  // -------------------------------------------------------------------------

  public void close() throws IOException
  {
    dest.flush();
    dest.close();
  }

  public void flush() throws IOException
  {
    dest.flush();
  }

  /**
   * When writing octets to the resulting stream, if a security layer has been
   * negotiated, each piece of data written (by a single invocation of
   * <code>write()</code>) will be encapsulated as a SASL buffer, as defined in
   * RFC 2222, and then written to the underlying <i>dest</i> output stream.
   */
  public void write(int b) throws IOException
  {
    write(new byte[] { (byte) b });
  }

  /**
   * When writing octets to the resulting stream, if a security layer has been
   * negotiated, each piece of data written (by a single invocation of
   * <code>write()</code>) will be encapsulated as a SASL buffer, as defined in
   * RFC 2222, and then written to the underlying <i>dest</i> output stream.
   */
  public void write(byte[] b, int off, int len) throws IOException
  {
    if (b == null)
      {
        throw new NullPointerException("b");
      }
    if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length)
        || ((off + len) < 0))
      {
        throw new IndexOutOfBoundsException("off=" + String.valueOf(off)
                                            + ", len=" + String.valueOf(len)
                                            + ", b.length="
                                            + String.valueOf(b.length));
      }
    if (len == 0)
      {
        return;
      }
    if (DEBUG && debuglevel > 8)
      debug(TRACE, "==> write()");

    int chunckSize, length, chunck = 1;
    byte[] output = null, result;
    if (DEBUG && debuglevel > 6)
      debug(TRACE, "About to wrap " + String.valueOf(len) + " byte(s)...");
    while (len > 0)
      {
        chunckSize = (len > maxRawSendSize ? maxRawSendSize : len);

        if (DEBUG && debuglevel > 6)
          debug(TRACE, "Outgoing buffer (before security) (hex): "
                       + Util.dumpString(b, off, chunckSize));
        if (DEBUG && debuglevel > 6)
          debug(TRACE, "Outgoing buffer (before security) (str): \""
                       + new String(b, off, chunckSize) + "\"");

        if (client != null)
          output = client.wrap(b, off, chunckSize);
        else
          output = server.wrap(b, off, chunckSize);

        if (DEBUG && debuglevel > 6)
          debug(TRACE, "Outgoing buffer (after security) (hex): "
                       + Util.dumpString(output));
        if (DEBUG && debuglevel > 6)
          debug(TRACE, "Outgoing buffer (after security) (str): \""
                       + new String(output) + "\"");

        length = output.length;
        result = new byte[length + 4];
        result[0] = (byte) (length >>> 24);
        result[1] = (byte) (length >>> 16);
        result[2] = (byte) (length >>> 8);
        result[3] = (byte) length;
        System.arraycopy(output, 0, result, 4, length);

        dest.write(result);

        off += chunckSize;
        len -= chunckSize;
        if (DEBUG && debuglevel > 6)
          debug(TRACE, "Wrapped chunck #" + String.valueOf(chunck));
        chunck++;
      }

    dest.flush();
    if (DEBUG && debuglevel > 8)
      debug(TRACE, "<== write()");
  }
}