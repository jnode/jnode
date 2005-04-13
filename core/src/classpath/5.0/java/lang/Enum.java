/* Enum.java - Base class for all enums
   Copyright (C) 2004 Free Software Foundation

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
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

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

package java.lang;

import java.io.Serializable;

/**
 * @since 1.5
 */
public abstract class Enum<T extends Enum<T>>
  implements Comparable<T>, Serializable
{

  /**
   * For compatability with Sun's JDK
   */
  private static final long serialVersionUID = -4300926546619394005L;

  String name;
  int ordinal;

  protected Enum(String name, int ordinal)
  {
    this.name = name;
    this.ordinal = ordinal;
  }

  public static <S extends Enum<S>> Enum valueOf(Class<S> etype, String s)
  {
    if (etype == null || s == null)
      throw new NullPointerException();
    return null;		// FIXME
  }

  public final boolean equals(Object o)
  {
    // Enum constants are singular, so we need only compare `=='.
    return this == o;
  }

  public final int hashCode()
  {
    return ordinal;
  }

  public String toString()
  {
    return name;
  }

  public final int compareTo(T e)
  {
    return ordinal - e.ordinal;
  }

  protected final Object clone() throws CloneNotSupportedException
  {
    throw new CloneNotSupportedException("can't clone an enum constant");
  }

  public final String name()
  {
    return name;
  }

  public final int ordinal()
  {
    return ordinal;
  }

  public final Class<T> getDeclaringClass()
  {
    Class k = getClass();
    // We might be in an anonymous subclass of the enum class, so go
    // up one more level.
    if (k.getSuperclass() != Enum.class)
      k = k.getSuperclass();
    return k;
  }
}
