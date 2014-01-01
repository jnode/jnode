/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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

import java.util.Map;

import javax.isolate.Isolate;
import javax.isolate.Link;

import org.jnode.shell.CommandRunner;
import org.jnode.vm.Unsafe;
import org.jnode.vm.isolate.ObjectLinkMessage;

/**
 * This is the class that the isolate invoker uses to launch the user's
 * command in the new isolate.  
 * 
 * @author crawley@jnode.org
 */
public class IsolateCommandLauncher {

    /**
     * The entry point that used to run the 'application' when the isolate is
     * started.  The actual command is then passed to us in a Link message
     * in the form of a CommandRunner object.
     * 
     * @param args
     */
    public static void main(String[] args) {
        Link cl = Isolate.getLinks()[0];
        CommandRunner cr;
        try {
            ObjectLinkMessage message = (ObjectLinkMessage) cl.receive();
            cr = (CommandRunner) message.extract();
            Map<String, String> env = cr.getEnv();
            int envSize = (env == null) ? 0 : env.size();
            byte[][] binEnv = new byte[envSize * 2][];
            if (envSize > 0) {
                int i = 0; 
                for (Map.Entry<String, String> entry : env.entrySet()) {
                    binEnv[i++] = entry.getKey().getBytes();
                    binEnv[i++] = entry.getValue().getBytes();
                }
            }
            NativeProcessEnvironment.setIsolateInitialEnv(binEnv);
        } catch (Exception e) {
            Unsafe.debugStackTrace(e.getMessage(), e);
            return;
        }
        cr.run();
    }
}
