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

import org.jnode.util.Command;

/**
 * @author epr
 */
public abstract class FloppyCommand extends Command implements FloppyConstants {

    /**
     * The drive number 0..3
     */
    private final int drive;
    private FloppyException error;

    /**
     * Create a new instance
     *
     * @param drive
     */
    public FloppyCommand(int drive) {
        if ((drive < 0) || (drive > 3)) {
            throw new IllegalArgumentException("Invalid drive " + drive);
        }
        this.drive = drive;
    }

    /**
     * Execute the command phase of this command.
     *
     * @param fdc
     * @throws FloppyException
     */
    public abstract void setup(FDC fdc)
        throws FloppyException;

    /**
     * Handle the given IRQ
     *
     * @param fdc
     * @throws FloppyException
     */
    public abstract void handleIRQ(FDC fdc)
        throws FloppyException;

    /**
     * Gets the driver number for which this command is intended.
     *
     * @return drive
     */
    public int getDrive() {
        return drive;
    }

    protected void notifyError(FloppyException ex) {
        this.error = ex;
        notifyFinished();
    }

    /**
     * Has an error occured?
     *
     * @return boolean
     */
    public boolean hasError() {
        return (error != null);
    }

    /**
     * Gets the error
     *
     * @return error
     */
    public FloppyException getError() {
        return error;
    }
}
