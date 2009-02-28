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
 
package org.jnode.apps.telnetd;

import java.io.InputStream;
import java.util.Properties;

import net.wimpi.telnetd.TelnetD;

/**
 * This is the command used to start the telnet daemon.
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 * 
 */
public class TelnetServerCommand {
    /**
     * Main method.
     * @param args not used
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        try {
            // 1. prepare daemon
            Properties props = new Properties();
            InputStream is = TelnetServerCommand.class.getResourceAsStream("telnetd.properties");
            props.load(is);

            TelnetD daemon = TelnetD.createTelnetD(props);

            // 2.start serving/accepting connections
            daemon.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
