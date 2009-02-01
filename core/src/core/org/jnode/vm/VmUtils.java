/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.vm;

import gnu.java.lang.VMClassHelper;

/**
 * Utility class to share some Vm features.
 * For now, it's especially used to know how native methods are implemented in JNode.
 *
 * @author Fabien DUMINY (fduminy at jnode.org)
 */
public class VmUtils {
    private static final String NATIVE_CLASSNAME_PREFIX = "Native";

    public static boolean couldImplementNativeMethods(String className) {
        String clsName = VMClassHelper.getClassNamePortion(className);
        return clsName.startsWith(NATIVE_CLASSNAME_PREFIX);
    }

    public static String getNativeClassName(String className) {
        final String pkg = VMClassHelper.getPackagePortion(className);
        final String nativeClassName = pkg + ((pkg.length() > 0) ? "." : "")
            + NATIVE_CLASSNAME_PREFIX + VMClassHelper.getClassNamePortion(className);
        return nativeClassName;
    }

    public static boolean allowNatives(String className, String architectureName) {
        boolean allowNatives = false;
        allowNatives |= className.equals("org.jnode.vm.Unsafe");
        /*
        allowNatives |= className.equals("org.jnode.vm." + architectureName + ".Unsafe"
                + architectureName.toUpperCase());
          */
        allowNatives |= className.indexOf("org.jnode.vm.") > -1 && className.indexOf(".Unsafe") > -1;
        return allowNatives;
    }
}
