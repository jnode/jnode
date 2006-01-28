/* ContentType.java -- record layer content type.
   Copyright (C) 2006  Free Software Foundation, Inc.

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


package gnu.javax.net.ssl.provider;

import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;

/**
 * The content type enumeration, which marks packets in the record layer.
 *
 * <pre>enum { change_cipher_spec(20), alert(21), handshake(22),
 *             application_data(23), (255) } ContentType;</pre>
 *
 * @author Casey Marshall (rsdio@metastatic.org)
 */
final class ContentType implements Enumerated
{

  // Constants and fields.
  // ------------------------------------------------------------------------

  static final ContentType CLIENT_HELLO_V2    = new ContentType( 1);
  static final ContentType CHANGE_CIPHER_SPEC = new ContentType(20);
  static final ContentType ALERT              = new ContentType(21);
  static final ContentType HANDSHAKE          = new ContentType(22);
  static final ContentType APPLICATION_DATA   = new ContentType(23);

  private int value;

  // Constructors.
  // ------------------------------------------------------------------------

  private ContentType(int value)
  {
    this.value = value;
  }

  // Class methods.
  // ------------------------------------------------------------------------

  static final ContentType read(InputStream in) throws IOException
  {
    int value = in.read();
    if (value == -1)
      {
        throw new EOFException("unexpected end of input stream");
      }
    switch (value & 0xFF)
      {
      case  1: return CLIENT_HELLO_V2;
      case 20: return CHANGE_CIPHER_SPEC;
      case 21: return ALERT;
      case 22: return HANDSHAKE;
      case 23: return APPLICATION_DATA;
      default: return new ContentType(value);
      }
  }

  // Instance methods.
  // ------------------------------------------------------------------------

  public byte[] getEncoded()
  {
    return new byte[] { (byte) value };
  }

  public int getValue()
  {
    return value;
  }

  public boolean equals(Object o)
  {
    if (o == null || !(o instanceof ContentType))
      {
        return false;
      }
    return ((ContentType) o).value == value;
  }

  public int hashCode()
  {
    return getValue();
  }

  public String toString()
  {
    switch (value)
      {
      case  1: return "v2_client_hello";
      case 20: return "change_cipher_spec";
      case 21: return "alert";
      case 22: return "handshake";
      case 23: return "application_data";
      default: return "unknown(" + value + ")";
      }
  }
}
