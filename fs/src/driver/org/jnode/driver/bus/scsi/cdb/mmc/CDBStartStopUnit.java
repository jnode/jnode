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
 
package org.jnode.driver.bus.scsi.cdb.mmc;

import org.jnode.driver.bus.scsi.CDB;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class CDBStartStopUnit extends CDB {

    /**
     * Initialize this instance.
     *
     * @param action     Action to perform.
     * @param returnAsap If true, the command will finish as soon as it is interpreted,
     *                   otherwise the command will finish as soon as the command has
     *                   beene executed.
     */
    public CDBStartStopUnit(Action action, boolean returnAsap) {
        super(6, 0x1b);
        if (returnAsap) {
            setInt8(1, 0x01);
        }
        setInt8(4, action.getCode());

    }

    public static final class Action {

        /**
         * Set the device into idle state
         */
        public static final Action SET_IDLE_STATE = new Action(0x20);

        /**
         * Set the device into standby state
         */
        public static final Action SET_STANDBY_STATE = new Action(0x30);

        /**
         * Set the device into sleep state
         */
        public static final Action SET_SLEEP_STATE = new Action(0x50);

        /**
         * Start the device
         */
        public static final Action START = new Action(0x01);

        /**
         * Stop the device
         */
        public static final Action STOP = new Action(0x00);

        /**
         * Eject the medium from the device
         */
        public static final Action EJECT = new Action(0x02);

        /**
         * Load the medium into the device
         */
        public static final Action LOAD = new Action(0x03);

        private final int code;

        private Action(int code) {
            this.code = code;
        }

        final int getCode() {
            return code;
        }
    }

    @Override
    public int getDataTransfertCount() {
        // TODO Auto-generated method stub
        return 0;
    }

}
