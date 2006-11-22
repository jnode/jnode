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
 
package org.jnode.driver.block.floppy;

import org.jnode.system.DMAException;
import org.jnode.system.IRQHandler;
import org.jnode.util.TimeoutException;

/**
 * @author epr
 */
public interface FDC extends IRQHandler, FloppyConstants {
	/**
	 * Gets the STATE register
 	 * @return int
	 */
	public int getStateReg();

	/**
	 * Gets the DOR register
	 * @return int
	 */
	public int getDorReg();

	/**
	 * Gets the DIR register
	 * @return int
	 */
	public int getDirReg();
	
	/**
	 * Has the disk changed since the last command?
	 * @param drive
	 * @param resetFlag
	 * @return boolean
	 */
	public boolean diskChanged(int drive, boolean resetFlag);
	
	/**
	 * Add the given command to the command queue and wait till the command
	 * has finished.
	 * @param cmd
	 * @param timeout
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	public void executeAndWait(FloppyCommand cmd, long timeout)
	        throws InterruptedException, TimeoutException;

	/**
	 * Release all resources.
	 */
	public void release();
	
    /**
     * Reset the FDC
     */
    public void reset();
    
	/**
	 * @param irq
	 * @see org.jnode.system.IRQHandler#handleInterrupt(int)
	 */
	public void handleInterrupt(int irq);

	/**
	 * Is the primary FDC used.
	 * @return True if the primary controller is used, false if the secondary controller is used.
	 */
	public boolean isPrimary();
	
	/**
	 * Gets the drive parameters for a given drive
	 * @param drive
	 * @return Parameters
	 */
	public FloppyDriveParameters getDriveParams(int drive);
	
	/**
	 * Gets the number of drives under control of this controller
	 * @return Number of drivers
	 */
	public int getDriveCount();
	
	/**
	 * Gets the data transfer rate for a given drive in Kb/sec
	 * @param drive
	 * @return DTR
	 */
	public int getDTR(int drive);
	
	/**
	 * Logs the DMA State via default logger
	 * @throws DMAException
	 */
	public void logDMAState() throws DMAException;

	/**
	 * Sets the DOR register
	 * @param drive
	 * @param motorOn
	 * @param dma
 	 */
    public void setDorReg(int drive, boolean motorOn, boolean dma);

	/**
	 * Send a command to the FDC
	 * @param command
	 * @param enableDMA
	 * @throws FloppyException
	 */
    public void sendCommand(byte[] command, boolean enableDMA) throws FloppyException;

	/**
	 * Gets a command status from the FDC
	 * @param length
	 * @return the command status from the FDC
	 * @throws TimeoutException
	 * @throws FloppyException
	 */
    public byte[] getCommandState(int length) throws TimeoutException, FloppyException;

	/**
	 * Setup the floppy DMA channel to transfer from/to the DMA memory buffer
	 * @param length Number of bytes to transfer
	 * @param mode DMAResource.MODE_READ or DMAResource.MODE_WRITE
	 * @throws FloppyException
	 */
    public void setupDMA(int length, int mode) throws FloppyException;

	/**
	 * Copy from the DMA buffer into the given byte array
	 * @param data
	 * @param dataOffset
	 * @param length
	 */
    public void copyFromDMA(byte[] data, int dataOffset, int length);

	/**
	 * Gets status register 0
	 * @return the status register 0
	 * @throws TimeoutException
	 * @throws FloppyException
	 */
    public int getST0() throws TimeoutException, FloppyException;

    /**
	 * Copy from the given byte array into the DMA buffer 
	 * @param data
	 * @param dataOffset
	 * @param length
	 */
    public void copyToDMA(byte[] data, int dataOffset, int length);
}
