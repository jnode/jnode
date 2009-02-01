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
 
package org.jnode.test.security;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Policy;
import java.security.PrivilegedAction;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class AccessControlTest implements Runnable {

    public static void main(String[] args) {

        new AccessControlTest("main").run();
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                new AccessControlTest("privileged").run();
                return null;
            }
        });
        new Thread(new AccessControlTest("threaded")).start();

    }

    private final String name;

    public AccessControlTest(String name) {
        this.name = name;
    }

    public void run() {
        AccessControlContext acc = AccessController.getContext();
        System.out.println("[" + name + "]");
        System.out.println("AccessControlContext = " + acc);
        System.out.println("DomainCombiner       = " + acc.getDomainCombiner());
        System.out.println("Policy               = " + Policy.getPolicy());
    }
}
