/* NumberUpDefault.java -- 
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

package gnu.javax.print.ipp.attribute.defaults;

import gnu.javax.print.ipp.attribute.DefaultValueAttribute;

import javax.print.attribute.Attribute;
import javax.print.attribute.IntegerSyntax;
import javax.print.attribute.standard.NumberUp;

/**
 * NumberUpDefault attribute provides the default value of
 * the numper up attribute.
 * 
 * @author Wolfgang Baer (WBaer@gmx.de)
 */
public final class NumberUpDefault extends IntegerSyntax
  implements DefaultValueAttribute
{
    
  /**
   * Creates a <code>NumberUpDefault</code> object.
   *
   * @param value the value
   * @throws IllegalArgumentException if value &lt; 1
   */
  public NumberUpDefault(int value)
  {
    super(value);
  }
  
  /**
   * Tests if the given object is equal to this object.
   *
   * @param obj the object to test
   *
   * @return <code>true</code> if both objects are equal, 
   * <code>false</code> otherwise.
   */
  public boolean equals(Object obj)
  {
    if(! (obj instanceof NumberUpDefault))
      return false;

    return super.equals(obj);
  }

  /**
   * Returns category of this class.
   *
   * @return The class <code>NumberUpDefault</code> itself.
   */
  public Class getCategory()
  {
    return NumberUpDefault.class;
  }

  /**
   * Returns name of this class.
   *
   * @return The name "number-up-default".
   */
  public String getName()
  {
    return "number-up-default";
  }
  
  /**
   * Returns the equally enum of the standard attribute class
   * of this DefaultValuesAttribute enum.
   * <p>May return null if no value exists in JPS API.</p>
   * 
   * @return The enum of the standard attribute class.
   */
  public Attribute getAssociatedAttribute() 
  {
    return new NumberUp(getValue());
  }
}
