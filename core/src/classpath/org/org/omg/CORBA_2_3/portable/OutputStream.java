/* OutputStream.java --
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


package org.omg.CORBA_2_3.portable;

import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.ValueBaseHelper;
import org.omg.CORBA.portable.BoxedValueHelper;
import org.omg.CORBA.portable.ValueBase;

import java.io.Serializable;

/**
 * This class defines a new CDR input stream methods, added since
 * CORBA 2.3.
 *
 * This class is abstract; no direct instances can be instantiated.
 * Also, up till v 1.4 inclusive there are no methods that would
 * return it directly.
 *
 * However since 1.3 all methods, declared as returning an
 * org.omg.CORBA.portable.InputStream actually return the instance of this
 * derived class and the new methods are accessible after the casting
 * operation.
 *
 * OMG specification states the writing format of the value types
 * is outside the scope of GIOP definition. This implementation uses
 * java serialization mechanism, calling {@link ObjectInputStream#readObject}.
 *
 * @author Audrius Meskauskas (AudriusA@Bioinformatics.org)
 */
public abstract class OutputStream
  extends org.omg.CORBA.portable.OutputStream
{
  /**
   * Writes an abstract interface to the stream. An abstract interface can
   * be eithe CORBA object or value type and is written as a union with
   * the boolean discriminator (false for objects, true for value types).
   *
   * The object from value is separated by fact that all values implement
   * the {@link ValueBase} interface. Also, the passed parameter is treated
   * as value it it does not implement CORBA Object.
   *
   * @param an_interface an abstract interface to write.
   */
  public void write_abstract_interface(java.lang.Object an_interface)
  {
    boolean isValue =
      an_interface instanceof ValueBase ||
      (!(an_interface instanceof org.omg.CORBA.Object));

    write_boolean(isValue);

    if (isValue)
      write_value((ValueBase) an_interface);
    else
      write_Object((org.omg.CORBA.Object) an_interface);
  }

  /**
   * Writes a value type into the output stream as java Serializable.
   *
   * The functionality is delegated to the {@link ValueBaseHelper}.
   *
   * @param value a value type object to write.
   */
  public void write_value(Serializable value)
  {
    ValueBaseHelper.write(this, value);
  }

  /**
   * Write value to the stream using the boxed value helper.
   *
   * @param value a value to write.
   * @param helper a helper, responsible for the writing operation.
   */
  public void write_value(Serializable value, BoxedValueHelper helper)
  {
    helper.write_value(this, value);
  }

  /**
   * Writes a value type into the output stream as java Serializable,
   * stating it is an instance of the given class.
   *
   * The functionality is delegated to the {@link ValueBaseHelper}.
   * The passed class is used for the check only.
   *
   * @param value a value type object to write.
   */
  public void write_value(Serializable value, Class clz)
  {
    if (!clz.isAssignableFrom(value.getClass()))
      throw new MARSHAL("The class is not the same");
    ValueBaseHelper.write(this, value);
  }

  /**
   * Writes a value type into the output stream as java Serializable,
   * stating it has the given repository id.
   *
   * The functionality is delegated to the {@link ValueBaseHelper}.
   *
   * @param repository_id a repository id of the value type.
   *
   * @param value a value type object to write.
   */
  public void write_value(Serializable value, String repository_id)
  {
    ValueBaseHelper.write(this, value);
  }
}