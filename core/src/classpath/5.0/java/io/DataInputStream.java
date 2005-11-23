/* DataInputStream.java -- FilteredInputStream that implements DataInput
   Copyright (C) 1998, 1999, 2000, 2001, 2003, 2005  Free Software Foundation

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
 
package java.io;

/* Written using "Java Class Libraries", 2nd edition, ISBN 0-201-31002-3
 * "The Java Language Specification", ISBN 0-201-63451-1
 * plus online API docs for JDK 1.2 beta from http://www.javasoft.com.
 * Status:  Believed complete and correct.
 */
 
/**
 * This subclass of <code>FilteredInputStream</code> implements the
 * <code>DataInput</code> interface that provides method for reading primitive
 * Java data types from a stream.
 *
 * @see DataInput
 *
 * @author Warren Levy (warrenl@cygnus.com)
 * @author Aaron M. Renn (arenn@urbanophile.com)
 * @date October 20, 1998.  
 */
public class DataInputStream extends FilterInputStream implements DataInput
{
  // Byte buffer, used to make primitive read calls more efficient.
  byte[] buf = new byte [8];
  
  /**
   * This constructor initializes a new <code>DataInputStream</code>
   * to read from the specified subordinate stream.
   *
   * @param in The subordinate <code>InputStream</code> to read from
   */
  public DataInputStream (InputStream in)
  {
    super (in);
  }

  /**
   * This method reads bytes from the underlying stream into the specified
   * byte array buffer.  It will attempt to fill the buffer completely, but
   * may return a short count if there is insufficient data remaining to be
   * read to fill the buffer.
   *
   * @param b The buffer into which bytes will be read.
   * 
   * @return The actual number of bytes read, or -1 if end of stream reached 
   * before reading any bytes.
   *
   * @exception IOException If an error occurs.
   */
  public final int read (byte[] b) throws IOException
  {
    return in.read (b, 0, b.length);
  }

  /**
   * This method reads bytes from the underlying stream into the specified
   * byte array buffer.  It will attempt to read <code>len</code> bytes and
   * will start storing them at position <code>off</code> into the buffer.
   * This method can return a short count if there is insufficient data
   * remaining to be read to complete the desired read length.
   *
   * @param b The buffer into which bytes will be read.
   * @param off The offset into the buffer to start storing bytes.
   * @param len The requested number of bytes to read.
   *
   * @return The actual number of bytes read, or -1 if end of stream reached
   * before reading any bytes.
   *
   * @exception IOException If an error occurs.
   */
  public final int read (byte[] b, int off, int len) throws IOException
  {
    return in.read (b, off, len);
  }

  /**
   * This method reads a Java boolean value from an input stream.  It does
   * so by reading a single byte of data.  If that byte is zero, then the
   * value returned is <code>false</code>.  If the byte is non-zero, then
   * the value returned is <code>true</code>.
   * <p>
   * This method can read a <code>boolean</code> written by an object
   * implementing the <code>writeBoolean()</code> method in the
   * <code>DataOutput</code> interface. 
   *
   * @return The <code>boolean</code> value read
   *
   * @exception EOFException If end of file is reached before reading
   * the boolean
   * @exception IOException If any other error occurs
   *
   * @see DataOutput#writeBoolean
   */
  public final boolean readBoolean () throws IOException
  {
    return convertToBoolean (in.read ());
  }

  /**
   * This method reads a Java byte value from an input stream.  The value
   * is in the range of -128 to 127.
   * <p>
   * This method can read a <code>byte</code> written by an object
   * implementing the <code>writeByte()</code> method in the
   * <code>DataOutput</code> interface.
   *
   * @return The <code>byte</code> value read
   *
   * @exception EOFException If end of file is reached before reading the byte
   * @exception IOException If any other error occurs
   *
   * @see DataOutput#writeByte
   */
  public final byte readByte () throws IOException
  {
    return convertToByte (in.read ());
  }

  /**
   * This method reads a Java <code>char</code> value from an input stream.  
   * It operates by reading two bytes from the stream and converting them to 
   * a single 16-bit Java <code>char</code>.  The two bytes are stored most
   * significant byte first (i.e., "big endian") regardless of the native
   * host byte ordering. 
   * <p>
   * As an example, if <code>byte1</code> and <code>byte2</code>
   * represent the first and second byte read from the stream
   * respectively, they will be transformed to a <code>char</code> in
   * the following manner: 
   * <p>
   * <code>(char)(((byte1 &amp; 0xFF) &lt;&lt; 8) | (byte2 &amp; 0xFF)</code>
   * <p>
   * This method can read a <code>char</code> written by an object
   * implementing the <code>writeChar()</code> method in the
   * <code>DataOutput</code> interface. 
   *
   * @return The <code>char</code> value read 
   *
   * @exception EOFException If end of file is reached before reading the char
   * @exception IOException If any other error occurs
   *
   * @see DataOutput#writeChar
   */
  public final char readChar () throws IOException
  {
    readFully (buf, 0, 2);
    return convertToChar (buf);
  }

  /**
   * This method reads a Java double value from an input stream.  It operates
   * by first reading a <code>long</code> value from the stream by calling the
   * <code>readLong()</code> method in this interface, then converts
   * that <code>long</code> to a <code>double</code> using the
   * <code>longBitsToDouble</code> method in the class
   * <code>java.lang.Double</code> 
   * <p>
   * This method can read a <code>double</code> written by an object
   * implementing the <code>writeDouble()</code> method in the
   * <code>DataOutput</code> interface.
   *
   * @return The <code>double</code> value read
   *
   * @exception EOFException If end of file is reached before reading
   * the double
   * @exception IOException If any other error occurs
   *
   * @see DataOutput#writeDouble
   * @see java.lang.Double#longBitsToDouble
   */
  public final double readDouble () throws IOException
  {
    return Double.longBitsToDouble (readLong ());
  }

  /**
   * This method reads a Java float value from an input stream.  It
   * operates by first reading an <code>int</code> value from the
   * stream by calling the <code>readInt()</code> method in this
   * interface, then converts that <code>int</code> to a
   * <code>float</code> using the <code>intBitsToFloat</code> method
   * in the class <code>java.lang.Float</code>
   * <p>
   * This method can read a <code>float</code> written by an object
   * implementing the <code>writeFloat()</code> method in the
   * <code>DataOutput</code> interface.
   *
   * @return The <code>float</code> value read
   *
   * @exception EOFException If end of file is reached before reading the float
   * @exception IOException If any other error occurs
   *
   * @see DataOutput#writeFloat 
   * @see java.lang.Float#intBitsToFloat
   */
  public final float readFloat () throws IOException
  {
    return Float.intBitsToFloat (readInt ());
  }

  /**
   * This method reads raw bytes into the passed array until the array is
   * full.  Note that this method blocks until the data is available and
   * throws an exception if there is not enough data left in the stream to
   * fill the buffer.  Note also that zero length buffers are permitted.
   * In this case, the method will return immediately without reading any
   * bytes from the stream.
   *
   * @param b The buffer into which to read the data
   *
   * @exception EOFException If end of file is reached before filling the
   * buffer
   * @exception IOException If any other error occurs
   */
  public final void readFully (byte[] b) throws IOException
  {
    readFully (b, 0, b.length);
  }

  /**
   * This method reads raw bytes into the passed array <code>buf</code>
   * starting
   * <code>offset</code> bytes into the buffer.  The number of bytes read
   * will be
   * exactly <code>len</code>.  Note that this method blocks until the data is
   * available and throws an exception if there is not enough data left in
   * the stream to read <code>len</code> bytes.  Note also that zero length
   * buffers are permitted.  In this case, the method will return immediately
   * without reading any bytes from the stream.
   *
   * @param buf The buffer into which to read the data
   * @param offset The offset into the buffer to start storing data
   * @param len The number of bytes to read into the buffer
   *
   * @exception EOFException If end of file is reached before filling the
   * buffer
   * @exception IOException If any other error occurs
   */
  public final void readFully (byte[] buf, int offset, int len) throws IOException
  {
    if (len < 0)
      throw new IndexOutOfBoundsException("Negative length: " + len);
    
    while (len > 0)
      {
	// in.read will block until some data is available.
	int numread = in.read (buf, offset, len);
	if (numread < 0)
	  throw new EOFException ();
	len -= numread;
	offset += numread;
      }
  }

  /**
   * This method reads a Java <code>int</code> value from an input stream
   * It operates by reading four bytes from the stream and converting them to
   * a single Java <code>int</code>.  The bytes are stored most
   * significant byte first (i.e., "big endian") regardless of the native
   * host byte ordering.
   * <p>
   * As an example, if <code>byte1</code> through <code>byte4</code> represent
   * the first four bytes read from the stream, they will be
   * transformed to an <code>int</code> in the following manner:
   * <p>
   * <code>(int)(((byte1 &amp; 0xFF) &lt;&lt; 24) + ((byte2 &amp; 0xFF) &lt;&lt; 16) +
   * ((byte3 &amp; 0xFF)&lt;&lt; 8) + (byte4 &amp; 0xFF)))</code>
   * <p>
   * The value returned is in the range of -2147483648 to 2147483647.
   * <p>
   * This method can read an <code>int</code> written by an object
   * implementing the <code>writeInt()</code> method in the
   * <code>DataOutput</code> interface.
   *
   * @return The <code>int</code> value read
   *
   * @exception EOFException If end of file is reached before reading the int
   * @exception IOException If any other error occurs
   *
   * @see DataOutput#writeInt
   */
  public final int readInt () throws IOException
  {
    readFully (buf, 0, 4);
    return convertToInt (buf);
  }

  /**
   * This method reads the next line of text data from an input
   * stream.  It operates by reading bytes and converting those bytes
   * to <code>char</code> values by treating the byte read as the low
   * eight bits of the <code>char</code> and using 0 as the high eight
   * bits.  Because of this, it does not support the full 16-bit
   * Unicode character set.
   * <p>
   * The reading of bytes ends when either the end of file or a line
   * terminator is encountered.  The bytes read are then returned as a
   * <code>String</code> A line terminator is a byte sequence
   * consisting of either <code>\r</code>, <code>\n</code> or
   * <code>\r\n</code>.  These termination charaters are discarded and
   * are not returned as part of the string.
   * <p>
   * This method can read data that was written by an object implementing the
   * <code>writeLine()</code> method in <code>DataOutput</code>.
   *
   * @return The line read as a <code>String</code>
   *
   * @exception IOException If an error occurs
   *
   * @see DataOutput
   *
   * @deprecated
   */
  public final String readLine() throws IOException
  {
    StringBuffer strb = new StringBuffer();

    while (true)
      {
        int c = in.read();
	if (c == -1)	// got an EOF
	    return strb.length() > 0 ? strb.toString() : null;
	if (c == '\r')
	  {
	    int next_c = in.read();
            if (next_c != '\n' && next_c != -1)
              {
                if (!(in instanceof PushbackInputStream))
                  in = new PushbackInputStream(in);
                ((PushbackInputStream) in).unread(next_c);
              }
            break;
	  }
        if (c == '\n')
            break;
	strb.append((char) c);
      }

    return strb.length() > 0 ? strb.toString() : "";
  }

  /**
   * This method reads a Java <code>long</code> value from an input stream
   * It operates by reading eight bytes from the stream and converting them to
   * a single Java <code>long</code>.  The bytes are stored most
   * significant byte first (i.e., "big endian") regardless of the native
   * host byte ordering.
   * <p>
   * As an example, if <code>byte1</code> through <code>byte8</code> represent
   * the first eight bytes read from the stream, they will be
   * transformed to an <code>long</code> in the following manner:
   * <p>
   * <code>(long)(((byte1 &amp; 0xFF) &lt;&lt; 56) + ((byte2 &amp; 0xFF) &lt;&lt; 48) +
   * ((byte3 &amp; 0xFF) &lt;&lt; 40) + ((byte4 &amp; 0xFF) &lt;&lt; 32) +
   * ((byte5 &amp; 0xFF) &lt;&lt; 24) + ((byte6 &amp; 0xFF) &lt;&lt; 16) +
   * ((byte7 &amp; 0xFF) &lt;&lt; 8) + (byte8 &amp; 0xFF)))
   * </code>
   * <p>
   * The value returned is in the range of -9223372036854775808 to
   * 9223372036854775807.
   * <p>
   * This method can read an <code>long</code> written by an object
   * implementing the <code>writeLong()</code> method in the
   * <code>DataOutput</code> interface.
   *
   * @return The <code>long</code> value read
   *
   * @exception EOFException If end of file is reached before reading the long
   * @exception IOException If any other error occurs
   *
   * @see DataOutput#writeLong
   */
  public final long readLong () throws IOException
  {
    readFully (buf, 0, 8);
    return convertToLong (buf);
  }

  /**
   * This method reads a signed 16-bit value into a Java in from the
   * stream.  It operates by reading two bytes from the stream and
   * converting them to a single 16-bit Java <code>short</code>.  The
   * two bytes are stored most significant byte first (i.e., "big
   * endian") regardless of the native host byte ordering.
   * <p>
   * As an example, if <code>byte1</code> and <code>byte2</code>
   * represent the first and second byte read from the stream
   * respectively, they will be transformed to a <code>short</code>. in
   * the following manner:
   * <p>
   * <code>(short)(((byte1 &amp; 0xFF) &lt;&lt; 8) | (byte2 &amp; 0xFF))</code>
   * <p>
   * The value returned is in the range of -32768 to 32767.
   * <p>
   * This method can read a <code>short</code> written by an object
   * implementing the <code>writeShort()</code> method in the
   * <code>DataOutput</code> interface.
   *
   * @return The <code>short</code> value read
   *
   * @exception EOFException If end of file is reached before reading the value
   * @exception IOException If any other error occurs
   *
   * @see DataOutput#writeShort
   */
  public final short readShort () throws IOException
  {
    readFully (buf, 0, 2);
    return convertToShort (buf);
  }
  
  /**
   * This method reads 8 unsigned bits into a Java <code>int</code>
   * value from the stream. The value returned is in the range of 0 to
   * 255.
   * <p>
   * This method can read an unsigned byte written by an object
   * implementing the <code>writeUnsignedByte()</code> method in the
   * <code>DataOutput</code> interface.
   *
   * @return The unsigned bytes value read as a Java <code>int</code>.
   *
   * @exception EOFException If end of file is reached before reading the value
   * @exception IOException If any other error occurs
   *
   * @see DataOutput#writeByte
   */
  public final int readUnsignedByte () throws IOException
  {
    return convertToUnsignedByte (in.read ());
  }

  /**
   * This method reads 16 unsigned bits into a Java int value from the stream.
   * It operates by reading two bytes from the stream and converting them to 
   * a single Java <code>int</code>  The two bytes are stored most
   * significant byte first (i.e., "big endian") regardless of the native
   * host byte ordering. 
   * <p>
   * As an example, if <code>byte1</code> and <code>byte2</code>
   * represent the first and second byte read from the stream
   * respectively, they will be transformed to an <code>int</code> in
   * the following manner:
   * <p>
   * <code>(int)(((byte1 &amp; 0xFF) &lt;&lt; 8) + (byte2 &amp; 0xFF))</code>
   * <p>
   * The value returned is in the range of 0 to 65535.
   * <p>
   * This method can read an unsigned short written by an object
   * implementing the <code>writeUnsignedShort()</code> method in the
   * <code>DataOutput</code> interface.
   *
   * @return The unsigned short value read as a Java <code>int</code>
   *
   * @exception EOFException If end of file is reached before reading the value
   * @exception IOException If any other error occurs
   *
   * @see DataOutput#writeShort
   */
  public final int readUnsignedShort () throws IOException
  {
    readFully (buf, 0, 2);
    return convertToUnsignedShort (buf);
  }

  /**
   * This method reads a <code>String</code> from an input stream that
   * is encoded in a modified UTF-8 format.  This format has a leading
   * two byte sequence that contains the remaining number of bytes to
   * read.  This two byte sequence is read using the
   * <code>readUnsignedShort()</code> method of this interface.
   * <p>
   * After the number of remaining bytes have been determined, these
   * bytes are read an transformed into <code>char</code> values.
   * These <code>char</code> values are encoded in the stream using
   * either a one, two, or three byte format.  The particular format
   * in use can be determined by examining the first byte read.
   * <p>
   * If the first byte has a high order bit of 0, then that character
   * consists on only one byte.  This character value consists of
   * seven bits that are at positions 0 through 6 of the byte.  As an
   * example, if <code>byte1</code> is the byte read from the stream,
   * it would be converted to a <code>char</code> like so:
   * <p>
   * <code>(char)byte1</code>
   * <p>
   * If the first byte has 110 as its high order bits, then the 
   * character consists of two bytes.  The bits that make up the character
   * value are in positions 0 through 4 of the first byte and bit positions
   * 0 through 5 of the second byte.  (The second byte should have 
   * 10 as its high order bits).  These values are in most significant
   * byte first (i.e., "big endian") order.
   * <p>
   * As an example, if <code>byte1</code> and <code>byte2</code> are
   * the first two bytes read respectively, and the high order bits of
   * them match the patterns which indicate a two byte character
   * encoding, then they would be converted to a Java
   * <code>char</code> like so:
   * <p>
   * <code>(char)(((byte1 & 0x1F) << 6) | (byte2 & 0x3F))</code>
   * <p>
   * If the first byte has a 1110 as its high order bits, then the
   * character consists of three bytes.  The bits that make up the character
   * value are in positions 0 through 3 of the first byte and bit positions
   * 0 through 5 of the other two bytes.  (The second and third bytes should
   * have 10 as their high order bits).  These values are in most
   * significant byte first (i.e., "big endian") order.
   * <p>
   * As an example, if <code>byte1</code> <code>byte2</code> and
   * <code>byte3</code> are the three bytes read, and the high order
   * bits of them match the patterns which indicate a three byte
   * character encoding, then they would be converted to a Java
   * <code>char</code> like so:
   * <p>
   * <code>(char)(((byte1 & 0x0F) << 12) | ((byte2 & 0x3F) << 6) | 
   * (byte3 & 0x3F))</code>
   * <p>
   * Note that all characters are encoded in the method that requires
   * the fewest number of bytes with the exception of the character
   * with the value of <code>&#92;u0000</code> which is encoded as two
   * bytes.  This is a modification of the UTF standard used to
   * prevent C language style <code>NUL</code> values from appearing
   * in the byte stream.
   * <p>
   * This method can read data that was written by an object implementing the
   * <code>writeUTF()</code> method in <code>DataOutput</code>
   * 
   * @return The <code>String</code> read
   *
   * @exception EOFException If end of file is reached before reading
   * the String
   * @exception UTFDataFormatException If the data is not in UTF-8 format
   * @exception IOException If any other error occurs
   *
   * @see DataOutput#writeUTF
   */
  public final String readUTF () throws IOException
  {
    return readUTF (this);
  }

  /**
   * This method reads a String encoded in UTF-8 format from the 
   * specified <code>DataInput</code> source.
   *
   * @param in The <code>DataInput</code> source to read from
   *
   * @return The String read from the source
   *
   * @exception IOException If an error occurs
   *
   * @see DataInput#readUTF
   */
  public static final String readUTF(DataInput in) throws IOException
  {
    final int UTFlen = in.readUnsignedShort ();
    byte[] buf = new byte [UTFlen];

    // This blocks until the entire string is available rather than
    // doing partial processing on the bytes that are available and then
    // blocking.  An advantage of the latter is that Exceptions
    // could be thrown earlier.  The former is a bit cleaner.
    in.readFully (buf, 0, UTFlen);

    return convertFromUTF (buf);
  }

  /**
   * This method attempts to skip and discard the specified number of bytes 
   * in the input stream.  It may actually skip fewer bytes than requested. 
   * This method will not skip any bytes if passed a negative number of bytes 
   * to skip. 
   *
   * @param n The requested number of bytes to skip.
   *
   * @return The requested number of bytes to skip.
   *
   * @exception IOException If an error occurs.
   * @specnote The JDK docs claim that this returns the number of bytes 
   *  actually skipped. The JCL claims that this method can throw an 
   *  EOFException. Neither of these appear to be true in the JDK 1.3's
   *  implementation. This tries to implement the actual JDK behaviour.
   */
  public final int skipBytes (int n) throws IOException
  {
    if (n <= 0)
      return 0;    
    try
      {
        return (int) in.skip (n);
      }
    catch (EOFException x)
      {
        // do nothing.
      }         
    return n;
  }
  
  static boolean convertToBoolean (int b) throws EOFException
  {
    if (b < 0)
      throw new EOFException ();
    
    return (b != 0);
  }

  static byte convertToByte (int i) throws EOFException
  {
    if (i < 0)
      throw new EOFException ();
    
    return (byte) i;
  }

  static int convertToUnsignedByte (int i) throws EOFException
  {
    if (i < 0)
      throw new EOFException ();
    
    return (i & 0xFF);
  }

  static char convertToChar (byte[] buf)
  {
    return (char) ((buf [0] << 8)
		    | (buf [1] & 0xff));  
  }  

  static short convertToShort (byte[] buf)
  {
    return (short) ((buf [0] << 8)
		    | (buf [1] & 0xff));  
  }  

  static int convertToUnsignedShort (byte[] buf)
  {
    return (((buf [0] & 0xff) << 8)
	    | (buf [1] & 0xff));  
  }

  static int convertToInt (byte[] buf)
  {
    return (((buf [0] & 0xff) << 24)
	    | ((buf [1] & 0xff) << 16)
	    | ((buf [2] & 0xff) << 8)
	    | (buf [3] & 0xff));  
  }

  static long convertToLong (byte[] buf)
  {
    return (((long)(buf [0] & 0xff) << 56) |
	    ((long)(buf [1] & 0xff) << 48) |
	    ((long)(buf [2] & 0xff) << 40) |
	    ((long)(buf [3] & 0xff) << 32) |
	    ((long)(buf [4] & 0xff) << 24) |
	    ((long)(buf [5] & 0xff) << 16) |
	    ((long)(buf [6] & 0xff) <<  8) |
	    ((long)(buf [7] & 0xff)));  
  }

  // FIXME: This method should be re-thought.  I suspect we have multiple
  // UTF-8 decoders floating around.  We should use the standard charset
  // converters, maybe and adding a direct call into one of the new
  // NIO converters for a super-fast UTF8 decode.
  static String convertFromUTF (byte[] buf) 
    throws EOFException, UTFDataFormatException
  {
    // Give StringBuffer an initial estimated size to avoid 
    // enlarge buffer frequently
    StringBuffer strbuf = new StringBuffer (buf.length / 2 + 2);

    for (int i = 0; i < buf.length; )
      {
	if ((buf [i] & 0x80) == 0)		// bit pattern 0xxxxxxx
	  strbuf.append ((char) (buf [i++] & 0xFF));
	else if ((buf [i] & 0xE0) == 0xC0)	// bit pattern 110xxxxx
	  {
	    if (i + 1 >= buf.length
		|| (buf [i + 1] & 0xC0) != 0x80)
	      throw new UTFDataFormatException ();

	    strbuf.append((char) (((buf [i++] & 0x1F) << 6)
				  | (buf [i++] & 0x3F)));
	  }
	else if ((buf [i] & 0xF0) == 0xE0)	// bit pattern 1110xxxx
	  {
	    if (i + 2 >= buf.length
		|| (buf [i + 1] & 0xC0) != 0x80
		|| (buf [i + 2] & 0xC0) != 0x80)
	      throw new UTFDataFormatException ();

	    strbuf.append ((char) (((buf [i++] & 0x0F) << 12)
				   | ((buf [i++] & 0x3F) << 6)
				   | (buf [i++] & 0x3F)));
	  }
	else // must be ((buf [i] & 0xF0) == 0xF0 || (buf [i] & 0xC0) == 0x80)
	  throw new UTFDataFormatException ();	// bit patterns 1111xxxx or
						// 		10xxxxxx
      }

    return strbuf.toString ();
  }
}
