/* NameDynAnyPairHolder.java --
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


package gnu.CORBA;

import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.Streamable;
import org.omg.DynamicAny.NameDynAnyPair;
import org.omg.DynamicAny.NameDynAnyPairHelper;

/**
 * A holder for the structure {@link NameDynAnyPair}.
 *
 * @author Audrius Meskauskas, Lithuania (AudriusA@Bioinformatics.org)
 */
public class NameDynAnyPairHolder
  implements Streamable
{
  /**
   * The stored NameDynAnyPair value.
   */
  public NameDynAnyPair value;

  /**
   * Create the unitialised instance, leaving the value field
   * with default <code>null</code> value.
   */
  public NameDynAnyPairHolder()
  {
  }

  /**
   * Create the initialised instance.
   * @param initialValue the value that will be assigned to
   * the <code>value</code> field.
   */
  public NameDynAnyPairHolder(NameDynAnyPair initialValue)
  {
    value = initialValue;
  }

  /**
   * The method should read this object from the CDR input stream, but
   * (following the JDK 1.5 API) it does not.
   *
   * @param input a org.omg.CORBA.portable stream to read from.
   *
   * @specenote Sun throws the same exception.
   *
   * @throws MARSHAL always.
   */
  public void _read(InputStream input)
  {
    value = NameDynAnyPairHelper.read(input);
  }

  /**
   * The method should write this object to the CDR input stream, but
   * (following the JDK 1.5 API) it does not.
   *
   * @param input a org.omg.CORBA.portable stream to read from.
   *
   * @specenote Sun throws the same exception.
   *
   * @throws MARSHAL always.
   */
  public void _write(OutputStream output)
  {
    NameDynAnyPairHelper.write(output, value);
  }

  /**
   * Get the typecode of the NameDynAnyPair.
   */
  public org.omg.CORBA.TypeCode _type()
  {
    return NameDynAnyPairHelper.type();
  }
}