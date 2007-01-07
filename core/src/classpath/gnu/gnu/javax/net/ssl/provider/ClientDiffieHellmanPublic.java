/* ClientDiffieHellmanPublic.java -- Client Diffie-Hellman value.
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

import java.io.PrintWriter;
import java.io.StringWriter;

import java.math.BigInteger;

import java.nio.ByteBuffer;

/**
 * The client's explicit Diffie Hellman value.
 *
 * <pre>
struct {
  select (PublicValueEncoding) {
    case implicit: struct { };
    case explicit: opaque dh_Yc&lt;1..2^16-1&gt;;
  } dh_public;
} ClientDiffieHellmanPublic;</pre> 
 */
public class ClientDiffieHellmanPublic extends ExchangeKeys implements Builder
{
  public ClientDiffieHellmanPublic(final ByteBuffer buffer)
  {
    super(buffer);
  }
  
  public ClientDiffieHellmanPublic(final BigInteger Yc)
  {
    super(wrap(Yc));
  }
  
  private static ByteBuffer wrap(BigInteger Yc)
  {
    byte[] b = Util.trim(Yc);
    ByteBuffer ret = ByteBuffer.allocate(b.length + 2);
    ret.putShort((short) b.length);
    ret.put(b);
    return (ByteBuffer) ret.rewind();
  }

  public ByteBuffer buffer()
  {
    return (ByteBuffer) buffer.duplicate().rewind().limit(length());
  }
  
  public BigInteger publicValue()
  {
    int len = length() - 2;
    byte[] b = new byte[len];
    buffer.position(2);
    buffer.get(b);
    buffer.rewind();
    return new BigInteger(1, b);
  }

  public void setPublicValue(final BigInteger Yc)
  {
    byte[] buf = Util.trim(Yc);
    if (buffer.capacity() < buf.length + 2)
      buffer = ByteBuffer.allocate(buf.length + 2);
    buffer.putShort((short) buf.length);
    buffer.put(buf);
    buffer.rewind();
  }

  public int length ()
  {
    return (buffer.getShort(0) & 0xFFFF) + 2;
  }

  public String toString ()
  {
    return toString (null);
  }

  public String toString (final String prefix)
  {
    StringWriter str = new StringWriter ();
    PrintWriter out = new PrintWriter (str);
    if (prefix != null) out.print (prefix);
    out.println ("struct {");
    if (prefix != null) out.print (prefix);
    out.print ("  dh_Yc = ");
    out.print (publicValue ().toString (16));
    out.println (';');
    if (prefix != null) out.print (prefix);
    out.print ("} ClientDiffieHellmanPublic;");
    return str.toString ();
  }
}
