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
 
package org.jnode.driver.net.prism2;

import static org.jnode.driver.net.prism2.Prism2Constants.Register.RESP0;
import static org.jnode.driver.net.prism2.Prism2Constants.Register.RESP1;
import static org.jnode.driver.net.prism2.Prism2Constants.Register.RESP2;
import static org.jnode.driver.net.prism2.Prism2Constants.Register.STATUS;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class Prism2CommandResponse {
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
        this.status = core.getReg(STATUS);
        this.response0 = core.getReg(RESP0);
        this.response1 = core.getReg(RESP1);
        this.response2 = core.getReg(RESP2);
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
     * 
     * @return Returns the status.
     */
    public final int getStatus() {
        return status;
    }

    /**
     * Gets the result code.
     * 
     * @return
     */
    public final int getResult() {
        return (status & Prism2Constants.STATUS_RESULT) >> 8;
    }

}
