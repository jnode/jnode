package org.jnode.apps.jpartition.utils.device;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import javax.naming.NameNotFoundException;

import org.jnode.apps.vmware.disk.handler.UnsupportedFormatException;
import org.jnode.driver.DriverException;
import org.jnode.driver.block.BlockDeviceAPIHelper;
import org.jnode.util.ByteBufferUtils;

/**
 * TODO should that be merged with FileDevice ?
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 * 
 */
public class FileIDEDevice extends AbstractIDEDevice {
    private RandomAccessFile raf;

    public FileIDEDevice(String name, boolean primary, boolean master, File file, long size)
        throws IOException, DriverException, NameNotFoundException, UnsupportedFormatException {
        super(name, primary, master);

        raf = new RandomAccessFile(file, "rw");
        raf.setLength(size);
    }

    public void flush() throws IOException {
        // nothing to do
    }

    public long getLength() throws IOException {
        return raf.length();
    }

    /**
     * 
     */
    public void read(long devOffset, ByteBuffer destBuf) throws IOException {
        BlockDeviceAPIHelper.checkBounds(this, devOffset, destBuf.remaining());
        raf.seek(devOffset);

        // TODO optimize it also to use ByteBuffer at lower level
        ByteBufferUtils.ByteArray destBA = ByteBufferUtils.toByteArray(destBuf);
        byte[] dest = destBA.toArray();
        raf.read(dest, 0, dest.length);
        destBA.refreshByteBuffer();
    }

    /**
     * 
     */
    public void write(long devOffset, ByteBuffer srcBuf) throws IOException {
        // log.debug("fd.write devOffset=" + devOffset + ", length=" + length);
        BlockDeviceAPIHelper.checkBounds(this, devOffset, srcBuf.remaining());
        raf.seek(devOffset);

        // TODO optimize it also to use ByteBuffer at lower level
        byte[] src = ByteBufferUtils.toArray(srcBuf);
        raf.write(src, 0, src.length);
    }
}
