/* StoreEntry.java -- 
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


package gnu.javax.crypto.sasl.srp;

/**
 * <p>A simple timing-related object for use by SRP re-use code.</p>
 */
class StoreEntry
{

  // Constants and variables
  // -------------------------------------------------------------------------

  private boolean perenial;

  private long timeToDie;

  // Constructor(s)
  // -------------------------------------------------------------------------

  StoreEntry(int ttl)
  {
    super();

    if (ttl == 0)
      {
        perenial = true;
        timeToDie = 0L;
      }
    else
      {
        perenial = false;
        timeToDie = System.currentTimeMillis() + (ttl & 0xFFFFFFFFL) * 1000L;
      }
  }

  // Class methods
  // -------------------------------------------------------------------------

  // Instance methods
  // -------------------------------------------------------------------------

  /**
   * <p>Returns <code>true</code> if the Time-To_live period has not elapsed.</p>
   *
   * @return <code>true</code> if the Time-To-Live period (in seconds) has not
   * elapsed yet; <code>false</code> otherwise.
   */
  boolean isAlive()
  {
    return (perenial ? true : (System.currentTimeMillis() < timeToDie));
  }
}