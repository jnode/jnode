/* GnuDHKeyPairGenerator.java -- 
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


package gnu.javax.crypto.key.dh;

import gnu.java.security.Registry;
import gnu.java.security.hash.Sha160;
import gnu.java.security.key.IKeyPairGenerator;
import gnu.java.security.util.PRNG;

import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Map;

import javax.crypto.spec.DHGenParameterSpec;

/**
 * <p>An implementation of a Diffie-Hellman keypair generator.</p>
 *
 * <p>Reference:</p>
 * <ol>
 *    <li><a href="http://www.ietf.org/rfc/rfc2631.txt">Diffie-Hellman Key
 *    Agreement Method</a><br>
 *    Eric Rescorla.</li>
 * </ol>
 */
public class GnuDHKeyPairGenerator implements IKeyPairGenerator
{

  // Debugging methods and variables
  // -------------------------------------------------------------------------

  private static final String NAME = "dh";

  private static final boolean DEBUG = false;

  private static final int debuglevel = 5;

  private static final PrintWriter err = new PrintWriter(System.out, true);

  private static void debug(String s)
  {
    err.println(">>> " + NAME + ": " + s);
  }

  // Constants and variables
  // -------------------------------------------------------------------------

  /**
   * Property name of an optional {@link SecureRandom} instance to use. The
   * default is to use a classloader singleton from {@link PRNG}.
   */
  public static final String SOURCE_OF_RANDOMNESS = "gnu.crypto.dh.prng";

  /**
   * Property name of an optional {@link DHGenParameterSpec} instance to use
   * for this generator.
   */
  public static final String DH_PARAMETERS = "gnu.crypto.dh.params";

  /** Property name of the size in bits (Integer) of the public prime (p). */
  public static final String PRIME_SIZE = "gnu.crypto.dh.L";

  /** Property name of the size in bits (Integer) of the private exponent (x). */
  public static final String EXPONENT_SIZE = "gnu.crypto.dh.m";

  /** Default value for the size in bits of the public prime (p). */
  //   private static final int DEFAULT_PRIME_SIZE = 1024;
  private static final int DEFAULT_PRIME_SIZE = 512;

  /** Default value for the size in bits of the private exponent (x). */
  private static final int DEFAULT_EXPONENT_SIZE = 160;

  /** The SHA instance to use. */
  private Sha160 sha = new Sha160();

  /** The optional {@link SecureRandom} instance to use. */
  private SecureRandom rnd = null;

  /** The desired size in bits of the public prime (p). */
  private int l;

  /** The desired size in bits of the private exponent (x). */
  private int m;

  private BigInteger seed;

  private BigInteger counter;

  private BigInteger q;

  private BigInteger p;

  private BigInteger j;

  private BigInteger g;

  /** Our default source of randomness. */
  private PRNG prng = null;

  // Constructor(s)
  // -------------------------------------------------------------------------

  // default 0-arguments constructor

  // Class methods
  // -------------------------------------------------------------------------

  // Instance methods
  // -------------------------------------------------------------------------

  // gnu.crypto.keys.IKeyPairGenerator interface implementation ---------------

  public String name()
  {
    return Registry.DH_KPG;
  }

  public void setup(Map attributes)
  {
    // do we have a SecureRandom, or should we use our own?
    rnd = (SecureRandom) attributes.get(SOURCE_OF_RANDOMNESS);

    // are we given a set of Diffie-Hellman generation parameters or we shall
    // use our own?
    DHGenParameterSpec params = (DHGenParameterSpec) attributes.get(DH_PARAMETERS);

    // find out the desired sizes
    if (params != null)
      {
        l = params.getPrimeSize();
        m = params.getExponentSize();
      }
    else
      {
        Integer bi = (Integer) attributes.get(PRIME_SIZE);
        l = (bi == null ? DEFAULT_PRIME_SIZE : bi.intValue());
        bi = (Integer) attributes.get(EXPONENT_SIZE);
        m = (bi == null ? DEFAULT_EXPONENT_SIZE : bi.intValue());
      }

    //      if ((L % 256) != 0 || L < 1024) {
    if ((l % 256) != 0 || l < DEFAULT_PRIME_SIZE)
      {
        throw new IllegalArgumentException("invalid modulus size");
      }
    if ((m % 8) != 0 || m < DEFAULT_EXPONENT_SIZE)
      {
        throw new IllegalArgumentException("invalid exponent size");
      }
    if (m > l)
      {
        throw new IllegalArgumentException("exponent size > modulus size");
      }
  }

  public KeyPair generate()
  {
    if (p == null)
      {
        BigInteger[] params = new RFC2631(m, l, rnd).generateParameters();
        seed = params[RFC2631.DH_PARAMS_SEED];
        counter = params[RFC2631.DH_PARAMS_COUNTER];
        q = params[RFC2631.DH_PARAMS_Q];
        p = params[RFC2631.DH_PARAMS_P];
        j = params[RFC2631.DH_PARAMS_J];
        g = params[RFC2631.DH_PARAMS_G];
        if (DEBUG && debuglevel > 0)
          {
            debug("seed: 0x" + seed.toString(16));
            debug("counter: " + counter.intValue());
            debug("q: 0x" + q.toString(16));
            debug("p: 0x" + p.toString(16));
            debug("j: 0x" + j.toString(16));
            debug("g: 0x" + g.toString(16));
          }
      }

    // generate a private number x of length m such as: 1 < x < q - 1
    BigInteger q_minus_1 = q.subtract(BigInteger.ONE);
    byte[] mag = new byte[(m + 7) / 8];
    BigInteger x;
    while (true)
      {
        nextRandomBytes(mag);
        x = new BigInteger(1, mag);
        if (x.bitLength() == m && x.compareTo(BigInteger.ONE) > 0
            && x.compareTo(q_minus_1) < 0)
          {
            break;
          }
      }
    BigInteger y = g.modPow(x, p);

    PrivateKey secK = new GnuDHPrivateKey(q, p, g, x);
    PublicKey pubK = new GnuDHPublicKey(q, p, g, y);

    return new KeyPair(pubK, secK);
  }

  // other methods -----------------------------------------------------------

  /**
   * <p>Fills the designated byte array with random data.</p>
   *
   * @param buffer the byte array to fill with random data.
   */
  private void nextRandomBytes(byte[] buffer)
  {
    if (rnd != null)
      {
        rnd.nextBytes(buffer);
      }
    else
      getDefaultPRNG().nextBytes(buffer);
      }

  private PRNG getDefaultPRNG()
  {
    if (prng == null)
      prng = PRNG.getInstance();

    return prng;
  }
}
