/*
 * Copyright 2001-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */
package javax.accessibility;

/**
  * Class AccessibleExtendedTable provides extended information about
  * a user-interface component that presents data in a two-dimensional
  * table format.
  * Applications can determine if an object supports the
  * AccessibleExtendedTable interface by first obtaining its
  * AccessibleContext and then calling the
  * {@link AccessibleContext#getAccessibleTable} method.
  * If the return value is not null and the type of the return value is
  * AccessibleExtendedTable, the object supports this interface.
  *
  * @author      Lynn Monsanto
  * @since 1.4
  */
public interface AccessibleExtendedTable extends AccessibleTable {

     /**
      * Returns the row number of an index in the table.
      *
      * @param index the zero-based index in the table.  The index is
      * the table cell offset from row == 0 and column == 0.
      * @return the zero-based row of the table if one exists;
      * otherwise -1.
      */
     public int getAccessibleRow(int index);

     /**
      * Returns the column number of an index in the table.
      *
      * @param index the zero-based index in the table.  The index is
      * the table cell offset from row == 0 and column == 0.
      * @return the zero-based column of the table if one exists;
      * otherwise -1.
      */
     public int getAccessibleColumn(int index);

     /*
      * Returns the index at a row and column in the table.
      *
      * @param r zero-based row of the table
      * @param c zero-based column of the table
      * @return the zero-based index in the table if one exists;
      * otherwise -1.  The index is  the table cell offset from
      * row == 0 and column == 0.
      */
     public int getAccessibleIndex(int r, int c);
}
