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
 
package org.jnode.driver.block;

import java.io.IOException;

import org.apache.log4j.Logger;

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
	public void read(long devOffset, byte[] dest, int destOffset, int length)
	throws IOException {
		
      //log.info("Original: devOffset="+ devOffset + " destOffset=" + destOffset + " length=" + length);
      //log.info("Original: alignment=" + alignment);
		if (length == 0) {
			return;
		}
		
		final int ofsMisAlign = (int)(devOffset % alignment);
		final int lenMisAlign = (length % alignment);
		
      //log.info("ofsMisAlign="+ ofsMisAlign + " lenMisAlign=" + lenMisAlign);
        
        
		if ((ofsMisAlign != 0) || (lenMisAlign != 0)) {
			// final byte[] buf = new byte[length + ofsMisAlign + (alignment - lenMisAlign)];
         final byte[] buf = new byte[(length / alignment)  + alignment];
        // log.info("temp buf =" + buf.length);
			parentApi.read(devOffset - ofsMisAlign, buf, 0, buf.length);
			if (ofsMisAlign != 0) {
            System.arraycopy(buf, alignment - ofsMisAlign, dest, destOffset, length);
			} else {
				System.arraycopy(buf, 0, dest, destOffset, length);
			}
		} else {
			// Aligned call, pass on
         //log.info("aligned call");
			parentApi.read(devOffset, dest, destOffset, length);
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
	public void write(long devOffset, byte[] src, int srcOffset, int length)
	throws IOException {
		
		if (length == 0) {
			return;
		}
		
		final int ofsMisAlign = (int)(devOffset % alignment);
		final int lenMisAlign = (length % alignment);
		
		if ((ofsMisAlign != 0) || (lenMisAlign != 0)) {
			log.warn("Very expensive misaligned write called!");
			final byte[] buf = new byte[length + ofsMisAlign + (alignment - lenMisAlign)];
			// TODO: This is very expensive, make it cheaper!
			parentApi.read(devOffset - ofsMisAlign, buf, 0, buf.length);
			if (ofsMisAlign != 0) {
				System.arraycopy(src, srcOffset, buf, alignment - ofsMisAlign, length);
			} else {
				System.arraycopy(src, srcOffset, buf, 0, length);
			}
			parentApi.write(devOffset - ofsMisAlign, buf, 0, buf.length);
		} else {
			// Aligned call, pass on
			parentApi.write(devOffset, src, srcOffset, length);
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
