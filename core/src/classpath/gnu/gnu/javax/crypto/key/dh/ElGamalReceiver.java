/* ElGamalReceiver.java -- 
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

import gnu.javax.crypto.key.KeyAgreementException;
import gnu.javax.crypto.key.IncomingMessage;
import gnu.javax.crypto.key.OutgoingMessage;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;

import javax.crypto.interfaces.DHPrivateKey;

/**
 * <p>This implementation is the receiver's part of the ElGamal key agreement
 * exchange (B in [HAC]).</p>
 *
 * @see ElGamalKeyAgreement
 */
public class ElGamalReceiver extends ElGamalKeyAgreement
{

  // Constants and variables
  // -------------------------------------------------------------------------

  /** The recipient's private key. */
  private DHPrivateKey B;

  // Constructor(s)
  // -------------------------------------------------------------------------

  // default 0-arguments constructor

  // Class methods
  // -------------------------------------------------------------------------

  // Instance methods
  // -------------------------------------------------------------------------

  // implementation of abstract methods in base class ------------------------

  protected void engineInit(Map attributes) throws KeyAgreementException
  {
    rnd = (SecureRandom) attributes.get(SOURCE_OF_RANDOMNESS);
    // One-time setup (key generation and publication). Each user B generates
    // a keypair and publishes its public key
    B = (DHPrivateKey) attributes.get(KA_ELGAMAL_RECIPIENT_PRIVATE_KEY);
    if (B == null)
      {
        throw new KeyAgreementException("missing recipient private key");
      }
  }

  protected OutgoingMessage engineProcessMessage(IncomingMessage in)
      throws KeyAgreementException
  {
    switch (step)
      {
      case 0:
        return computeSharedSecret(in);
      default:
        throw new IllegalStateException("unexpected state");
      }
  }

  // own methods -------------------------------------------------------------

  private OutgoingMessage computeSharedSecret(IncomingMessage in)
      throws KeyAgreementException
  {
    // (b) B computes the same key on receipt of message (1) as
    // K = (g^x)^xb mod p
    BigInteger m1 = in.readMPI();
    if (m1 == null)
      {
        throw new KeyAgreementException("missing message (1)");
      }

    ZZ = m1.modPow(B.getX(), B.getParams().getP()); // ZZ = (ya ^ xb) mod p

    complete = true;
    return null;
  }
}
