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

package org.jnode.shell.command;

import java.io.PrintWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ClassNameArgument;
import org.jnode.vm.classmgr.VmArrayClass;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.classmgr.VmType;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author crawley@jnode.org
 */
public class ClassCommand extends AbstractCommand {

    private final ClassNameArgument ARG_CLASS = new ClassNameArgument(
            "className", Argument.SINGLE | Argument.MANDATORY, 
            "the fully qualified Java name of the class to be viewed");

    public ClassCommand() {
        super("View a Java class");
        registerArguments(ARG_CLASS);
    }

    public void execute() throws Exception {
        String className = ARG_CLASS.getValue();
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            final Class<?> type = cl.loadClass(className);
            showClass(type, getOutput().getPrintWriter());
        } catch (ClassNotFoundException ex) {
            getError().getPrintWriter().println("Cannot find the requested class: " + className);
            exit(1);
        }
    }

    public static void main(String[] args) throws Exception {
        new ClassCommand().execute(args);
    }

    private void showClass(final Class<?> type, final PrintWriter out) {
        final VmType<?> vmType = AccessController.doPrivileged(
                new PrivilegedAction<VmType<?>>() {
                    public VmType<?> run() {
                        return type.getVmClass();
                    }
                });
        out.println("Name             : " + type.getName());
        // out.println("Is abstract      : " + type.isAbstract());
        out.println("Is array         : " + type.isArray());
        out.println("Is primitive     : " + type.isPrimitive());
        out.println("Shared statics   : " + vmType.isSharedStatics());
        out.println("Is initialized   : " + vmType.isInitialized());
        AccessController.doPrivileged(
                new PrivilegedAction<Void>() {
                    public Void run() {
                        out.println("Protection domain: " + type.getProtectionDomain());
                        return null;
                    }
                });

        if (vmType instanceof VmClassType) {
            out.println("#Instances       : " + ((VmClassType<?>) vmType).getInstanceCount());
        }
        if (vmType instanceof VmArrayClass) {
            out.println("Total length     : " + ((VmArrayClass<?>) vmType).getTotalLength());            
            out.println("Maximum length   : " + ((VmArrayClass<?>) vmType).getMaximumLength());            
        }
    }
}
