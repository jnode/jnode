/* AnonymousUtil.java -- 
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


package gnu.javax.crypto.sasl.anonymous;

import gnu.javax.crypto.sasl.SaslUtil;

/**
 * An ANONYMOUS-specific utility class.
 */
public class AnonymousUtil
{

  // Constants and variables
  // -------------------------------------------------------------------------

  // Constructor(s)
  // -------------------------------------------------------------------------

  /** Trivial private constructor to enforce Singleton pattern. */
  private AnonymousUtil()
  {
    super();
  }

  // Class methods
  // -------------------------------------------------------------------------

  static boolean isValidTraceInformation(String traceInformation)
  {
    if (traceInformation == null)
      {
        return false;
      }
    if (traceInformation.length() == 0)
      {
        return true;
      }
    if (SaslUtil.validEmailAddress(traceInformation))
      {
        return true;
      }
    return isValidToken(traceInformation);
  }

  static boolean isValidToken(String token)
  {
    if (token == null)
      {
        return false;
      }
    if (token.length() == 0)
      {
        return false;
      }
    if (token.length() > 255)
      {
        return false;
      }
    if (token.indexOf('@') != -1)
      {
        return false;
      }
    for (int i = 0; i < token.length(); i++)
      {
        char c = token.charAt(i);
        if (c < 0x20 || c > 0x7E)
          {
            return false;
          }
      }
    return true;
  }
}