/*
 * $Id$
 */
package org.jnode.driver.net.lance;

import org.jnode.system.IOResource;

/**
 * @author Chris
 *
 */
public class WordIOAccess extends IOAccess implements LanceConstants {
	
	public WordIOAccess(IOResource io, int iobase) {
		super(io, iobase);
	}
	
	public String getType() {
		return "Word";
	}
	
	public void reset() {
		// Read triggers a reset
		io.inPortWord(iobase + WIO_RESET);
	}

	public int getCSR(int csrnr) {
		io.outPortWord(iobase + WIO_RAP, csrnr);
		return io.inPortWord(iobase + WIO_RDP);
	}

	public void setCSR(int csrnr, int value) {
		io.outPortWord(iobase + WIO_RAP, csrnr);
		io.outPortWord(iobase + WIO_RDP, value);
	}

	public int getBCR(int bcrnr) {
		io.outPortWord(iobase + WIO_RAP, bcrnr);
		return io.inPortWord(iobase + WIO_BDP);
	}

	public void setBCR(int bcrnr, int value) {
		io.outPortWord(iobase + WIO_RAP, bcrnr);
		io.outPortWord(iobase + WIO_BDP, value);
	}

}
