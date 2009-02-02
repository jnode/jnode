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
 
package org.jnode.fs.smbfs;

import org.jnode.driver.Bus;
import org.jnode.driver.Device;

/**
 * @author Levente S\u00e1ntha
 */
public class SMBFSDevice extends Device {
    private String host;
    private String user;
    private String password;
    private String path;

    public SMBFSDevice(String host, String path, String user, String password) {
        super(null, "smb-(" + host + "," + path + "," + user + ")");
        this.host = host;
        this.path = path;
        this.user = user;
        this.password = password;
        System.setProperty("java.protocol.handler.pkgs", "jcifs");
    }

    String getHost() {
        return host;
    }

    String getPath() {
        return path;
    }

    String getPassword() {
        return password;
    }

    String getUser() {
        return user;
    }

    public SMBFSDevice(Bus bus, String id) {
        super(bus, id);
    }
}
