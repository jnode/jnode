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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.driver.block;

import org.apache.log4j.Logger;
import org.jnode.util.ByteBufferUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author epr
 */
public class BlockAlignmentSupport implements BlockDeviceAPI {
    /**
     * My logger
     */
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
     * @return The length
     * @throws IOException
     * @see BlockDeviceAPI#getLength()
     */
    public long getLength() throws IOException {
        return parentApi.getLength();
    }

    /*
    * gvt comment: only a picture let to understand this little evil ;-)
    *
    *  offset - ofsR |-----------|                     length - lenR
    *                |           |                     offset - ofsR
    *                |^^^^^^^^^^^| offset
    *                |-----------|                     are integer
    *  (--- is the   |           |  (^^^ are           multiples of
    *    Buffer )    |           |    Data)            "alignment"
    *                |-----------|
    *                |           |
    *  alignment +   |^^^^^^^^^^^| offset + length
    *  length - lenR |-----------|
    *
    *
    *                ofsR = offset % alignment
    *  let me set:
    *                lenR = length % alignment
    *
    *  so (offset - ofsR) and (length - lenR) are integer multiples of
    *  alignment, i.e. n*alignment and m*aligment
    *
    *  if the I/O operation is done at (offset - ofsR) position, with
    *  a length of [alignment + (length - lenR)], then, by definition
    *  the operation is "alignment" aligned ... ;-) ...
    *
    *  the "real" data will be in the buffer at "ofsR" position in any
    *  case ... I hope ... ;-) ...
    *
    *
    *  as observed from ewout, this way to go, is very "expensive"
    *  in the "write" side of the I/O ... we have to read the block.
    *  copy the data to be written and only then we can write the block
    *  back on the device
    *
    *  probably we can use a "write through" buffer here: the idea
    *  is to define a buffer (with a length that is a multiple of alignment)
    *  and then we can read and write "through" this buffer
    */


    /**
     * @param offset
     * @param dst
     * @throws IOException
     * @see BlockDeviceAPI#read(long, ByteBuffer)
     */
    public void read(long offset, ByteBuffer dst)
            throws IOException {
        final int length = dst.remaining();

        //log.info ( "offset=" + offset + " with length=" + length +
        //	   " to be " + alignment + " aligned" );

        if (length == 0)
            return;

        final int ofsR = (int) (offset % alignment);
        final int lenR = (int) (length % alignment);

        //log.info ( "ofsR=" + ofsR + " lenR=" + lenR );

        if ((ofsR != 0) || (lenR != 0)) {
            final int buflen = alignment + (length - lenR);

            //log.info ( "buffer length is "  +  buflen +
            //	       " to be read at "    +  (offset - ofsR) +
            //	       " and has data at "  +  ofsR );

            ByteBuffer buf = ByteBuffer.allocate(buflen);

            parentApi.read(offset - ofsR, buf);

            ByteBufferUtils.buffercopy
                    (buf, ofsR, dst, dst.position(), length);
        } else {
            //log.info ( "aligned call" );
            parentApi.read(offset, dst);
        }
    }


    /**
     * @param offset
     * @param src
     * @throws IOException
     * @see BlockDeviceAPI#write(long, ByteBuffer)
     */
    public void write(long offset, ByteBuffer src)
            throws IOException {
        int length = src.remaining();

        //log.info ( "offset=" + offset + " with length=" + length +
        //           " to be " + alignment + " aligned" );

        if (length == 0)
            return;

        final int ofsR = (int) (offset % alignment);
        final int lenR = (int) (length % alignment);

        //log.info ( "ofsR=" + ofsR + " lenR=" + lenR );

        if ((ofsR != 0) || (lenR != 0)) {
            final int buflen = alignment + (length - lenR);

            //log.info ( "buffer length is "  +  buflen +
            //	       " to be written at " +  (offset - ofsR) +
            //	       " and has data at "  +  ofsR );

            ByteBuffer buf = ByteBuffer.allocate(buflen);

            // TODO: this is very expensive, make it cheaper!
            // it can be done buffering the IO at this level? (gvt)
            log.warn("Very expensive misaligned write called!");

            parentApi.read(offset - ofsR, buf);

            ByteBufferUtils.buffercopy
                    (src, src.position(), buf, ofsR, length);

            buf.clear();

            parentApi.write(offset - ofsR, buf);
        } else {
            //log.info ( "aligned call" );
            parentApi.write(offset, src);
        }

    }


    /**
     * @throws IOException
     * @see BlockDeviceAPI#flush()
     */
    public void flush() throws IOException {
        parentApi.flush();
    }


    /**
     * Gets the alignment value
     *
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
