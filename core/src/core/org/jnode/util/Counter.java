/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

package org.jnode.util;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class Counter extends Statistic
{

  private final static String is = "=";
  private int counter = 0;

  public Counter(String name)
  {
    super(name, null);
  }

  public Counter(String name, String description)
  {
    super(name, description);
  }

  /**
   * Gets the counter of this statistic
   *
   * @return the counter
   */
  public int get()
  {
    return counter;
  }

  public Object getValue()
  {
    return new Integer(counter);
  }

  /**
   * Increment the counter of this statistic by 1.
   */
  public void inc()
  {
    counter++;
  }

  /**
   * Reset the counter to 0.
   */
  public void reset()
  {
    counter = 0;
  }

  /**
   * Add <i>increment</i> to the counter of this statistic.
   */
  public void add(int increment) {
      counter+=increment;
  }
      
  /**
   * Convert to a String representation
   *
   * @return String
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    StringBuffer stringBuffer = new StringBuffer(getName());
    stringBuffer.append(is);
    stringBuffer.append(counter);

    return stringBuffer.toString();
  }
}
