/* ARCFourRandomSpi.java -- 
   Copyright (C) 2002, 2003, 2006  Free Software Foundation, Inc.

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


package gnu.javax.crypto.jce.prng;

import gnu.java.security.Registry;
import gnu.javax.crypto.prng.ARCFour;
import gnu.java.security.prng.IRandom;
import gnu.java.security.prng.LimitReachedException;
import gnu.javax.crypto.prng.PRNGFactory;

import java.security.SecureRandomSpi;
import java.util.HashMap;

/**
 * Implementation of the <i>Service Provider Interface</i> (<b>SPI</b>)
 * for the ARCFOUR keystream generator.
 */
public class ARCFourRandomSpi extends SecureRandomSpi
{

  // Constants and variables
  // -------------------------------------------------------------------------

  /** Our underlying prng instance. */
  private IRandom adaptee;

  /** Have we been initialized? */
  private boolean virgin;

  // Constructor(s)
  // -------------------------------------------------------------------------

  /**
   * Default 0-arguments constructor.
   */
  public ARCFourRandomSpi()
  {
    super();
    adaptee = PRNGFactory.getInstance(Registry.ARCFOUR_PRNG);
    virgin = true;
  }

  // Class methods
  // -------------------------------------------------------------------------

  // Instance methods
  // -------------------------------------------------------------------------

  // java.security.SecureRandomSpi interface implementation ------------------

  public byte[] engineGenerateSeed(int numBytes)
  {
    if (numBytes < 1)
      {
        return new byte[0];
      }
    byte[] result = new byte[numBytes];
    this.engineNextBytes(result);
    return result;
  }

  public void engineNextBytes(byte[] bytes)
  {
    if (virgin)
      {
        this.engineSetSeed(new byte[0]);
      }
    try
      {
        adaptee.nextBytes(bytes, 0, bytes.length);
      }
    catch (LimitReachedException ignored)
      {
      }
  }

  public void engineSetSeed(byte[] seed)
  {
    HashMap attributes = new HashMap();
    attributes.put(ARCFour.ARCFOUR_KEY_MATERIAL, seed);
    adaptee.init(attributes);
    virgin = false;
  }
}