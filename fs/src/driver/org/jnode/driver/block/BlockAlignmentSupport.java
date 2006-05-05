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
 * @author gvt
 *         <p/>
 *         We need a picture to understand this little evil:
 *         <p/>
 *         |----------------|- n ------------------------------------> start
 *         |                |                                            |
 *         | ^^^^ HEAD ^^^^ |^^^^^^^^^^^^^^^^^^^^^^^^^^^> offset         |
 *         | ^            ^ |                               |            |
 *         |----------------|- n+1                          |            |
 *         | ^            ^ |                               |            |
 *         .            .                                 |            |
 *         .   BODY     .                                 |            |
 *         |            |
 *         | ^            ^ |                             length        lA
 *         |----------------|- q-1                          |            |
 *         | ^            ^ |                               |            |
 *         | ^   BODY     ^ |                               |            |
 *         | ^            ^ |                               |            |
 *         |----------------|- q = (n+m+k)                  |            |
 *         | ^            ^ |                               |            |
 *         | ^^^^ TAIL ^^^^ |^^^^^^^^^^^^^^^^^^^^^^^^^^^>  lst           |
 *         |                |                                            |
 *         |----------------|- q+1 = (n+m+k+p) = n+nA ----------------> end
 *         <p/>
 *         <p/>
 *         where: offset >= 0, length >= 0, align > 0 are given and
 *         lst = (offset+length).
 *         <p/>
 *         The "Buffer" is defined, in terms of offsets of bytes that
 *         have to be written to or read from the device, by the set:
 *         <p/>
 *         U = { i | offset <= i < lst }
 *         <p/>
 *         and the "Aligned Buffer", that have to be handled on the device
 *         is defined by:
 *         <p/>
 *         A = { i | start <= i < end }
 *         <p/>
 *         where
 *         <p/>
 *         start =  max { i>=0 | i <= offset, i % align = 0 }
 *         <p/>
 *         end   =  min { i>=0 | i >= lst,    i % align = 0 }
 *         <p/>
 *         "A" is the "smallest" aligned buffer that contains U, i.e.
 *         start = min(A) and end = max(A) are multiples of "align"
 *         and there are no "smallest sets" (in terms of the number
 *         of elements o(A)) that contains "U".
 *         <p/>
 *         Let me define:
 *         <p/>
 *         n = offset / align      ofsR = offset % align
 *         m = length / align      lenR = length % align
 *         q = lst    / align      lstR = lst    % align
 *         <p/>
 *         where "/" is the euclidean integer division and "%" is
 *         the euclidean integer remains (or modulo), the usual
 *         Java tokens for these operators.
 *         <p/>
 *         From the definition of Euclidean division we have:
 *         <p/>
 *         offset = n * align + ofsR   0 <= ofsR < align
 *         length = m * align + lenR   0 <= lenR < align
 *         lst    = q * align + lstR   0 <= lstR < align
 *         <p/>
 *         It is very easy to see that:
 *         <p/>
 *         start = offset - ofsR = n * align
 *         <p/>
 *         end   = lst - lstR + p * align = (n+m+k+p) * align
 *         <p/>
 *         where
 *         / 0, lstR = 0            / 0, (ofsR+lenR) < align
 *         p = |                    k = |
 *         \ 1, lstR > 0            \ 1, (ofsR+lenR) >= align
 *         <p/>
 *         The last equation come from this fact:
 *         <p/>
 *         lst = offset + length = (n+m)*align + (ofsR+lenR) =
 *         = (n+m)*align + (k*align + lstR) =
 *         = (n+m+k)*align + lstR   i.e.   q = n + m + k.
 *         <p/>
 *         Hence, the "order of A", o(A), the number of elements in A is:
 *         <p/>
 *         lA = o(A) = (end - start) = (n + m + k + p) * align
 *         <p/>
 *         and the number of "aligned blocks" in "A" is:
 *         <p/>
 *         nA = lA/align = n + m + k + p
 *         <p/>
 *         that is the "order" o(AA), of the set:
 *         <p/>
 *         AA = { i/align | i % align = 0, start <= i < end } =
 *         = { n, n+1, n+2, ..., n+m+k+p-1 }
 *         <p/>
 *         These considerations establish the basic quantities I need
 *         to correcly decide how the input buffer have to be read or
 *         written to the device, device that is supposed to be able
 *         to handle only aligned blocks, i.e. offsets and length
 *         that are intger multiples of "align".
 *         <p/>
 *         Now, we want to define what are the HEAD "H", the BODY "B" and the
 *         TAIL "T" of the "Aligned Buffer". Again they will defined as
 *         sets, "0" will be the "empty set" and we will use
 *         the notation nH=o(H), nB=o(B), nT=o(T) for the number of
 *         their elements.
 *         <p/>
 *         Let me to get rid of the "EMPTY" trivial, where nothing have to
 *         be done (length=0): we define H=0, B=0, T=0. So nB=nA=0.
 *         <p/>
 *         Although it can be handled as a "BODY" only buffer, we get
 *         rid of the "ALIGNED" trivial case too, (ofsR=0 and lstR=0),
 *         definining H=0, defining H=0, B=AA, T=0. So nB=nA=m.
 *         <p/>
 *         Anyway the EMPTY and the ALIGNED cases have to be handled
 *         separately in the code for efficiency purpouses.
 *         <p/>
 *         From now we can suppose (length>0) and (ofsR>0 or lstR>0).
 *         <p/>
 *         Again we have to handle a special case, the case where
 *         the "Buffer" is completely (and properly) "CONTAINED"
 *         inside a "single aligned block". The case is identified
 *         by the condition "lst <= start + align" and for the assumptions
 *         we have done "0 < length < align". For this case it doesn't
 *         make any sense to define HEAD, BODY and TAIL but we define
 *         as an artifact, for the sake of generality, H=0, B=0, T=0.
 *         We have "nB = 0 and nA = 1", so "nB = nA - 1".
 *         <p/>
 *         From now we can assume that "lst > start + align", i.e.
 *         the "Buffer" is "CROSSED", it "cross" the boundary of
 *         at least "one aligned block". We can now define:
 *         <p/>
 *         H =  { i/align | i not in U, i % align = 0,
 *         start <= i < start + align }
 *         <p/>
 *         B =  { i/align | i not in U, i % align = 0,
 *         start + align <= i < end - align }
 *         <p/>
 *         T =  { i/align | i not in U, i % align = 0,
 *         end - align <= i < end }
 *         <p/>
 *         It is easy to see that "AA" is the union of the mutually disjoint
 *         sets "H", "B" and "T". So "nA = nH + nB + nT" where:
 *         <p/>
 *         0 <= nH <= 1     0 <= nT <= 1    0 <= nB <= nA
 *         <p/>
 *         and that "nB = nA - nH - nT".
 *         <p/>
 *         The "Aligned Buffer" will:
 *         <p/>
 *         has an HEAD (nH=1) if and only if ofsR > 0
 *         has a  TAIL (nT=1) if and only if lstR > 0
 *         <p/>
 *         and because we excluded the "ALIGNED" case, it has to have
 *         the HEAD or the TAIL because ofsR>0 or lstR>0.
 *         <p/>
 *         As an example if the "Aligned Buffer" is CROSSED, has an HEAD,
 *         a non empty BODY and a TAIL ( k=1, p=1, m>=1 ):
 *         <p/>
 *         H = { n },  B = { n+1, n+2, ..., n+m }, T = { n+m+1 }
 *         A = { n, n+1, n+2, ..., n+m, n+m+1 }
 *         <p/>
 *         Then:
 *         / nA-1, (HEAD)    and (no TAIL), ofsR>0 and lstR>0
 *         |
 *         nB = |  nA-1, (no HEAD) and (TAIL),    ofsR=0 and lstR=0
 *         |
 *         \ nA-2, (HEAD)    and (TAIL),    ofsR>0 and lstR>0
 *         <p/>
 *         In general, using the positions we have done for the EMPTY and
 *         ALIGNED case:
 *         <p/>
 *         / nA=0, EMPTY           , length = 0
 *         |  nA=m, ALIGNED         , ofsR = 0, lstR = 0
 *         nB = |  nA-1, CONTAINED       , lst <= start + align
 *         |  nA-1, CROSSED(H)      , lst > start + align, ofsR>0, lstR=0
 *         |  nA-1, CROSSED(T)      , lst > start + align, ofsR=0, lstR>0
 *         \ nA-2, CROSSED(HT)     , lst > start + align, ofsR
 *         <p/>
 *         Now you can ask "why all of this semi math stuff?" ... ;-) ...
 *         well it helps ... believe me ... it actually is a "proof" that
 *         the code inside the "BufferType" inner class does a correct job ...
 *         I hope ... the constructor of the helper "BufferType" class
 *         uses the "(offset,length)" couple to compute some of this
 *         quantities and use them to decide if the "Buffer" at the given
 *         offset is EMPTY, ALIGNED, CONTAINED or CROSSED and correctly
 *         compute nB for all the cases.
 *         <p/>
 *         The code of the "read", "write" methods uses the type of the
 *         buffer, ofsR, lstR and nB to decide how it has to
 *         handle the "alignment" of the blocks.
 *         <p/>
 *         Infact, speaking roughly, beside the trivial "EMPTY", "ALIGNED" cases
 *         and the "CONTAINED" case, in the general "CROSSED" case we can optimize
 *         read and write for "BODY" of the buffer. We cannot do anything for
 *         the CONTAINED case and for the HEAD and the TAIL of the buffer.
 *         <p/>
 *         The basic idea is to reduce the number of "buffercopy" (read method)
 *         and the number of "read/buffercopy" (write method) to the minimum
 *         possible number (that actually is nA-nB). At most it will "buffercopy"
 *         or "read/buffercopy" 2 blocks (because 0<=nA-nB<=2).
 *         <p/>
 *         Hope this long comment will help to understand and verify the code
 *         and to understand how bad my brain is working ... ;-) ...
 *         <p/>
 *         gvt
 */
public class BlockAlignmentSupport implements BlockDeviceAPI {
    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(BlockAlignmentSupport.class);
    private final BlockDeviceAPI parentApi;
    private int alignment;
    private boolean dolog = false;


    public BlockAlignmentSupport(BlockDeviceAPI parentApi, int alignment) {
        this.parentApi = parentApi;
        this.alignment = alignment;
        if (alignment <= 0) {
            throw new IllegalArgumentException("alignment <= 0");
        }
    }


    private void mylog(String msg) {
        if (dolog)
            log.debug(msg);
    }


    /**
     * @return The length
     * @throws IOException
     * @see BlockDeviceAPI#getLength()
     */
    public long getLength() throws IOException {
        return parentApi.getLength();
    }


    /**
     * @param offset
     * @param dst
     * @throws IOException
     * @see BlockDeviceAPI#read(long, ByteBuffer)
     */
    public void read(long offset, ByteBuffer dst)
            throws IOException {
        BufferType t = new BufferType(offset, dst);

        mylog("\n" + t);

        switch (t.type) {
            case BufferType.EMPTY:
                break;

            case BufferType.CONTAINED: {
                mylog("read[CONT] " + t.toBlockString(t.ofsR, t.length));
                //
                ByteBuffer buf = ByteBuffer.allocate(alignment);
                parentApi.read(t.start, buf);
                t.copyFrom(buf, t.ofsR, t.length);
            }
            break;

            case BufferType.CROSSED:
                ByteBuffer buf = ByteBuffer.allocate(alignment);

                if (t.ofsR != 0) {
                    mylog("read[HEAD] " + t.toBlockString(t.ofsR, alignment - t.ofsR));
                    //
                    parentApi.read(t.start, buf);
                    t.copyFrom(buf, t.ofsR, alignment - t.ofsR);
                }
                //
                if (t.lB != 0) {
                    mylog("read[BODY] " + t.toBlockString(t.sB, t.lB));
                    //
                    t.limit(t.lB);
                    parentApi.read(t.sB, dst);
                    t.restore();
                }
                //
                if (t.lstR != 0) {
                    mylog("read[TAIL] " + t.toBlockString(t.end - alignment, t.lstR));
                    //
                    if (t.ofsR != 0)
                        buf.clear();
                    //
                    parentApi.read(t.end - alignment, buf);
                    t.copyFrom(buf, 0, t.lstR);
                }
                break;

            case BufferType.ALIGNED:
                mylog("read[ALGN] " + t.toBlockString(t.offset, t.length));
                //
                parentApi.read(offset, dst);
                break;

            default:
                throw new IllegalArgumentException
                        ("no type: shouldn't happen");
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
        BufferType t = new BufferType(offset, src);

        mylog("\n" + t);

        switch (t.type) {
            case BufferType.EMPTY:
                return;

            case BufferType.CONTAINED: {
                mylog("write[CONT] " + t.toBlockString(t.ofsR, t.length));
                //
                ByteBuffer buf = ByteBuffer.allocate(alignment);
                parentApi.read(t.start, buf);
                t.copyTo(buf, t.ofsR, t.length);
                //
                buf.clear();
                parentApi.write(t.start, buf);
            }
            break;

            case BufferType.CROSSED:
                ByteBuffer buf = ByteBuffer.allocate(alignment);

                if (t.ofsR != 0) {
                    mylog("write[HEAD] " + t.toBlockString(t.ofsR, alignment - t.ofsR));
                    //
                    parentApi.read(t.start, buf);
                    t.copyTo(buf, t.ofsR, alignment - t.ofsR);
                    //
                    buf.clear();
                    parentApi.write(t.start, buf);
                }
                //
                if (t.lB != 0) {
                    mylog("write[BODY] " + t.toBlockString(t.sB, t.lB));
                    //
                    t.limit(t.lB);
                    parentApi.write(t.sB, src);
                    t.restore();
                }
                //
                if (t.lstR != 0) {
                    mylog("write[TAIL] " + t.toBlockString(t.end - alignment, t.lstR));
                    //
                    if (t.ofsR != 0)
                        buf.clear();
                    //
                    parentApi.read(t.end - alignment, buf);
                    t.copyTo(buf, 0, t.lstR);
                    //
                    buf.clear();
                    parentApi.write(t.end - alignment, buf);
                }
                break;

            case BufferType.ALIGNED:
                mylog("writeALGN] " + t.toBlockString(t.offset, t.length));
                //
                parentApi.write(offset, src);
                break;

            default:
                throw new IllegalArgumentException
                        ("no type: shouldn't happen");
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


    private class BufferType {
        static private final int EMPTY = 0;
        static private final int CONTAINED = 1;
        static private final int CROSSED = 2;
        static private final int ALIGNED = 3;

        private final ByteBuffer buffer;

        private final long offset;
        private final int length;

        private final int ofsR;
        private final long lst;
        private final int lstR;

        private final long start;
        private final long end;

        private final int lA;
        private final int nA;

        private final int lB;
        private final long sB;

        private int type;


        private BufferType(long offset, ByteBuffer buffer) {
            if (offset < 0)
                throw new IllegalArgumentException
                        ("offset < 0");

            this.buffer = buffer;

            this.offset = offset;
            this.length = buffer.remaining();

            this.ofsR = (int) (offset % alignment);

            this.lst = offset + length;
            this.lstR = (int) (lst % alignment);

            this.start = offset - ofsR;
            this.end = (lstR == 0) ? lst : lst - lstR + alignment;

            this.lA = (int) (end - start);
            this.nA = lA / alignment;

            int nB = nA;
            long s = start;

            if (length == 0) { // EMPTY
                type = EMPTY;
            } else {
                if ((ofsR != 0) || (lstR != 0)) {
                    if (lst <= (start + alignment)) { // CONTAINED
                        type = CONTAINED;
                        nB--;
                    } else {                                  // CROSSED
                        type = CROSSED;
                        //
                        if (ofsR != 0) {      // HEAD
                            nB--;
                            s += alignment;
                        }
                        //
                        if (lstR != 0)        // TAIL
                            nB--;
                    }
                } else { // ALIGNED
                    type = ALIGNED;
                }
            }

            lB = nB * alignment;
            sB = s;
        }


        private void copyFrom(ByteBuffer src, int ofs, int len) {
            if (len <= 0)
                throw new IllegalArgumentException
                        ("len <= 0, shouldn't happen");

            if (ofs < 0)
                throw new IllegalArgumentException
                        ("ofs < 0, shouldn't happen");

            ByteBufferUtils.buffercopy
                    (src, ofs, buffer, buffer.position(), len);
        }


        private void copyTo(ByteBuffer dst, int ofs, int len) {
            if (len <= 0)
                throw new IllegalArgumentException
                        ("len <= 0, shouldn't happen");

            if (ofs < 0)
                throw new IllegalArgumentException
                        ("ofs < 0, shouldn't happen");

            ByteBufferUtils.buffercopy
                    (buffer, buffer.position(), dst, ofs, len);
        }


        private void limit(int len) {
            if (len <= 0)
                throw new IllegalArgumentException
                        ("len <= 0, shouldn't happen");

            buffer.limit(buffer.position() + len);
        }


        private void restore() {
            buffer.limit(length);
        }


        private String toBlockString(long offset, long length) {
            StringBuilder str = new StringBuilder();

            switch (type) {
                case EMPTY:
                    str.append("Empty");
                    break;

                case CONTAINED:
                    str.append("Contained: ");
                    break;

                case CROSSED:
                    str.append("Crossed: ");
                    break;

                case ALIGNED:
                    str.append("Aligned: ");
                    break;

                default:
                    throw new IllegalArgumentException
                            ("no type: shouldn't happen");
            }

            if (type != EMPTY)
                str.append("pos[" + buffer.position() + "] " +
                        "ofs[" + offset + "] " +
                        "len[" + length + "]");

            return str.toString();
        }


        private String toTypeString() {
            StringBuilder str = new StringBuilder();

            switch (type) {
                case EMPTY:
                    str.append("Empty");
                    break;

                case CONTAINED:
                    str.append("Contained[" + start + ":" + length + "]");
                    break;

                case CROSSED:
                    str.append("Crossed ");
                    if (ofsR != 0)
                        str.append("H[" + start + ":" + ofsR + "] ");
                    if (lB != 0)
                        str.append("B[" + sB + ":" + lB + "] ");
                    if (lstR != 0)
                        str.append("T[" + (end - alignment) + ":" + lstR + "]");
                    break;

                case ALIGNED:
                    str.append("Aligned[" + start + ":" + lA + "]");
                    break;

                default:
                    throw new IllegalArgumentException
                            ("no type: shouldn't happen");
            }

            return str.toString();
        }


        public String toString() {
            StringBuilder str = new StringBuilder();

            str.append("offset[" + offset + "] " +
                    "length[" + length + "] " +
                    "lst[" + lst + "] " +
                    "alignment[" + alignment + "]\n");

            str.append("ofsR[" + ofsR + "] " +
                    "lstR[" + lstR + "] " +
                    "start[" + start + "] " +
                    "end[" + end + "] " +
                    "lA[" + lA + "] " +
                    "sB[" + sB + "] " +
                    "lB[" + lB + "]\n");

            str.append(toTypeString());

            return str.toString();
        }
    }
}
