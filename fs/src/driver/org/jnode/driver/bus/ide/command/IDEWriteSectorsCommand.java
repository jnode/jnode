/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.driver.bus.ide.command;

import org.jnode.driver.bus.ide.IDEBus;
import org.jnode.driver.bus.ide.IDEIO;
import org.jnode.util.TimeoutException;

/**
 * This is a simple write command for IDE drives.
 * It uses the old style non DMA command style for the moment.
 * @author gbin
 */
public class IDEWriteSectorsCommand extends IDERWSectorsCommand {

	private final byte[] data;
	private final int offset;
	private final int length;
	private int currentPosition;

	//private int readSectors;

	public IDEWriteSectorsCommand(
	        boolean primary,
		boolean master,
		long lbaStart,
		int sectors,
		byte[] src,
		int srcOffset,
		int length) {
		super(primary, master, lbaStart, sectors);
		this.data = src;
		this.offset = srcOffset;
		this.currentPosition = srcOffset;
		this.length = length;
	}

	/**
	 * @see org.jnode.driver.bus.ide.IDECommand#setup(IDEBus, IDEIO)
	 */
	protected void setup(IDEBus ide, IDEIO io) 
	throws TimeoutException {
		super.setup(ide, io);
		io.setCommandReg(CMD_WRITE);
		transfertASector(ide, io);
	}

	private void transfertASector(IDEBus ide, IDEIO io) throws TimeoutException {
		io.waitUntilNotBusy(IDE_TIMEOUT);
		for (int i = 0; i < 256; i++) {
			io.setDataReg(
				(data[currentPosition] & 0xFF)  + ((data[currentPosition + 1]&0xFF) << 8));
			currentPosition += 2;
		}
	}

	/**
	 * @see org.jnode.driver.bus.ide.IDECommand#handleIRQ(IDEBus, IDEIO)
	 */
	protected void handleIRQ(IDEBus ide, IDEIO io) throws TimeoutException {
		final int state = io.getStatusReg();
		if ((state & ST_ERROR) != 0) {
			setError(io.getErrorReg());
		} else {
		    if ((state & (ST_BUSY | ST_DEVICE_READY)) == ST_DEVICE_READY) {
		        if (currentPosition < offset + length) {
		            transfertASector(ide, io);
		        } else {
		            notifyFinished();
		        }
		    }
		}
	}

}
