
package org.jnode.util;

import java.io.OutputStream;
import java.io.IOException;
import java.nio.ByteBuffer; 
import java.nio.channels.WritableByteChannel; 

/**
 * This is a stream wrapper for a WritableByteChannel. This stream
 * buffers the data internally. The buffer contents are written to
 * the channel with the flush() method.
 * 
 * Currently throws an IOException if not all bytes can be written.
 */
public class ChannelOutputStream extends OutputStream
{
	private WritableByteChannel channel;
	private ByteBuffer buffer; 
	
	public ChannelOutputStream (WritableByteChannel c, int bufsize)
	{
		channel = c;
		buffer = ByteBuffer.allocateDirect (bufsize);
	}
	
	public void write (int b) throws IOException
	{
		buffer.put ((byte)b);
		if (!buffer.hasRemaining ())
			flush ();
	}

	public void write (byte[] b) throws IOException, NullPointerException
	{
		flush ();
		out (ByteBuffer.wrap (b));
	}

	public void write (byte[] b, int off, int len)
	  throws IOException, NullPointerException, IndexOutOfBoundsException
	{
		flush ();
		out (ByteBuffer.wrap (b, off, len));
	}

	public void flush () throws IOException
	{
		buffer.flip ();
		out (buffer);
		buffer.clear ();
	}

	public void close () throws IOException
	{
		channel.close ();
	}
	
	private void out (ByteBuffer b) throws IOException
	{
		int n = b.remaining ();
		if (channel.write (b) != n)
			throw new IOException ("could not write all bytes"); 
	}
}
