
package org.jnode.util;

import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer; 
import java.nio.channels.ReadableByteChannel; 

/**
 * This is a stream wrapper for a ReadableByteChannel. 
 */
public class ChannelInputStream extends InputStream
{
	private ReadableByteChannel channel;
	private ByteBuffer buffer; 
	
	public ChannelInputStream (ReadableByteChannel c)
	{
		channel = c;
		buffer = ByteBuffer.allocateDirect (1);
	}
	
	public int read() throws IOException
	{
		buffer.clear ();
		if (channel.read (buffer) < 1) return -1;
		else return buffer.get ();
	}
	
	public void close() throws IOException
	{
		channel.close ();
	}

	public int read(byte[] b) throws IOException
	{
		return channel.read (ByteBuffer.wrap (b));
	}
	
	public int read(byte[] b, int off, int len) throws IOException
	{
		return channel.read (ByteBuffer.wrap (b, off, len));
	}
	
	public long skip (long n) throws IOException
	{
		ByteBuffer b = ByteBuffer.allocateDirect (2048);
		long rem = n;
		
		while (rem > 2048) {
			int num = channel.read (b);
			if (num < 1) return n - rem;
			rem -= num; 
			b.rewind ();
		}
		
		int x = (int)rem;
		b.limit (x);
		return n - rem + channel.read (b);
	}
	
	public boolean markSupported()
	{
		return false;
	}

	public void mark(int readlimit)
	{
		// Do nothing
	}

	public void reset() throws IOException
	{
		throw new IOException("mark/reset not supported");
	}
}
