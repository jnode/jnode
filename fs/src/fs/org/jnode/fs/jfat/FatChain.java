/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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
 
package org.jnode.fs.jfat;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.jnode.fs.FileSystemFullException;

/**
 * @author gvt
 * 
 */
public class FatChain {
    private static final Logger log = Logger.getLogger(FatChain.class);

    private final FatFileSystem fs;
    private final Fat fat;

    private int head;
    private boolean dirty;

    private boolean dolog = false;

    private ChainPosition position;
    private ChainIterator iterator;

    public FatChain(FatFileSystem fs, int startEntry) {
        this.fs = fs;
        this.fat = fs.getFat();

        this.position = new ChainPosition();
        this.iterator = listIterator();

        setStartCluster(startEntry);

        this.dirty = false;
    }

    /**
     * Performs validation on the chain.
     *
     * @throws IllegalStateException if the validation fails.
     */
    public void validate() {
        if ((head < 0) || (head > fat.size())) {
            throw new IllegalStateException("illegal head: " + head);
        }
    }

    private void mylog(String msg) {
        log.debug(msg);
    }

    public FatFileSystem getFatFileSystem() {
        return fs;
    }

    public int getStartCluster() {
        return head;
    }

    private void setStartCluster(int value) {
        head = value;

        iterator.reset();
        position.setPosition(0);

        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void flush() {
        dirty = false;
    }

    public ChainIterator listIterator() {
        return new ChainIterator();
    }

    public ChainIterator listIterator(int index) throws IOException {
        return new ChainIterator(index);
    }

    private int getEndCluster() throws IOException {
        int last = 0;
        /*
         * not cheap: we have to follow the whole chain to get the last cluster
         * value
         */
        for (ChainIterator i = listIterator(0); i.hasNext(); last = i.next())
            ;

        return last;
    }

    public int size() throws IOException {
        int count = 0;
        /*
         * not cheap: we have to follow the whole chain to know the chain size
         */
        for (ChainIterator i = listIterator(0); i.hasNext(); i.next())
            count++;

        return count;
    }

    private int allocateTail(int n, int m, int offset, boolean zero) throws IOException {
        if (n <= 0)
            throw new IllegalArgumentException("n<=0");

        if (m < 0)
            throw new IllegalArgumentException("m<0");

        if (offset < 0)
            throw new IllegalArgumentException("offset<0");

        if (dolog)
            mylog("n[" + n + "] m[" + m + "] offset[" + offset + "]");

        final int last;
        int i, found = 0, l = 0;
        int k = (offset > 0) ? 2 : 1;

        for (i = fat.getLastFree(); i < fat.size(); i++) {
            if (fat.isFreeEntry(i)) {
                l = i;
                found++;
            }
            if (found == n)
                break;
        }

        if (found < n) {
            for (i = fat.firstCluster(); i < fat.getLastFree(); i++) {
                if (fat.isFreeEntry(i))
                    found++;
                if (found == n)
                    break;
            }
        }

        if (found < n)
            throw new FileSystemFullException("no free clusters");

        last = l;

        if (dolog)
            mylog("found[" + found + "] last[" + last + "]");

        fat.set(last, fat.eofChain());
        if (dolog)
            mylog(n + "\t|allo|\t" + last + " " + fat.eofChain());

        if (zero) {
            if (dolog)
                mylog(n + "\t|ZERO|\t" + last + " " + fat.eofChain());
            fat.clearCluster(last);
        }

        //
        found = 0;
        l = last;
        i = last;
        //
        for (; found < (n - m - k); i--) {
            if (fat.isFreeEntry(i)) {
                fat.set(i, l);
                if (dolog)
                    mylog((n - found - 1) + "\t|allo|\t" + i + " " + l);
                l = i;
                found++;
            }
        }
        //
        if (offset > 0) {
            for (;; i--) {
                if (fat.isFreeEntry(i)) {
                    fat.clearCluster(i, 0, offset);
                    fat.set(i, l);
                    if (dolog)
                        mylog((n - found - 1) + "\t|part|\t" + i + " " + l);
                    l = i;
                    found++;
                    break;
                }

            }
        }
        //
        for (; found < (n - 1); i--) {
            if (fat.isFreeEntry(i)) {
                fat.clearCluster(i);
                fat.set(i, l);
                if (dolog)
                    mylog((n - found - 1) + "\t|zero|\t" + i + " " + l);
                l = i;
                found++;
            }
        }

        //
        fat.rewindFree();
        //
        for (i = last; i < fat.size(); i++) {
            if (fat.isFreeEntry(i)) {
                fat.setLastFree(i);
                break;
            }
        }

        if (dolog)
            mylog("LastFree: " + fat.getLastFree());

        return l;
    }

    private int allocateTail(int n, int m, int offset) throws IOException {
        return allocateTail(n, m, offset, false);
    }

    private int allocateTail(int n) throws IOException {
        return allocateTail(n, 0, 0);
    }

    /*
     * private void allocate ( int n ) throws IOException { try { int last =
     * allocateTail ( n ); int first = getEndCluster();
     * 
     * if ( dolog ) mylog ( first + ":" + last );
     * 
     * if ( first != 0 ) fat.set ( first, last ); else { if ( dolog ) mylog (
     * "allocate chain" ); setStartCluster ( last ); } } finally { fat.flush(); } }
     */

    public void allocateAndClear(int n) throws IOException {
        try {
            int last = allocateTail(n, n - 1, 0, true);
            int first = getEndCluster();

            if (dolog)
                mylog(first + ":" + last);

            if (first != 0)
                fat.set(first, last);
            else {
                if (dolog)
                    mylog("allocate chain");
                setStartCluster(last);
            }
        } finally {
            fat.flush();
        }
    }

    public void free(int n) throws IOException {
        if (n <= 0)
            throw new IllegalArgumentException("n<=0");

        int count = size();

        if (count < n)
            throw new IOException("not enough cluster: count[" + count + "] n[" + n + "]");

        if (dolog)
            mylog("count[" + count + "] n[" + n + "]");

        ChainIterator i;

        try {
            if (count > n) {
                i = listIterator(count - n - 1);
                int l = i.next();
                fat.set(l, fat.eofChain());
                if (dolog)
                    mylog(l + ":" + fat.eofChain());
            } else
                i = listIterator(0);

            while (i.hasNext()) {
                int l = i.next();
                fat.set(l, fat.freeEntry());
                if (dolog)
                    mylog(l + ":" + fat.freeEntry());
            }
        } finally {
            fat.flush();
        }

        if (count == n) {
            setStartCluster(0);
            if (dolog)
                mylog("zero");
        }
    }

    /*
     * implemented separately for efficiency
     */
    public void freeAllClusters() throws IOException {

        ChainIterator i = listIterator(0);

        try {
            while (i.hasNext()) {
                int l = i.next();
                fat.set(l, fat.freeEntry());
            }
        } finally {
            fat.flush();
        }

        setStartCluster(0);
    }

    public void read(long offset, ByteBuffer dst) throws IOException {
        if (offset < 0)
            throw new IllegalArgumentException("offset<0");

        if (dst.remaining() == 0)
            return;

        ChainPosition p = position;
        ChainIterator i = iterator;

        p.setPosition(offset);

        try {
            i.setPosition(p.getIndex());
        } catch (NoSuchElementException ex) {
            throw new IOException("attempt to seek after End Of Chain " + offset, ex);
        }

        for (int l = dst.remaining(), sz = p.getPartial(), ofs = p.getOffset(), size; l > 0; l -=
                size, sz = p.getSize(), ofs = 0) {

            int cluster = i.next();

            size = Math.min(sz, l);

            if (dolog)
                mylog("read " + size + " bytes from cluster " + cluster + " at offset " + ofs);

            int limit = dst.limit();

            try {
                dst.limit(dst.position() + size);
                fat.readCluster(cluster, ofs, dst);
            } finally {
                dst.limit(limit);
            }
        }
    }

    /*
     * length is used to zero the last cluster allocated to a chain when this is
     * required i.e. from FatFile
     * 
     * when there is no need to zero the cluster at the end of the chain, last
     * cluster, we can use any clsize multiple or zero
     */
    public void write(long length, long offset, ByteBuffer src) throws IOException {

        if (length < 0)
            throw new IllegalArgumentException("length<0");

        if (offset < 0)
            throw new IllegalArgumentException("offset<0");

        // ChainPosition p = new ChainPosition ( offset );
        ChainPosition p = position;
        p.setPosition(offset);
        int clsize = p.getSize();
        int clidx = p.getIndex();

        // int last;
        // int cluster = 0;

        // ChainIterator i = listIterator ( 0 );
        ChainIterator i = iterator;
        int cluster = i.getCluster(clidx);
        int last = i.nextIndex();

        // System.out.println ( "head=" + head + " clidx=" + clidx + " cluster="
        // + cluster + " last=" + last );

        /*
         * for ( last = 0; last < clidx; last++ ) if ( i.hasNext() ) cluster =
         * i.next(); else break;
         */

        try {
            if (last != clidx) {
                int m = clidx - last;

                long lst = offset + src.remaining() - last * clsize;

                int n = (int) (lst / clsize);
                if ((lst % clsize) != 0)
                    n++;

                last = allocateTail(n, m, p.getOffset());

                if (cluster != 0) {
                    fat.set(cluster, last);
                    i.appendChain(last);
                } else {
                    setStartCluster(last);
                    // i = listIterator ( clidx );
                }

                /*
                 * here length is used to decide if we have to zero the data
                 * inside the last cluster tail
                 */
                int ofs = (int) (length % clsize);

                if (ofs != 0)
                    fat.clearCluster(cluster, ofs, clsize);
            }
        } finally {
            fat.flush();
        }

        for (int l = src.remaining(), sz = p.getPartial(), ofs = p.getOffset(), size; l > 0; l -=
                size, sz = clsize, ofs = 0) {

            if (!i.hasNext()) {
                int n = l / clsize;
                if ((l % clsize) != 0)
                    n++;

                try {
                    last = allocateTail(n);

                    if (cluster != 0) {
                        fat.set(cluster, last);
                        i.appendChain(last);
                    } else {
                        setStartCluster(last);
                        // i = listIterator ( 0 );
                    }
                } finally {
                    fat.flush();
                }
            }

            cluster = i.next();

            size = Math.min(sz, l);

            if (dolog)
                mylog("write " + size + " bytes to cluster " + cluster + " at offset " + ofs);

            int limit = src.limit();

            try {
                src.limit(src.position() + size);
                fat.writeCluster(cluster, ofs, src);
            } finally {
                src.limit(limit);
            }
        }
    }

    /*
     * used when we don't need to zero the data inside the last cluster tail
     */
    public void write(long offset, ByteBuffer src) throws IOException {
        write(0, offset, src);
    }

    public long getLength() throws IOException {
        /*
         * not cheap: we have to follow the whole chain to know the chain length
         */
        return size() * fat.getClusterSize();
    }

    public String toString() {
        StrWriter out = new StrWriter();

        boolean first = true;

        int prev = 0;
        int last = 0;

        try {
            ChainIterator i = listIterator(0);

            out.print("[(Start:" + head + ",Size:" + size() + ") ");

            out.print("<");

            while (i.hasNext()) {
                int curr = i.next();

                if (first) {
                    first = false;
                    out.print(curr);
                    last = curr;
                } else if (curr != prev + 1) {
                    if (prev != last)
                        out.print("-" + prev);
                    out.print("> <" + curr);
                    last = curr;
                }

                prev = curr;
            }

            if (prev != last)
                out.print("-" + prev);

            out.print(">]");
        } catch (IOException ex) {
            log.debug("error in chain");
            out.print("error in chain");
        }

        return out.toString();
    }

    /*
     * dump a chain on a file: used for debugging and testing inside the
     * FatChain class size() can and must be used
     */
    public void dump(String fileName) throws IOException, FileNotFoundException {
        int size = size();
        FileOutputStream f = new FileOutputStream(fileName);
        ByteBuffer buf = ByteBuffer.allocate(fat.getClusterSize());

        for (int i = 0; i < size; i++) {
            buf.clear();
            read(i * fat.getClusterSize(), buf);
            buf.flip();
            f.getChannel().write(buf);
        }

        f.close();
    }

    /*
     * dump a chain cluster: used for debugging and testing "inside" the
     * FatChain class
     */
    public void dumpCluster(String fileName, int index) throws IOException, FileNotFoundException {
        FileOutputStream f = new FileOutputStream(fileName);
        ByteBuffer buf = ByteBuffer.allocate(fat.getClusterSize());

        buf.clear();
        read(index * fat.getClusterSize(), buf);
        buf.flip();
        f.getChannel().write(buf);

        f.close();
    }

    private class ChainPosition {
        private long position;
        private int index;
        private int offset;
        private final int size;

        private ChainPosition() {
            this(0);
        }

        private ChainPosition(long pos) {
            this.size = fat.getClusterSize();
            setPosition(pos);
        }

        private final int getIndex() {
            return index;
        }

        private final int getOffset() {
            return offset;
        }

        private final int getSize() {
            return size;
        }

        private final int getPartial() {
            return (size - offset);
        }

        @SuppressWarnings("unused")
        private final long getPosition() {
            return position;
        }

        private final void setPosition(long value) {
            if (value < 0L || value > 0xFFFFFFFFL)
                throw new IllegalArgumentException();
            this.position = value;
            this.index = (int) (value / size);
            this.offset = (int) (value % size);
        }
    }

    public class ChainIterator {
        private int address;
        private int cursor;
        private int index;

        private ChainIterator() {
            reset();
        }

        private ChainIterator(int index) throws IOException {
            this();
            setPosition(index);
        }

        private void reset() {
            address = head;
            cursor = head;
            index = 0;
        }

        private void setPosition(int position) throws IOException {
            if (position < 0)
                throw new IllegalArgumentException("negative index: " + position);

            if (position > index) {
                for (int i = index; i < position; i++)
                    next();
            } else if (position < index) {
                reset();
                for (int i = 0; i < position; i++)
                    next();
            }
        }

        private int getCluster(int position) throws IOException {
            int cluster = 0;

            if (position > index) {
                for (int i = index; i < position; i++)
                    if (hasNext())
                        cluster = next();
                    else
                        break;
            } else if (position < index) {
                reset();
                for (int i = 0; i < position; i++)
                    if (hasNext())
                        cluster = next();
                    else
                        break;
            } else
                cluster = address;

            return cluster;
        }

        /**
         * this method is used to append a new allocated chain to the current
         * chain while is positioned at the end of chain
         * 
         * it can be used if and only if cursor is an EndOfChain it will throw
         * an exception otherwise
         * 
         * chain index is not changed ... the chain remains positioned where it
         * was ...
         */
        private void appendChain(int startCluster) {
            if (!fat.isEofChain(cursor))
                throw new IllegalArgumentException("cannot append to: " + cursor);
            cursor = startCluster;
        }

        public boolean hasNext() {
            return (fat.hasNext(cursor));
        }

        public int next() throws IOException {
            if (!hasNext())
                throw new NoSuchElementException();

            address = cursor;

            cursor = fat.get(address);

            if (cursor == address)
                throw new IOException("circular chain at: " + cursor);

            if (fat.isFree(cursor))
                throw new IOException("free entry in chain at: " + address);

            index++;

            return address;
        }

        private boolean hasPrevious() {
            return !(cursor == head);
        }

        /*
         * Take care: this method is implemented for the sake of interface
         * completness, but it is expensive ... this a "true forward only list"
         * ... the previous element can be recovered only by a complete list
         * scan
         * 
         * ... peraphs an UnsupportedOperationException would be better here ...
         * but who knows? ;-)
         */
        @SuppressWarnings("unused")
        private int previous() throws IOException {
            if (!hasPrevious())
                throw new NoSuchElementException();

            int prev = index - 1;

            reset();

            for (int i = 0; i < prev; i++)
                next();

            return cursor;
        }

        private int nextIndex() {
            return index;
        }

        @SuppressWarnings("unused")
        private int previousIndex() {
            return (index - 1);
        }
    }
}
