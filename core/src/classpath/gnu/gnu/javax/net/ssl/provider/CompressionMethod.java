/* CompressionMethod.java -- the compression method enum.
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

final class CompressionMethod implements Enumerated
{

  // Constants and fields.
  // -------------------------------------------------------------------------

  static final CompressionMethod NULL = new CompressionMethod(0),
    ZLIB = new CompressionMethod(1);

  private final int value;

  // Constructor.
  // -------------------------------------------------------------------------

  private CompressionMethod(int value)
  {
    this.value = value;
  }

  // Class method.
  // -------------------------------------------------------------------------

  static CompressionMethod read(InputStream in) throws IOException
  {
    int value = in.read();
    if (value == -1)
      {
        throw new EOFException("unexpected end of input stream");
      }
    switch (value & 0xFF)
      {
      case 0: return NULL;
      case 1: return ZLIB;
      default: return new CompressionMethod(value);
      }
  }

  // Instance methods.
  // -------------------------------------------------------------------------

  public byte[] getEncoded()
  {
    return new byte[] { (byte) value };
  }

  public int getValue()
  {
    return value;
  }

  public String toString()
  {
    switch (value)
      {
      case 0: return "null";
      case 1: return "zlib";
      default: return "unknown(" + value + ")";
      }
  }
}
