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

package org.jnode.test;

import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ViewMethodTest {

    public static void main(String[] args) throws ClassNotFoundException {
        final String className = args[0];
        final String mname = (args.length > 1) ? args[1] : null;

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final VmType cls = VmType.fromClass(cl.loadClass(className));

        final int cnt = cls.getNoDeclaredMethods();
        for (int i = 0; i < cnt; i++) {
            final VmMethod method = cls.getDeclaredMethod(i);
            if ((mname == null) || method.getName().equals(mname)) {
                System.out.println("OptL: " + method.getNativeCodeOptLevel());
                System.out.println("Code: " + method.getDefaultCompiledCode());
            }
        }

    }
}
