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

package javax.sql.rowset.serial;

import java.sql.*;
import java.io.*;
import java.lang.reflect.*;


/**
 * A serialized mapping in the Java programming language of an SQL 
 * <code>BLOB</code> value. 
 * <P>
 * The <code>SerialBlob</code> class provides a constructor for creating
 * an instance from a <code>Blob</code> object.  Note that the 
 * <code>Blob</code>
 * object should have brought the SQL <code>BLOB</code> value's data over
 * to the client before a <code>SerialBlob</code> object
 * is constructed from it.  The data of an SQL <code>BLOB</code> value can
 * be materialized on the client as an array of bytes (using the method
 * <code>Blob.getBytes</code>) or as a stream of uninterpreted bytes
 * (using the method <code>Blob.getBinaryStream</code>).
 * <P>
 * <code>SerialBlob</code> methods make it possible to make a copy of a 
 * <code>SerialBlob</code> object as an array of bytes or as a stream.
 * They also make it possible to locate a given pattern of bytes or a
 * <code>Blob</code> object within a <code>SerialBlob</code> object
 * and to update or truncate a <code>Blob</code> object.
 *
 * @author Jonathan Bruce
 */
public class SerialBlob implements Blob, Serializable, Cloneable {
       
    /**	 
     * A serialized array of uninterpreted bytes representing the
     * value of this <code>SerialBlob</code> object.
     * @serial
     */
    private byte buf[];
    
    /**
     * The internal representation of the <code>Blob</code> object on which this
     * <code>SerialBlob</code> object is based.
     */
    private Blob blob;

    /**
     * The number of bytes in this <code>SerialBlob</code> object's
     * array of bytes.
     * @serial
     */
    private long len;
	
    /**
     * The orginal number of bytes in this <code>SerialBlob</code> object's
     * array of bytes when it was first established.
     * @serial
     */
    private long origLen;
        
    /**
     * Constructs a <code>SerialBlob</code> object that is a serialized version of
     * the given <code>byte</code> array.
     * <p>
     * The new <code>SerialBlob</code> object is initialized with the data from the
     * <code>byte</code> array, thus allowing disconnected <code>RowSet</code>
     * objects to establish serialized <code>Blob</code> objects without 
     * touching the data source.
     *
     * @param b the <code>byte</code> array containing the data for the 
     *        <code>Blob</code> object to be serialized
     * @throws SerialException if an error occurs during serialization
     * @throws SQLException if a SQL errors occurs
     */
    public SerialBlob(byte[] b) throws SerialException, SQLException {
    	
        len = b.length;                
        buf = new byte[(int)len];               
        for(int i = 0; i < len; i++) {
           buf[i] = b[i];
        }          
        origLen = len;
    }
    
       
    /** 	 
     * Constructs a <code>SerialBlob</code> object that is a serialized
     * version of the given <code>Blob</code> object. 
     * <P>
     * The new <code>SerialBlob</code> object is initialized with the
     * data from the <code>Blob</code> object; therefore, the 
     * <code>Blob</code> object should have previously brought the
     * SQL <code>BLOB</code> value's data over to the client from
     * the database. Otherwise, the new <code>SerialBlob</code> object
     * will contain no data.
     * 
     * @param blob the <code>Blob</code> object from which this
     *     <code>SerialBlob</code> object is to be constructed;
     *     cannot be null.
     * @throws SerialException if an error occurs during serialization
     * @throws SQLException if the <code>Blob</code> passed to this
     *     to this constructor is a <code>null</code>.
     * @see java.sql.Blob     
     */
    public SerialBlob (Blob blob) throws SerialException, SQLException {
    
        if (blob == null) {
            throw new SQLException("Cannot instantiate a SerialBlob " +
                 "object with a null Blob object");
        }
        
        len = blob.length();                       
        buf = blob.getBytes(1, (int)len );
        this.blob = blob;
        
         //if ( len < 10240000)
         // len = 10240000;          
        origLen = len;
    }
    
    /**
     * Copies the specified number of bytes, starting at the given
     * position, from this <code>SerialBlob</code> object to 
     * another array of bytes.
     * <P>
     * Note that if the given number of bytes to be copied is larger than
     * the length of this <code>SerialBlob</code> object's array of
     * bytes, the given number will be shortened to the array's length.
     *
     * @param pos the ordinal position of the first byte in this
     *            <code>SerialBlob</code> object to be copied;
     *            numbering starts at <code>1</code>; must not be less 
     *            than <code>1</code> and must be less than or equal
     *            to the length of this <code>SerialBlob</code> object
     * @param length the number of bytes to be copied 
     * @return an array of bytes that is a copy of a region of this
     *         <code>SerialBlob</code> object, starting at the given
     *         position and containing the given number of consecutive bytes
     * @throws SerialException if the given starting position is out of bounds
     */
    public byte[] getBytes(long pos, int length) throws SerialException {
        if (length > len) {
            length = (int)len;                
        }

        if (pos < 1 || length - pos < 0 ) {
            throw new SerialException("Invalid arguments: position cannot be less that 1");
        }      
        
        pos--; // correct pos to array index
      
        byte[] b = new byte[length];
        
        for (int i = 0; i < length; i++) {
            b[i] = this.buf[(int)pos];
            pos++;
        }
        return b;
    }
	
    /**
     * Retrieves the number of bytes in this <code>SerialBlob</code>
     * object's array of bytes.
     *
     * @return a <code>long</code> indicating the length in bytes of this
     *         <code>SerialBlob</code> object's array of bytes
     * @throws SerialException if an error occurs
     */
    public long length() throws SerialException {
        return len;
    }

    /**
     * Returns this <code>SerialBlob</code> object as an input stream.
     * Unlike the related method, <code>setBinaryStream</code>,
     * a stream is produced regardless of whether the <code>SerialBlob</code>
     * was created with a <code>Blob</code> object or a <code>byte</code> array.
     *
     * @return a <code>java.io.InputStream</code> object that contains
     *         this <code>SerialBlob</code> object's array of bytes
     * @throws SerialException if an error occurs     
     * @see #setBinaryStream
     */
    public java.io.InputStream getBinaryStream() throws SerialException {            
         InputStream stream = new ByteArrayInputStream(buf);
         return (java.io.InputStream)stream;
    }
	
    /** 	
     * Returns the position in this <code>SerialBlob</code> object where	
     * the given pattern of bytes begins, starting the search at the	
     * specified position.	
     *	
     * @param pattern the pattern of bytes for which to search 	
     * @param start the position of the byte in this	
     *              <code>SerialBlob</code> object from which to begin 	
     *              the search; the first position is <code>1</code>;	
     *              must not be less than <code>1</code> nor greater than	
     *              the length of this <code>SerialBlob</code> object	
     * @return the position in this <code>SerialBlob</code> object	
     *         where the given pattern begins, starting at the specified	
     *         position; <code>-1</code> if the pattern is not found
     *         or the given starting position is out of bounds; position
     *         numbering for the return value starts at <code>1</code>	
     * @throws SerialException if an error occurs when serializing the blob	
     * @throws SQLException if there is an error accessing the <code>BLOB</code>
     *         value from the database     
     */
    public long position(byte[] pattern, long start) 
                throws SerialException, SQLException {                
        if (start < 1 || start > len) {
            return -1;
        } 

        int pos = (int)start-1; // internally Blobs are stored as arrays. 
        int i = 0;        
        long patlen = pattern.length;                
        
        while (pos < len) {     
            if (pattern[i] == buf[pos]) {                
                if (i + 1 == patlen) {
                    return (pos + 1) - (patlen - 1);
                }
                i++; pos++; // increment pos, and i
            } else if (pattern[i] != buf[pos]) {
                pos++; // increment pos only
            }                                    
        }        
        return -1; // not found
    }
    	
    /** 	 
     * Returns the position in this <code>SerialBlob</code> object where	
     * the given <code>Blob</code> object begins, starting the search at the	
     * specified position.	
     *	
     * @param pattern the <code>Blob</code> object for which to search; 	
     * @param start the position of the byte in this	
     *              <code>SerialBlob</code> object from which to begin 	
     *              the search; the first position is <code>1</code>;	
     *              must not be less than <code>1</code> nor greater than	
     *              the length of this <code>SerialBlob</code> object	
     * @return the position in this <code>SerialBlob</code> object	
     *         where the given <code>Blob</code> object begins, starting 	
     *         at the specified position; <code>-1</code> if the pattern is 
     *         not found or the given starting position is out of bounds;	     
     *         position numbering for the return value starts at <code>1</code>     
     * @throws SerialException if an error occurs when serializing the blob
     * @throws SQLException if there is an error accessing the <code>BLOB</code>
     *         value from the database     
     */
    public long position(Blob pattern, long start) 
       throws SerialException, SQLException {
        return position(pattern.getBytes(1, (int)(pattern.length())), start);
    }
    
    /**
     * Writes the given array of bytes to the <code>BLOB</code> value that
     * this <code>Blob</code> object represents, starting at position 
     * <code>pos</code>, and returns the number of bytes written.
     *
     * @param pos the position in the SQL <code>BLOB</code> value at which
     *     to start writing. The first position is <code>1</code>;	
     *     must not be less than <code>1</code> nor greater than	
     *     the length of this <code>SerialBlob</code> object.
     * @param bytes the array of bytes to be written to the <code>BLOB</code>
     *        value that this <code>Blob</code> object represents
     * @return the number of bytes written
     * @throws SerialException if there is an error accessing the
     *     <code>BLOB</code> value; or if an invalid position is set; if an 
     *     invalid offset value is set
     * @throws SQLException if there is an error accessing the <code>BLOB</code>
     *         value from the database     
     * @see #getBytes
     */
    public int setBytes(long pos, byte[] bytes) 
        throws SerialException, SQLException {
        return (setBytes(pos, bytes, 0, bytes.length));
    }
    
    /**
     * Writes all or part of the given <code>byte</code> array to the
     * <code>BLOB</code> value that this <code>Blob</code> object represents
     * and returns the number of bytes written.
     * Writing starts at position <code>pos</code> in the <code>BLOB</code>
     * value; <i>len</i> bytes from the given byte array are written.
     *
     * @param pos the position in the <code>BLOB</code> object at which
     *     to start writing. The first position is <code>1</code>;	
     *     must not be less than <code>1</code> nor greater than	
     *     the length of this <code>SerialBlob</code> object.
     * @param bytes the array of bytes to be written to the <code>BLOB</code>
     *     value
     * @param offset the offset in the <code>byte</code> array at which
     *     to start reading the bytes. The first offset position is
     *     <code>0</code>; must not be less than <code>0</code> nor greater
     *     than the length of the <code>byte</code> array
     * @param length the number of bytes to be written to the 
     *     <code>BLOB</code> value from the array of bytes <i>bytes</i>.
     *        
     * @return the number of bytes written
     * @throws SerialException if there is an error accessing the 
     *     <code>BLOB</code> value; if an invalid position is set; if an 
     *     invalid offset value is set; if number of bytes to be written
     *     is greater than the <code>SerialBlob</code> length; or the combined
     *     values of the length and offset is greater than the Blob buffer
     * @throws SQLException if there is an error accessing the <code>BLOB</code>
     *         value from the database.     
     * @see #getBytes
     */
    public int setBytes(long pos, byte[] bytes, int offset, int length) 
        throws SerialException, SQLException {
        
        if (offset < 0 || offset > bytes.length) {
            throw new SerialException("Invalid offset in byte array set");
        }            
        
        if (pos < 1 || pos > this.length()) {
            throw new SerialException("Invalid position in BLOB object set");
        }
                    
        if ((long)(length) > origLen) {
	    throw new SerialException("Buffer is not sufficient to hold the value");
	}    
        
        if ((length + offset) > bytes.length) {  
            throw new SerialException("Invalid OffSet. Cannot have combined offset " +
                "and length that is greater that the Blob buffer");            
        }                                       
        
        int i = 0;
        pos--; // correct to array indexing
        while ( i < length || (offset + i +1) < (bytes.length-offset) ) {
            this.buf[(int)pos + i] = bytes[offset + i ]; 
            i++;
        }
        return i;        
    }
    
    /**
     * Retrieves a stream that can be used to write to the <code>BLOB</code> 
     * value that this <code>Blob</code> object represents.  The stream begins
     * at position <code>pos</code>. This method forwards the 
     * <code>setBinaryStream()</code> call to the underlying <code>Blob</code> in 
     * the event that this <code>SerialBlob</code> object is instantiated with a 
     * <code>Blob</code>. If this <code>SerialBlob</code> is instantiated with
     * a <code>byte</code> array, a <code>SerialException</code> is thrown.
     *
     * @param pos the position in the <code>BLOB</code> value at which
     *        to start writing
     * @return a <code>java.io.OutputStream</code> object to which data can 
     *         be written
     * @throws SQLException if there is an error accessing the
     *            <code>BLOB</code> value
     * @throws SerialException if the SerialBlob in not instantiated with a 
     *     <code>Blob</code> object that supports <code>setBinaryStream()</code>
     * @see #getBinaryStream
     */
    public java.io.OutputStream setBinaryStream(long pos) 
        throws SerialException, SQLException {       
        if (this.blob.setBinaryStream(pos) != null) {
            return this.blob.setBinaryStream(pos);
        } else {
            throw new SerialException("Unsupported operation. SerialBlob cannot " +
                "return a writable binary stream, unless instantiated with a Blob object " +
                "that provides a setBinaryStream() implementation");
        }        
    }
    
    /**
     * Truncates the <code>BLOB</code> value that this <code>Blob</code>
     * object represents to be <code>len</code> bytes in length.
     *
     * @param length the length, in bytes, to which the <code>BLOB</code>
     *        value that this <code>Blob</code> object represents should be 
     *        truncated
     * @throws SerialException if there is an error accessing the Blob value;
     *     or the length to truncate is greater that the SerialBlob length     
     */
    public void truncate(long length) throws SerialException {
         
         if (length > len) {
            throw new SerialException
               ("Length more than what can be truncated");
         } else if((int)length == 0) { 
              buf = new byte[0];
              len = length;
         } else {      
              len = length;              
              buf = this.getBytes(1, (int)len);
         }  
    }
    
    
    /**
     * Returns an <code>InputStream</code> object that contains a partial <code>Blob</code> value, 
     * starting  with the byte specified by pos, which is length bytes in length.
     *
     * @param pos the offset to the first byte of the partial value to be retrieved.
     *  The first byte in the <code>Blob</code> is at position 1
     * @param length the length in bytes of the partial value to be retrieved
     * @return <code>InputStream</code> through which the partial <code>Blob</code> value can be read.
     * @throws SQLException if pos is less than 1 or if pos is greater than the number of bytes
     * in the <code>Blob</code> or if pos + length is greater than the number of bytes 
     * in the <code>Blob</code>
     *
     * @since 1.6
     */
    public InputStream getBinaryStream(long pos,long length) throws SQLException {
        throw new java.lang.UnsupportedOperationException("Not supported");
    }


    /**
     * This method frees the <code>Blob</code> object and releases the resources that it holds.
     * <code>Blob</code> object. The object is invalid once the <code>free</code>
     * method is called. If <code>free</code> is called multiple times, the subsequent
     * calls to <code>free</code> are treated as a no-op.
     * 
     * @throws SQLException if an error occurs releasing 
     * the Blob's resources
     * @since 1.6
     */
    public void free() throws SQLException {
        throw new java.lang.UnsupportedOperationException("Not supported");
    }
    /**
	 * The identifier that assists in the serialization of this <code>SerialBlob</code>
     * object.
     */

    static final long serialVersionUID = -8144641928112860441L;
}
