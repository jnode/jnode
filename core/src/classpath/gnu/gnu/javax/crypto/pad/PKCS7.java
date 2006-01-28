/* PKCS7.java -- 
   Copyright (C) 2001, 2002, 2006 Free Software Foundation, Inc.

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


package gnu.javax.crypto.pad;

import gnu.java.security.Registry;
import gnu.java.security.util.Util;

import java.io.PrintWriter;

/**
 * <p>The implementation of the PKCS7 padding algorithm.</p>
 *
 * <p>This algorithm is described for 8-byte blocks in [RFC-1423] and extended to
 * block sizes of up to 256 bytes in [PKCS-7].</p>
 *
 * References:<br>
 * <a href="http://www.ietf.org/rfc/rfc1423.txt">RFC-1423</a>: Privacy
 * Enhancement for Internet Electronic Mail: Part III: Algorithms, Modes, and
 * Identifiers.<br>
 * <a href="http://www.ietf.org/">IETF</a>.
 * <a href="http://www.rsasecurity.com/rsalabs/pkcs/pkcs-7/">[PKCS-7]</a>PKCS #7:
 * Cryptographic Message Syntax Standard - An RSA Laboratories Technical Note.<br>
 * <a href="http://www.rsasecurity.com/">RSA Security</a>.<p>
 */
public final class PKCS7 extends BasePad
{

  // Debugging methods and variables
  // -------------------------------------------------------------------------

  private static final String NAME = "pkcs7";

  private static final boolean DEBUG = false;

  private static final int debuglevel = 9;

  private static final PrintWriter err = new PrintWriter(System.out, true);

  private static void debug(String s)
  {
    err.println(">>> " + NAME + ": " + s);
  }

  // Constants and variables
  // -------------------------------------------------------------------------

  // Constructor(s)
  // -------------------------------------------------------------------------

  /**
   * Trivial package-private constructor for use by the <i>Factory</i> class.<p>
   *
   * @see gnu.crypto.pad.PadFactory
   */
  PKCS7()
  {
    super(Registry.PKCS7_PAD);
  }

  // Class methods
  // -------------------------------------------------------------------------

  // Implementation of abstract methods in BasePad
  // -------------------------------------------------------------------------

  public void setup()
  {
    if (blockSize < 2 || blockSize > 256)
      {
        throw new IllegalArgumentException();
      }
  }

  public byte[] pad(byte[] in, int offset, int length)
  {
    int padLength = blockSize;
    if (length % blockSize != 0)
      {
        padLength = blockSize - length % blockSize;
      }
    byte[] result = new byte[padLength];
    for (int i = 0; i < padLength;)
      {
        result[i++] = (byte) padLength;
      }

    if (DEBUG && debuglevel > 8)
      {
        debug("padding: 0x" + Util.toString(result));
      }
    return result;
  }

  public int unpad(byte[] in, int offset, int length)
      throws WrongPaddingException
  {
    int limit = offset + length;
    int result = in[limit - 1] & 0xFF;
    for (int i = 0; i < result; i++)
      {
        if (result != (in[--limit] & 0xFF))
          {
            throw new WrongPaddingException();
          }
      }

    if (DEBUG && debuglevel > 8)
      {
        debug("padding length: " + String.valueOf(result));
      }
    return result;
  }
}