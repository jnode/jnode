/* SQLData.java -- Custom mapping for a user defined datatype
   Copyright (C) 1999, 2000, 2006 Free Software Foundation, Inc.

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


package java.sql;

/**
 * This interface is used for mapping SQL data to user defined datatypes.
 *
 * @author Aaron M. Renn (arenn@urbanophile.com)
 */
public interface SQLData 
{
  /**
   * This method returns the user defined datatype name for this object.
   *
   * @return The user defined data type name for this object.
   * @exception SQLException If an error occurs.
   */
  String getSQLTypeName() throws SQLException;

  /**
   * This method populates the data in the object from the specified stream.
   *
   * @param stream The stream to read the data from.
   * @param typeName The data type name of the data on the stream.
   * @exception SQLException If an error occurs.
   */
  void readSQL(SQLInput stream, String typeName) throws SQLException;

  /**
   * This method writes the data in this object to the specified stream.
   *
   * @param stream The stream to write the data to.
   * @exception SQLException If an error occurs.
   */
  void writeSQL(SQLOutput stream) throws SQLException;
}
