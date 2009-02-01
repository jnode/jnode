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
 * CDB for an READ (10) command.
 * See SCSI Multimedia Commands-4, section 6.19.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class CDBRead10 extends CDB {

    /**
     * Initialize this instance.
     *
     * @param lba      Logical block address of first block that will be read.
     * @param nrBlocks The number of blocks that will be read.
     */
    public CDBRead10(int lba, int nrBlocks) {
        super(10, 0x28);
        setInt32(2, lba);
        setInt16(7, nrBlocks);
    }

    @Override
    public int getDataTransfertCount() {
        // TODO Auto-generated method stub
        return 0;
    }

}
