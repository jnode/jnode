/*
 * $Id: JMXTest.java,v 1.1 2003/11/25 11:41:41 epr Exp $
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
