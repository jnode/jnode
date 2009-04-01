package java.nio;

import gnu.classpath.Pointer;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceNotFreeException;
import org.vmmagic.unboxed.Offset;
import org.vmmagic.unboxed.Extent;

/**
 *
 */
public class NativeVMDirectByteBuffer {
    static Pointer allocate(int capacity) {
        return new MemoryRawData(capacity);
    }

    static void free(Pointer address) {
        ((MemoryRawData) address).resource.release();
    }

    static byte get(Pointer address, int index) {
        final MemoryRawData mrd = (MemoryRawData) address;
        return mrd.address.loadByte(Offset.fromIntZeroExtend(index));
    }

    static void get(Pointer address, int index, byte[] dst, int offset, int length) {
        ((MemoryRawData) address).resource.getBytes(index, dst, offset, length);
    }

    static void put(Pointer address, int index, byte value) {
        ((MemoryRawData) address).resource.setByte(index, value);
    }

    static void put(Pointer address, int index, byte[] src, int offset, int length) {
        ((MemoryRawData) address).resource.setBytes(src, offset, index, length);
    }

    static Pointer adjustAddress(Pointer address, int offset) {
        final MemoryResource res = ((MemoryRawData) address).resource;
        final Extent size = res.getSize().sub(offset);
        try {
            return new MemoryRawData(res.claimChildResource(Offset.fromIntZeroExtend(offset), size, true));
        } catch (ResourceNotFreeException ex) {
            throw new Error("Cannot adjustAddress", ex);
        }
    }

    static void shiftDown(Pointer address, int dst_offset, int src_offset, int count) {
        ((MemoryRawData) address).resource.copy(src_offset, dst_offset, count);
    }
}
