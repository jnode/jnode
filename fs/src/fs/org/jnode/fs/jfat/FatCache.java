package org.jnode.fs.jfat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.util.LittleEndian;


public class FatCache {

    private final float loadFactor = 0.75f;

    private final Fat fat;
    private final BlockDeviceAPI api;
    private final long fatsize;
    private final int nrfats;

    private int elementSize;

    private CacheMap map;

    private long access = 0;
    private long hit = 0;

    public FatCache(Fat fat, int cacheSize, int elementSize) {
        this.fat = fat;
        this.api = fat.getApi();
        this.fatsize =
                fat.getBootSector().getSectorsPerFat() * fat.getBootSector().getBytesPerSector();
        this.nrfats = fat.getBootSector().getNrFats();
        this.elementSize = elementSize;

        // allocate the LinkedHashMap
        // that do the dirty LRU job
        this.map = new CacheMap(cacheSize);
    }

    public int getCacheSize() {
        return map.getCacheSize();
    }

    public int usedEntries() {
        return map.usedEntries();
    }

    public int freeEntries() {
        return map.freeEntries();
    }

    private CacheElement put(long address) throws IOException {
        /**
         * get a CacheElement from the stack object pool
         */
        CacheElement c = map.pop();

        /**
         * read the element from the device
         */
        c.read(address);

        /**
         * and insert the element into the LinkedHashMap
         */
        map.put(c);

        /**
         * stack "must" contains at least one entry the placeholder ... so let
         * it throw an exception if this is false
         */
        CacheElement e = map.peek();
        // if an element was discarded from the LRU cache
        // now we can free it ... this will send the element
        // to storage if is marked as dirty
        if (!e.isFree())
            e.free();

        return c;
    }

    private CacheElement get(long address) throws IOException {
        CacheElement c = map.get(address);
        access++;

        // if the cache contains the element just return it, we have a cache hit
        // this will update the LRU order: the LinkedHashMap will make it the
        // newest
        //
        // the cache element cannot be null so we can avoid to call
        // containsKey();
        if (c != null)
            hit++;
        // otherwise put a new element inside the cache
        // possibly flushing and discarding the eldest element
        else
            c = put(address);

        return c;
    }

    private long getUInt32(long offset) throws IOException {
        long addr = (long) (offset / elementSize);
        int ofs = (int) (offset % elementSize);

        byte[] data = get(addr).getData();
        return LittleEndian.getUInt32(data, ofs);
    }

    private void setInt32(long offset, int value) throws IOException {
        long addr = (long) (offset / elementSize);
        int ofs = (int) (offset % elementSize);

        CacheElement c = get(addr);
        byte[] data = c.getData();

        LittleEndian.setInt32(data, ofs, value);

        c.setDirty();
    }

    public long getUInt32(int index) throws IOException {
        return getUInt32(fat.position(0, index));
    }

    public void setInt32(int index, int element) throws IOException {
        setInt32(fat.position(0, index), element);
    }

    public void flush(long address) throws IOException {
        CacheElement c = map.get(address);
        if (c != null)
            c.flush();
    }

    public void flush() throws IOException {
        for (CacheElement c : map.values()) {
            c.flush();
        }
    }

    public long getHit() {
        return hit;
    }

    public long getAccess() {
        return access;
    }

    public double getRatio() {
        if (access > 0)
            return ((double) hit / (double) access);
        else
            return 0.0f;
    }

    public String flushOrder() {
        return map.flushOrder();
    }

    public String toString() {
        StrWriter out = new StrWriter();

        out.print(map);
        out.println("size=" + getCacheSize() + " used=" + usedEntries() + " free=" + freeEntries());

        return out.toString();
    }

    private class CacheMap extends LinkedHashMap<CacheKey, CacheElement> {
        private final int cacheSize;
        private final CacheKey key = new CacheKey();
        private final Stack<CacheElement> free = new Stack<CacheElement>();

        private CacheMap(int cacheSize) {
            super((int) Math.ceil(cacheSize / loadFactor) + 1, loadFactor, true);
            this.cacheSize = cacheSize;

            for (int i = 0; i < cacheSize + 1; i++)
                free.push(new CacheElement());
        }

        private int getCacheSize() {
            return cacheSize;
        }

        private int usedEntries() {
            return size();
        }

        private int freeEntries() {
            return (free.size() - 1);
        }

        private CacheElement peek() {
            return free.peek();
        }

        private CacheElement push(CacheElement c) {
            return free.push(c);
        }

        private CacheElement pop() {
            return free.pop();
        }

        private CacheElement get(long address) {
            key.set(address);
            return get(key);
        }

        private CacheElement put(CacheElement c) {
            return put(c.getAddress(), c);
        }

        /**
         * discard the eldest element when the cache is full
         */
        protected boolean removeEldestEntry(Map.Entry<CacheKey, CacheElement> eldest) {
            boolean remove = (size() > cacheSize);

            /**
             * before going to discard the eldest push it back on the stacked
             * object pool
             */
            if (remove)
                push(eldest.getValue());

            return remove;
        }

        public String flushOrder() {
            StrWriter out = new StrWriter();

            for (CacheElement c : values()) {
                if (c.isDirty())
                    out.print("<" + c.getAddress().get() + ">");
            }

            return out.toString();
        }

        public String toString() {
            StrWriter out = new StrWriter();

            for (CacheElement c : values())
                out.println(c);

            return out.toString();
        }
    }

    /**
     * Here we need to "wrap" a long because Java Long wrapper is an "immutable"
     * type
     */
    private class CacheKey {
        private static final long FREE = -1;

        private long key;

        private CacheKey(long key) {
            this.key = key;
        }

        private CacheKey() {
            free();
        }

        private void free() {
            key = FREE;
        }

        private boolean isFree() {
            return (key == FREE);
        }

        private long get() {
            return key;
        }

        private void set(long value) {
            key = value;
        }

        public int hashCode() {
            return (int) (key ^ (key >>> 32));
        }

        public boolean equals(Object obj) {
            return obj instanceof CacheKey && key == ((CacheKey) obj).get();
        }

        public String toString() {
            return String.valueOf(key);
        }
    }

    private class CacheElement {
        /**
         * CacheKey element is allocated and its reference is stored here to
         * avoid to allocate new CacheKey objects at runtime
         * 
         * In this way .. just one global key will be enough to access
         * CacheElements
         */
        private boolean dirty;
        private CacheKey address;
        private final ByteBuffer elem;

        private CacheElement() {
            this.dirty = false;
            this.address = new CacheKey();
            this.elem = ByteBuffer.wrap(new byte[elementSize]);
        }

        private boolean isFree() {
            return address.isFree();
        }

        private CacheKey getAddress() {
            return address;
        }

        private byte[] getData() {
            return elem.array();
        }

        /**
         * some more work is needed in read and write to handle the multiple fat
         * availability we have to correcly handle the exception to be sure that
         * if we have at least a correct fat we get it - gvt
         */
        private void read(long address) throws IOException {
            if (!isFree())
                throw new IllegalArgumentException("cannot read a busy element");

            this.address.set(address);
            elem.clear();
            api.read(address * elementSize, elem);
            elem.clear();
        }

        private void write() throws IOException {
            if (isFree())
                throw new IllegalArgumentException("cannot write a free element");

            elem.clear();

            long addr = address.get() * elementSize;

            for (int i = 0; i < nrfats; i++) {
                api.write(addr, elem);
                addr += fatsize;
                elem.clear();
            }
        }

        private boolean isDirty() {
            return dirty;
        }

        private void setDirty() {
            dirty = true;
        }

        private void flush() throws IOException {
            if (isDirty()) {
                write();
                dirty = false;
            }
        }

        private void free() throws IOException {
            if (isFree())
                throw new IllegalArgumentException("cannot free a free element");
            flush();
            address.free();
        }

        public String toString() {
            StrWriter out = new StrWriter();

            out.print("address=" + address.get() + " dirty=" + dirty);

            return out.toString();
        }
    }
}
