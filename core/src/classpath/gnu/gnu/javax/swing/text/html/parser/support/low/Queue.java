/* Queue.java -- a token queue.
   Copyright (C) 2005 Free Software Foundation, Inc.

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


package gnu.javax.swing.text.html.parser.support.low;

import java.util.Arrays;

/**
 * A token queue.
 * @author Audrius Meskauskas, Lithuania (AudriusA@Bioinformatics.org)
 */
public class Queue
{
  Token[] m = new Token[ 64 ];
  int a = 0;
  int b = 0;

  /**
   * True for the empty queue.
   */
  public boolean isEmpty()
  {
    return size() == 0;
  }

  /**
   *  Add this trace to the end of the queue.
   */
  public void add(Token u)
  {
    if (a < m.length)
      {
        m [ a ] = u;
        a++;
      }
    else // The end of array has been reached.
      {
        if (b > 0) // If some elements were deleted from the start of the queue, shift.
          {
            int d = b;
            System.arraycopy(m, b, m, 0, a - b);
            b = b - d;
            a = a - d;
            m [ a ] = u;
            a++;
          }
        else // Enlarge the queue, doubling the size.
          {
            int n = m.length * 2;
            Token[] nm = new Token[ 2 * n ];
            System.arraycopy(m, 0, nm, 0, m.length);
            Arrays.fill(m, null);

            nm [ a ] = u;
            m = nm;
            a++;
          }
      }
  }

  /**
   * Clear the queue.
   */
  public void clear()
  {
    a = b = 0;
    Arrays.fill(m, null);
  }

  /**
   * Read the value ahead. 0 is the value that will be returned with
   * the following next. This method does not remove values from the
   * queue. To test if there is enough tokens in the queue, size() must
   * be checked before calling this method.
   */
  public Token get(int ahead)
  {
    int p = b + ahead;
    if (p < a)
      return m [ p ];
    else
      throw new ArrayIndexOutOfBoundsException("Not enough tokens");
  }

  /**
   * Read the oldest value from the queue and remove this value from
   * the queue.
   */
  public Token next()
  {
    if (a == b)
      throw new ArrayIndexOutOfBoundsException("queue empty");

    Token r = m [ b ];
    m [ b ] = null;
    b++;
    return r;
  }

  /**
   * Size of the queue.
   */
  public int size()
  {
    return a - b;
  }
}
