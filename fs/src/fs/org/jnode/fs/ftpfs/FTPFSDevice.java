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
 
package org.jnode.fs.ftpfs;

import org.jnode.driver.Device;
import org.jnode.driver.Bus;

/**
 * @author Levente S\u00e1ntha
 */
public class FTPFSDevice extends Device {
    private String host;
    private String user;
    private String password;
    
    public FTPFSDevice(String host, String user, String password) {
        super(null, "ftp-(" + host +"," + user + ")");
        this.host = host;
        this.user = user;
        this.password = password;
    }

    String getHost() {
        return host;
    }

    String getPassword() {
        return password;
    }

    String getUser() {
        return user;
    }
}
