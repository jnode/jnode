/*
 * Copyright 2003 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.sql.*;
import java.io.*;

/**
 * The abstract base class from which the classes <code>Row</code> 
 * The class <code>BaseRow</code> stores
 * a row's original values as an array of <code>Object</code>
 * values, which can be retrieved with the method <code>getOrigRow</code>.
 * This class also provides methods for getting and setting individual
 * values in the row.
 * <P>
 * A row's original values are the values it contained before it was last
 * modified.  For example, when the <code>CachedRowSet</code>method 
 * <code>acceptChanges</code> is called, it will reset a row's original
 * values to be the row's current values.  Then, when the row is modified, 
 * the values that were previously the current values will become the row's
 * original values (the values the row had immediately before it was modified).
 * If a row has not been modified, its original values are its initial values.
 * <P> 
 * Subclasses of this class contain more specific details, such as
 * the conditions under which an exception is thrown or the bounds for
 * index parameters.
 */
public abstract class BaseRow implements Serializable, Cloneable {

/**
 * The array containing the original values for this <code>BaseRow</code>
 * object.
 * @serial
 */
    protected Object[] origVals;

/**
 * Retrieves the values that this row contained immediately 
 * prior to its last modification.
 *
 * @return an array of <code>Object</code> values containing this row's
 * original values
 */
    public Object[] getOrigRow() {
        return origVals;
    }

/**
 * Retrieves the array element at the given index, which is
 * the original value of column number <i>idx</i> in this row.
 *
 * @param idx the index of the element to return
 * @return the <code>Object</code> value at the given index into this
 *         row's array of original values 
 * @throws <code>SQLException</code> if there is an error
 */
    public abstract Object getColumnObject(int idx) throws SQLException;

/**
 * Sets the element at the given index into this row's array of
 * original values to the given value.  Implementations of the classes
 * <code>Row</code> and determine what happens
 * when the cursor is on the insert row and when it is on any other row.
 *
 * @param idx the index of the element to be set
 * @param obj the <code>Object</code> to which the element at index 
 *		<code>idx</code> to be set
 * @throws <code>SQLException</code> if there is an error
 */
    public abstract void setColumnObject(int idx, Object obj) throws SQLException;
}
