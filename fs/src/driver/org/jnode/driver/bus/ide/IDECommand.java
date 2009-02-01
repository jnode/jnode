/*
 * $Id$
 *
 * JNode.org
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
 
package org.jnode.driver.bus.ide;

import org.apache.log4j.Logger;
import org.jnode.util.Command;
import org.jnode.util.TimeoutException;

/**
 * @author epr
 */
public abstract class IDECommand extends Command implements IDEConstants {

    protected static final Logger log = Logger.getLogger(IDECommand.class);

    /**
     * ID From master (true) or slave (false)
     */
    protected final boolean master;

    /**
     * The taskfile for which this command is intended
     */
    private final boolean primary;

    /**
     * Error that occurred during this command. -1 means no error
     */
    private int error = -1;

    /**
     * Create a new instance
     *
     * @param master Command intended for master (true) or slave (false)
     * @throws IllegalArgumentException Invalid argument
     */
    protected IDECommand(boolean primary, boolean master)
        throws IllegalArgumentException {
        this.primary = primary;
        this.master = master;
    }

    /**
     * Setup the IDE controller for this command
     */
    protected abstract void setup(IDEBus ide, IDEIO io) throws TimeoutException;

    /**
     * Handle an IDE IRQ.
     */
    protected abstract void handleIRQ(IDEBus ide, IDEIO io)
        throws TimeoutException;

    /**
     * Is this command for the master (true) or slave (false).
     *
     * @return if this command is for the master
     */
    public final boolean isMaster() {
        return master;
    }

    /**
     * Is this command intended for the primary channel.
     *
     * @return if this command is intended for the primary channel
     */
    public final boolean isPrimary() {
        return primary;
    }

    /**
     * Is this command intended for the secondary channel.
     *
     * @return if this command is intended for the secondary channel
     */
    public final boolean isSecondary() {
        return !primary;
    }

    /**
     * Has this command got an error?
     *
     * @return if this command got an error
     */
    public final boolean hasError() {
        return (error != -1);
    }

    /**
     * Gets the error code that occurred during this command
     *
     * @return the error code that occurred during this command
     */
    public final int getError() {
        return error;
    }

    /**
     * Set the error code.
     * This also notifies this command as being finished.
     *
     * @param error
     */
    protected void setError(int error) {
        this.error = error;
        notifyFinished();
    }

    /**
     * Gets the value for the select register.
     *
     * @return the value for the select register
     */
    protected final int getSelect() {
        if (master) {
            return SEL_BLANK | SEL_DRIVE_MASTER;
        } else {
            return SEL_BLANK | SEL_DRIVE_SLAVE;
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getClass().getName() + " " + (primary ? "primary" : "secondary")
            + "." + (master ? "master" : "slave");
    }
}
