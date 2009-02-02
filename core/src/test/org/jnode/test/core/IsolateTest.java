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
 
package org.jnode.test.core;

import javax.isolate.Isolate;
import javax.isolate.IsolateStartupException;

public class IsolateTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        String mainClass;
        String[] isolateArgs;
        if (args.length > 0) {
            mainClass = args[0];
            isolateArgs = new String[args.length - 1];
            System.arraycopy(args, 1, isolateArgs, 0, args.length - 1);
        } else {
            mainClass = "org.jnode.test.core.IsolatedHelloWorld";
            isolateArgs = new String[0];
        }

        Isolate newIsolate = new Isolate(mainClass, isolateArgs);
        try {
            newIsolate.start();
        } catch (IsolateStartupException e) {
            e.printStackTrace();
        }
    }

}
