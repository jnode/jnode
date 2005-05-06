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

package org.mmtk.vm.gcspy;

import org.mmtk.utility.gcspy.AbstractTile;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.Address;

/**
 * VM-neutral stub file for the GCspy Space abstraction. Here, it largely to
 * forward calls to the gcspy C library. $Id: ServerSpace.java,v 1.1 2005/05/04
 * 08:59:28 epr Exp $
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author <a href="http://www.ukc.ac.uk/people/staff/rej">Richard Jones</a>
 * @version $Revision$
 * @date $Date$
 */
public class ServerSpace implements Uninterruptible {
    public ServerSpace(int id, String serverName, String driverName,
            String title, String blockInfo, int tileNum, String unused,
            boolean mainSpace) {
    }

    public void setTilename(int i, Address start, Address end) {
    }

    Address addStream(int id) {
        return Address.zero();
    }

    Address getDriverAddress() {
        return Address.zero();
    }

    public void resize(int size) {
    }

    public void startComm() {
    }

    public void stream(int id, int len) {
    }

    public void streamByteValue(byte value) {
    }

    public void streamShortValue(short value) {
    }

    public void streamIntValue(int value) {
    }

    public void streamEnd() {
    }

    public void summary(int id, int len) {
    }

    public void summaryValue(int val) {
    }

    public void summaryEnd() {
    }

    public void controlEnd(int tileNum, AbstractTile[] tiles) {
    }

    public void spaceInfo(Address info) {
    }

    public void endComm() {
    }
}
