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
 
package org.jnode.driver.bus.scsi.cdb.mmc;

import org.jnode.driver.bus.scsi.CDB;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class CDBMediaRemoval extends CDB {

    /**
     * Initialize this instance.
     *
     * @param prevent    If true, the device will not open, if false the device can be opened.
     * @param persistent Should the setting be persistent
     */
    public CDBMediaRemoval(boolean prevent, boolean persistent) {
        super(6, 0x1e);
        int code = 0;
        code |= (prevent ? 0x01 : 0x00);
        code |= (persistent ? 0x02 : 0x00);
        setInt8(4, code);
    }

    @Override
    public int getDataTransfertCount() {
        return 0;
    }
}
