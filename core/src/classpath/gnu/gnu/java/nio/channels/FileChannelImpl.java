/* FileChannelImpl.java -- 
   Copyright (C) 2002, 2004, 2005  Free Software Foundation, Inc.

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


package gnu.java.nio.channels;

import gnu.java.nio.FileLockImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.VMFile;
import java.io.VMIOUtils;
import java.io.VMOpenMode;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.jnode.java.io.VMFileHandle;

/**
 * This file is not user visible !
 * But alas, Java does not have a concept of friendly packages
 * so this class is public. 
 * Instances of this class are created by invoking getChannel
 * Upon a Input/Output/RandomAccessFile object.
 */
public final class FileChannelImpl extends FileChannel
{
  // These are mode values for open().
  public static final int READ   = 1;
  public static final int WRITE  = 2;
  public static final int APPEND = 4;

  // EXCL is used only when making a temp file.
  public static final int EXCL   = 8;
  public static final int SYNC   = 16;
  public static final int DSYNC  = 32;

  // System's notion of file descriptor.  It might seem redundant to
  // initialize this given that it is reassigned in the constructors.
  // However, this is necessary because if open() throws an exception
  // we want to make sure this has the value -1.  This is the most
  // efficient way to accomplish that.
  private VMFileHandle fh;  

  private int mode;

  public FileChannelImpl ()
  {
  }

  /* Open a file.  MODE is a combination of the above mode flags. */
  /* This is a static factory method, so that VM implementors can decide
   * substitute subclasses of FileChannelImpl. */
  public static FileChannelImpl create(File file, int mode)
    throws FileNotFoundException
  {
    return new FileChannelImpl(file, mode);
  }

  private FileChannelImpl(File file, int mode)
    throws FileNotFoundException
  {
    final String path = file.getPath();
    fh = open (path, mode);
    this.mode = mode;

    // First open the file and then check if it is a a directory
    // to avoid race condition.
    if (file.isDirectory())
      {
	try 
	  {
	      close();
	  }
	catch (IOException e)
	  {
	      /* ignore it */
	  }

	throw new FileNotFoundException(path + " is a directory");
      }
  }

  public static FileChannelImpl in;
  public static FileChannelImpl out;
  public static FileChannelImpl err;

  private VMFileHandle open (String path, int mode) throws FileNotFoundException
  {
    try
    {
        return VMIOUtils.getAPI().open(VMFile.getNormalizedPath(path), VMOpenMode.valueOf(mode));
    }
    catch (IOException e)
    {
        FileNotFoundException fnf = new FileNotFoundException("can't find "+path);
        fnf.initCause(e);
        throw fnf;
    }
  }

  public int available () throws IOException
  {
      return fh.available();      
  }
  
  private long implPosition () throws IOException
  {      
      return fh.getPosition();      
  }
  
  private void seek (long newPosition) throws IOException
  {
      fh.setPosition(newPosition);
  }
  
  private void implTruncate (long size) throws IOException
  {
      fh.setLength(size);      
  }
  
  public void unlock (long pos, long len) throws IOException
  {
      fh.unlock(pos, len);
  }

  public long size () throws IOException
  {
      return fh.getLength();
  }
    
  protected void implCloseChannel() throws IOException
  {
      fh.close();
  }

  /**
   * Makes sure the Channel is properly closed.
   */
  protected void finalize() throws IOException
  {
    this.close();
  }

  public int read (ByteBuffer dst) throws IOException
  {
//    int result;
//    byte[] buffer = new byte [dst.remaining ()];
//    
//    result = read (buffer, 0, buffer.length);
//
//    if (result > 0)
//      dst.put (buffer, 0, result);
//
//    return result;
      return fh.read(dst);
  }

  public int read (ByteBuffer dst, long position)
    throws IOException
  {
    if (position < 0)
      throw new IllegalArgumentException ("position: " + position);
    long oldPosition = implPosition ();
    position (position);
    int result = read(dst);
    position (oldPosition);
    
    return result;
  }

  public int read ()
    throws IOException
    {
      return fh.read();
    }

  public int read (byte[] buffer, int offset, int length)
    throws IOException
    {      
      ByteBuffer buf = ByteBuffer.wrap(buffer, offset, length);
        return fh.read(buf);
    }

  public long read (ByteBuffer[] dsts, int offset, int length)
    throws IOException
  {
    long result = 0;

    for (int i = offset; i < offset + length; i++)
      {
        result += read (dsts [i]);
      }

    return result;
  }

  public int write (ByteBuffer src) throws IOException
  {
    int len = src.remaining ();
    if (src.hasArray())
      {
	byte[] buffer = src.array();
	write(buffer, src.arrayOffset() + src.position(), len);
	src.position(src.position() + len);
      }
    else
      {
	// Use a more efficient method! FIXME!
	byte[] buffer = new byte [len];
    	src.get (buffer, 0, len);
	write (buffer, 0, len);
      }
    return len;
  }
    
  public int write (ByteBuffer src, long position)
    throws IOException
  {
    if (position < 0)
      throw new IllegalArgumentException ("position: " + position);

    if (!isOpen ())
      throw new ClosedChannelException ();
    
    if ((mode & WRITE) == 0)
       throw new NonWritableChannelException ();

    int result;
    long oldPosition;

    oldPosition = implPosition ();
    seek (position);
    result = write(src);
    seek (oldPosition);
    
    return result;
  }

  public void write (byte[] buffer, int offset, int length)
    throws IOException
    {
      fh.write(ByteBuffer.wrap(buffer, offset, length));
    }
  
  public void write (int b) throws IOException
  {      
      fh.write(b);                  
  }

  public long write(ByteBuffer[] srcs, int offset, int length)
    throws IOException
  {
    long result = 0;

    for (int i = offset;i < offset + length;i++)
      {
        result += write (srcs[i]);
      }
    
    return result;
  }
				   
  public MappedByteBuffer mapImpl (char mode, long position, int size)
    throws IOException
    {
      return fh.mapImpl(mode, position, size);            
    }

  public MappedByteBuffer map (FileChannel.MapMode mode,
			       long position, long size)
    throws IOException
  {
    char nmode = 0;
    if (mode == MapMode.READ_ONLY)
      {
	nmode = 'r';
	if ((this.mode & READ) == 0)
	  throw new NonReadableChannelException();
      }
    else if (mode == MapMode.READ_WRITE || mode == MapMode.PRIVATE)
      {
	nmode = mode == MapMode.READ_WRITE ? '+' : 'c';
	if ((this.mode & (READ|WRITE)) != (READ|WRITE))
	  throw new NonWritableChannelException();
      }
    else
      throw new IllegalArgumentException ("mode: " + mode);
    
    if (position < 0 || size < 0 || size > Integer.MAX_VALUE)
      throw new IllegalArgumentException ("position: " + position
					  + ", size: " + size);
    return mapImpl(nmode, position, (int) size);
  }

  /**
   * msync with the disk
   */
  public void force (boolean metaData) throws IOException
  {
    if (!isOpen ())
      throw new ClosedChannelException ();

    force ();
  }

  private void force ()
  {
  	// @vm-specific
  	//TODO implement me
  }

  // like transferTo, but with a count of less than 2Gbytes
  private int smallTransferTo (long position, int count, 
			       WritableByteChannel target)
    throws IOException
  {
    ByteBuffer buffer;
    try
      {
	// Try to use a mapped buffer if we can.  If this fails for
	// any reason we'll fall back to using a ByteBuffer.
	buffer = map (MapMode.READ_ONLY, position, count);
      }
    catch (IOException e)
      {
	buffer = ByteBuffer.allocate (count);
	read (buffer, position);
	buffer.flip();
      }

    return target.write (buffer);
  }

  public long transferTo (long position, long count, 
			  WritableByteChannel target)
    throws IOException
  {
    if (position < 0
        || count < 0)
      throw new IllegalArgumentException ();

    if (!isOpen ())
      throw new ClosedChannelException ();

    if ((mode & READ) == 0)
       throw new NonReadableChannelException ();
   
    final int pageSize = 65536;
    long total = 0;

    while (count > 0)
      {
	int transferred 
	  = smallTransferTo (position, (int)Math.min (count, pageSize), 
			     target);
	if (transferred < 0)
	  break;
	total += transferred;
	position += transferred;
	count -= transferred;
      }

    return total;
  }

  // like transferFrom, but with a count of less than 2Gbytes
  private int smallTransferFrom (ReadableByteChannel src, long position, 
				 int count)
    throws IOException
  {
    ByteBuffer buffer = null;

    if (src instanceof FileChannel)
      {
	try
	  {
	    // Try to use a mapped buffer if we can.  If this fails
	    // for any reason we'll fall back to using a ByteBuffer.
	    buffer = ((FileChannel)src).map (MapMode.READ_ONLY, position, 
					     count);
	  }
	catch (IOException e)
	  {
	  }
      }

    if (buffer == null)
      {
	buffer = ByteBuffer.allocate ((int) count);
	src.read (buffer);
    buffer.flip();
  }

    return write (buffer, position);
  }

  public long transferFrom (ReadableByteChannel src, long position, 
			    long count)
    throws IOException
  {
    if (position < 0
        || count < 0)
      throw new IllegalArgumentException ();

    if (!isOpen ())
      throw new ClosedChannelException ();

    if ((mode & WRITE) == 0)
       throw new NonWritableChannelException ();

    final int pageSize = 65536;
    long total = 0;

    while (count > 0)
      {
	int transferred = smallTransferFrom (src, position, 
					     (int)Math.min (count, pageSize));
	if (transferred < 0)
	  break;
	total += transferred;
	position += transferred;
	count -= transferred;
      }

    return total;
  }

  public FileLock tryLock (long position, long size, boolean shared)
    throws IOException
  {
    if (position < 0
        || size < 0)
      throw new IllegalArgumentException ("position: " + position
					  + ", size: " + size);

    if (!isOpen ())
      throw new ClosedChannelException ();

    if (shared && ((mode & READ) == 0))
      throw new NonReadableChannelException ();
	
    if (!shared && ((mode & WRITE) == 0))
      throw new NonWritableChannelException ();
	
    boolean completed = false;
    
    try
      {
	begin();
	boolean lockable = lock(position, size, shared, false);
	completed = true;
	return (lockable
		? new FileLockImpl(this, position, size, shared)
		: null);
      }
    finally
      {
	end(completed);
      }
  }

  /** Try to acquire a lock at the given position and size.
   * On success return true.
   * If wait as specified, block until we can get it.
   * Otherwise return false.
   */
  private boolean lock(long position, long size,
			      boolean shared, boolean wait) throws IOException
  {
      return fh.lock();
  }
  
  public FileLock lock (long position, long size, boolean shared)
    throws IOException
  {
    if (position < 0
        || size < 0)
      throw new IllegalArgumentException ();

    if (!isOpen ())
      throw new ClosedChannelException ();

    boolean completed = false;

    try
      {
	boolean lockable = lock(position, size, shared, true);
	completed = true;
	return (lockable
		? new FileLockImpl(this, position, size, shared)
		: null);
      }
    finally
      {
	end(completed);
      }
  }

  public long position ()
    throws IOException
  {
    if (!isOpen ())
      throw new ClosedChannelException ();

    return implPosition ();
  }
  
  public FileChannel position (long newPosition)
    throws IOException
  {
    if (newPosition < 0)
      throw new IllegalArgumentException ();

    if (!isOpen ())
      throw new ClosedChannelException ();

    // FIXME note semantics if seeking beyond eof.
    // We should seek lazily - only on a write.
    seek (newPosition);
    return this;
  }
  
  public FileChannel truncate (long size)
    throws IOException
  {
    if (size < 0)
      throw new IllegalArgumentException ();

    if (!isOpen ())
      throw new ClosedChannelException ();

    if ((mode & WRITE) == 0)
       throw new NonWritableChannelException ();

    if (size < size ())
      implTruncate (size);

    return this;
  }
}
