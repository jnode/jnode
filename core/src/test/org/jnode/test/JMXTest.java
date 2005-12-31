/*
 * $Id: header.txt,v 1.2 2005/01/04 17:50:55 epr Exp $
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
 
package org.jnode.test;

import java.security.*;
import java.util.List;
import java.util.Set;
import java.util.Iterator;
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
