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

package org.jnode.driver.net.lance;

import org.jnode.system.IOResource;

/**
 * @author Chris
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
