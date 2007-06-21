/*
 * Copyright 2003-2004 Sun Microsystems, Inc.  All Rights Reserved.
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

package javax.sql.rowset;

import javax.sql.*;
import java.sql.*;

/**
 * The standard interface that provides the framework for all 
 * <code>FilteredRowSet</code> objects to describe their filters.
 * <p>
 * <h3>1.0 Background</h3>
 * The <code>Predicate</code> interface is a standard interface that
 * applications can implement to define the filter they wish to apply to a 
 * a <code>FilteredRowSet</code> object. A <code>FilteredRowSet</code>
 * object consumes implementations of this interface and enforces the
 * constraints defined in the implementation of the method <code>evaluate</code>.
 * A <code>FilteredRowSet</code> object enforces the filter constraints in a 
 * bi-directional manner: It outputs only rows that are within
 * the constraints of the filter; and conversely, it inserts, modifies, or updates
 * only rows that are within the constraints of the filter.
 * 
 * <h3>2.0 Implementation Guidelines</h3>
 * In order to supply a predicate for the <code>FilteredRowSet</code>.
 * this interface must be implemented.  At this time, the JDBC RowSet 
 * Implementations (JSR-114) does not specify any standard filters definitions. 
 * By specifying a standard means and mechanism for a range of filters to be
 * defined and deployed with both the reference and vendor implementations 
 * of the <code>FilteredRowSet</code> interface, this allows for a flexible
 * and application motivated implementations of <code>Predicate</code> to emerge.
 * <p>
 * A sample implementation would look something like this:
 * <pre>
 * <code>
 *    public class Range implements Predicate {
 *
 *       private Object lo[];
 *       private Object hi[];
 *       private int idx[];
 *
 *       public Range(Object[] lo, Object[] hi, int[] idx) {
 *          this.lo = lo;
 *          this.hi = hi;
 *          this.idx = idx;
 *       }
 *
 *      public boolean evaluate(RowSet rs) {
 *          CachedRowSet crs = (CachedRowSet)rs;
 *          boolean bool1,bool2;           
 *       
 *          // Check the present row determine if it lies
 *          // within the filtering criteria. 
 *      
 *          for (int i = 0; i < idx.length; i++) {
 *        
 *	        if ((rs.getObject(idx[i]) >= lo[i]) && 
 *                  (rs.getObject(idx[i]) >= hi[i]) { 
 *	            bool1 = true; // within filter constraints
 *          } else {
 *            bool2 = true; // outside of filter constraints             
 *          } 
 *      }
 *      
 *      if (bool2) {
 *         return false;
 *      } else {
 *         return true;
 *      }
 *  }          
 * </code>
 * </pre>
 * <P>
 * The example above implements a simple range predicate. Note, that 
 * implementations should but are not required to provider <code>String</code>
 * and integer index based constructors to provide for JDBC RowSet Implementation
 * applications that use both column identification conventions.
 *
 * @author Jonathan Bruce, Amit Handa
 *
 */

 // <h3>3.0 FilteredRowSet Internals</h3>
 // internalNext, Frist, Last. Discuss guidelines on how to approach this
 // and cite examples in reference implementations.
public interface Predicate {
    /**
     * This method is typically called a <code>FilteredRowSet</code> object
     * internal methods (not public) that control the <code>RowSet</code> object's
     * cursor moving  from row to the next. In addition, if this internal method 
     * moves the cursor onto a row that has been deleted, the internal method will 
     * continue to ove the cursor until a valid row is found. 
     *
     * @return <code>true</code> if there are more rows in the filter; 
     *     <code>false</code> otherwise
     */
    public boolean evaluate(RowSet rs);
    
    
    /**
     * This method is called by a <code>FilteredRowSet</code> object
     * to check whether the value lies between the filtering criterion (or criteria
     * if multiple constraints exist) set using the <code>setFilter()</code> method.
     * <P>
     * The <code>FilteredRowSet</code> object will use this method internally
     * while inserting new rows to a <code>FilteredRowSet</code> instance.
     *
     * @param value An <code>Object</code> value which needs to be checked,
     *        whether it can be part of this <code>FilterRowSet</code> object.
     * @param column a <code>int</code> object that must match the
     *        SQL index of a column in this <code>RowSet</code> object. This must 
     *        have been passed to <code>Predicate</code> as one of the columns 
     *        for filtering while initializing a <code>Predicate</code>
     * @return <code>true</code> ifrow value lies within the filter; 
     *     <code>false</code> otherwise
     * @throws SQLException if the column is not part of filtering criteria 
     */
    public boolean evaluate(Object value, int column) throws SQLException;

    /**
     * This method is called by the <code>FilteredRowSet</code> object
     * to check whether the value lies between the filtering criteria set 
     * using the setFilter method.
     * <P>
     * The <code>FilteredRowSet</code> object will use this method internally
     * while inserting new rows to a <code>FilteredRowSet</code> instance.
     *
     * @param value An <code>Object</code> value which needs to be checked,
     * whether it can be part of this <code>FilterRowSet</code>.
     *
     * @param columnName a <code>String</code> object that must match the
     *        SQL name of a column in this <code>RowSet</code>, ignoring case. This must 
     *        have been passed to <code>Predicate</code> as one of the columns for filtering
     *        while initializing a <code>Predicate</code>
     *
     * @return <code>true</code> if value lies within the filter; <code>false</code> otherwise
     * 
     * @throws SQLException if the column is not part of filtering criteria 
     */
    public boolean evaluate(Object value, String columnName) throws SQLException;
    
}
