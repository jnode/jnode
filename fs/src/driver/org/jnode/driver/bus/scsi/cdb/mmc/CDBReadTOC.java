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
 * CDB for an READ TOC command.
 * See SCSI Multimedia Commands-4, section 6.30.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class CDBReadTOC extends CDB {
    private int dataTransfertCount;

    public CDBReadTOC(int allocationLength) {
        super(10, 0x43);
        setInt8(1, 0x02); // MSF flag
        dataTransfertCount = allocationLength;
        setInt16(7, dataTransfertCount);
        setInt8(9, 0x80); // TOC format
    }

    @Override
    public int getDataTransfertCount() {
        return dataTransfertCount;
    }
}
