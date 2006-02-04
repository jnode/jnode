/* RSA.java -- 
   Copyright (C) 2001, 2002, 2003, 2006 Free Software Foundation, Inc.

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


package gnu.java.security.sig.rsa;

import gnu.java.security.Properties;
import gnu.java.security.util.PRNG;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * <p>Utility methods related to the RSA algorithm.</p>
 *
 * <p>References:</p>
 * <ol>
 *    <li><a href="http://www.cosic.esat.kuleuven.ac.be/nessie/workshop/submissions/rsa-pss.zip">
 *    RSA-PSS Signature Scheme with Appendix, part B.</a><br>
 *    Primitive specification and supporting documentation.<br>
 *    Jakob Jonsson and Burt Kaliski.</li>
 *
 *    <li><a href="http://www.ietf.org/rfc/rfc3447.txt">Public-Key Cryptography
 *    Standards (PKCS) #1:</a><br>
 *    RSA Cryptography Specifications Version 2.1.<br>
 *    Jakob Jonsson and Burt Kaliski.</li>
 *
 *    <li><a href="http://crypto.stanford.edu/~dabo/abstracts/ssl-timing.html">
 *    Remote timing attacks are practical</a><br>
 *    D. Boneh and D. Brumley.</li>
 * </ol>
 */
public class RSA
{

  // Constants and variables
  // -------------------------------------------------------------------------

  private static final BigInteger ZERO = BigInteger.ZERO;

  private static final BigInteger ONE = BigInteger.ONE;

  /** Our default source of randomness. */
  private static final PRNG prng = PRNG.getInstance();

  // Constructor(s)
  // -------------------------------------------------------------------------

  /** Trivial private constructor to enforce Singleton pattern. */
  private RSA()
  {
    super();
  }

  // Class methods
  // -------------------------------------------------------------------------

  // Signature and verification methods --------------------------------------

  /**
   * <p>An implementation of the <b>RSASP</b> method: Assuming that the
   * designated RSA private key is a valid one, this method computes a
   * <i>signature representative</i> for a designated <i>message
   * representative</i> signed by the holder of the designated RSA private
   * key.<p>
   *
   * @param K the RSA private key.
   * @param m the <i>message representative</i>: an integer between
   * <code>0</code> and <code>n - 1</code>, where <code>n</code> is the RSA
   * <i>modulus</i>.
   * @return the <i>signature representative</i>, an integer between
   * <code>0</code> and <code>n - 1</code>, where <code>n</code> is the RSA
   * <i>modulus</i>.
   * @throws ClassCastException if <code>K</code> is not an RSA one.
   * @throws IllegalArgumentException if <code>m</code> (the <i>message
   * representative</i>) is out of range.
   */
  public static final BigInteger sign(final PrivateKey K, final BigInteger m)
  {
    try
      {
        return RSADP((RSAPrivateKey) K, m);
      }
    catch (IllegalArgumentException x)
      {
        throw new IllegalArgumentException(
                                           "message representative out of range");
      }
  }

  /**
   * <p>An implementation of the <b>RSAVP</b> method: Assuming that the
   * designated RSA public key is a valid one, this method computes a
   * <i>message representative</i> for the designated <i>signature
   * representative</i> generated by an RSA private key, for a message
   * intended for the holder of the designated RSA public key.</p>
   *
   * @param K the RSA public key.
   * @param s the <i>signature representative</i>, an integer between
   * <code>0</code> and <code>n - 1</code>, where <code>n</code> is the RSA
   * <i>modulus</i>.
   * @return a <i>message representative</i>: an integer between <code>0</code>
   * and <code>n - 1</code>, where <code>n</code> is the RSA <i>modulus</i>.
   * @throws ClassCastException if <code>K</code> is not an RSA one.
   * @throws IllegalArgumentException if <code>s</code> (the <i>signature
   * representative</i>) is out of range.
   */
  public static final BigInteger verify(final PublicKey K, final BigInteger s)
  {
    try
      {
        return RSAEP((RSAPublicKey) K, s);
      }
    catch (IllegalArgumentException x)
      {
        throw new IllegalArgumentException(
                                           "signature representative out of range");
      }
  }

  // Encryption and decryption methods ---------------------------------------

  /**
   * <p>An implementation of the <code>RSAEP</code> algorithm.</p>
   *
   * @param K the recipient's RSA public key.
   * @param m the message representative as an MPI.
   * @return the resulting MPI --an MPI between <code>0</code> and
   * <code>n - 1</code> (<code>n</code> being the public shared modulus)-- that
   * will eventually be padded with an appropriate framing/padding scheme.
   * @throws ClassCastException if <code>K</code> is not an RSA one.
   * @throws IllegalArgumentException if <code>m</code>, the message
   * representative is not between <code>0</code> and <code>n - 1</code>
   * (<code>n</code> being the public shared modulus).
   */
  public static final BigInteger encrypt(final PublicKey K, final BigInteger m)
  {
    try
      {
        return RSAEP((RSAPublicKey) K, m);
      }
    catch (IllegalArgumentException x)
      {
        throw new IllegalArgumentException(
                                           "message representative out of range");
      }
  }

  /**
   * <p>An implementation of the <code>RSADP</code> algorithm.</p>
   *
   * @param K the recipient's RSA private key.
   * @param c the ciphertext representative as an MPI.
   * @return the message representative, an MPI between <code>0</code> and
   * <code>n - 1</code> (<code>n</code> being the shared public modulus).
   * @throws ClassCastException if <code>K</code> is not an RSA one.
   * @throws IllegalArgumentException if <code>c</code>, the ciphertext
   * representative is not between <code>0</code> and <code>n - 1</code>
   * (<code>n</code> being the shared public modulus).
   */
  public static final BigInteger decrypt(final PrivateKey K, final BigInteger c)
  {
    try
      {
        return RSADP((RSAPrivateKey) K, c);
      }
    catch (IllegalArgumentException x)
      {
        throw new IllegalArgumentException(
                                           "ciphertext representative out of range");
      }
  }

  // Conversion methods ------------------------------------------------------

  /**
   * <p>Converts a <i>multi-precision integer</i> (MPI) <code>s</code> into an
   * octet sequence of length <code>k</code>.</p>
   *
   * @param s the multi-precision integer to convert.
   * @param k the length of the output.
   * @return the result of the transform.
   * @exception IllegalArgumentException if the length in octets of meaningful
   * bytes of <code>s</code> is greater than <code>k</code>.
   */
  public static final byte[] I2OSP(final BigInteger s, final int k)
  {
    byte[] result = s.toByteArray();
    if (result.length < k)
      {
        final byte[] newResult = new byte[k];
        System.arraycopy(result, 0, newResult, k - result.length, result.length);
        result = newResult;
      }
    else if (result.length > k)
      { // leftmost extra bytes should all be 0
        final int limit = result.length - k;
        for (int i = 0; i < limit; i++)
          {
            if (result[i] != 0x00)
              {
                throw new IllegalArgumentException("integer too large");
              }
          }
        final byte[] newResult = new byte[k];
        System.arraycopy(result, limit, newResult, 0, k);
        result = newResult;
      }
    return result;
  }

  // helper methods ----------------------------------------------------------

  private static final BigInteger RSAEP(final RSAPublicKey K, final BigInteger m)
  {
    // 1. If the representative m is not between 0 and n - 1, output
    //    "representative out of range" and stop.
    final BigInteger n = K.getModulus();
    if (m.compareTo(ZERO) < 0 || m.compareTo(n.subtract(ONE)) > 0)
      {
        throw new IllegalArgumentException();
      }
    // 2. Let c = m^e mod n.
    final BigInteger e = K.getPublicExponent();
    final BigInteger result = m.modPow(e, n);
    // 3. Output c.
    return result;
  }

  private static final BigInteger RSADP(final RSAPrivateKey K, BigInteger c)
  {
    // 1. If the representative c is not between 0 and n - 1, output
    //    "representative out of range" and stop.
    final BigInteger n = K.getModulus();
    if (c.compareTo(ZERO) < 0 || c.compareTo(n.subtract(ONE)) > 0)
      {
        throw new IllegalArgumentException();
      }

    // 2. The representative m is computed as follows.
    BigInteger result;
    if (!(K instanceof RSAPrivateCrtKey))
      {
        // a. If the first form (n, d) of K is used, let m = c^d mod n.
        final BigInteger d = K.getPrivateExponent();
        result = c.modPow(d, n);
      }
    else
      {
        // from [3] p.13 --see class docs:
        // The RSA blinding operation calculates x = (r^e) * g mod n before
        // decryption, where r is random, e is the RSA encryption exponent, and
        // g is the ciphertext to be decrypted. x is then decrypted as normal,
        // followed by division by r, i.e. (x^e) / r mod n. Since r is random,
        // x is random and timing the decryption should not reveal information
        // about the key. Note that r should be a new random number for every
        // decryption.
        final boolean rsaBlinding = Properties.doRSABlinding();
        BigInteger r = null;
        BigInteger e = null;
        if (rsaBlinding)
          { // pre-decryption
            r = newR(n);
            e = ((RSAPrivateCrtKey) K).getPublicExponent();
            final BigInteger x = r.modPow(e, n).multiply(c).mod(n);
            c = x;
          }

        // b. If the second form (p, q, dP, dQ, qInv) and (r_i, d_i, t_i)
        //    of K is used, proceed as follows:
        final BigInteger p = ((RSAPrivateCrtKey) K).getPrimeP();
        final BigInteger q = ((RSAPrivateCrtKey) K).getPrimeQ();
        final BigInteger dP = ((RSAPrivateCrtKey) K).getPrimeExponentP();
        final BigInteger dQ = ((RSAPrivateCrtKey) K).getPrimeExponentQ();
        final BigInteger qInv = ((RSAPrivateCrtKey) K).getCrtCoefficient();

        // i.    Let m_1 = c^dP mod p and m_2 = c^dQ mod q.
        final BigInteger m_1 = c.modPow(dP, p);
        final BigInteger m_2 = c.modPow(dQ, q);
        // ii.   If u > 2, let m_i = c^(d_i) mod r_i, i = 3, ..., u.
        // iii.  Let h = (m_1 - m_2) * qInv mod p.
        final BigInteger h = m_1.subtract(m_2).multiply(qInv).mod(p);
        // iv.   Let m = m_2 + q * h.
        result = m_2.add(q.multiply(h));

        if (rsaBlinding)
          { // post-decryption
            result = result.multiply(r.modInverse(n)).mod(n);
          }
      }

    // 3. Output m
    return result;
  }

  /**
   * <p>Returns a random MPI with a random bit-length of the form <code>8b</code>,
   * where <code>b</code> is in the range <code>[32..64]</code>.</p>
   *
   * @return a random MPI whose length in bytes is between 32 and 64 inclusive.
   */
  private static final BigInteger newR(final BigInteger N)
  {
    final int upper = (N.bitLength() + 7) / 8;
    final int lower = upper / 2;
    final byte[] bl = new byte[1];
    int b;
    do
      {
        prng.nextBytes(bl);
        b = bl[0] & 0xFF;
      }
    while (b < lower || b > upper);
    final byte[] buffer = new byte[b]; // 256-bit MPI
    prng.nextBytes(buffer);
    return new BigInteger(1, buffer);
  }
}
