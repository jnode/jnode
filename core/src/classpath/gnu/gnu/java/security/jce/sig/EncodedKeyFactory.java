/* EncodedKeyFactory.java -- JCE Encoded key factory Adapter
   Copyright (C) 2006 Free Software Foundation, Inc.

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
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

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


package gnu.java.security.jce.sig;

import gnu.java.security.Registry;
import gnu.java.security.key.dss.DSSPrivateKey;
import gnu.java.security.key.dss.DSSPublicKey;
import gnu.java.security.key.rsa.GnuRSAPrivateKey;
import gnu.java.security.key.rsa.GnuRSAPublicKey;

import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.KeyFactorySpi;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * A factory for keys encoded in either the X.509 format (for public keys) or
 * the PKCS#8 format (for private keys).
 */
public class EncodedKeyFactory
    extends KeyFactorySpi
{
  // implicit 0-arguments constructor

  protected PublicKey engineGeneratePublic(KeySpec keySpec)
      throws InvalidKeySpecException
  {
    if (! (keySpec instanceof X509EncodedKeySpec))
      throw new InvalidKeySpecException("only supports X.509 key specs");

    byte[] input = ((X509EncodedKeySpec) keySpec).getEncoded();

    // try DSS
    try
      {
        return DSSPublicKey.valueOf(input);
      }
    catch (InvalidParameterException ignored)
      {
      }

    // try RSA
    try
    {
      return GnuRSAPublicKey.valueOf(input);
    }
  catch (InvalidParameterException ignored)
    {
    }

    // FIXME: try DH

    throw new InvalidKeySpecException();
  }

  protected PrivateKey engineGeneratePrivate(KeySpec keySpec)
      throws InvalidKeySpecException
  {
    if (! (keySpec instanceof PKCS8EncodedKeySpec))
      throw new InvalidKeySpecException("only supports PKCS8 key specs");

    byte[] input = ((PKCS8EncodedKeySpec) keySpec).getEncoded();

    // try DSS
    try
      {
        return DSSPrivateKey.valueOf(input);
      }
    catch (InvalidParameterException ignored)
      {
      }

    // try RSA
    try
    {
      return GnuRSAPrivateKey.valueOf(input);
    }
  catch (InvalidParameterException ignored)
    {
    }

    // FIXME: try DH

    throw new InvalidKeySpecException();
  }

  protected KeySpec engineGetKeySpec(Key key, Class keySpec)
      throws InvalidKeySpecException
  {
    if (key instanceof PublicKey
        && Registry.X509_ENCODING_SORT_NAME.equalsIgnoreCase(key.getFormat())
        && keySpec.isAssignableFrom(X509EncodedKeySpec.class))
      return new X509EncodedKeySpec(key.getEncoded());

    if (key instanceof PrivateKey
        && Registry.PKCS8_ENCODING_SHORT_NAME.equalsIgnoreCase(key.getFormat())
        && keySpec.isAssignableFrom(PKCS8EncodedKeySpec.class))
      return new PKCS8EncodedKeySpec(key.getEncoded());

    throw new InvalidKeySpecException("Unsupported format or invalid key spec class");
  }

  protected Key engineTranslateKey(Key key) throws InvalidKeyException
  {
    throw new InvalidKeyException("Key translation not supported");
  }
}
