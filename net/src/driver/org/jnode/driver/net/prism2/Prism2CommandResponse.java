/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 
package org.jnode.driver.net.prism2;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class Prism2CommandResponse implements Prism2Constants {

    private int status;

    private int response0;

    private int response1;

    private int response2;

    /**
     * Read the status from the device.
     * 
     * @param core
     */
    final void initialize(Prism2IO core) {
        this.status = core.getReg(REG_STATUS);
        this.response0 = core.getReg(REG_RESP0);
        this.response1 = core.getReg(REG_RESP1);
        this.response2 = core.getReg(REG_RESP2);
    }

    /**
     * @return Returns the response0.
     */
    public final int getResponse0() {
        return response0;
    }

    /**
     * @return Returns the response1.
     */
    public final int getResponse1() {
        return response1;
    }

    /**
     * @return Returns the response2.
     */
    public final int getResponse2() {
        return response2;
    }

    /**
     * Gets the full status value 
     * @return Returns the status.
     */
    public final int getStatus() {
        return status;
    }
    
    /**
     * Gets the result code.
     * @return
     */
    public final int getResult() {
        return (status & STATUS_RESULT) >> 8;
    }

}
