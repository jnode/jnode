/* TypeCode.java --
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


package org.omg.CORBA;

/**
 * An information about a CORBA data type.
 * @author Audrius Meskauskas (AudriusA@Bioinformatics.org)
 */
import java.io.Serializable;

import org.omg.CORBA.TypeCodePackage.BadKind;

public abstract class TypeCode
  implements IDLEntity, Serializable
{
  /**
   * Returns the concrete base type for this TypeCode.
   * @return a TypeCode, defining the concrete base type for this
   * Typecode.
   * @throws org.omg.CORBA.TypeCodePackage.BadKind
   */
  public abstract TypeCode concrete_base_type()
                                       throws BadKind;

  /**
   * For sequences, arrays, aliases and value boxes, returns the IDL type for
   * the members of the object.
   * @return a TypeCode of the memebers of this type.
   * @throws org.omg.CORBA.TypeCodePackage.BadKind for types other than
   * sequences, arrays, aliases and value boxes.
   */
  public abstract TypeCode content_type()
                                 throws BadKind;

  /**
   * For unions, returs the index of the default member.
   * @return the index of the default member, -1 if there is
   * no default member.
   * @throws org.omg.CORBA.TypeCodePackage.BadKind if this type is not
   * a union.
   */
  public abstract int default_index()
                             throws BadKind;

  /**
   * Returs definition of member labels for untions
   * @return a TypeCode, describing all non-default member labels.
   * @throws org.omg.CORBA.TypeCodePackage.BadKind if this type is not a
   * union.
   */
  public abstract TypeCode discriminator_type()
                                       throws BadKind;

  /**
   * Test two types for equality. The default implementation
   * returs true of the types of the same kind.
   * @param other the other type to compere with
   * @return true if the types are interchangeable.
   */
  public abstract boolean equal(TypeCode other);

  /**
   * Following the current 1.4 API specifcation, this should just throw
   * NO_IMPLEMENT.
   * @throws org.omg.CORBA.NO_IMPLEMENT, always.
   */
  public abstract boolean equivalent(TypeCode other);

  /**
   * For the fixed type, returns the number of digits.
   * @return the number of digits for the fixed type
   * @throws org.omg.CORBA.TypeCodePackage.BadKind if this is not a fixed
   * type.
   */
  public abstract short fixed_digits()
                              throws BadKind;

  /**
   * Returns the scale for the fixed type. The returned value can be either
   * positive (the number of digits to the right of the decimal point) or
   * negative (adds zeros to the left of the decimal point).
   * @return the scale.
   * @throws org.omg.CORBA.TypeCodePackage.BadKind if this is not a fixed
   * type.
   */
  public abstract short fixed_scale()
                             throws BadKind;

  /**
   * Returns a version of this instance without the optional memeber and
   * member name fields.
   * @return the truncated version.
   */
  public abstract TypeCode get_compact_typecode();

  /**
   * Returns the RepositoryId globally identifying the type, defined by
   * this TypeCode.
   * @return tje RepositoryId. In some cases, it may be an empty string.
   * @throws org.omg.CORBA.TypeCodePackage.BadKind if the type is other than
   * reference, structure, union, enumeration, alias, exception, valuetype,
   * boxed valuetype and also native and abstract interfaces.
   */
  public abstract String id()
                     throws BadKind;

  /**
   * Return the kind of this type code object.
   * @return one of the <code>TCKind.t_..</code> fields.
   */
  public abstract TCKind kind();

  /**
   * Returns the number of elements in the type. For arrays, this
   * method returns the length of the array. For strings and sequences,
   * it returns the bound of the type, zero indicating the unbounded
   * type.
   *
   * @return length or bound
   *
   * @throws org.omg.CORBA.TypeCodePackage.BadKind for types other than
   * string, sequence and array.
   */
  public abstract int length()
                      throws BadKind;

  /**
   * Returns the number of type memebers.
   *
   * @return the number of memebers
   * @throws org.omg.CORBA.TypeCodePackage.BadKind for types other than
   * structure, union, enumeration or exception.
   */
  public abstract int member_count()
                            throws BadKind;

  /**
   * Retrieves the label of the union member at the given index.
   * For the default member, this label is the zero octet.
   *
   * @param index the index of the union memeber.
   *
   * @return the label
   *
   * @throws org.omg.CORBA.TypeCodePackage.BadKind if this is not a union
   * type.
   * @throws org.omg.CORBA.TypeCodePackage.Bounds if the index is out of
   * valid bounds.
   */
  /* Disabled due to build breakage -- Andrew John Hughes -- 04/03/2005
  public abstract Any member_label(int index)
                            throws BadKind, Bounds;
  */
  /**
   * Retrieves the simple name of the member identified by the given index.
   *
   * @param index the index of the memeber.
   *
   * @return the member name that in some cases can be an empty string.
   *
   * @throws org.omg.CORBA.TypeCodePackage.BadKind for types other than
   * structure, union or enumeration.
   * @throws org.omg.CORBA.TypeCodePackage.Bounds if the index is out of
   * valid bounds.
   */
  public abstract String member_name(int index)
                              throws BadKind, Bounds;

  /**
   * Retrieves the member type of the member identified by the given index.
   *
   * @param index the index of the memeber.
   *
   * @return the member type.
   *
   * @throws org.omg.CORBA.TypeCodePackage.BadKind for types other than
   * structure, union, enumeration or exception.
   * @throws org.omg.CORBA.TypeCodePackage.Bounds if the index is out of
   * valid bounds.
   */
  public abstract TypeCode member_type(int index)
                                throws BadKind, Bounds;

  /**
   * Returns the visibility scope of the member at the given index.
   * This operation can only be invoked on non-boxed value types.
   *
   * @param index the index of the member
   *
   * @return either PRIVATE_MEMBER.value or PUBLIC_MEMBER.value
   *
   * @throws org.omg.CORBA.TypeCodePackage.BadKind if this is not a non boxed 
   * value type.
   *
   * @throws org.omg.CORBA.TypeCodePackage.Bounds if the index is out of
   * valid bounds.
   */
  public abstract short member_visibility(int index)
                                   throws BadKind, Bounds;

  /**
   * Retrieves the simple name identifying this TypeCode object
   * within its enclosing scope.
   * @return the name, can be an empty string.
   * @throws org.omg.CORBA.TypeCodePackage.BadKind for typer other than
   * reference, structure, union, enumeration, alias, exception,
   * valuetype, boxed valuetype, native, and abstract interface
   */
  public abstract String name()
                       throws BadKind;

  /**
   * Returns a constant indicating the modifier of the value type.
   *
   * @return one of the following constants:
   * VM_NONE.value, VM_ABSTRACT.value, VM_CUSTOM.value, or
   * VM_TRUNCATABLE.value,
   *
   * @throws org.omg.CORBA.TypeCodePackage.BadKind for types other than
   * value type.
   */
  public abstract short type_modifier()
                               throws BadKind;
}
