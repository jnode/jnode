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
 
package org.jnode.shell.isolate;

import javax.isolate.Isolate;
import javax.isolate.Link;

import org.jnode.shell.CommandRunner;
import org.jnode.vm.isolate.ObjectLinkMessage;

public class IsolateCommandLauncher {

    /**
     * @param args
     */
    public static void main(String[] args) {
        Link cl = Isolate.getLinks()[0];
        CommandRunner cr;
        try {
            ObjectLinkMessage message = (ObjectLinkMessage) cl.receive();
            cr = (CommandRunner) message.extract();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        cr.run();
    }
}
