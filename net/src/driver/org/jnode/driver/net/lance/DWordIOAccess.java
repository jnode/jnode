/*
 * $Id$
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
