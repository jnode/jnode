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

package org.jnode.test.fs.driver.tests;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.block.floppy.FloppyDriver;
import org.jnode.driver.block.ide.disk.IDEDiskDriver;
import org.jnode.driver.bus.ide.IDEConstants;
import org.jnode.test.fs.driver.BlockDeviceAPITestConfig;
import org.jnode.test.support.AbstractTest;

public class BlockDeviceAPITest extends AbstractTest {
    public BlockDeviceAPITest() {
        super(BlockDeviceAPITestConfig.class);
    }

    public BlockDeviceAPITest(String name) {
        super(BlockDeviceAPITestConfig.class, name);
    }

    public void setUp() throws Exception {
        super.setUp();

        //put specific setUp here        
    }

    /**
     *
     */
    public void tearDown() throws Exception {
        //put specific tearDown here

        super.tearDown();
    }

    public BlockDeviceAPI getBlockDeviceAPI() {
        return ((BlockDeviceAPITestConfig) getTestConfig()).getBlockDeviceAPI();
    }

    public void testFlush() throws IOException {
        getBlockDeviceAPI().flush();
    }

    public void testGetLength() throws IOException {
        long length = getBlockDeviceAPI().getLength();
        assertTrue("length must be > 0 (actual:" + length + ")", length > 0);
    }

    public void testRegularReadUnaligned() throws Exception {
        doRead(false, Bounds.LOWER);
        doRead(false, Bounds.NOMINAL);
        doRead(false, Bounds.UPPER);
    }

    public void testRegularReadAligned() throws Exception {
        doRead(true, Bounds.LOWER);
        doRead(true, Bounds.NOMINAL);
        doRead(true, Bounds.UPPER);
    }

    public void testOutOfBoundsRead() throws Exception {
        doRead(true, Bounds.BEFORE_LOWER);
        doRead(true, Bounds.AROUND_LOWER);
        doRead(true, Bounds.AROUND_UPPER);
        doRead(true, Bounds.AFTER_UPPER);
    }

    public void testRegularWriteUnaligned() throws Exception {
        doWrite(false, Bounds.LOWER);
        doWrite(false, Bounds.NOMINAL);
        doWrite(false, Bounds.UPPER);
    }

    public void testRegularWriteAligned() throws Exception {
        doWrite(true, Bounds.LOWER);
        doWrite(true, Bounds.NOMINAL);
        doWrite(true, Bounds.UPPER);
    }

    public void testOutOfBoundsWrite() throws Exception {
        doWrite(true, Bounds.BEFORE_LOWER);
        doWrite(true, Bounds.AROUND_LOWER);
        doWrite(true, Bounds.AROUND_UPPER);
        doWrite(true, Bounds.AFTER_UPPER);
    }

    private void doRead(boolean aligned, byte boundsType) throws Exception {
        Bounds bounds = new Bounds(true, aligned, boundsType);
        boolean errorOccured;

        try {
            doRead(bounds);
            errorOccured = false;
        } catch (Throwable t) {
            if (!bounds.expectError()) {
                log.error("Unexpected error occurred", t);
            }
            errorOccured = true;
        }

        if (bounds.expectError()) {
            assertTrue("expected an error for " + bounds, errorOccured);
        } else {
            assertFalse("error not expected for " + bounds, errorOccured);
        }
    }

    private void doWrite(boolean aligned, byte boundsType) throws Exception {
        Bounds bounds = new Bounds(false, aligned, boundsType);
        boolean errorOccured;

        try {
            doWrite(bounds);
            errorOccured = false;
        } catch (Throwable t) {
            if (!bounds.expectError()) {
                log.error("Unexpected error occurred", t);
            }
            errorOccured = true;
        }

        if (bounds.expectError()) {
            assertTrue("expected an error for " + bounds, errorOccured);
        } else {
            assertFalse("error not expected for " + bounds, errorOccured);
        }
    }

    private void doRead(Bounds bounds) throws IOException {
        log.info(bounds.toString());
        ByteBuffer bb = ByteBuffer.allocate(IDEConstants.SECTOR_SIZE);

        long offset = bounds.getStart();
        int toRead;
        BlockDeviceAPI api = getBlockDeviceAPI();

        while (offset < bounds.getEnd()) {
            toRead = Math.min(bb.remaining(), (int) (bounds.getEnd() - offset));

            bb.position(0).limit(toRead);
            api.read(offset, bb);
            bb.clear();

            offset += toRead;
        }
    }

    private void doWrite(Bounds bounds) throws IOException {
        log.info(bounds.toString());
        ByteBuffer bb = ByteBuffer.allocate(IDEConstants.SECTOR_SIZE);

        long offset = bounds.getStart();
        int toWrite;
        BlockDeviceAPI api = getBlockDeviceAPI();

        while (offset < bounds.getEnd()) {
            toWrite = Math.min(bb.remaining(), (int) (bounds.getEnd() - offset));

            bb.position(0).limit(toWrite);
            api.write(offset, bb);
            bb.clear();

            offset += toWrite;
        }
    }

    private class Bounds {
        // bounds types
        public static final byte BEFORE_LOWER = 0; // lead to an error
        public static final byte AROUND_LOWER = 1; // lead to an error
        public static final byte LOWER = 2;
        public static final byte NOMINAL = 3;
        public static final byte UPPER = 4;
        public static final byte AROUND_UPPER = 5; // lead to an error
        public static final byte AFTER_UPPER = 6; // lead to an error

        private static final long UNALIGNMENT_OFFSET = IDEConstants.SECTOR_SIZE / 2;
        private static final long DELTA = IDEConstants.SECTOR_SIZE;

        private long start;
        private long end;
        private boolean expectError;
        private boolean read;
        private String toStringDesc = "";

        public Bounds(boolean read, boolean aligned, byte boundsType) throws Exception {
            this.read = read;

            expectError = true;
            toStringDesc = aligned ? "aligned " : "unaligned ";
            long middle;

            switch (boundsType) {
                case BEFORE_LOWER:
                    toStringDesc += "BEFORE_LOWER";
                    expectError = true; // must give an error
                    start = -DELTA;
                    end = 0;
                    break;

                case AROUND_LOWER:
                    toStringDesc += "AROUND_LOWER";
                    expectError = true; // must give an error
                    start = -DELTA;
                    end = +DELTA;
                    break;

                case LOWER:
                    toStringDesc += "LOWER";
                    expectError = false; // must NOT give an error
                    start = 0;
                    end = +DELTA;
                    break;

                case NOMINAL:
                    toStringDesc += "NOMINAL";
                    expectError = false; // must NOT give an error
                    middle = getBlockDeviceAPI().getLength() / 2;
                    start = middle - DELTA;
                    end = middle + DELTA;
                    break;

                case UPPER:
                    toStringDesc += "UPPER";
                    expectError = false; // must NOT give an error
                    start = getBlockDeviceAPI().getLength() - DELTA;
                    end = getBlockDeviceAPI().getLength();
                    break;

                case AROUND_UPPER:
                    toStringDesc += "AROUND_UPPER";
                    expectError = true; // must give an error
                    start = getBlockDeviceAPI().getLength() - DELTA;
                    end = getBlockDeviceAPI().getLength() + DELTA;
                    break;

                case AFTER_UPPER:
                    toStringDesc += "AFTER_UPPER";
                    expectError = true; // must give an error
                    start = getBlockDeviceAPI().getLength();
                    end = getBlockDeviceAPI().getLength() + DELTA;
                    break;

                default:
                    throw new Exception("unexpected boundsType: " + boundsType);
            }

            // is it a regular usage ?
            if (!expectError) {
                if (!aligned) {
                    start += UNALIGNMENT_OFFSET;
                    end += UNALIGNMENT_OFFSET;
                }

                // adjustment for regular usage (to be in the bounds)
                start = Math.max(0, start);
                end = Math.min(getBlockDeviceAPI().getLength(), end);
            }

            boolean apiNeedAlignment = (getBlockDeviceAPI() instanceof FloppyDriver) ||
                (getBlockDeviceAPI() instanceof IDEDiskDriver);
            expectError |= !aligned && apiNeedAlignment;
        }


        public long getEnd() {
            return end;
        }

        public long getStart() {
            return start;
        }

        public boolean expectError() {
            return expectError;
        }

        public String toString() {
            String devSize;
            try {
                devSize = "" + getBlockDeviceAPI().getLength();
            } catch (IOException e) {
                log.error("error in toString", e);
                devSize = "???";
            }

            return (read ? "read " : "write ") + " " + toStringDesc +
                " [" + start + ", " + end + "] (devSize=" + devSize + ") with config " + getTestConfig().getName();
        }
    }
}
