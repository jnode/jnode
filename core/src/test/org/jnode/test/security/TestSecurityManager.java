/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.test.security;

import java.net.URLClassLoader;
import java.security.Security;

public class TestSecurityManager {
    static class mytest {
        mytest() {
        }
    }

    static class MySM extends SecurityManager {
        public void checkPermission(java.security.Permission perm) {
            if (perm.getName().equals("aaaaaa")) {
                throw new SecurityException("no exit !");
            } else if (!perm.getName().equals("charsetProvider")) {
                System.err.println("perm.getName()=" + perm.getName());
            }
        }
    }

    static public void main(String args[]) throws Exception {
        Class sc = SecurityManager.class;
        Class sc2 = Security.class;
        Class sc3 = java.security.Permission.class;
        Class sc4 = java.lang.StringBuffer.class;
        Class sc5 = java.io.PrintStream.class;

        System.setSecurityManager(new MySM());

        URLClassLoader cl = (URLClassLoader) TestSecurityManager.class.getClassLoader();
        URLClassLoader cl2 = new URLClassLoader(cl.getURLs());
        Class c = Class.forName("org.jnode.test.security.TestSecurityManager$mytest", true, cl2);

        c.newInstance();
    }
}
