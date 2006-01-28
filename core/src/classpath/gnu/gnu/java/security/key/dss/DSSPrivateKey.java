/* DSSPrivateKey.java -- 
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


package gnu.java.security.key.dss;

import gnu.java.security.Registry;
import gnu.java.security.key.IKeyPairCodec;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.interfaces.DSAPrivateKey;

/**
 * <p>An object that embodies a DSS (Digital Signature Standard) private key.</p>
 *
 * @version $Revision$
 * @see #getEncoded
 */
public class DSSPrivateKey extends DSSKey implements PrivateKey, DSAPrivateKey
{

  // Constants and variables
  // -------------------------------------------------------------------------

  /**
   * <p>A randomly or pseudorandomly generated integer with <code>0 &lt; x &lt;
   * q</code>.</p>
   */
  private final BigInteger x;

  // Constructor(s)
  // -------------------------------------------------------------------------

  /**
   * <p>Trivial constructor.</p>
   *
   * @param p the public modulus.
   * @param q the public prime divisor of <code>p-1</code>.
   * @param g a generator of the unique cyclic group <code>Z<sup>*</sup>
   * <sub>p</sub></code>.
   * @param x the private key part.
   */
  public DSSPrivateKey(BigInteger p, BigInteger q, BigInteger g, BigInteger x)
  {
    super(p, q, g);

    this.x = x;
  }

  // Class methods
  // -------------------------------------------------------------------------

  /**
   * <p>A class method that takes the output of the <code>encodePrivateKey()</code>
   * method of a DSS keypair codec object (an instance implementing
   * {@link gnu.crypto.key.IKeyPairCodec} for DSS keys, and re-constructs an
   * instance of this object.</p>
   *
   * @param k the contents of a previously encoded instance of this object.
   * @exception ArrayIndexOutOfBoundsException if there is not enough bytes,
   * in <code>k</code>, to represent a valid encoding of an instance of
   * this object.
   * @exception IllegalArgumentException if the byte sequence does not
   * represent a valid encoding of an instance of this object.
   */
  public static DSSPrivateKey valueOf(byte[] k)
  {
    // check magic...
    // we should parse here enough bytes to know which codec to use, and
    // direct the byte array to the appropriate codec.  since we only have one
    // codec, we could have immediately tried it; nevertheless since testing
    // one byte is cheaper than instatiating a codec that will fail we test
    // the first byte before we carry on.
    if (k[0] == Registry.MAGIC_RAW_DSS_PRIVATE_KEY[0])
      {
        // it's likely to be in raw format. get a raw codec and hand it over
        IKeyPairCodec codec = new DSSKeyPairRawCodec();
        return (DSSPrivateKey) codec.decodePrivateKey(k);
      }
    else
      {
        throw new IllegalArgumentException("magic");
      }
  }

  // Instance methods
  // -------------------------------------------------------------------------

  // java.security.interfaces.DSAPrivateKey interface implementation ---------

  public BigInteger getX()
  {
    return x;
  }

  // Other instance methods --------------------------------------------------

  /**
   * <p>Returns the encoded form of this private key according to the
   * designated format.</p>
   *
   * @param format the desired format identifier of the resulting encoding.
   * @return the byte sequence encoding this key according to the designated
   * format.
   * @exception IllegalArgumentException if the format is not supported.
   * @see DSSKeyPairRawCodec
   */
  public byte[] getEncoded(int format)
  {
    byte[] result;
    switch (format)
      {
      case IKeyPairCodec.RAW_FORMAT:
        result = new DSSKeyPairRawCodec().encodePrivateKey(this);
        break;
      default:
        throw new IllegalArgumentException("format");
      }
    return result;
  }

  /**
   * <p>Returns <code>true</code> if the designated object is an instance of
   * {@link DSAPrivateKey} and has the same DSS (Digital Signature Standard)
   * parameter values as this one.</p>
   *
   * @param obj the other non-null DSS key to compare to.
   * @return <code>true</code> if the designated object is of the same type and
   * value as this one.
   */
  public boolean equals(Object obj)
  {
    if (obj == null)
      {
        return false;
      }
    if (!(obj instanceof DSAPrivateKey))
      {
        return false;
      }
    DSAPrivateKey that = (DSAPrivateKey) obj;
    return super.equals(that) && x.equals(that.getX());
  }
}
