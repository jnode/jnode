/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package gnu.classpath.jdwp;

import gnu.classpath.jdwp.util.Location;

/**
 * Reference implementation of VM hooks for JDWP Frame access.
 * 
 * @author aluchko 
 */

public class VMFrame
{
  /**
   * Returns the size of a frame ID over JDWP
   */
  public static final int SIZE = 8;

  // The object this frame resides in
  private Object obj;
  
  // The current location of this frame
  private Location loc;
  
  // id of this frame
  private long id;
  
  /**
   * Gets the current location of the frame.
   */
  public Location getLocation()
  {
    return loc;
  }

  /**
   * Returns the value of the variable in the given slot.
   * 
   * @param slot the slot containing the variable
   */
  public native Object getValue(int slot);

  /**
   * Assigns the given variable to the given value. 
   * @param slot The slot which contains the variable
   * @param value The value to assign the variable to
   */
  public native void setValue(int slot, Object value);

  /**
   * Get the object which is represented by 'this' in the context of the frame,
   * returns null if the method is native or static.
   */
  public Object getObject()
  {
    return obj;
  }

  /**
   * Get the frameID
   * @return an id which is unique within the scope of the VM
   */
  public long getId()
  {
    return id;
  }

}
