/* CharBuffer.java -- 
   Copyright (C) 2002, 2003, 2004  Free Software Foundation, Inc.

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


package java.nio;

import java.io.IOException;

/**
 * @since 1.4
 */
public abstract class CharBuffer extends Buffer
  implements Comparable, CharSequence, Readable, Appendable
{
  int array_offset;
  char[] backing_buffer;

  CharBuffer (int capacity, int limit, int position, int mark)
  {
    super (capacity, limit, position, mark);
    array_offset = 0;
  }

  /**
   * Allocates a new <code>CharBuffer</code> object with a given capacity.
   */
  public static CharBuffer allocate (int capacity)
  {
    return new CharBufferImpl (capacity);
  }
  
  /**
   * Wraps a <code>char</code> array into a <code>CharBuffer</code>
   * object.
   *
   * @param array the array to wrap
   * @param offset the offset of the region in the array to wrap
   * @param length the length of the region in the array to wrap
   *
   * @return a new <code>CharBuffer</code> object
   * 
   * @exception IndexOutOfBoundsException If the preconditions on the offset
   * and length parameters do not hold
   */
  public static final CharBuffer wrap(char[] array, int offset, int length)
  {
    return new CharBufferImpl(array, 0, array.length, offset + length, offset, -1, false);
  }

  /**
   * Wraps a character sequence into a <code>CharBuffer</code> object.
   *
   * @param seq the sequence to wrap
   *
   * @return a new <code>CharBuffer</code> object
   */
  public static final CharBuffer wrap(CharSequence seq)
  {
    return wrap(seq, 0, seq.length());
  }
  
  /**
   * Wraps a character sequence into a <code>CharBuffer</code> object.
   * 
   * @param seq the sequence to wrap
   * @param start the index of the first character to wrap
   * @param end the index of the first character not to wrap
   *
   * @return a new <code>CharBuffer</code> object
   * 
   * @exception IndexOutOfBoundsException If the preconditions on the offset
   * and length parameters do not hold
   */
  public static final CharBuffer wrap(CharSequence seq, int start, int end)
  {
    // FIXME: implement better handling of java.lang.String.
    // Probably share data with String via reflection.
	  
    if ((start < 0)
        || (start > seq.length())
        || (end < start)
        || (end > (seq.length() - start)))
      throw new IndexOutOfBoundsException();
    
    int len = end - start;
    char[] buffer = new char[len];
    
    for (int i = 0; i < len; i++)
      buffer[i] = seq.charAt(i + start);
    
    return wrap(buffer, 0, len).asReadOnlyBuffer();
  }
  
  /**
   * Wraps a <code>char</code> array into a <code>CharBuffer</code>
   * object.
   *
   * @param array the array to wrap
   *
   * @return a new <code>CharBuffer</code> object
   */
  public static final CharBuffer wrap(char[] array)
  {
    return wrap(array, 0, array.length);
  }
 
  /**
   * This method transfers <code>char</code>s from this buffer into the given
   * destination array. Before the transfer, it checks if there are fewer than
   * length <code>char</code>s remaining in this buffer. 
   *
   * @param dst The destination array
   * @param offset The offset within the array of the first <code>char</code>
   * to be written; must be non-negative and no larger than dst.length.
   * @param length The maximum number of bytes to be written to the given array;
   * must be non-negative and no larger than dst.length - offset.
   *
   * @exception BufferUnderflowException If there are fewer than length
   * <code>char</code>s remaining in this buffer.
   * @exception IndexOutOfBoundsException If the preconditions on the offset
   * and length parameters do not hold.
   */
  public CharBuffer get (char[] dst, int offset, int length)
  {
    checkArraySize(dst.length, offset, length);
    checkForUnderflow(length);

    for (int i = offset; i < offset + length; i++)
      {
      dst [i] = get ();
      }
    
    return this;
  }

  /**
   * This method transfers <code>char</code>s from this buffer into the given
   * destination array.
   *
   * @param dst The byte array to write into.
   *
   * @exception BufferUnderflowException If there are fewer than dst.length
   * <code>char</code>s remaining in this buffer.
   */
  public CharBuffer get (char[] dst)
  {
    return get (dst, 0, dst.length);
  }
 
  /**
   * Writes the content of the the <code>CharBUFFER</code> src
   * into the buffer. Before the transfer, it checks if there is fewer than
   * <code>src.remaining()</code> space remaining in this buffer.
   *
   * @param src The source data.
   *
   * @exception BufferOverflowException If there is insufficient space in this
   * buffer for the remaining <code>char</code>s in the source buffer.
   * @exception IllegalArgumentException If the source buffer is this buffer.
   * @exception ReadOnlyBufferException If this buffer is read-only.
   */
  public CharBuffer put (CharBuffer src)
  {
    if (src == this)
      throw new IllegalArgumentException ();

    checkForOverflow(src.remaining());

    if (src.remaining () > 0)
      {
        char[] toPut = new char [src.remaining ()];
        src.get (toPut);
	put (toPut);
      }

    return this;
  }
 
  /**
   * Writes the content of the the <code>char array</code> src
   * into the buffer. Before the transfer, it checks if there is fewer than
   * length space remaining in this buffer.
   *
   * @param src The array to copy into the buffer.
   * @param offset The offset within the array of the first byte to be read;
   * must be non-negative and no larger than src.length.
   * @param length The number of bytes to be read from the given array;
   * must be non-negative and no larger than src.length - offset.
   *
   * @exception BufferOverflowException If there is insufficient space in this
   * buffer for the remaining <code>char</code>s in the source array.
   * @exception IndexOutOfBoundsException If the preconditions on the offset
   * and length parameters do not hold
   * @exception ReadOnlyBufferException If this buffer is read-only.
   */
  public CharBuffer put (char[] src, int offset, int length)
  {
    checkArraySize(src.length, offset, length);
    checkForOverflow(length);
    
    for (int i = offset; i < offset + length; i++)
      put (src [i]);
    
    return this;
  }

  /**
   * Writes the content of the the <code>char array</code> src
   * into the buffer.
   *
   * @param src The array to copy into the buffer.
   *
   * @exception BufferOverflowException If there is insufficient space in this
   * buffer for the remaining <code>char</code>s in the source array.
   * @exception ReadOnlyBufferException If this buffer is read-only.
   */
  public final CharBuffer put (char[] src)
  {
    return put (src, 0, src.length);
  }

  /**
   * Tells whether ot not this buffer is backed by an accessible
   * <code>char</code> array.
   */
  public final boolean hasArray ()
  {
    return (backing_buffer != null
            && !isReadOnly ());
  }

  /**
   * Returns the <code>char</code> array that backs this buffer.
   *
   * @exception ReadOnlyBufferException If this buffer is read-only.
   * @exception UnsupportedOperationException If this buffer is not backed
   * by an accessible array.
   */
  public final char[] array ()
  {
    if (backing_buffer == null)
      throw new UnsupportedOperationException ();

    checkIfReadOnly();

    return backing_buffer;
  }

  /**
   * Returns the offset within this buffer's backing array of the first element.
   *
   * @exception ReadOnlyBufferException If this buffer is read-only.
   * @exception UnsupportedOperationException If this buffer is not backed
   * by an accessible array.
   */  
  public final int arrayOffset ()
  {
    if (backing_buffer == null)
      throw new UnsupportedOperationException ();

    checkIfReadOnly();

    return array_offset;
  }
  
  /**
   * Calculates a hash code for this buffer.
   *
   * This is done with int arithmetic,
   * where ** represents exponentiation, by this formula:<br>
   * <code>s[position()] + 31 + (s[position()+1] + 30)*31**1 + ... +
   * (s[limit()-1]+30)*31**(limit()-1)</code>.
   * Where s is the buffer data. Note that the hashcode is dependent
   * on buffer content, and therefore is not useful if the buffer
   * content may change.
   */
  public int hashCode ()
  {
    int hashCode = get(position()) + 31;
    int multiplier = 1;
    for (int i = position() + 1; i < limit(); ++i)
      {
	  multiplier *= 31;
	  hashCode += (get(i) + 30)*multiplier;
      }
    return hashCode;
  }
  
  /**
   * Checks if this buffer is equal to obj.
   */
  public boolean equals (Object obj)
  {
    if (obj instanceof CharBuffer)
      {
      return compareTo (obj) == 0;
  }

    return false;
  }

  /**
   * Compares two <code>CharBuffer</code> objects.
   *
   * @exception ClassCastException If obj is not an object derived from
   * <code>CharBuffer</code>.
   */
  public int compareTo (Object obj)
  {
    CharBuffer other = (CharBuffer) obj;

    int num = Math.min(remaining(), other.remaining());
    int pos_this = position();
    int pos_other = other.position();

    for (int count = 0; count < num; count++)
      {
	 char a = get(pos_this++);
	 char b = other.get(pos_other++);

	 if (a == b)
	   continue;

	 if (a < b)
	   return -1;

      return 1;
      }
      
     return remaining() - other.remaining();
  }

  /**
   * Returns the byte order of this buffer.
   */
  public abstract ByteOrder order ();
  
  /**
   * Reads the <code>char</code> at this buffer's current position,
   * and then increments the position.
   *
   * @exception BufferUnderflowException If there are no remaining
   * <code>char</code>s in this buffer.
   */
  public abstract char get ();
  
  /**
   * Writes the <code>char</code> at this buffer's current position,
   * and then increments the position.
   *
   * @exception BufferOverflowException If there no remaining 
   * <code>char</code>s in this buffer.
   * @exception ReadOnlyBufferException If this buffer is read-only.
   */
  public abstract CharBuffer put (char b);
  
  /**
   * Absolute get method.
   *
   * @exception IndexOutOfBoundsException If index is negative or not smaller
   * than the buffer's limit.
   */
  public abstract char get (int index);

  /**
   * Absolute put method.
   *
   * @exception IndexOutOfBoundsException If index is negative or not smaller
   * than the buffer's limit.
   * @exception ReadOnlyBufferException If this buffer is read-only.
   */
  public abstract CharBuffer put (int index, char b);

  /**
   * Compacts this buffer.
   *
   * @exception ReadOnlyBufferException If this buffer is read-only.
   */
  public abstract CharBuffer compact ();

  /**
   * Tells wether or not this buffer is direct.
   */
  public abstract boolean isDirect ();

  /**
   * Creates a new <code>CharBuffer</code> whose content is a shared
   * subsequence of this buffer's content.
   */
  public abstract CharBuffer slice ();

  /**
   * Creates a new <code>CharBuffer</code> that shares this buffer's
   * content.
   */
  public abstract CharBuffer duplicate ();

  /**
   * Creates a new read-only <code>CharBuffer</code> that shares this
   * buffer's content.
   */
  public abstract CharBuffer asReadOnlyBuffer ();
  
  /**
   * Returns the remaining content of the buffer as a string.
   */
  public String toString ()
  {
    if (hasArray ())
      return new String (array (), position (), length ());

    char[] buf = new char [length ()];
    int pos = position ();
    get (buf, 0, buf.length);
    position (pos);
    return new String (buf);
  }

  /**
   * Returns the length of the remaining chars in this buffer.
   */
  public final int length ()
  { 
    return remaining ();
  }

  /**
   * Creates a new character buffer that represents the specified subsequence
   * of this buffer, relative to the current position.
   *
   * @exception IndexOutOfBoundsException If the preconditions on start and
   * end do not hold.
   */
  public abstract CharSequence subSequence (int start, int length);

  /**
   * Relative put method.
   * 
   * @exception BufferOverflowException If there is insufficient space in this
   * buffer.
   * @exception IndexOutOfBoundsException If the preconditions on the start
   * and end parameters do not hold.
   * @exception ReadOnlyBufferException If this buffer is read-only.
   */
  public CharBuffer put (String str, int start, int length)
  {
    return put (str.toCharArray (), start, length);
  }
  
  /**
   * Relative put method.
   * 
   * @exception BufferOverflowException If there is insufficient space in this
   * buffer.
   * @exception ReadOnlyBufferException If this buffer is read-only.
   */
  public final CharBuffer put (String str)
  {
    return put (str.toCharArray (), 0, str.length ());
  }
  
  /**
   * Returns the character at <code>position() + index</code>.
   * 
   * @exception IndexOutOfBoundsException If index is negative not smaller than
   * <code>remaining()</code>.
   */
  public final char charAt (int index)
  {
    if (index < 0
        || index >= remaining ())
      throw new IndexOutOfBoundsException ();
    
    return get (position () + index);
  }

    //jnode + openjdk
    /**
     * Attempts to read characters into the specified character buffer.
     * The buffer is used as a repository of characters as-is: the only
     * changes made are the results of a put operation. No flipping or
     * rewinding of the buffer is performed.
     *
     * @param target the buffer to read characters into
     * @return The number of characters added to the buffer, or
     *         -1 if this source of characters is at its end
     * @throws java.io.IOException if an I/O error occurs
     * @throws NullPointerException if target is null
     * @throws ReadOnlyBufferException if target is a read only buffer
     * @since 1.5
     */
    public int read(CharBuffer target) throws IOException {
        // Determine the number of bytes n that can be transferred
        int targetRemaining = target.remaining();
        int remaining = remaining();
        if (remaining == 0)
            return -1;
        int n = Math.min(remaining, targetRemaining);
        int limit = limit();
        // Set source limit to prevent target overflow
        if (targetRemaining < remaining)
            limit(position() + n);
        try {
            if (n > 0)
                target.put(this);
        } finally {
            limit(limit); // restore real limit
        }
        return n;
    }

    /**
     * Appends the specified character sequence  to this
     * buffer&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> An invocation of this method of the form <tt>dst.append(csq)</tt>
     * behaves in exactly the same way as the invocation
     *
     * <pre>
     *     dst.put(csq.toString()) </pre>
     *
     * <p> Depending on the specification of <tt>toString</tt> for the
     * character sequence <tt>csq</tt>, the entire sequence may not be
     * appended.  For instance, invoking the {@link CharBuffer#toString()
     * toString} method of a character buffer will return a subsequence whose
     * content depends upon the buffer's position and limit.
     *
     * @param  csq
     *         The character sequence to append.  If <tt>csq</tt> is
     *         <tt>null</tt>, then the four characters <tt>"null"</tt> are
     *         appended to this character buffer.
     *
     * @return  This buffer
     *
     * @throws  BufferOverflowException
     *          If there is insufficient space in this buffer
     *
     * @throws  ReadOnlyBufferException
     *          If this buffer is read-only
     *
     * @since  1.5
     */
    public CharBuffer append(CharSequence csq) {
	if (csq == null)
	    return put("null");
	else
	    return put(csq.toString());
    }

    /**
     * Appends a subsequence of the  specified character sequence  to this
     * buffer&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> An invocation of this method of the form <tt>dst.append(csq, start,
     * end)</tt> when <tt>csq</tt> is not <tt>null</tt>, behaves in exactly the
     * same way as the invocation
     *
     * <pre>
     *     dst.put(csq.subSequence(start, end).toString()) </pre>
     *
     * @param  csq
     *         The character sequence from which a subsequence will be
     *         appended.  If <tt>csq</tt> is <tt>null</tt>, then characters
     *         will be appended as if <tt>csq</tt> contained the four
     *         characters <tt>"null"</tt>.
     *
     * @return  This buffer
     *
     * @throws  BufferOverflowException
     *          If there is insufficient space in this buffer
     *
     * @throws  IndexOutOfBoundsException
     *          If <tt>start</tt> or <tt>end</tt> are negative, <tt>start</tt>
     *          is greater than <tt>end</tt>, or <tt>end</tt> is greater than
     *          <tt>csq.length()</tt>
     *
     * @throws  ReadOnlyBufferException
     *          If this buffer is read-only
     *
     * @since  1.5
     */
    public CharBuffer append(CharSequence csq, int start, int end) {
	CharSequence cs = (csq == null ? "null" : csq);
	return put(cs.subSequence(start, end).toString());
    }

    /**
     * Appends the specified character to this
     * buffer&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> An invocation of this method of the form <tt>dst.append(c)</tt>
     * behaves in exactly the same way as the invocation
     *
     * <pre>
     *     dst.put(c) </pre>
     *
     * @param  c
     *         The 16-bit character to append
     *
     * @return  This buffer
     *
     * @throws  BufferOverflowException
     *          If there is insufficient space in this buffer
     *
     * @throws  ReadOnlyBufferException
     *          If this buffer is read-only
     *
     * @since  1.5
     */
    public CharBuffer append(char c) {
	return put(c);
    }

    /**
     * Compares this buffer to another.
     *
     * <p> Two char buffers are compared by comparing their sequences of
     * remaining elements lexicographically, without regard to the starting
     * position of each sequence within its corresponding buffer.
     *
     * <p> A char buffer is not comparable to any other type of object.
     *
     * @return  A negative integer, zero, or a positive integer as this buffer
     *		is less than, equal to, or greater than the given buffer
     */
    public int compareTo(CharBuffer that) {
	int n = this.position() + Math.min(this.remaining(), that.remaining());
	for (int i = this.position(), j = that.position(); i < n; i++, j++) {
	    char v1 = this.get(i);
	    char v2 = that.get(j);
	    if (v1 == v2)
		continue;
	    if ((v1 != v1) && (v2 != v2)) 	// For float and double
		continue;
	    if (v1 < v2)
		return -1;
	    return +1;
	}
	return this.remaining() - that.remaining();
    }
}
