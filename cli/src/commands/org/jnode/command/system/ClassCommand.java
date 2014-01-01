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
 
package org.jnode.command.system;

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
    
    private static final String help_class = "the fully qualified Java name of the class to be viewed";
    private static final String help_super = "View a Java class";
    private static final String err_no_class = "Cannot find the requested class: %s%n";
    private static final String fmt_info = "%17s: %s%n";
    private static final String str_name = "Name";
    private static final String str_array = "Is array";
    private static final String str_prim = "Is primitive";
    private static final String str_statics = "Shared statics";
    private static final String str_init = "Is initialized";
    private static final String str_prot = "Protection domain";
    private static final String str_len = "Total Length";
    private static final String str_max_len = "Maximum length";
    private static final String str_instance = "#Instances";
    
    private final ClassNameArgument argClass;

    public ClassCommand() {
        super(help_super);
        argClass = new ClassNameArgument("className", Argument.SINGLE | Argument.MANDATORY, help_class);
        registerArguments(argClass);
    }

    public void execute() throws Exception {
        String className = argClass.getValue();
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            final Class<?> type = cl.loadClass(className);
            showClass(type, getOutput().getPrintWriter());
        } catch (ClassNotFoundException ex) {
            getError().getPrintWriter().format(err_no_class, className);
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
                        return VmType.fromClass((Class<?>) type);
                    }
                });
        out.format(fmt_info, str_name, type.getName());
        // out.println("Is abstract      : " + type.isAbstract());
        out.format(fmt_info, str_array, type.isArray());
        out.format(fmt_info, str_prim, type.isPrimitive());
        out.format(fmt_info, str_statics, vmType.isSharedStatics());
        out.format(fmt_info, str_init, vmType.isInitialized());
        AccessController.doPrivileged(
                new PrivilegedAction<Void>() {
                    public Void run() {
                        out.format(fmt_info, str_prot, type.getProtectionDomain());
                        return null;
                    }
                });

        if (vmType instanceof VmClassType) {
            out.format(fmt_info, str_instance, ((VmClassType<?>) vmType).getInstanceCount());
        }
        if (vmType instanceof VmArrayClass) {
            out.format(fmt_info, str_len, ((VmArrayClass<?>) vmType).getTotalLength());
            out.format(fmt_info, str_max_len, ((VmArrayClass<?>) vmType).getMaximumLength());
        }
    }
}
