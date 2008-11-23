/*
 * Copyright 2003-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.rowset.internal;

import com.sun.rowset.JdbcRowSetResourceBundle;
import java.sql.*;
import javax.sql.*;
import java.io.*;
import java.util.*;

/**
 * A class used internally to manage a <code>CachedRowSet</code> object's
 * insert row.  This class keeps track of the number of columns in the
 * insert row and which columns have had a value inserted.  It provides
 * methods for retrieving a column value, setting a column value, and finding
 * out whether the insert row is complete.
 */
public class InsertRow extends BaseRow implements Serializable, Cloneable {
 
/**
 * An internal <code>BitSet</code> object used to keep track of the
 * columns in this <code>InsertRow</code> object that have had a value
 * inserted.
 */
    private BitSet colsInserted;

/**
 * The number of columns in this <code>InsertRow</code> object.
 */
    private int cols;

    private JdbcRowSetResourceBundle resBundle;

/**
 * Creates an <code>InsertRow</code> object initialized with the
 * given number of columns, an array for keeping track of the
 * original values in this insert row, and a 
 * <code>BitSet</code> object with the same number of bits as 
 * there are columns. 
 *
 * @param numCols an <code>int</code> indicating the number of columns
 *                in this <code>InsertRow</code> object
 */
    public InsertRow(int numCols) {
        origVals = new Object[numCols];
        colsInserted = new BitSet(numCols);
        cols = numCols;
        try {
           resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

/**
 * Sets the bit in this <code>InsertRow</code> object's internal
 * <code>BitSet</code> object that corresponds to the specified column
 * in this <code>InsertRow</code> object. Setting a bit indicates
 * that a value has been set.
 *
 * @param col the number of the column to be marked as inserted;
 *            the first column is <code>1</code>
 */
    protected void markColInserted(int col) {
        colsInserted.set(col);
    }

/**
 * Indicates whether this <code>InsertRow</code> object has a value
 * for every column that cannot be null.
 * @param RowSetMD the <code>RowSetMetaData</code> object for the
 *                 <code>CachedRowSet</code> object that maintains this
 *                 <code>InsertRow</code> object
 * @return <code>true</code> if this <code>InsertRow</code> object is
 *         complete; <code>false</code> otherwise
 * @throws SQLException if there is an error accessing data
 */
    public boolean isCompleteRow(RowSetMetaData RowSetMD) throws SQLException {
        for (int i = 0; i < cols; i++) {
            if (colsInserted.get(i) == false && 
                RowSetMD.isNullable(i + 1) == 
                ResultSetMetaData.columnNoNulls) {
                return false;
            }

        }
        return true;
    }

/**
 * Clears all the bits in the internal <code>BitSet</code> object
 * maintained by this <code>InsertRow</code> object.  Clearing all the bits
 * indicates that none of the columns have had a value inserted.
 */
    public void initInsertRow() {
        for (int i = 0; i < cols; i++) {
            colsInserted.clear(i);
        }
    }

/**
 * Retrieves the value of the designated column in this
 * <code>InsertRow</code> object.  If no value has been inserted
 * into the designated column, this method throws an
 * <code>SQLException</code>.
 *
 * @param idx the column number of the value to be retrieved;
 *            the first column is <code>1</code>
 * @throws SQLException if no value has been inserted into
 *                                   the designated column
 */
    public Object getColumnObject(int idx) throws SQLException {
        if (colsInserted.get(idx - 1) == false) {
            throw new SQLException(resBundle.handleGetObject("insertrow.novalue").toString());
        }
        return (origVals[idx - 1]);
    }

/**
 * Sets the element in this <code>InsertRow</code> object's
 * internal array of original values that corresponds to the
 * designated column with the given value.  If the third 
 * argument is <code>true</code>,
 * which means that the cursor is on the insert row, this
 * <code>InsertRow</code> object's internal <code>BitSet</code> object
 * is set so that the bit corresponding to the column being set is
 * turned on.
 *
 * @param idx the number of the column in the insert row to be set;
 *              the first column is <code>1</code>
 * @param val the value to be set
 */
    public void setColumnObject(int idx, Object val) {
        origVals[idx - 1] = val;
	markColInserted(idx - 1);
    }
}
