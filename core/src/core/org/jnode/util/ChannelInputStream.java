/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
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
