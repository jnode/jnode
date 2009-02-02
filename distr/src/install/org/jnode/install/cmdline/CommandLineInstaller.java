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
 
package org.jnode.install.cmdline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.jnode.install.AbstractInstaller;
import org.jnode.install.InputContext;
import org.jnode.install.OutputContext;
import org.jnode.install.action.CopyFilesAction;
import org.jnode.install.action.GrubInstallerAction;

/**
 * @author Levente S\u00e1ntha
 */
public class CommandLineInstaller extends AbstractInstaller {

    public CommandLineInstaller() {
        //grub
        actionList.add(new GrubInstallerAction());
        //files
        actionList.add(new CopyFilesAction());
    }

    public static void main(String... argv) {
        new CommandLineInstaller().start();
    }


    protected InputContext getInputContext() {
        return new InputContext() {
            private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            public String getStringInput(String message) {
                try {
                    System.out.println(message);
                    return in.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    protected OutputContext getOutputContext() {
        return new OutputContext() {
            public void showMessage(String msg) {
                System.out.println(msg);
            }
        };
    }
}
