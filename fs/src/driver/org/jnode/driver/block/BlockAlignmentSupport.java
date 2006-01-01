/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 * You should have received a copy of the GNU General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.driver.block;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.jnode.util.ByteBufferUtils;

/**
 * @author epr
 */
public class BlockAlignmentSupport implements BlockDeviceAPI {

	/** My logger */
	private static final Logger log = Logger.getLogger(BlockAlignmentSupport.class);
	private final BlockDeviceAPI parentApi;
	private int alignment;

	public BlockAlignmentSupport(BlockDeviceAPI parentApi, int alignment) {
		this.parentApi = parentApi;
		this.alignment = alignment;
		if (alignment < 0) {
			throw new IllegalArgumentException("alignment < 0");
		}
	}

	/**
	 * @see org.jnode.driver.block.BlockDeviceAPI#getLength()
	 * @return The length
	 * @throws IOException
	 */
	public long getLength() throws IOException {
		return parentApi.getLength();
	}

	/**
	 * @param devOffset
	 * @param dest
	 * @param destOffset
	 * @param length
	 * @see org.jnode.driver.block.BlockDeviceAPI#read(long, byte[], int, int)
	 * @throws IOException
	 */
	public void read(long devOffset, ByteBuffer dest)
	throws IOException {
      //log.info("Original: devOffset="+ devOffset + " destOffset=" + destOffset + " length=" + length);
      //log.info("Original: alignment=" + alignment);
		if (dest.remaining() == 0) {
			return;
		}
		
		final int ofsMisAlign = (int)(devOffset % alignment);
		final int lenMisAlign = (dest.remaining() % alignment);
		
      //log.info("ofsMisAlign="+ ofsMisAlign + " lenMisAlign=" + lenMisAlign);
        
        
		if ((ofsMisAlign != 0) || (lenMisAlign != 0)) {
			// final byte[] buf = new byte[length + ofsMisAlign + (alignment - lenMisAlign)];
         final ByteBuffer buf = ByteBuffer.allocate((dest.remaining() / alignment)  + alignment);
        // log.info("temp buf =" + buf.length);
			parentApi.read(devOffset - ofsMisAlign, buf);
			if (ofsMisAlign != 0) {
                ByteBufferUtils.buffercopy(buf, alignment - ofsMisAlign, dest, dest.position(), dest.remaining());
			} else {
                ByteBufferUtils.buffercopy(buf, 0, dest, dest.position(), dest.remaining());
			}
		} else {
			// Aligned call, pass on
         //log.info("aligned call");
			parentApi.read(devOffset, dest);
		}
	}

	/**
	 * @param devOffset
	 * @param src
	 * @param srcOffset
	 * @param length
	 * @see org.jnode.driver.block.BlockDeviceAPI#write(long, byte[], int, int)
	 * @throws IOException
	 */
	public void write(long devOffset, ByteBuffer src)
	throws IOException {
		
		if (src.remaining() == 0) {
			return;
		}
		
		final int ofsMisAlign = (int)(devOffset % alignment);
		final int lenMisAlign = (src.remaining() % alignment);
		
		if ((ofsMisAlign != 0) || (lenMisAlign != 0)) {
			log.warn("Very expensive misaligned write called!");
			final ByteBuffer buf = ByteBuffer.allocate(src.remaining() + ofsMisAlign + (alignment - lenMisAlign));
			// TODO: This is very expensive, make it cheaper!
			parentApi.read(devOffset - ofsMisAlign, buf);
			if (ofsMisAlign != 0) {
				ByteBufferUtils.buffercopy(src, src.position(), buf, alignment - ofsMisAlign, src.remaining());
			} else {
                ByteBufferUtils.buffercopy(src, src.position(), buf, 0, src.remaining());
			}
            buf.clear();
			parentApi.write(devOffset - ofsMisAlign, buf);
		} else {
			// Aligned call, pass on
			parentApi.write(devOffset, src);
		}
	}

	/**
	 * @see org.jnode.driver.block.BlockDeviceAPI#flush()
	 * @throws IOException
	 */
	public void flush() throws IOException {
		parentApi.flush();
	}

	/**
	 * Gets the alignment value
	 * @return alignment
	 */
	public int getAlignment() {
		return alignment;
	} 
	
	/**
	 * @param i
	 */
	public void setAlignment(int i) {
		alignment = i;
	}
}
