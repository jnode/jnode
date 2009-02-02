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
 
package org.jnode.test;

import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

/**
 * @author Mark Hale (markhale@users.sourceforge.net)
 */
public class JMXTest {

    public static void main(String[] args) throws Exception {
        AccessController.doPrivileged(new PrivilegedExceptionAction() {
            public Object run() throws Exception {
                MBeanServerFactory.createMBeanServer();
                printMBeanServers();
                return null;
            }
        });
    }

    private static void printMBeanServers() {
        System.out.println("Listing MBean servers");
        List servers = MBeanServerFactory.findMBeanServer(null);
        for (Iterator iter = servers.iterator(); iter.hasNext();) {
            MBeanServer server = (MBeanServer) iter.next();
            printMBeans(server);
        }
    }

    private static void printMBeans(MBeanServer server) {
        System.out.println("Listing MBeans for " + server);
        Set mbeans = server.queryNames(null, null);
        for (Iterator iter = mbeans.iterator(); iter.hasNext();) {
            System.out.println(iter.next());
        }
    }
}
