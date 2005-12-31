/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
 
package org.jnode.driver.net.lance;

import org.jnode.system.IOResource;

/**
 * @author Chris Cole
 *
 */
public class DWordIOAccess extends IOAccess implements LanceConstants {

	public DWordIOAccess(IOResource io, int iobase) {
		super(io, iobase);
	}
	
	public String getType() {
		return "DWord";
	}
	
	public void reset() {
		// Read triggers a reset
		io.inPortDword(iobase + DWIO_RESET);
	}

	public int getCSR(int csrnr) {
		io.outPortDword(iobase + DWIO_RAP, csrnr);
		return io.inPortDword(iobase + DWIO_RDP);
	}

	public void setCSR(int csrnr, int value) {
		io.outPortDword(iobase + DWIO_RAP, csrnr);
		io.outPortDword(iobase + DWIO_RDP, value);
	}

	public int getBCR(int bcrnr) {
		io.outPortDword(iobase + DWIO_RAP, bcrnr);
		return io.inPortDword(iobase + DWIO_BDP);
	}

	public void setBCR(int bcrnr, int value) {
		io.outPortDword(iobase + DWIO_RAP, bcrnr);
		io.outPortDword(iobase + DWIO_BDP, value);
	}
}
