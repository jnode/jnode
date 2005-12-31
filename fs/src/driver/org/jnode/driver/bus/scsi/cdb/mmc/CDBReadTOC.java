/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.driver.bus.scsi.cdb.mmc;

import org.jnode.driver.bus.scsi.CDB;


/**
 * CDB for an READ TOC command.
 * See SCSI Multimedia Commands-4, section 6.30.

 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class CDBReadTOC extends CDB {

    public CDBReadTOC(int allocationLength) {
        super(10, 0x43);
        setInt8(1, 0x02); // MSF flag
        setInt16(7, allocationLength);
        setInt8(9, 0x80); // TOC format
    }
    
}
