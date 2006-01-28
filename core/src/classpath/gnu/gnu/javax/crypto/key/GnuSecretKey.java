/* GnuSecretKey.java -- 
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


package gnu.javax.crypto.key;

import gnu.java.security.util.Util;
import java.security.Key;

/**
 * A secret key composed of a sequence of raw, unformatted octets. This class
 * is analogous to the {@link javax.crypto.spec.SecretKeySpec} class, but is
 * provided for platforms that do not or cannot contain that class.
 */
public class GnuSecretKey implements Key
{

  // Field.
  // ------------------------------------------------------------------------

  private final byte[] key;

  private final String algorithm;

  // Constructors.
  // ------------------------------------------------------------------------

  /**
   * Creates a new secret key. The supplied byte array is copied by this
   * constructor.
   *
   * @param key The raw, secret key.
   * @param algorithm The algorithm name, which can be null or empty.
   */
  public GnuSecretKey(byte[] key, String algorithm)
  {
    this(key, 0, key.length, algorithm);
  }

  /**
   * Creates a new secret key from a portion of a byte array.
   *
   * @param key The raw, secret key.
   * @param offset The offset at which the key begins.
   * @param length The number of bytes that comprise the key.
   * @param algorithm The algorithm name, which can be null or empty.
   */
  public GnuSecretKey(byte[] key, int offset, int length, String algorithm)
  {
    this.key = new byte[length];
    System.arraycopy(key, offset, this.key, 0, length);
    this.algorithm = algorithm;
  }

  // Instance methods.
  // ------------------------------------------------------------------------

  /**
   * Returns the algorithm name, if any.
   *
   * @return The algorithm name.
   */
  public String getAlgorithm()
  {
    return null;
  }

  /**
   * Returns the encoded key, which is merely the byte array this class was
   * created with. A reference to the internal byte array is returned, so the
   * caller can delete this key from memory by modifying the returned array.
   *
   * @return The raw key.
   */
  public byte[] getEncoded()
  {
    return key;
  }

  /**
   * Returns the string "RAW".
   *
   * @return The string "RAW".
   */
  public String getFormat()
  {
    return "RAW";
  }

  public boolean equals(Object o)
  {
    if (!(o instanceof GnuSecretKey))
      {
        return false;
      }
    if (key.length != ((GnuSecretKey) o).key.length)
      {
        return false;
      }
    byte[] key2 = ((GnuSecretKey) o).key;
    for (int i = 0; i < key.length; i++)
      {
        if (key[i] != key2[i])
          {
            return false;
          }
      }
    return true;
  }

  public String toString()
  {
    return "GnuSecretKey [ " + algorithm + " " + Util.toString(key) + " ]";
  }
}