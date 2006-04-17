/* SecureRandom.java --- Secure Random class implementation
   Copyright (C) 1999, 2001, 2002, 2003, 2005, 2006
   Free Software Foundation, Inc.

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

package java.security;

import gnu.classpath.SystemProperties;
import gnu.java.security.Engine;
import gnu.java.security.action.GetSecurityPropertyAction;
import gnu.java.security.jce.prng.Sha160RandomSpi;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An interface to a cryptographically secure pseudo-random number
 * generator (PRNG). Random (or at least unguessable) numbers are used
 * in all areas of security and cryptography, from the generation of
 * keys and initialization vectors to the generation of random padding
 * bytes.
 *
 * @author Mark Benvenuto (ivymccough@worldnet.att.net)
 * @author Casey Marshall
 */
public class SecureRandom extends Random
{

  // Constants and fields.
  // ------------------------------------------------------------------------

  /** Service name for PRNGs. */
  private static final String SECURE_RANDOM = "SecureRandom";

  private static final long serialVersionUID = 4940670005562187L;

  //Serialized Field
  long counter = 0;		//Serialized
  Provider provider = null;
  byte[] randomBytes = null;	//Always null
  int randomBytesUsed = 0;
  SecureRandomSpi secureRandomSpi = null;
  byte[] state = null;
  private String algorithm;

  private boolean isSeeded = false;

  // Constructors.
  // ------------------------------------------------------------------------

  /**
     Default constructor for SecureRandom. It constructs a 
     new SecureRandom by instantating the first SecureRandom 
     algorithm in the default security provier. 

     It is not seeded and should be seeded using setSeed or else
     on the first call to getnextBytes it will force a seed.

     It is maintained for backwards compatibility and programs
     should use {@link #getInstance(java.lang.String)}.
   */
  public SecureRandom()
  {
    Provider[] p = Security.getProviders();

    //Format of Key: SecureRandom.algname
    String key;

    String classname = null;
    int i;
    Enumeration e;
    for (i = 0; i < p.length; i++)
      {
        e = p[i].propertyNames();
        while (e.hasMoreElements())
          {
            key = (String) e.nextElement();
            if (key.startsWith("SECURERANDOM."))
	      {
		if ((classname = p[i].getProperty(key)) != null)
		  {
		    try
		      {
			secureRandomSpi = (SecureRandomSpi) Class.
			  forName(classname).newInstance();
			provider = p[i];
                        algorithm = key.substring(13); // Minus SecureRandom.
			return;
		      }
                    catch (ThreadDeath death)
                      {
                        throw death;
                      }
                    catch (Throwable t)
		      {
			// Ignore.
		      }
		  }
	      }
	  }
      }

    // Nothing found. Fall back to SHA1PRNG
    secureRandomSpi = new Sha160RandomSpi();
    algorithm = "Sha160";
  }

  /**
     A constructor for SecureRandom. It constructs a new 
     SecureRandom by instantating the first SecureRandom algorithm 
     in the default security provier. 

     It is seeded with the passed function and is useful if the user
     has access to hardware random device (like a radiation detector).

     It is maintained for backwards compatibility and programs
     should use getInstance.

     @param seed Seed bytes for class
   */
  public SecureRandom(byte[] seed)
  {
    this();
    setSeed(seed);
  }

  /**
     A constructor for SecureRandom. It constructs a new 
     SecureRandom using the specified SecureRandomSpi from
     the specified security provier. 

     @param secureRandomSpi A SecureRandomSpi class
     @param provider A Provider class
   */
  protected SecureRandom(SecureRandomSpi secureRandomSpi, Provider provider)
  {
    this(secureRandomSpi, provider, "unknown");
  }

  /**
   * Private constructor called from the getInstance() method.
   */
  private SecureRandom(SecureRandomSpi secureRandomSpi, Provider provider,
		       String algorithm)
  {
    this.secureRandomSpi = secureRandomSpi;
    this.provider = provider;
    this.algorithm = algorithm;
  }

  // Class methods.
  // ------------------------------------------------------------------------

  /**
   * Returns an instance of a SecureRandom. It creates the class from
   * the first provider that implements it.
   *
   * @param algorithm The algorithm name.
   * @return A new SecureRandom implementing the given algorithm.
   * @throws NoSuchAlgorithmException If no installed provider implements
   *         the given algorithm.
   */
  public static SecureRandom getInstance(String algorithm)
    throws NoSuchAlgorithmException
  {
    Provider[] p = Security.getProviders();
    
    for (int i = 0; i < p.length; i++)
      {
	try
	  {
	    return getInstance(algorithm, p[i]);
	  }
        catch (NoSuchAlgorithmException e)
          {
	    // Ignore.
          }
      }

    // None found.
    throw new NoSuchAlgorithmException(algorithm);
  }

  /**
   * Returns an instance of a SecureRandom. It creates the class
   * for the specified algorithm from the named provider.
   *
   * @param algorithm The algorithm name.
   * @param provider  The provider name.
   * @return A new SecureRandom implementing the chosen algorithm.
   * @throws NoSuchAlgorithmException If the named provider does not implement
   *         the algorithm, or if the implementation cannot be
   *         instantiated.
   * @throws NoSuchProviderException If no provider named
   *         <code>provider</code> is currently installed.
   * @throws IllegalArgumentException If <code>provider</code> is null
   *         or is empty.
   */
  public static SecureRandom getInstance(String algorithm, String provider)
  throws NoSuchAlgorithmException, NoSuchProviderException
  {
    if (provider == null || provider.length() == 0)
      throw new IllegalArgumentException("Illegal provider");

    Provider p = Security.getProvider(provider);
    if (p == null)
      throw new NoSuchProviderException(provider);
    
    return getInstance(algorithm, p);
  }

  /**
   * Returns an instance of a SecureRandom. It creates the class for
   * the specified algorithm from the given provider.
   *
   * @param algorithm The SecureRandom algorithm to create.
   * @param provider  The provider to get the instance from.
   * @throws NoSuchAlgorithmException If the algorithm cannot be found, or
   *         if the class cannot be instantiated.
   * @throws IllegalArgumentException If <code>provider</code> is null.
   */
  public static SecureRandom getInstance(String algorithm, Provider provider)
    throws NoSuchAlgorithmException
  {
    if (provider == null)
      throw new IllegalArgumentException("Illegal provider");
	    try
	      {
        return new SecureRandom((SecureRandomSpi)
          Engine.getInstance(SECURE_RANDOM, algorithm, provider),
          provider, algorithm);
	      }
    catch (java.lang.reflect.InvocationTargetException ite)
	      {
	throw new NoSuchAlgorithmException(algorithm);
		      }
    catch (ClassCastException cce)
		      {
    throw new NoSuchAlgorithmException(algorithm);
  }
  }

  // Instance methods.
  // ------------------------------------------------------------------------

  /**
     Returns the provider being used by the current SecureRandom class.

     @return The provider from which this SecureRandom was attained
   */
  public final Provider getProvider()
  {
    return provider;
  }

  /**
   * Returns the algorithm name used or "unknown" when the algorithm
   * used couldn't be determined (as when constructed by the protected
   * 2 argument constructor).
   *
   * @since 1.5
   */
  public String getAlgorithm()
  {
    return algorithm;
  }

  /**
     Seeds the SecureRandom. The class is re-seeded for each call and 
     each seed builds on the previous seed so as not to weaken security.

     @param seed seed bytes to seed with
   */
  public void setSeed(byte[] seed)
  {
    secureRandomSpi.engineSetSeed(seed);
    isSeeded = true;
  }

  /**
     Seeds the SecureRandom. The class is re-seeded for each call and 
     each seed builds on the previous seed so as not to weaken security.

     @param seed 8 seed bytes to seed with
   */
  public void setSeed(long seed)
  {
    // This particular setSeed will be called by Random.Random(), via
    // our own constructor, before secureRandomSpi is initialized.  In
    // this case we can't call a method on secureRandomSpi, and we
    // definitely don't want to throw a NullPointerException.
    // Therefore we test.
    if (secureRandomSpi != null)
      {
        byte[] tmp = { (byte) (0xff & (seed >> 56)),
		       (byte) (0xff & (seed >> 48)),
		       (byte) (0xff & (seed >> 40)),
		       (byte) (0xff & (seed >> 32)),
		       (byte) (0xff & (seed >> 24)),
		       (byte) (0xff & (seed >> 16)),
		       (byte) (0xff & (seed >> 8)),
		       (byte) (0xff & seed)
	};
	secureRandomSpi.engineSetSeed(tmp);
        isSeeded = true;
      }
  }

  /**
     Generates a user specified number of bytes. This function
     is the basis for all the random functions.

     @param bytes array to store generated bytes in
   */
  public void nextBytes(byte[] bytes)
  {
    if (!isSeeded)
      setSeed(getSeed(32));
    randomBytesUsed += bytes.length;
    counter++;
    secureRandomSpi.engineNextBytes(bytes);
  }

  /**
     Generates an integer containing the user specified
     number of random bits. It is right justified and padded
     with zeros.

     @param numBits number of random bits to get, 0 <= numBits <= 32;

     @return the random bits
   */
  protected final int next(int numBits)
  {
    if (numBits == 0)
      return 0;

    byte[] tmp = new byte[numBits / 8 + (1 * (numBits % 8))];

    secureRandomSpi.engineNextBytes(tmp);
    randomBytesUsed += tmp.length;
    counter++;

    int ret = 0;

    for (int i = 0; i < tmp.length; i++)
      ret |= (tmp[i] & 0xFF) << (8 * i);

    long mask = (1L << numBits) - 1;
    return (int) (ret & mask);
  }

  /**
     Returns the given number of seed bytes. This method is
     maintained only for backwards capability. 

     @param numBytes number of seed bytes to get

     @return an array containing the seed bytes
   */
  public static byte[] getSeed(int numBytes)
  {
    byte[] tmp = new byte[numBytes];
    generateSeed(tmp);
    return tmp;
  }

  /**
     Returns the specified number of seed bytes.

     @param numBytes number of seed bytes to get

     @return an array containing the seed bytes
   */
  public byte[] generateSeed(int numBytes)
  {
    return secureRandomSpi.engineGenerateSeed(numBytes);
  }

  // Seed methods.

  private static final String SECURERANDOM_SOURCE = "securerandom.source";
  private static final String JAVA_SECURITY_EGD = "java.security.egd";
  private static final Logger logger = Logger.getLogger(SecureRandom.class.getName());

  private static int generateSeed(byte[] buffer)
  {
    return generateSeed(buffer, 0, buffer.length);
  }

  private static int generateSeed(byte[] buffer, int offset, int length)
  {
    URL sourceUrl = null;
    String urlStr = null;

    GetSecurityPropertyAction action = new GetSecurityPropertyAction(SECURERANDOM_SOURCE);
    try
      {
        urlStr = (String) AccessController.doPrivileged(action);
        if (urlStr != null)
          sourceUrl = new URL(urlStr);
      }
    catch (MalformedURLException ignored)
      {
        logger.log(Level.WARNING, SECURERANDOM_SOURCE + " property is malformed: {0}", 
                   urlStr);
      }

    if (sourceUrl == null)
      {
        try
          {
            urlStr = SystemProperties.getProperty(JAVA_SECURITY_EGD);
            if (urlStr != null)
              sourceUrl = new URL(urlStr);
          }
        catch (MalformedURLException mue)
          {
            logger.log(Level.WARNING, JAVA_SECURITY_EGD + " property is malformed: {0}",
                       urlStr);
          }
      }

    if (sourceUrl != null)
      {
        try
          {
            InputStream in = sourceUrl.openStream();
            return in.read(buffer, offset, length);
          }
        catch (IOException ioe)
          {
            logger.log(Level.FINE, "error reading random bytes", ioe);
          }
      }

    // If we get here, we did not get any seed from a property URL.
    return VMSecureRandom.generateSeed(buffer, offset, length);
  }
}
