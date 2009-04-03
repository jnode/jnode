/* ZipOutputStream.java --
   Copyright (C) 2001, 2004, 2005, 2006  Free Software Foundation, Inc.

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


package java.util.zip;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Vector;

/**
 * This is a FilterOutputStream that writes the files into a zip
 * archive one after another.  It has a special method to start a new
 * zip entry.  The zip entries contains information about the file name
 * size, compressed size, CRC, etc.
 *
 * It includes support for STORED and DEFLATED entries.
 *
 * This class is not thread safe.
 *
 * @author Jochen Hoenicke 
 */
public class ZipOutputStream extends DeflaterOutputStream implements ZipConstants
{
	private Vector entries = new Vector();
	private CRC32 crc = new CRC32();
	private ZipEntry curEntry = null;

	private int curMethod;
	private int size;
	private int offset = 0;

	private byte[] zipComment = new byte[0];
	private int defaultMethod = DEFLATED;

	/**
	 * Our Zip version is hard coded to 1.0 resp. 2.0
	 */
  private static final int ZIP_STORED_VERSION = 10;
  private static final int ZIP_DEFLATED_VERSION = 20;

	/**
	 * Compression method.  This method doesn't compress at all.
	 */
  public static final int STORED = 0;
  
	/**
	 * Compression method.  This method uses the Deflater.
	 */
  public static final int DEFLATED = 8;

	/**
	 * Creates a new Zip output stream, writing a zip archive.
	 * @param out the output stream to which the zip archive is written.
	 */
  public ZipOutputStream(OutputStream out)
  {
		super(out, new Deflater(Deflater.DEFAULT_COMPRESSION, true));
	}

	/**
	 * Set the zip file comment.
	 * @param comment the comment.
	 * @exception IllegalArgumentException if encoding of comment is
	 * longer than 0xffff bytes.
	 */
  public void setComment(String comment)
  {
		byte[] commentBytes;
    try
      {
	commentBytes = comment.getBytes("UTF-8");
      }
    catch (UnsupportedEncodingException uee)
      {
	throw new AssertionError(uee);
      }
		if (commentBytes.length > 0xffff)
			throw new IllegalArgumentException("Comment too long.");
		zipComment = commentBytes;
	}

	/**
	 * Sets default compression method.  If the Zip entry specifies
	 * another method its method takes precedence.
	 * @param method the method.
	 * @exception IllegalArgumentException if method is not supported.
	 * @see #STORED
	 * @see #DEFLATED
	 */
  public void setMethod(int method)
  {
		if (method != STORED && method != DEFLATED)
			throw new IllegalArgumentException("Method not supported.");
		defaultMethod = method;
	}

	/**
	 * Sets default compression level.  The new level will be activated
	 * immediately.  
	 * @exception IllegalArgumentException if level is not supported.
	 * @see Deflater
	 */
  public void setLevel(int level)
  {
		def.setLevel(level);
	}

	/**
	 * Write an unsigned short in little endian byte order.
	 */
  private void writeLeShort(int value) throws IOException 
  {
		out.write(value & 0xff);
		out.write((value >> 8) & 0xff);
	}

	/**
	 * Write an int in little endian byte order.
	 */
  private void writeLeInt(int value) throws IOException 
  {
		writeLeShort(value);
		writeLeShort(value >> 16);
	}

	/**
   * Write a long value as an int.  Some of the zip constants
   * are declared as longs even though they fit perfectly well
   * into integers.
   */
  private void writeLeInt(long value) throws IOException
  {
    writeLeInt((int) value);
  }

  /**
	 * Starts a new Zip entry. It automatically closes the previous
	 * entry if present.  If the compression method is stored, the entry
	 * must have a valid size and crc, otherwise all elements (except
	 * name) are optional, but must be correct if present.  If the time
	 * is not set in the entry, the current time is used.
	 * @param entry the entry.
	 * @exception IOException if an I/O error occured.
	 * @exception ZipException if stream was finished.
	 */
  public void putNextEntry(ZipEntry entry) throws IOException
  {
		if (entries == null)
			throw new ZipException("ZipOutputStream was finished");

		int method = entry.getMethod();
		int flags = 0;
		if (method == -1)
			method = defaultMethod;

    if (method == STORED)
      {
	if (entry.getCompressedSize() >= 0)
	  {
				if (entry.getSize() < 0)
					entry.setSize(entry.getCompressedSize());
				else if (entry.getSize() != entry.getCompressedSize())
	      throw new ZipException
		("Method STORED, but compressed size != size");
	  }
	else
				entry.setCompressedSize(entry.getSize());

			if (entry.getSize() < 0)
				throw new ZipException("Method STORED, but size not set");
			if (entry.getCrc() < 0)
				throw new ZipException("Method STORED, but crc not set");
      }
    else if (method == DEFLATED)
      {
	if (entry.getCompressedSize() < 0
	    || entry.getSize() < 0 || entry.getCrc() < 0)
				flags |= 8;
		}

		if (curEntry != null)
			closeEntry();

		if (entry.getTime() < 0)
			entry.setTime(System.currentTimeMillis());

		entry.flags = flags;
		entry.offset = offset;
		entry.setMethod(method);
		curMethod = method;
		/* Write the local file header */
		writeLeInt(LOCSIG);
    writeLeShort(method == STORED
		 ? ZIP_STORED_VERSION : ZIP_DEFLATED_VERSION);
		writeLeShort(flags);
		writeLeShort(method);
		writeLeInt(entry.getDOSTime());
    if ((flags & 8) == 0)
      {
	writeLeInt((int)entry.getCrc());
	writeLeInt((int)entry.getCompressedSize());
	writeLeInt((int)entry.getSize());
      }
    else
      {
			writeLeInt(0);
			writeLeInt(0);
			writeLeInt(0);
		}
    byte[] name;
    try
      {
	name = entry.getName().getBytes("UTF-8");
      }
    catch (UnsupportedEncodingException uee)
      {
	throw new AssertionError(uee);
      }
		if (name.length > 0xffff)
			throw new ZipException("Name too long.");
		byte[] extra = entry.getExtra();
		if (extra == null)
			extra = new byte[0];
		writeLeShort(name.length);
		writeLeShort(extra.length);
		out.write(name);
		out.write(extra);

		offset += LOCHDR + name.length + extra.length;

		/* Activate the entry. */

		curEntry = entry;
		crc.reset();
		if (method == DEFLATED)
			def.reset();
		size = 0;
	}

	/**
	 * Closes the current entry.
	 * @exception IOException if an I/O error occured.
	 * @exception ZipException if no entry is active.
	 */
  public void closeEntry() throws IOException
  {
		if (curEntry == null)
			throw new ZipException("No open entry");

		/* First finish the deflater, if appropriate */
		if (curMethod == DEFLATED)
			super.finish();

		int csize = curMethod == DEFLATED ? def.getTotalOut() : size;

		if (curEntry.getSize() < 0)
			curEntry.setSize(size);
		else if (curEntry.getSize() != size)
      throw new ZipException("size was "+size
			     +", but I expected "+curEntry.getSize());

		if (curEntry.getCompressedSize() < 0)
			curEntry.setCompressedSize(csize);
		else if (curEntry.getCompressedSize() != csize)
      throw new ZipException("compressed size was "+csize
			     +", but I expected "+curEntry.getSize());

		if (curEntry.getCrc() < 0)
			curEntry.setCrc(crc.getValue());
		else if (curEntry.getCrc() != crc.getValue())
      throw new ZipException("crc was " + Long.toHexString(crc.getValue())
			     + ", but I expected " 
			     + Long.toHexString(curEntry.getCrc()));

		offset += csize;

		/* Now write the data descriptor entry if needed. */
    if (curMethod == DEFLATED && (curEntry.flags & 8) != 0)
      {
			writeLeInt(EXTSIG);
	writeLeInt((int)curEntry.getCrc());
	writeLeInt((int)curEntry.getCompressedSize());
	writeLeInt((int)curEntry.getSize());
			offset += EXTHDR;
		}

		entries.addElement(curEntry);
		curEntry = null;
	}

	/**
	 * Writes the given buffer to the current entry.
	 * @exception IOException if an I/O error occured.
	 * @exception ZipException if no entry is active.
	 */
  public void write(byte[] b, int off, int len) throws IOException
  {
		if (curEntry == null)
			throw new ZipException("No open entry.");

    switch (curMethod)
      {
      case DEFLATED:
				super.write(b, off, len);
				break;

      case STORED:
				out.write(b, off, len);
				break;
		}

		crc.update(b, off, len);
		size += len;
	}

	/**
	 * Finishes the stream.  This will write the central directory at the
	 * end of the zip file and flush the stream.
	 * @exception IOException if an I/O error occured.
	 */
  public void finish() throws IOException
  {
		if (entries == null)
			return;
		if (curEntry != null)
			closeEntry();

		int numEntries = 0;
		int sizeEntries = 0;

		Enumeration e = entries.elements();
    while (e.hasMoreElements())
      {
			ZipEntry entry = (ZipEntry) e.nextElement();

			int method = entry.getMethod();
			writeLeInt(CENSIG);
	writeLeShort(method == STORED
		     ? ZIP_STORED_VERSION : ZIP_DEFLATED_VERSION);
	writeLeShort(method == STORED
		     ? ZIP_STORED_VERSION : ZIP_DEFLATED_VERSION);
			writeLeShort(entry.flags);
			writeLeShort(method);
			writeLeInt(entry.getDOSTime());
	writeLeInt((int)entry.getCrc());
	writeLeInt((int)entry.getCompressedSize());
	writeLeInt((int)entry.getSize());

	byte[] name;
	try
	  {
	    name = entry.getName().getBytes("UTF-8");
	  }
	catch (UnsupportedEncodingException uee)
	  {
	    throw new AssertionError(uee);
	  }
			if (name.length > 0xffff)
				throw new ZipException("Name too long.");
			byte[] extra = entry.getExtra();
			if (extra == null)
				extra = new byte[0];
	String str = entry.getComment();
	byte[] comment;
	try
	  {
	    comment = str != null ? str.getBytes("UTF-8") : new byte[0];
	  }
	catch (UnsupportedEncodingException uee)
	  {
	    throw new AssertionError(uee);
	  }
			if (comment.length > 0xffff)
				throw new ZipException("Comment too long.");

			writeLeShort(name.length);
			writeLeShort(extra.length);
			writeLeShort(comment.length);
			writeLeShort(0); /* disk number */
			writeLeShort(0); /* internal file attr */
			writeLeInt(0); /* external file attr */
			writeLeInt(entry.offset);

			out.write(name);
			out.write(extra);
			out.write(comment);
			numEntries++;
			sizeEntries += CENHDR + name.length + extra.length + comment.length;
		}

		writeLeInt(ENDSIG);
		writeLeShort(0); /* disk number */
		writeLeShort(0); /* disk with start of central dir */
		writeLeShort(numEntries);
		writeLeShort(numEntries);
		writeLeInt(sizeEntries);
		writeLeInt(offset);
		writeLeShort(zipComment.length);
		out.write(zipComment);
		out.flush();
		entries = null;
	}
}
