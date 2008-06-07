/*
 *
 */
package org.jnode.fs.jfat;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.jnode.util.LittleEndian;


/**
 * @author gvt
 */
public class FatMarshal {
    private final byte[] array;
    private boolean dirty = false;

    public FatMarshal(byte[] array) {
        if (array == null)
            throw new NullPointerException("array cannot be null");
        this.array = array;
    }

    public FatMarshal(int length) {
        this.array = new byte[length];
        Arrays.fill(this.array, 0, this.array.length, (byte) 0);
    }

    public static FatMarshal wrap(byte[] array) {
        return new FatMarshal(array);
    }

    public byte[] getArray() {
        return array;
    }

    public ByteBuffer getByteBuffer() {
        ByteBuffer buf = ByteBuffer.wrap(array);
        buf.clear();
        return buf;
    }

    public int length() {
        return array.length;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty() {
        dirty = true;
    }

    public void flush() {
        dirty = false;
    }

    private void checkOffset(int offset, int length) {
        if (offset < 0)
            throw new IndexOutOfBoundsException("offset[" + offset + "] cannot be negative");

        if (length <= 0)
            throw new IndexOutOfBoundsException("length[" + length + "] has to be positive");

        if (offset > (array.length - length))
            throw new IndexOutOfBoundsException("length[" + length + "] + offset[" + offset +
                    "] >" + "array.length[" + array.length + "]");
    }

    public byte get(int offset) {
        checkOffset(offset, 1);
        return array[offset];
    }

    public void put(int offset, byte value) {
        checkOffset(offset, 1);
        array[offset] = value;
        setDirty();
    }

    public int getUInt8(int offset) {
        checkOffset(offset, 1);
        return LittleEndian.getUInt8(array, offset);
    }

    public void setUInt8(int offset, int value) {
        checkOffset(offset, 1);
        LittleEndian.setInt8(array, offset, value);
        setDirty();
    }

    public int getUInt16(int offset) {
        checkOffset(offset, 2);
        return LittleEndian.getUInt16(array, offset);
    }

    public void setUInt16(int offset, int value) {
        checkOffset(offset, 2);
        LittleEndian.setInt16(array, offset, value);
        setDirty();
    }

    public long getUInt32(int offset) {
        checkOffset(offset, 4);
        return LittleEndian.getUInt32(array, offset);
    }

    public void setUInt32(int offset, long value) {
        checkOffset(offset, 4);
        LittleEndian.setInt32(array, offset, (int) value);
        setDirty();
    }

    public String getString(int offset, int length) {
        checkOffset(offset, length);

        StringBuilder b = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int v = array[offset + i];
            b.append((char) v);
        }
        return b.toString();
    }

    public void setString(int offset, int length, String value) {
        checkOffset(offset, length);

        for (int i = 0; i < length; i++) {
            char ch;
            if (i < value.length())
                ch = value.charAt(i);
            else
                ch = (char) 0;
            LittleEndian.setInt8(array, offset + i, ch);
        }
        setDirty();
    }

    public char[] getChars(int offset, int length) {
        checkOffset(offset, length);

        char[] value = new char[length];

        for (int i = 0; i < length; i++)
            value[i] = (char) LittleEndian.getUInt8(array, offset + i);

        return value;
    }

    public void setChars(int offset, int length, char[] value) {
        checkOffset(offset, length);

        for (int i = 0; i < length; i++)
            LittleEndian.setInt8(array, offset + i, (int) value[i]);

        setDirty();
    }

    public byte[] getBytes(int offset, int length) {
        byte[] value = new byte[length];
        getBytes(offset, length, 0, value);
        return value;
    }

    public void setBytes(int offset, int length, byte[] value) {
        setBytes(offset, length, 0, value);
    }

    public void getBytes(int offset, int length, int start, byte[] dst) {
        checkOffset(offset, length);
        System.arraycopy(array, offset, dst, start, length);
    }

    public void setBytes(int offset, int length, int start, byte[] src) {
        checkOffset(offset, length);
        System.arraycopy(src, start, array, offset, length);
        setDirty();
    }

    public String toString() {
        StrWriter out = new StrWriter();

        out.println("*************************************************");
        out.println("Fat Marshal");
        out.println("*************************************************");
        out.println("length =\t" + length());
        out.println("dirty  =\t" + isDirty());
        out.println("array");
        out.println(array);
        out.print("*************************************************");

        return out.toString();
    }
}
