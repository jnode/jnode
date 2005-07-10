/* PipeImpl.java -- 
   Copyright (C) 2002, 2003, 2004, 2005  Free Software Foundation, Inc.

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

package gnu.java.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.spi.SelectorProvider;

class PipeImpl extends Pipe
{
  public static final class SourceChannelImpl extends Pipe.SourceChannel
  {
    private int native_fd;
    
    public SourceChannelImpl (SelectorProvider selectorProvider,
                              int native_fd)
    {
      super (selectorProvider);
      this.native_fd = native_fd;
    }

    protected final void implCloseSelectableChannel()
      throws IOException
    {
      throw new Error ("Not implemented");
    }

    protected void implConfigureBlocking (boolean blocking)
      throws IOException
    {
      throw new Error ("Not implemented");
    }

    public final int read (ByteBuffer src)
      throws IOException
    {
      throw new Error ("Not implemented");
    }

    public final long read (ByteBuffer[] srcs)
      throws IOException
    {
      return read (srcs, 0, srcs.length);
    }

    public final synchronized long read (ByteBuffer[] srcs, int offset,
					 int len)
      throws IOException
    {
      if (offset < 0
	  || offset > srcs.length
	  || len < 0
	  || len > srcs.length - offset)
	throw new IndexOutOfBoundsException();

      long bytesRead = 0;
      
      for (int index = 0; index < len; index++)
	bytesRead += read (srcs [offset + index]);

      return bytesRead;

    }

    public final int getNativeFD()
    {
      return native_fd;
    }
  }

  public static final class SinkChannelImpl extends Pipe.SinkChannel
  {
    private int native_fd;
    
    public SinkChannelImpl (SelectorProvider selectorProvider,
                            int native_fd)
    {
      super (selectorProvider);
      this.native_fd = native_fd;
    }

    protected final void implCloseSelectableChannel()
      throws IOException
    {
      throw new Error ("Not implemented");
    }

    protected final void implConfigureBlocking (boolean blocking)
      throws IOException
    {
      throw new Error ("Not implemented");
    }

    public final int write (ByteBuffer dst)
      throws IOException
    {
      throw new Error ("Not implemented");
    }

    public final long write (ByteBuffer[] srcs)
      throws IOException
    {
      return write (srcs, 0, srcs.length);
    }

    public final synchronized long write (ByteBuffer[] srcs, int offset, int len)
      throws IOException
    {
      if (offset < 0
	  || offset > srcs.length
	  || len < 0
	  || len > srcs.length - offset)
	throw new IndexOutOfBoundsException();

      long bytesWritten = 0;
      
      for (int index = 0; index < len; index++)
	bytesWritten += write (srcs [offset + index]);

      return bytesWritten;
    }

    public final int getNativeFD()
    {
      return native_fd;
    }
  }

  private SinkChannelImpl sink;
  private SourceChannelImpl source;
  
  public PipeImpl (SelectorProvider provider)
    throws IOException
  {
    super();
    VMPipe.init (this, provider);
  }
    
  public Pipe.SinkChannel sink()
  {
    return sink;
  }

  public Pipe.SourceChannel source()
  {
    return source;
  }
}
