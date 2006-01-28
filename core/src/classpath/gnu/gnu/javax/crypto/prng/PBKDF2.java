/* PBKDF2.java -- 
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


package gnu.javax.crypto.prng;

import gnu.java.security.prng.BasePRNG;
import gnu.java.security.prng.LimitReachedException;
import gnu.javax.crypto.mac.HMac;
import gnu.javax.crypto.mac.IMac;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>An implementation of the <i>key derivation function</i> KDF2 from PKCS #5:
 * Password-Based Cryptography (<b>PBE</b>). This KDF is essentially a way to
 * transform a password and a salt into a stream of random bytes, which may then
 * be used to initialize a cipher or a MAC.</p>
 *
 * <p>This version uses a MAC as its pseudo-random function, and the password is
 * used as the key.</p>
 *
 * <p>References:</p>
 * <ol>
 *    <li>B. Kaliski, <a href="http://www.ietf.org/rfc/rfc2898.txt">RFC 2898:
 *    Password-Based Cryptography Specification, Version 2.0</a></li>
 * </ol>
 *
 * @version $Revision$
 */
public class PBKDF2 extends BasePRNG implements Cloneable
{

  // Contstants and variables
  // -------------------------------------------------------------------------

  /**
   * The bytes fed into the MAC. This is initially the concatenation of the
   * salt and the block number.
   */
  private byte[] in;

  /** The iteration count. */
  private int iterationCount;

  /** The salt. */
  private byte[] salt;

  /** The MAC (the pseudo-random function we use). */
  private IMac mac;

  /** The number of hLen-sized blocks generated. */
  private long count;

  // Constructor(s)
  // -------------------------------------------------------------------------

  /**
   * <p>Creates a new PBKDF2 object. The argument is the MAC that will serve as
   * the pseudo-random function. The MAC does not need to be initialized.</p>
   *
   * @param mac The pseudo-random function.
   */
  public PBKDF2(IMac mac)
  {
    super("PBKDF2-" + mac.name());
    this.mac = mac;
    iterationCount = -1;
  }

  // Class methods
  // -------------------------------------------------------------------------

  // Instance methods
  // -------------------------------------------------------------------------

  public void setup(Map attributes)
  {
    Map macAttrib = new HashMap();
    macAttrib.put(HMac.USE_WITH_PKCS5_V2, Boolean.TRUE);

    byte[] s = (byte[]) attributes.get(IPBE.SALT);
    if (s == null)
      {
        if (salt == null)
          {
            throw new IllegalArgumentException("no salt specified");
          } // Otherwise re-use.
      }
    else
      {
        salt = s;
      }

    char[] password = (char[]) attributes.get(IPBE.PASSWORD);
    if (password != null)
      {
        try
          {
            macAttrib.put(IMac.MAC_KEY_MATERIAL,
                          new String(password).getBytes("UTF-8"));
          }
        catch (UnsupportedEncodingException uee)
          {
            throw new Error(uee.getMessage());
          }
      }
    else if (!initialised)
      {
        throw new IllegalArgumentException("no password specified");
      } // otherwise re-use previous password.

    try
      {
        mac.init(macAttrib);
      }
    catch (Exception x)
      {
        throw new IllegalArgumentException(x.getMessage());
      }

    Integer ic = (Integer) attributes.get(IPBE.ITERATION_COUNT);
    if (ic != null)
      {
        iterationCount = ic.intValue();
      }
    if (iterationCount <= 0)
      {
        throw new IllegalArgumentException("bad iteration count");
      }

    count = 0L;
    buffer = new byte[mac.macSize()];
    try
      {
        fillBlock();
        //      } catch (Exception x) {
      }
    catch (LimitReachedException x)
      {
        //         x.printStackTrace(System.err);
        throw new Error(x.getMessage());
      }
  }

  public void fillBlock() throws LimitReachedException
  {
    if (++count > ((1L << 32) - 1))
      {
        throw new LimitReachedException();
      }
    //      for (int i = 0; i < buffer.length; i++) {
    //         buffer[i] = 0;
    //      }
    Arrays.fill(buffer, (byte) 0x00);
    int limit = salt.length;
    //      in = new byte[salt.length + 4];
    in = new byte[limit + 4];
    System.arraycopy(salt, 0, in, 0, salt.length);
    //      in[salt.length  ] = (byte)(count >>> 24);
    //      in[salt.length+1] = (byte)(count >>> 16);
    //      in[salt.length+2] = (byte)(count >>>  8);
    //      in[salt.length+3] = (byte) count;
    in[limit++] = (byte) (count >>> 24);
    in[limit++] = (byte) (count >>> 16);
    in[limit++] = (byte) (count >>> 8);
    in[limit] = (byte) count;
    for (int i = 0; i < iterationCount; i++)
      {
        mac.reset();
        mac.update(in, 0, in.length);
        in = mac.digest();
        for (int j = 0; j < buffer.length; j++)
          {
            buffer[j] ^= in[j];
          }
      }
  }
}