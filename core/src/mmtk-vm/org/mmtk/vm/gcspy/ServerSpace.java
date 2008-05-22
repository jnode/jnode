/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Common Public License (CPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/cpl1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
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
