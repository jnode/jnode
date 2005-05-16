/* MessageHeader.java -- GIOP 1.0 message header.
   Copyright (C) 2005 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

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
exception statement from your version. */


package gnu.CORBA.GIOP;

import gnu.CORBA.Version;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.util.Arrays;

import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.portable.IDLEntity;

/**
 * The GIOP message header.
 *
 * @author Audrius Meskauskas (AudriusA@Bioinformatics.org)
 */
public class MessageHeader
  implements IDLEntity
{
  /**
   * Request message.
   */
  public static final byte REQUEST = 0;

  /**
   * Reply message
   */
  public static final byte REPLY = 1;

  /**
   * Cancel request message.
   */
  public static final byte CANCEL_REQUEST = 2;

  /**
   * Locate request message, used to check the server ability to
   * process requests for the object reference.
   * This message is also used to get the
   * address where the object reference should be sent.
   */
  public static final byte LOCATE_REQUEST = 3;

  /**
   * Locate reply message, sent in response to the
   * {@link #LocateRequest} message.
   */
  public static final byte LOCATE_REPLY = 4;

  /**
   * Instruction to close the connection.
   */
  public static final byte CLOSE_CONNECTION = 5;

  /**
   * Error report.
   */
  public static final byte MESSAGE_ERROR = 6;

  /**
   * The fragment messge, following the previous message that
   * has more fragments flag set. Added in GIOP 1.1
   */
  public static final byte FRAGMENT = 7;

  /**
   * This must always be "GIOP".
   */
  public static final byte[] MAGIC = new byte[] { 'G', 'I', 'O', 'P' };

  /**
   * The message type names.
   */
  protected static String[] types =
    new String[]
    {
      "Request", "Reply", "Cancel", "Locate request", "Locate reply",
      "Close connection", "Error", "Fragment"
    };

  /**
   * The GIOP version. Initialised to 1.0 .
   */
  public Version version;

  /**
   * The flags field, introduced since GIOP 1.1.
   */
  public byte flags = 0;

  /**
   * The message type.
   */
  public byte message_type = REQUEST;

  /**
   * The message size, excluding the message header.
   */
  public int message_size = 0;

  /**
   * Create an empty message header, corresponding version 1.0.
   */
  public MessageHeader()
  {
    version = new Version(1,0);
  }

  /**
   * Create an empty message header, corresponding the given version.
   *
   * @param major the major message header version.
   * @param minor the minot message header version.
   */
  public MessageHeader(int major, int minor)
  {
    version = new Version(major, minor);
  }

  /**
   * Checks if the message is encoded in the Big Endian, most significant
   * byte first.
   */
  public boolean isBigEndian()
  {
    return (flags & 0x1) == 0;
  }

  /**
   * Get the size of the message header itself. So far, it is always 12 bytes.
   */
  public int getHeaderSize()
  {
    return 12;
  }

  /**
   * Get the message type as string.
   *
   * @param type the message type as int (the field {@link message_type}).
   *
   * @return the message type as string.
   */
  public String getTypeString(int type)
  {
    try
      {
        return types [ type ];
      }
    catch (ArrayIndexOutOfBoundsException ex)
      {
        return "unknown type (" + type + ")";
      }
  }

  /**
   * Creates reply header, matching the message header version number.
   *
   * @return one of {@link gnu.CORBA.GIOP.v1_0.ReplyHeader},
   * {@link gnu.CORBA.GIOP.v1_2.ReplyHeader}, etc - depending on
   * the version number in this header.
   */
  public ReplyHeader create_reply_header()
  {
    if (version.since_inclusive(1, 2))
      return new gnu.CORBA.GIOP.v1_2.ReplyHeader();
    else
      return new gnu.CORBA.GIOP.v1_0.ReplyHeader();
  }

  /**
   * Creates request header, matching the message header version number.
   *
   * @return one of {@link gnu.CORBA.GIOP.v1_0.RequestHeader},
   * {@link gnu.CORBA.GIOP.v1_2.RequestHeader}, etc - depending on
   * the version number in this header.
   */
  public RequestHeader create_request_header()
  {
    if (version.since_inclusive(1, 2))
      return new gnu.CORBA.GIOP.v1_2.RequestHeader();
    else
      return new gnu.CORBA.GIOP.v1_0.RequestHeader();
  }

  /**
   * Create the cancel header, matching the message header version number.
   */
  public CancelHeader create_cancel_header()
  {
    return new gnu.CORBA.GIOP.v1_0.CancelHeader();
  }

  /**
   * Create the error message.
   */
  public ErrorMessage create_error_message()
  {
    return new ErrorMessage(version);
  }

  /**
   * Read the header from the stream.
   *
   * @param istream a stream to read from.
   *
   * @throws MARSHAL if this is not a GIOP 1.0 header.
   */
  public void read(java.io.InputStream istream)
            throws MARSHAL
  {
    try
      {
        DataInputStream din = new DataInputStream(istream);

        byte[] xMagic = new byte[ MAGIC.length ];
        din.read(xMagic);
        if (!Arrays.equals(xMagic, MAGIC))
          throw new MARSHAL("Not a GIOP message");

        version = Version.read_version(din);

        flags = (byte) din.read();

        /** TODO implement support for the little endian. */
        if (!isBigEndian())
          throw new MARSHAL("Little endian unsupported.");

        message_type = (byte) din.read();
        message_size = din.readInt();
      }
    catch (IOException ex)
      {
        throw new MARSHAL(ex.toString());
      }
  }

  /**
   * Get the short string summary of the message.
   *
   * @return a short message summary.
   */
  public String toString()
  {
    return "GIOP " + version + ", " + (isBigEndian() ? "Big" : "Little") +
           " endian, " + getTypeString(message_type) + ", " + message_size +
           " bytes. ";
  }

  /**
   * Write the header to stream.
   *
   * @param out a stream to write into.
   */
  public void write(java.io.OutputStream out)
  {
    try
      {
        DataOutputStream dout = new DataOutputStream(out);

        // Write magic sequence.
        dout.write(MAGIC);

        // Write version number.
        version.write(dout);

        dout.write(flags);

        dout.write(message_type);
        dout.writeInt(message_size);
      }
    catch (IOException ex)
      {
        throw new MARSHAL(ex.toString());
      }
  }
}
