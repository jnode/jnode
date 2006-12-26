package org.jnode.test.bugs;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import junit.framework.TestCase;

import org.jnode.util.ByteBufferInputStream;

/**
 * 
 * @author Andrei DORE
 *
 */
public class TestByteBufferInputStream extends TestCase {
	/**
	 * That test show if the (ByteBuffer)InputStream.available is properly implemented 
	 * or not
	 * @throws IOException
	 */
	public void testWrappedByBufferedInputStream() throws IOException{
		final int SIZE = 5000;
		
		ByteBuffer buffer=ByteBuffer.allocate(SIZE);
		for(int i=0;i<SIZE;i++){
			buffer.put((byte)1);	
		}
		
		buffer.rewind();
		
		ByteBufferInputStream input=new ByteBufferInputStream(buffer);
		
		BufferedInputStream bufferedInputStream=new BufferedInputStream(input,2048);
		
		
		byte data[]=new byte[SIZE];
		
		assertEquals(SIZE, bufferedInputStream.read(data));
	}
}
