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
 
package org.jnode.driver.block.floppy;

import java.security.PrivilegedExceptionAction;
import javax.naming.NameNotFoundException;
import org.apache.log4j.Logger;
import org.jnode.driver.DriverException;
import org.jnode.driver.system.cmos.CMOSConstants;
import org.jnode.driver.system.cmos.CMOSService;
import org.jnode.naming.InitialNaming;
import org.jnode.system.DMAException;
import org.jnode.system.DMAManager;
import org.jnode.system.DMAResource;
import org.jnode.system.IOResource;
import org.jnode.system.IRQResource;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.util.AccessControllerUtils;
import org.jnode.util.NumberUtils;
import org.jnode.util.TimeoutException;

/**
 * @author epr
 */
public class DefaultFDC implements FDC {

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(DefaultFDC.class);
    /**
     * Floppy IRQ
     */
    private final IRQResource irq;
    /**
     * Floppy DMA channel
     */
    private final DMAResource dma;
    /**
     * IO-port range1
     */
    private final IOResource io1;
    /**
     * IO-port range2
     */
    private final IOResource io2;
    /**
     * DMA transfer buffer
     */
    private final MemoryResource dmaMem;
    /**
     * Use primary controller?
     */
    private final boolean primary;
    /**
     * The first I/O port
     */
    private final int startPort;
    /**
     * Currently executing command
     */
    private FloppyCommand currentCommand;
    /**
     * The drive parameters
     */
    private final FloppyDriveParameters[] driveParams;
    private final boolean[] diskChanged;

    /**
     * Create a new instance
     *
     * @param owner
     * @param primary
     * @throws ResourceNotFreeException
     * @throws DriverException
     */
    public DefaultFDC(ResourceOwner owner, boolean primary)
        throws ResourceNotFreeException, DriverException {
        IRQResource irq = null;
        DMAResource dma = null;
        IOResource io1 = null;
        IOResource io2 = null;
        MemoryResource dmaMem = null;

        if (primary) {
            startPort = PRIMARY_START_PORT;
        } else {
            startPort = SECONDARY_START_PORT;
        }

        // Try to read the floppy parameters in CMOS
        try {
            final CMOSService cmos = InitialNaming.lookup(CMOSService.NAME);
            final int fd = cmos.getRegister(CMOSConstants.CMOS_FLOPPY_DRIVES);
            driveParams = new FloppyDriveParameters[NR_DRIVES];
            diskChanged = new boolean[NR_DRIVES];
            for (int i = 0; i < NR_DRIVES; i++) {
                final int cmosType;
                if (i == 0) {
                    cmosType = (fd >> 4) & 0xf;
                } else if (i == 1) {
                    cmosType = (fd & 0xf);
                } else {
                    cmosType = 0;
                }
                driveParams[i] = getDriveParam(cmosType);
                diskChanged[i] = true;
            }
        } catch (NameNotFoundException ex) {
            throw new ResourceNotFreeException("Cannot find CMOSService", ex);
        }

        try {
            final ResourceManager rm = InitialNaming.lookup(ResourceManager.NAME);
            final DMAManager dmaService = InitialNaming.lookup(DMAManager.NAME);
            // PRESERVE THIS CLAIMING ORDER!
            irq = rm.claimIRQ(owner, FLOPPY_IRQ, this, false);
            dma = dmaService.claimDMAChannel(owner, FLOPPY_DMA);
            io1 = claimPorts(rm, owner, startPort + OFFSET_RANGE1, NR_PORTS_RANGE1);
            io2 = claimPorts(rm, owner, startPort + OFFSET_RANGE2, NR_PORTS_RANGE2);
            dmaMem = rm.claimMemoryResource(owner, null, 64 * 1024, ResourceManager.MEMMODE_ALLOC_DMA);
        } catch (NameNotFoundException ex) {
            throw new ResourceNotFreeException("Cannot find ResourceManager or DMAService", ex);
        } catch (ResourceNotFreeException ex) {
            if (dmaMem != null) {
                dmaMem.release();
            }
            if (io2 != null) {
                io2.release();
            }
            if (io1 != null) {
                io1.release();
            }
            if (dma != null) {
                dma.release();
            }
            if (irq != null) {
                irq.release();
            }
            throw ex;
        }
        this.primary = primary;
        this.irq = irq;
        this.dma = dma;
        this.io1 = io1;
        this.io2 = io2;
        this.dmaMem = dmaMem;
    }


    /**
     * (non-Javadoc)
     *
     * @see org.jnode.driver.block.floppy.FDC#sendCommand(byte[], boolean)
     */
    public void sendCommand(byte[] command, boolean enableDMA)
        throws FloppyException {
        if (enableDMA) {
            try {
                dma.enable();
            } catch (DMAException ex) {
                throw new FloppyException("Cannot enable DMA", ex);
            }
        }
        final int len = command.length;
        for (int i = 0; i < len; i++) {
            // Wait until MRQ is on
            /*while ((getStateReg() & STATE_MRQ) == 0) {
                   Thread.yield();
               }*/
            io1.outPortByte(startPort + RW8_DATA_OFFSET, command[i] & 0xFF);
        }
    }


    /**
     * (non-Javadoc)
     *
     * @see org.jnode.driver.block.floppy.FDC#setupDMA(int, int)
     */
    public void setupDMA(int length, int mode)
        throws FloppyException {
        try {
            dma.setup(dmaMem, length, mode);
        } catch (DMAException ex) {
            throw new FloppyException("Cannot setup DMA", ex);
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see org.jnode.driver.block.floppy.FDC#copyToDMA(byte[], int, int)
     */
    public void copyToDMA(byte[] src, int srcOffset, int length) {
        dmaMem.setBytes(src, srcOffset, 0, length);
    }

    /**
     * (non-Javadoc)
     *
     * @see org.jnode.driver.block.floppy.FDC#copyFromDMA(byte[], int, int)
     */
    public void copyFromDMA(byte[] dest, int destOffset, int length) {
        dmaMem.getBytes(0, dest, destOffset, length);
    }

    /**
     * Copy from the given byte array into the DMA buffer
     *
     * @param src
     * @param length
     */
    protected void copyToDMA(byte[] src, int length) {
        dmaMem.setBytes(src, 0, 0, length);
    }


    /**
     * (non-Javadoc)
     *
     * @see org.jnode.driver.block.floppy.FDC#getCommandState(int)
     */
    public byte[] getCommandState(int length)
        throws TimeoutException, FloppyException {
        final byte[] res = new byte[MAX_REPLIES];
        for (int i = 0; i < MAX_REPLIES; i++) {
            int status;
            status = waitUntilReady();
            status &= STATE_DIO | STATE_READY | STATE_BUSY | STATE_NDMA;
            if ((status & ~STATE_BUSY) == STATE_READY) {
                return res;
            }
            if (status == (STATE_DIO | STATE_READY | STATE_BUSY)) {
                res[i] = (byte) io1.inPortByte(startPort + RW8_DATA_OFFSET);
            } else {
                throw new FloppyException("Error in reading state: state=" + NumberUtils.hex(status, 2));
            }
        }
        throw new FloppyException("Too many result bytes");
    }

    /**
     * Wait until the FDC is in the ready state
     *
     * @return The contents of the state register
     * @throws TimeoutException
     */
    private final int waitUntilReady()
        throws TimeoutException {
        for (int i = 0; i < 1000; i++) {
            final int state = getStateReg();
            if ((state & STATE_READY) != 0) {
                return state;
            }
        }
        throw new TimeoutException("Timeout in waiting for ready");
    }

    /**
     * Gets the STATE register
     *
     * @return int
     */
    public final int getStateReg() {
        return io1.inPortByte(startPort + R8_STATE_OFFSET);
    }

    /**
     * Gets the DOR register
     *
     * @return int
     */
    public final int getDorReg() {
        return io1.inPortByte(startPort + RW8_DOR_OFFSET);
    }

    /**
     * Gets the DIR register
     *
     * @return int
     */
    public final int getDirReg() {
        return io2.inPortByte(startPort + R8_DIR_OFFSET);
    }

    /**
     * Has the disk changed since the last command?
     *
     * @param drive
     * @param resetFlag
     * @return boolean
     */
    public final boolean diskChanged(int drive, boolean resetFlag) {
        final boolean rc = diskChanged[drive];
        if (rc && resetFlag) {
            diskChanged[drive] = false;
        }
        return rc;
    }


    /**
     * (non-Javadoc)
     *
     * @see org.jnode.driver.block.floppy.FDC#setDorReg(int, boolean, boolean)
     */
    public final void setDorReg(int drive, boolean motorOn, boolean dma) {
        setDorReg(drive, motorOn, dma, false);
    }

    /**
     * Sets the DOR register
     *
     * @param drive
     * @param motorOn
     * @param dma
     * @param reset
     */
    private final void setDorReg(int drive, boolean motorOn, boolean dma, boolean reset) {
        final int driveV;
        final int motorV;
        final int oldDor = io1.inPortByte(startPort + RW8_DOR_OFFSET);
        final int oldDrive = oldDor & DOR_DRIVE_MASK;
        if ((oldDrive != drive) || reset) {
            // Test disk change for old drive
            diskChanged[oldDrive] |= ((getDirReg() & DIR_DISKCHANGE) != 0);
        }

        switch (drive) {
            case 0:
                driveV = DOR_DRIVE0;
                motorV = DOR_MOTOR0;
                break;
            case 1:
                driveV = DOR_DRIVE1;
                motorV = DOR_MOTOR1;
                break;
            case 2:
                driveV = DOR_DRIVE2;
                motorV = DOR_MOTOR2;
                break;
            case 3:
                driveV = DOR_DRIVE3;
                motorV = DOR_MOTOR3;
                break;
            default:
                throw new IllegalArgumentException("Invalid drive value " + drive);
        }
        int v = driveV;
        if (motorOn) {
            v |= motorV;
        }
        if (dma) {
            v |= DOR_DMA;
        }
        if (!reset) {
            v |= DOR_NRESET;
        }
        io1.outPortByte(startPort + RW8_DOR_OFFSET, v);
        if ((oldDrive != drive) || reset) {
            // Test disk change for new drive
            diskChanged[drive] |= ((getDirReg() & DIR_DISKCHANGE) != 0);
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see org.jnode.driver.block.floppy.FDC#getST0()
     */
    public final int getST0() throws TimeoutException, FloppyException {
        final byte[] res = senseInterruptStatus();
        return res[0] & 0xFF;
    }

    /**
     * Gets the current cylinder
     *
     * @return int
     * @throws TimeoutException
     * @throws FloppyException
     */
    protected final int getCylinder()
        throws TimeoutException, FloppyException {
        final byte[] res = senseInterruptStatus();
        return res[0] & 0xFF;
    }

    /**
     * Perform a "Sense interrupt" command
     *
     * @return byte[]
     * @throws TimeoutException
     * @throws FloppyException
     */
    protected final byte[] senseInterruptStatus() throws TimeoutException, FloppyException {
        final byte[] cmd = new byte[1];
        cmd[0] = 0x08;
        sendCommand(cmd, false);
        final byte[] res = getCommandState(2);
        return res;
    }

    /**
     * Perform a "Sense drive status" command
     *
     * @param drive
     * @param head
     * @return ST3
     * @throws TimeoutException
     * @throws FloppyException
     */
    protected final int senseDriveStatus(int drive, int head) throws TimeoutException, FloppyException {
        final byte[] cmd = new byte[2];
        cmd[0] = 0x04;
        cmd[1] = (byte) ((drive & 3) | ((head & 1) << 2));
        sendCommand(cmd, false);
        final byte[] res = getCommandState(1);
        return res[0] & 0xFF;
    }

    /**
     * Add the given command to the command queue and wait till the command
     * has finished.
     *
     * @param cmd
     * @param timeout
     * @throws InterruptedException
     * @throws TimeoutException
     */
    public synchronized void executeAndWait(FloppyCommand cmd, long timeout)
        throws InterruptedException, TimeoutException {
        currentCommand = cmd;
        try {
            cmd.setup(this);
            cmd.waitUntilFinished(timeout);
        } catch (FloppyException ex) {
            cmd.notifyError(ex);
        } finally {
            currentCommand = null;
        }
    }

    /**
     * Release all resources.
     */
    public void release() {
        dmaMem.release();
        io2.release();
        io1.release();
        dma.release();
        irq.release();
    }

    /**
     * Reset the FDC
     */
    public final void reset() {
        final int dor = io1.inPortByte(startPort + RW8_DOR_OFFSET);
        setDorReg(dor & DOR_DRIVE_MASK, false, true, true);
        // Wait a while
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            // Ignore
        }
        setDorReg(dor & DOR_DRIVE_MASK, false, true, false);
    }

    /**
     * @param irq
     * @see org.jnode.system.IRQHandler#handleInterrupt(int)
     */
    public void handleInterrupt(int irq) {
        final FloppyCommand cmd = currentCommand;
        if (cmd != null) {
            try {
                cmd.handleIRQ(this);
            } catch (FloppyException ex) {
                log.error("Error in Floppy IRQ", ex);
                cmd.notifyError(ex);
            }
            if (cmd.isFinished()) {
                currentCommand = null;
            }
        } else {
            try {
                log.debug("Unhandled Floppy IRQ, " +
                    "DIR=" + NumberUtils.hex(getDirReg(), 2) + ", " +
                    "ST0=" + NumberUtils.hex(getST0(), 2) + ", " +
                    "State=" + NumberUtils.hex(getStateReg(), 2)
                );
            } catch (Exception ex) {
                log.error("Exception in unhandled Floppy IRQ", ex);
            }
        }
    }

    /**
     * Is the primary FDC used.
     *
     * @return True if the primary controller is used, false if the secondary controller is used.
     */
    public boolean isPrimary() {
        return primary;
    }

    /**
     * Gets the drive parameters for a given drive
     *
     * @param drive
     * @return Parameters
     */
    public FloppyDriveParameters getDriveParams(int drive) {
        return driveParams[drive];
    }

    /**
     * Gets the number of drives under control of this controller
     *
     * @return Number of drivers
     */
    public int getDriveCount() {
        return driveParams.length;
    }

    /**
     * Gets the data transfer rate for a given drive in Kb/sec
     *
     * @param drive
     * @return DTR
     */
    public int getDTR(int drive) {
        return 500;
    }

    /**
     * (non-Javadoc)
     *
     * @see org.jnode.driver.block.floppy.FDC#logDMAState()
     */
    public void logDMAState() throws DMAException {
        log.debug("dma.length = " + dma.getLength());
    }

    private final FloppyDriveParameters getDriveParam(int cmosType) {
        final FloppyDriveParameters[] list = DRIVE_PARAMS;
        for (int i = 0; i < list.length; i++) {
            final FloppyDriveParameters dp = list[i];
            if (dp.getCmosType() == cmosType) {
                return dp;
            }
        }
        return FDP_UNKNOWN;
    }

    private IOResource claimPorts(final ResourceManager rm, final ResourceOwner owner, final int low, final int length)
        throws ResourceNotFreeException, DriverException {
        try {
            return (IOResource) AccessControllerUtils.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws ResourceNotFreeException {
                    return rm.claimIOResource(owner, low, length);
                }
            });
        } catch (ResourceNotFreeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DriverException("Unknown exception", ex);
        }

    }
}
