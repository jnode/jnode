/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.net;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;

/**
 * A SocketBuffer is container of a network packet. It enables efficient storage
 * even when various network layers prefix and/or postfix header/footers. It
 * also contains other information of a network packet, such as the headers of
 * the various network layers.
 * 
 * All numbers larger then a single byte written into this class are converted
 * to network byte order.
 * 
 * All numbers larger then a single byte read from this class are converted from
 * network byte order.
 * 
 * @author epr
 */
public class SocketBuffer {

    private static final int INITIAL_SIZE = 256;

    /** My logger */
    private static final Logger log = Logger.getLogger(SocketBuffer.class);
    /** Actual data */
    private byte[] data;
    /** Size of the buffer that is in use */
    private int size;
    /** Start offset in data */
    private int start;
    /** Next buffer, that is concatenated with this one */
    private SocketBuffer next;

    /** The network device who will be sending, or has received this buffer */
    private Device device;
    /** Identifying type of the packettype */
    private int protocolID;
    /** Link layer header (if any) */
    private LinkLayerHeader linkLayerHeader;
    /** Network layer header (if any) */
    private NetworkLayerHeader networkLayerHeader;
    /** Transport layer header (if any) */
    private TransportLayerHeader transportLayerHeader;

    /**
     * Create a new instance
     */
    public SocketBuffer() {
    }

    /**
     * Create a new instance with a buffer of a given capacity
     */
    public SocketBuffer(int initialCapacity) {
        this(initialCapacity, 0);
    }

    /**
     * Create a new instance with a buffer of a given capacity
     */
    public SocketBuffer(int initialCapacity, int initialStart) {
        this.data = new byte[initialCapacity];
    }

    /**
     * Create a clone of the data of src. Other attributes are not cloned!.
     * 
     * @param src
     */
    public SocketBuffer(SocketBuffer src) {
        this.start = 0;
        this.size = src.getSize();
        this.data = src.toByteArray();
        this.next = null;
    }

    /**
     * Create a new instance, using the given byte array as data. No copy of the
     * data is made!
     * 
     * @param data
     * @param offset
     * @param length
     */
    public SocketBuffer(byte[] data, int offset, int length) {
        this.data = new byte[data.length];
        System.arraycopy(data, 0, this.data, 0, data.length);
        this.start = offset;
        this.size = length;
        testBuffer();
    }

    /**
     * Gets the network device who will be sending, or has received this buffer
     */
    public Device getDevice() {
        return device;
    }

    /**
     * Sets the network device who will be sending, or has received this buffer
     * 
     * @param device
     */
    public void setDevice(Device device) {
        this.device = device;
    }

    /**
     * Gets the identifying type of the packettype.
     */
    public int getProtocolID() {
        return protocolID;
    }

    /**
     * Sets the identifying type of the packettype.
     * 
     * @param i
     */
    public void setProtocolID(int i) {
        protocolID = i;
    }

    /**
     * Clear this buffer, so it can be used for another purpose
     * 
     */
    public void clear() {
        size = 0;
        start = 0;
        next = null;
        protocolID = 0;
        linkLayerHeader = null;
        networkLayerHeader = null;
        transportLayerHeader = null;
        device = null;
        // preserve data (if set), we can used it again
    }

    /**
     * Insert a given number of bytes to the front of the buffer. The inserted
     * bytes are cleaned with a value of <code>(byte)0</code>.
     * 
     * @param count
     */
    public void insert(int count) {
        if (start >= count) {
            start -= count;
            size += count;
        } else {
            setSize(size + count);
            for (int i = size - 1; i >= count; i--) {
                data[start + i] = data[start + i - count];
            }
        }
        for (int i = 0; i < count; i++) {
            data[start + i] = 0;
        }
        testBuffer();
    }

    /**
     * Remove a given number of bytes from the front of the buffer
     * 
     * @param count
     */
    public void pull(int count) {
        if (count > size) {
            if (next != null) {
                // Pull a bit of myself and the rest of next
                count -= size;
                start = size;
                size = 0;
                next.pull(count);
            } else {
                throw new IllegalArgumentException("Cannot pull " + count + " bytes (" + start +
                        "," + size + ")");
            }
        } else {
            start += count;
            size -= count;
        }
        testBuffer();
    }

    /**
     * Undo a pull action. This method is different from insert, as this method
     * can only unpull that what has been removed by an earlier call to pull,
     * insert will actually make new room at the head on the buffer.
     * 
     * @param count
     * @throws IllegalArgumentException It is not possible to unpull count
     *             bytes.
     */
    public void unpull(int count) {
        if (start >= count) {
            start -= count;
            size += count;
        } else {
            if (next != null) {
                // Unpull most of next and that what I can from me
                final int remaining = (count - start);
                size += start;
                start = 0;
                next.unpull(remaining);
            } else {
                throw new IllegalArgumentException("Cannot unpull " + count + " bytes (" + start +
                        "," + size + ")");
            }
        }
        testBuffer();
    }

    /**
     * Remove data from the tail of the buffer, until size <= length. If the
     * current size < length, nothing happens.
     * 
     * @param length
     */
    public void trim(int length) {
        if (length < size) {
            // Cut the tail of myself and remove any next buffer
            size = length;
            next = null;
        } else if (length == size) {
            // Remove any next buffer
            next = null;
        } else {
            // Length > size
            if (next != null) {
                next.trim(length - size);
            }
        }
    }

    /**
     * Insert a given number of bytes to the back of the buffer
     * 
     * @param count
     */
    public void append(int count) {
        if (next != null) {
            next.append(count);
        } else {
            setSize(size + count);
        }
        testBuffer();
    }

    /**
     * Insert a given number of bytes to the front of the buffer
     * 
     * @param src
     * @param srcOffset
     * @param length
     */
    public void append(byte[] src, int srcOffset, int length) {
        if (next != null) {
            next.append(src, srcOffset, length);
        } else {
            final int dstOffset = start + size;
            setSize(size + length);
            System.arraycopy(src, srcOffset, data, dstOffset, length);
        }
        testBuffer();
    }

    /**
     * Append a complete buffer to the end of this buffer.
     * 
     * @param skbuf
     */
    public void append(SocketBuffer skbuf) {
        if (next != null) {
            next.append(skbuf);
        } else {
            next = skbuf;
        }
        testBuffer();
    }

    /**
     * Append a buffer to the end of this buffer starting at the given offset in
     * the appended buffer.
     * 
     * @param skbufOffset
     * @param skbuf
     */
    public void append(int skbufOffset, SocketBuffer skbuf) {
        final byte[] src = skbuf.toByteArray();
        append(src, skbufOffset, src.length - skbufOffset);
    }

    /**
     * Append a buffer to the end of this buffer with only a given amount of
     * bytes. The given buffer must not contain a next buffer and must have a
     * size greater or equal to length
     * 
     * @param skbuf
     */
    public void append(SocketBuffer skbuf, int length) throws IllegalArgumentException {
        if (length == 0) {
            return;
        }
        if (next != null) {
            next.append(skbuf, length);
        } else {
            if (length < 0) {
                throw new IllegalArgumentException("Length < 0");
            }
            if (skbuf.next != null) {
                throw new IllegalArgumentException("skbuf.next != null");
            }
            if (skbuf.size < length) {
                throw new IllegalArgumentException("skbuf.size < length");
            }
            next = skbuf;
            skbuf.size = length;
        }
        testBuffer();
    }

    /**
     * Gets a byte in the buffer
     * 
     * @param index
     */
    public int get(int index) {
        if (index >= size) {
            if (next != null) {
                return next.get(index - size);
            } else {
                throw new IndexOutOfBoundsException("at index " + index);
            }
        } else {
            return data[start + index] & 0xFF;
        }
    }

    /**
     * Gets a 16-bit word from the buffer
     * 
     * @param index
     */
    public int get16(int index) {
        if (index >= size) {
            // Index is beyond my data
            if (next != null) {
                return next.get16(index - size);
            } else {
                throw new IndexOutOfBoundsException("at index " + index);
            }
        } else if (index + 1 < size) {
            // Both bytes are within my data
            final int b0 = data[start + index + 0] & 0xFF;
            final int b1 = data[start + index + 1] & 0xFF;
            return (b0 << 8) | b1;
        } else {
            // First byte is within my data, second is not
            final int b0 = get(index + 0);
            final int b1 = get(index + 1);
            return (b0 << 8) | b1;
        }
    }

    /**
     * Gets a 32-bit word from the buffer
     * 
     * @param index
     */
    public int get32(int index) {
        if (index >= size) {
            // Index is beyond my data
            if (next != null) {
                return next.get32(index - size);
            } else {
                throw new IndexOutOfBoundsException("at index " + index);
            }
        } else if (index + 3 < size) {
            // Both bytes are within my data
            final int b0 = data[start + index + 0] & 0xFF;
            final int b1 = data[start + index + 1] & 0xFF;
            final int b2 = data[start + index + 2] & 0xFF;
            final int b3 = data[start + index + 3] & 0xFF;
            return (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
        } else {
            // First byte is within my data, second is not
            final int b0 = get(index + 0);
            final int b1 = get(index + 1);
            final int b2 = get(index + 1);
            final int b3 = get(index + 1);
            return (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
        }
    }

    /**
     * Sets a byte in the buffer
     * 
     * @param index
     */
    public void set(int index, int value) {
        if (index >= size) {
            if (next != null) {
                next.set(index - size, value);
            } else {
                throw new IndexOutOfBoundsException("at index " + index);
            }
        } else {
            data[start + index] = (byte) value;
        }
    }

    /**
     * Sets a 16-bit word in the buffer
     * 
     * @param index
     */
    public void set16(int index, int value) {
        if (index >= size) {
            // Index is beyond my data
            if (next != null) {
                next.set16(index - size, value);
            } else {
                throw new IndexOutOfBoundsException("at index " + index);
            }
        } else if (index + 1 < size) {
            // Both bytes are within my data
            data[start + index + 0] = (byte) ((value >> 8) & 0xFF);
            data[start + index + 1] = (byte) (value & 0xFF);
        } else {
            // First byte is within my data, second is not
            set(index + 0, ((value >> 8) & 0xFF));
            set(index + 1, (value & 0xFF));
        }
    }

    /**
     * Sets a 32-bit word in the buffer
     * 
     * @param index
     */
    public void set32(int index, int value) {
        if (index >= size) {
            // Index is beyond my data
            if (next != null) {
                next.set32(index - size, value);
            } else {
                throw new IndexOutOfBoundsException("at index " + index);
            }
        } else if (index + 3 < size) {
            // All bytes are within my data
            data[start + index + 0] = (byte) ((value >> 24) & 0xFF);
            data[start + index + 1] = (byte) ((value >> 16) & 0xFF);
            data[start + index + 2] = (byte) ((value >> 8) & 0xFF);
            data[start + index + 3] = (byte) (value & 0xFF);
        } else {
            // First byte is within my data, last is not
            set(index + 0, ((value >> 24) & 0xFF));
            set(index + 1, ((value >> 16) & 0xFF));
            set(index + 2, ((value >> 8) & 0xFF));
            set(index + 3, (value & 0xFF));
        }
    }

    /**
     * Sets a byte-array in the buffer
     * 
     * @param index
     */
    public void set(int index, byte[] src, int srcOffset, int length) {
        if (index >= size) {
            // Index is beyond my data
            if (next != null) {
                next.set(index - size, src, srcOffset, length);
            } else {
                throw new IndexOutOfBoundsException("at index " + index);
            }
        } else if (index + length <= size) {
            // All bytes are within my data
            System.arraycopy(src, srcOffset, data, start + index, length);
        } else {
            // First byte is within my data, last is not
            if (next != null) {
                final int myLength = size - index;
                System.arraycopy(src, srcOffset, data, start + index, myLength);
                next.set(index - myLength, src, srcOffset + myLength, length - myLength);
            } else {
                throw new IndexOutOfBoundsException("at index " + index);
            }
        }
    }

    /**
     * Gets a byte-array in the buffer
     * 
     * @param index
     */
    public void get(byte[] dst, int dstOffset, int index, int length) {
        try {
            if (index >= size) {
                // Index is beyond my data
                if (next != null) {
                    next.get(dst, dstOffset, index - size, length);
                } else {
                    throw new IndexOutOfBoundsException("at index " + index);
                }
            } else if (index + length <= size) {
                // All bytes are within my data
                System.arraycopy(data, start + index, dst, dstOffset, length);
            } else {
                // First byte is within my data, last is not
                if (next != null) {
                    final int myLength = size - index;
                    System.arraycopy(data, start + index, dst, dstOffset, myLength);
                    next.get(dst, dstOffset + myLength, Math.max(0, index - myLength), length -
                            myLength);
                } else {
                    throw new IndexOutOfBoundsException("at index " + index);
                }
            }
        } catch (IndexOutOfBoundsException ex) {
            log.debug("get(dst, " + dstOffset + ", " + index + ", " + length + ") start=" + start +
                    ", size=" + size);
            throw new IndexOutOfBoundsException(ex.getMessage());
        }
    }

    /**
     * Gets the contents of this buffer as a single bytearray. Please note that
     * on concatenated buffers, this can be an expensive function!
     * 
     * @return The contents of this buffer
     */
    public byte[] toByteArray() {
        final byte[] result = new byte[getSize()];
        int ofs = 0;
        SocketBuffer skbuf = this;
        do {
            System.arraycopy(skbuf.data, skbuf.start, result, ofs, skbuf.size);
            ofs += skbuf.size;
            skbuf = skbuf.next;
        } while (skbuf != null);
        return result;
    }

    /**
     * Gets the used number of bytes in the buffer (and any appended buffers)
     */
    public int getSize() {
        if (next != null) {
            return size + next.getSize();
        } else {
            return size;
        }
    }

    /**
     * Set the new buffer size
     * @param newSize
     */
    private void setSize(int newSize) {
        if (data == null) {
            if (newSize > 0) {
                // There is no buffer, create one
                data = new byte[alignSize(Math.max(newSize, INITIAL_SIZE))];
                size = newSize;
            }
        } else if (data.length < start + newSize) {
            // Enlarge the buffer
            final byte[] newData = new byte[alignSize(start + newSize)];
            System.arraycopy(data, start, newData, start, size);
            this.data = newData;
            this.size = newSize;
        } else {
            // The buffer is large enough, update size
            this.size = newSize;
        }
        testBuffer();
    }

    private final int alignSize(int size) {
        return (size + (INITIAL_SIZE - 1)) & ~(INITIAL_SIZE - 1);
    }

    /**
     * Test the parameters of this buffer for illegal combinations.
     */
    private final void testBuffer() {
        if (data == null) {
            if (size != 0) {
                throw new RuntimeException("size(" + size + ") must be 0 when data is null");
            }
            if (start != 0) {
                throw new RuntimeException("start(" + start + ") must be 0 when data is null");
            }
        } else {
            if (size < 0) {
                throw new RuntimeException("size(" + size + ") must be >= 0");
            }
            if (start < 0) {
                throw new RuntimeException("start(" + start + ") must be >= 0");
            }
            if (start + size > data.length) {
                throw new RuntimeException("start(" + start + ")+size(" + size +
                        ") must be <= data.length(" + data.length + ")");
            }
        }
    }

    /**
     * Gets the header of the linklayer 
     */
    public LinkLayerHeader getLinkLayerHeader() {
        if (linkLayerHeader != null) {
            return linkLayerHeader;
        } else if (next != null) {
            return next.getLinkLayerHeader();
        } else {
            return null;
        }
    }

    /**
     * Gets the header of the networklayer 
     */
    public NetworkLayerHeader getNetworkLayerHeader() {
        if (networkLayerHeader != null) {
            return networkLayerHeader;
        } else if (next != null) {
            return next.getNetworkLayerHeader();
        } else {
            return null;
        }
    }

    /**
     * Gets the header of the transportlayer 
     */
    public TransportLayerHeader getTransportLayerHeader() {
        if (transportLayerHeader != null) {
            return transportLayerHeader;
        } else if (next != null) {
            return next.getTransportLayerHeader();
        } else {
            return null;
        }
    }

    /**
     * Sets the header of the linklayer 
     * @param header
     */
    public void setLinkLayerHeader(LinkLayerHeader header) {
        linkLayerHeader = header;
    }

    /**
     * Sets the header of the networklayer 
     * @param header
     */
    public void setNetworkLayerHeader(NetworkLayerHeader header) {
        networkLayerHeader = header;
    }

    /**
     * Sets the header of the transportlayer 
     * @param header
     */
    public void setTransportLayerHeader(TransportLayerHeader header) {
        transportLayerHeader = header;
    }

}
