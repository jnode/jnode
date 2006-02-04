/* RSAKeyPairGenerator.java -- 
   Copyright 2001, 2002, 2003, 2006 Free Software Foundation, Inc.

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


package gnu.java.security.key.rsa;

import gnu.java.security.Registry;
import gnu.java.security.key.IKeyPairGenerator;
import gnu.java.security.util.PRNG;
import gnu.java.security.util.Prime2;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Map;

/**
 * <p>A key-pair generator for asymetric keys to use in conjunction with the RSA
 * scheme.</p>
 *
 * <p>Reference:</p>
 * <ol>
 *    <li><a href="http://www.cosic.esat.kuleuven.ac.be/nessie/workshop/submissions/rsa-pss.zip">
 *    RSA-PSS Signature Scheme with Appendix</a>, part B. Primitive
 *    specification and supporting documentation. Jakob Jonsson and Burt Kaliski.
 *    </li>
 *    <li><a href="http://www.cacr.math.uwaterloo.ca/hac/">Handbook of Applied
 *    Cryptography</a>, Alfred J. Menezes, Paul C. van Oorschot and Scott A.
 *    Vanstone. Section 11.3 RSA and related signature schemes.</li>
 * </ol>
 */
public class RSAKeyPairGenerator implements IKeyPairGenerator
{

  // Constants and variables
  // -------------------------------------------------------------------------

  /** The BigInteger constant 1. */
  private static final BigInteger ONE = BigInteger.ONE;

  /** The BigInteger constant 2. */
  private static final BigInteger TWO = BigInteger.valueOf(2L);

  /** Property name of the length (Integer) of the modulus of an RSA key. */
  public static final String MODULUS_LENGTH = "gnu.crypto.rsa.L";

  /**
   * Property name of an optional {@link SecureRandom} instance to use. The
   * default is to use a classloader singleton from {@link PRNG}.
   */
  public static final String SOURCE_OF_RANDOMNESS = "gnu.crypto.rsa.prng";

  /**
   * Property name of an optional {@link RSAKeyGenParameterSpec} instance to
   * use for this generator's <code>n</code>, and <code>e</code> values. The
   * default is to generate <code>n</code> and use a fixed value for
   * <code>e</.code> (Fermat's F4 number).
   */
  public static final String RSA_PARAMETERS = "gnu.crypto.rsa.params";

  /** Default value for the modulus length. */
  private static final int DEFAULT_MODULUS_LENGTH = 1024;

  /** The desired bit length of the modulus. */
  private int L;

  /**
   * This implementation uses, by default, Fermat's F4 number as the public
   * exponent.
   */
  private BigInteger e = BigInteger.valueOf(65537L);

  /** The optional {@link SecureRandom} instance to use. */
  private SecureRandom rnd = null;

  /** Our default source of randomness. */
  private PRNG prng = null;

  // Constructor(s)
  // -------------------------------------------------------------------------

  // implicit 0-arguments constructor

  // Class methods
  // -------------------------------------------------------------------------

  // gnu.crypto.key.IKeyPairGenerator interface implementation ---------------

  public String name()
  {
    return Registry.RSA_KPG;
  }

  /**
   * <p>Configures this instance.</p>
   *
   * @param attributes the map of name/value pairs to use.
   * @exception IllegalArgumentException if the designated MODULUS_LENGTH
   * value is less than 1024.
   */
  public void setup(Map attributes)
  {
    // do we have a SecureRandom, or should we use our own?
    rnd = (SecureRandom) attributes.get(SOURCE_OF_RANDOMNESS);

    // are we given a set of RSA params or we shall use our own?
    RSAKeyGenParameterSpec params = (RSAKeyGenParameterSpec) attributes.get(RSA_PARAMETERS);

    // find out the modulus length
    if (params != null)
      {
        L = params.getKeysize();
        e = params.getPublicExponent();
      }
    else
      {
        Integer l = (Integer) attributes.get(MODULUS_LENGTH);
        L = (l == null ? DEFAULT_MODULUS_LENGTH : l.intValue());
      }

    if (L < 1024)
      {
        throw new IllegalArgumentException(MODULUS_LENGTH);
      }
  }

  /**
   * <p>The algorithm used here is described in <i>nessie-pss-B.pdf</i>
   * document which is part of the RSA-PSS submission to NESSIE.</p>
   *
   * @return an RSA keypair.
   */
  public KeyPair generate()
  {
    BigInteger p, q, n, d;

    // 1. Generate a prime p in the interval [2**(M-1), 2**M - 1], where
    // M = CEILING(L/2), and such that GCD(p, e) = 1
    int M = (L + 1) / 2;
    BigInteger lower = TWO.pow(M - 1);
    BigInteger upper = TWO.pow(M).subtract(ONE);
    byte[] kb = new byte[(M + 7) / 8]; // enough bytes to frame M bits
    step1: while (true)
      {
        nextRandomBytes(kb);
        p = new BigInteger(1, kb).setBit(0);
        if (p.compareTo(lower) >= 0 && p.compareTo(upper) <= 0
            && Prime2.isProbablePrime(p) && p.gcd(e).equals(ONE))
          {
            break step1;
          }
      }

    // 2. Generate a prime q such that the product of p and q is an L-bit
    // number, and such that GCD(q, e) = 1
    step2: while (true)
      {
        nextRandomBytes(kb);
        q = new BigInteger(1, kb).setBit(0);
        n = p.multiply(q);
        if (n.bitLength() == L && Prime2.isProbablePrime(q)
            && q.gcd(e).equals(ONE))
          {
            break step2;
          }

        // TODO: test for p != q
      }

    // TODO: ensure p < q

    // 3. Put n = pq. The public key is (n, e).
    // 4. Compute the parameters necessary for the private key K (see
    // Section 2.2).
    BigInteger phi = p.subtract(ONE).multiply(q.subtract(ONE));
    d = e.modInverse(phi);

    // 5. Output the public key and the private key.
    PublicKey pubK = new GnuRSAPublicKey(n, e);
    PrivateKey secK = new GnuRSAPrivateKey(p, q, e, d);

    return new KeyPair(pubK, secK);
  }

  // helper methods ----------------------------------------------------------

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
