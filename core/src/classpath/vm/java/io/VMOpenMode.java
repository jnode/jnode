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
 
package java.io;

import org.apache.log4j.Logger;

import gnu.java.nio.channels.FileChannelImpl;

/**
 * @author epr
 */
public enum VMOpenMode {
	
	/** Open for reading only, open fails if file does not exist */
	READ ("r", true, false),
	/** Open for writing only, file is created if file does not exist */
	WRITE("w", false, true),
	/** Open for reading and writing, file is created if file does not exist */
	READ_WRITE("rw", true, true);

    private static final Logger log = Logger.getLogger(VMOpenMode.class);
        
	private final String mode;
	private final boolean read;	
	private final boolean write;	
	private VMOpenMode(String mode, boolean read, boolean write) {
		this.mode = mode;
		this.read = read;
		this.write = write;
	}
    
    public static VMOpenMode valueOf(int mode)
    {
        // These are mode values for open().
        VMOpenMode value = null;
        boolean read = ((mode & FileChannelImpl.READ) == FileChannelImpl.READ); 
        boolean write = ((mode & FileChannelImpl.WRITE) == FileChannelImpl.WRITE);
        boolean append = ((mode & FileChannelImpl.APPEND) == FileChannelImpl.APPEND);
        if(read && !write)
        {
            value = READ;
        }
        else if(write && !append)
        {
            value = WRITE;
        }
        else if(write && append)
        {
            value = READ_WRITE;
        }
        else
        {
            //TODO: valueOf for EXCL, SYNC, DSYNC and combination of the 6 values               
            //FileChannelImpl.EXCL:
            //FileChannelImpl.SYNC:
            //FileChannelImpl.DSYNC:
            throw new IllegalArgumentException("unknown open mode: "+mode);
        }
        
        return value;
    }
	
	public String toString() {
		return mode;
	}

	/**
	 * Open for (at least) read?
	 */
	public boolean canRead() {
		return read;
	}

	/**
	 * Open for (at least) write?
	 */
	public boolean canWrite() {
		return write;
	}
}
