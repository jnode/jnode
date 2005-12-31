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
 
package org.jnode.driver.bus.scsi;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface SCSIConstants {
    
    // Timeouts
    
    /** No timeout in the device */
    public static final long GROUP_NOTIMEOUT = 500;
    /** Group1 timeout, do not retry */
    public static final long GROUP1_TIMEOUT = 5000;
    /** Group2 timeout, you can retry */
    public static final long GROUP2_TIMEOUT = 5000;
    

}
