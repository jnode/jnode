/* ServiceIdHelper.java --
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


package org.omg.IOP;

import gnu.CORBA.OrbRestricted;

import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.ORB;
import org.omg.CORBA.StructMember;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;

/**
* A helper operations for a context service id. A service Id is an integer
* constant and needs no helper, but the one is included to
* to facilitate the automated code handling.
*
* @specnote In this implementation, this class is not in use. Its "logical"
* place is the read/write methods of the ServiceContextHelper, to handle the
* first member (int) of the context record.
*
* @author Audrius Meskauskas, Lithuania (AudriusA@Bioinformatics.org)
*/
public abstract class ServiceIdHelper
{
  /**
   * Create the ServiceId typecode (alias of CORBA ulong, named "ServiceId".
   */
  public static TypeCode type()
  {
    ORB orb = OrbRestricted.Singleton;
    return orb.create_alias_tc("IDL:omg.org/IOP/ServiceId:1.0", "ServiceId",
                               orb.get_primitive_tc(TCKind.tk_ulong)
                              );
  }

  /**
  * Insert the int into the given Any.
  */
  public static void insert(Any any, int that)
  {
    any.insert_ulong(that);
  }

  /**
   * Extract the int from given Any.
   * This method uses the ServiceContextHolder.
   *
   * @throws BAD_OPERATION if the passed Any does not contain int.
   */
  public static int extract(Any any)
  {
    return any.extract_ulong();
  }

  /**
   * Get the int repository id.
   *
   * @return "IDL:omg.org/IOP/ServiceId:1.0", always.
   */
  public static String id()
  {
    return "IDL:omg.org/IOP/ServiceId:1.0";
  }

  /**
   * Read the ServiceId from the CDR intput stream.
   *
   * @param input a org.omg.CORBA.portable stream to read from.
   */
  public static int read(InputStream input)
  {
    return input.read_ulong();
  }

  /**
   * Write the ServiceId to the CDR output stream.
   *
   * @param output a org.omg.CORBA.portable stream stream to write into.
   * @param value a value to write.
   */
  public static void write(OutputStream output, int value)
  {
    output.write_ulong(value);
  }
}