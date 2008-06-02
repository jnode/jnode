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

package org.jnode.driver.bus.ide;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import org.apache.log4j.Logger;
import org.jnode.driver.Bus;
import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.bus.ide.command.IDEIdCommand;
import org.jnode.naming.InitialNaming;
import org.jnode.system.IRQHandler;
import org.jnode.system.IRQResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.util.NumberUtils;
import org.jnode.util.Queue;
import org.jnode.util.QueueProcessor;
import org.jnode.util.QueueProcessorThread;
import org.jnode.util.TimeUtils;
import org.jnode.util.TimeoutException;

/**
 * @author epr
 */
public class IDEBus extends Bus implements IDEConstants, IRQHandler,
    QueueProcessor<IDECommand> {

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(IDEBus.class);

    private final IRQResource irqRes;

    /**
     * The command queue for IDE commands
     */
    private final Queue<IDECommand> commandQueue = new Queue<IDECommand>();

    /**
     * The command that is currently being processed
     */
    private IDECommand currentCommand;

    /**
     * The worker thread for the command queue
     */
    private final QueueProcessorThread<IDECommand> queueProcessor;

    /**
     * IO Accessor
     */
    private final IDEIO io;

    /**
     * Is the the primary (true) or secondary (false) channel
     */
    private final boolean primary;

    /**
     * Name of this bus
     */
    private final String name;

    /**
     * Create a new instance
     */
    protected IDEBus(Device parent, boolean primary)
        throws IllegalArgumentException, DriverException, ResourceNotFreeException {
        super(parent);
        this.name = parent.getId() + ':' + (primary ? "primary" : "secondary");
        this.primary = primary;
        IDEDeviceFactory factory;
        try {
            factory = IDEDriverUtils.getIDEDeviceFactory();
        } catch (NamingException ex) {
            throw new DriverException(ex);
        }
        this.io = factory.createIDEIO(parent, primary);

        // Register the irq handler
        final ResourceManager rm;
        try {
            rm = InitialNaming.lookup(ResourceManager.NAME);
        } catch (NameNotFoundException ex) {
            throw new ResourceNotFreeException("Cannot find ResourceManager",
                ex);
        }
        irqRes = rm.claimIRQ(parent, io.getIrq(), this, true);
        // Reset the controller
        softwareReset();
        // Create and start the queue processor
        queueProcessor = new QueueProcessorThread<IDECommand>(name, commandQueue, this);
        queueProcessor.start();

    }

    /**
     * Add the given command to the queue of commands to be executed.
     *
     * @param command
     */
    public void execute(IDECommand command) {
        commandQueue.add(command);
    }

    /**
     * Add the given command to the queue of commands to be executed and wait
     * for the command to finish.
     *
     * @param command
     * @param timeout Maximum time to wait
     * @throws InterruptedException
     * @throws TimeoutException
     */
    public void executeAndWait(IDECommand command, long timeout)
        throws InterruptedException, TimeoutException {
        execute(command);
        command.waitUntilFinished(timeout);
    }

    /**
     * Stop this processor.
     */
    public void stop() {
        queueProcessor.stopProcessor();
        irqRes.release();
        io.release();
    }

    /**
     * Handle the IDE interrupt.
     *
     * @param irq
     */
    public void handleInterrupt(int irq) {
        final IDECommand cmd = currentCommand;
        //log.debug("IDE IRQ " + irq + " cmd=" + cmd);
        if (cmd != null) {
            try {
                cmd.handleIRQ(this, io);
                if (cmd.isFinished()) {
                    this.currentCommand = null;
                }
            } catch (TimeoutException ex) {
                log.error("Timeout in handleIRQ of " + cmd);
                this.currentCommand = null;
                cmd.setError(ERR_ABORT);
            }
        } else if (log.isDebugEnabled()) {
            log.debug("Unknown IDE IRQ " + irq + " status 0x" + NumberUtils.hex(io.getStatusReg(), 2));
        }
    }

    /**
     * Probe for the existence of a given IDE device.
     *
     * @param master
     * @return the found IDEDriveDescriptor
     * @throws InterruptedException
     */
    public IDEDriveDescriptor probe(boolean master) throws InterruptedException {

        final String ideId = name + "." + (master ? "master" : "slave");
        log.debug("Probing " + ideId);
        if (!selectDrive(master)) {
            // Cannot select, so we give up
            log.debug("Cannot select drive " + ideId);
            return null;
        }

        // Interrupts enabled
        io.setControlReg(CTR_BLANK);

        // First try a normal IDE Identify command
        IDEIdCommand cmd = new IDEIdCommand(primary, master, false);
        try {
            executeAndWait(cmd, IDE_TIMEOUT);
        } catch (TimeoutException ex) {
            // Do nothing, handled later
        }
        if (cmd.isFinished()) {
            final IDEDriveDescriptor result = cmd.getResult();
            if (result != null) {
                return result;
            } else {
                if (!cmd.isPacketResponse()) {
                    // Not an ATAPI response, try it anyway.
                    log.debug("NON-Packet response from ID command");
                } else {
                    log.debug("Packet response from ID command");
                }
            }
        } else {
            // Finish the command
            cmd.setError(ERR_ABORT);
            // Force a reset of any IRQ
            final int state = io.getStatusReg();
            // Timeout
            log.debug("Timeout of " + ideId + " state=" + NumberUtils.hex(state));
            return null;
        }

        // Clear any interrupts
        io.getStatusReg();
        // Interrupts enabled
        io.setControlReg(CTR_BLANK);

        // IDE Identify failed, do an ATAPI Identify
        cmd = new IDEIdCommand(primary, master, true);
        try {
            executeAndWait(cmd, IDE_TIMEOUT);
        } catch (TimeoutException ex1) {
            // Do nothing, handled later
        }
        // Force a reset of any IRQ
        io.getStatusReg();
        // Good result?
        if (cmd.isFinished()) {
            return cmd.getResult();
        } else {
            // Finish the command
            cmd.setError(ERR_ABORT);
            log.debug("Probe of " + ideId + " done, return null");
            // No device found
            return null;
        }
    }

    protected void softwareReset() {
        // Set reset
        io.setControlReg(CTR_SRST);
        TimeUtils.sleep(5);
        // Reset reset
        io.setControlReg(CTR_BLANK);
        // Wait until not busy
        int loops = 5;
        while (loops-- > 0) {
            if ((io.getStatusReg() & ST_BUSY) == 0) {
                // Device is ready
                this.currentCommand = null;
                return;
            } else {
                TimeUtils.sleep(5);
            }
        }
        log.info("Software reset of ide " + (primary ? "pri" : "sec") + " failed, still busy");
    }

    /**
     * Select the master of slave drive
     *
     * @param master
     * @return True if the controller accepted the setting, false otherwise.
     */
    protected final boolean selectDrive(boolean master) {
        final int select = SEL_BLANK
            | (master ? SEL_DRIVE_MASTER : SEL_DRIVE_SLAVE);
        io.setSelectReg(select);
        // Wait a while
        TimeUtils.sleep(50);
        return (io.getSelectReg() == select);
    }

    /**
     * Is the current register state a packet response.
     *
     * @return if the current register state is a packet response
     */
    protected boolean isPacketResponse() {
        final int sectCount = io.getSectorCountReg();
        final int lbaLow = io.getLbaLowReg();
        final int lbaMid = io.getLbaMidReg();
        final int lbaHigh = io.getLbaHighReg();
        io.getSelectReg();
        return ((sectCount == 0x01) && (lbaLow == 0x01) && (lbaMid == 0x14) && (lbaHigh == 0xEB));
    }

    /**
     * Write data towards the device.
     * If the length is longer then the byte-array, 0 bytes are padded
     * after the byte-array.
     *
     * @param src
     * @param ofs
     * @param length
     */
    public final void writeData(byte[] src, int ofs, int length) {
        final int srcLen = src.length - ofs;
        int len = Math.min(length, srcLen);
        //waitUntilNotBusy();
        for (; len > 0; len -= 2, length -= 2) {
            final int v0 = src[ofs++] & 0xFF;
            final int v1 = src[ofs++] & 0xFF;
            io.setDataReg(v0 | (v1 << 8));
        }
        // Send padding
        for (; length > 0; length -= 2) {
            io.setDataReg(0);
        }
    }

    /**
     * Read data from the device.
     *
     * @param dst
     * @param ofs
     * @param length
     */
    public final void readData(byte[] dst, int ofs, int length) {
        //waitUntilNotBusy();
        for (; length > 0; length -= 2) {
            final int v = io.getDataReg();
            dst[ofs++] = (byte) (v & 0xFF);
            dst[ofs++] = (byte) ((v >> 8) & 0xFF);
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see org.jnode.util.QueueProcessor#process(java.lang.Object)
     */
    public void process(IDECommand cmd) /*throws Exception*/ {
        // Wait until the controller is not busy anymore
        if (io.isBusy()) {
            try {
                io.waitUntilNotBusy(IDE_DATA_XFER_TIMEOUT);
            } catch (TimeoutException ex) {
                log.debug("Controller still busy");
            }
        }

        // If there is a current command active, wait for it.
        final IDECommand current = this.currentCommand;
        if (current != null) {
            if (!current.isFinished()) {
                log.debug("process: wait for current.isFinished");
                try {
                    current.waitUntilFinished(IDE_DATA_XFER_TIMEOUT);
                } catch (InterruptedException ex) {
                    // Ignore
                } catch (TimeoutException ex) {
                    // Ignore
                }
            }
            if (!current.isFinished()) {
                log.error("Last command not finished in time: " + current + " error " + current.hasError());
                current.setError(ERR_ABORT);
                softwareReset();
            }
        }

        this.currentCommand = cmd;
        try {
            cmd.setup(IDEBus.this, io);
        } catch (TimeoutException ex) {
            log.error("Timeout in setup of " + cmd);
            cmd.setError(ERR_ABORT);
            this.currentCommand = null;
        }
    }
}
