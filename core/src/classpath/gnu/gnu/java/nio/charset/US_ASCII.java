/* US_ASCII.java -- 
   Copyright (C) 2002 Free Software Foundation, Inc.

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

package gnu.java.nio.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

/**
 * US-ASCII charset.
 *
 * @author Jesse Rosenstock
 */
final class US_ASCII extends Charset
{
  US_ASCII ()
  {
    super ("US-ASCII", new String[]{"ISO646-US"});
  }

  public boolean contains (Charset cs)
  {
    return cs instanceof US_ASCII;
  }

  public CharsetDecoder newDecoder ()
  {
    return new Decoder (this);
  }

  public CharsetEncoder newEncoder ()
  {
    return new Encoder (this);
  }

  private static final class Decoder extends CharsetDecoder
  {
    private Decoder (Charset cs)
    {
      super (cs, 1.0f, 1.0f);
    }

    protected CoderResult decodeLoop (ByteBuffer in, CharBuffer out)
    {
      // TODO: Optimize this in the case in.hasArray() / out.hasArray()
      while (in.hasRemaining ())
        {
          byte b = in.get ();

          if (b < 0)
            {
              in.position (in.position () - 1);
              return CoderResult.malformedForLength (1);
            }
          if (!out.hasRemaining ())
            {
              in.position (in.position () - 1);
              return CoderResult.OVERFLOW;
            }

          out.put ((char) b);
        }

      return CoderResult.UNDERFLOW;
    }
  }

  private static final class Encoder extends CharsetEncoder
  {
    private Encoder (Charset cs)
    {
      super (cs, 1.0f, 1.0f);
    }

    protected CoderResult encodeLoop (CharBuffer in, ByteBuffer out)
    {
      // TODO: Optimize this in the case in.hasArray() / out.hasArray()
      while (in.hasRemaining ())
      {
        char c = in.get ();

        if (c > Byte.MAX_VALUE)
          {
            in.position (in.position () - 1);
            return CoderResult.unmappableForLength (1);
          }
        if (!out.hasRemaining ())
          {
            in.position (in.position () - 1);
            return CoderResult.OVERFLOW;
          }

        out.put ((byte) c);
      }

      return CoderResult.UNDERFLOW;
    }
  }
}
