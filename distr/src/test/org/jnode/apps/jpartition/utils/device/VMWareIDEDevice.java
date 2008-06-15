package org.jnode.apps.jpartition.utils.device;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.naming.NameNotFoundException;

import org.jnode.apps.vmware.disk.VMWareDisk;
import org.jnode.apps.vmware.disk.handler.UnsupportedFormatException;
import org.jnode.driver.DriverException;
import org.jnode.driver.block.BlockDeviceAPIHelper;

public class VMWareIDEDevice extends AbstractIDEDevice {
    private VMWareDisk vmwareDisk;

    public VMWareIDEDevice(String name, boolean primary, boolean master, VMWareDisk vmwareDisk)
        throws IOException, DriverException, NameNotFoundException, UnsupportedFormatException {
        super(name, primary, master);

        this.vmwareDisk = vmwareDisk;
    }

    public void flush() throws IOException {
        vmwareDisk.flush();
    }

    public long getLength() throws IOException {
        return vmwareDisk.getLength();
    }

    public void read(long devOffset, ByteBuffer destBuf) throws IOException {
        BlockDeviceAPIHelper.checkBounds(this, devOffset, destBuf.remaining());

        vmwareDisk.read(devOffset, destBuf);
    }

    public void write(long devOffset, ByteBuffer srcBuf) throws IOException {
        BlockDeviceAPIHelper.checkBounds(this, devOffset, srcBuf.remaining());

        vmwareDisk.write(devOffset, srcBuf);
    }
}
