/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.vm.facade;

import gnu.java.lang.VMClassHelper;
import java.io.PrintWriter;
import org.jnode.annotation.Inline;
import org.jnode.annotation.KernelSpace;
import org.jnode.annotation.NoInline;
import org.jnode.annotation.SharedStatics;
import org.jnode.annotation.Uninterruptible;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.objects.Counter;
import org.jnode.vm.objects.Statistic;

/**
 * Utility class to share some Vm features.
 * For now, it's especially used to know how native methods are implemented in JNode.
 *
 * @author Fabien DUMINY (fduminy at jnode.org)
 */
@SharedStatics
public class VmUtils {
    private static Vm VM_INSTANCE;

    /**
     * Should assertions be verified?
     */
    private static final boolean VERIFY_ASSERTIONS = true;

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
        allowNatives |= className.contains("org.jnode.vm.") && className.contains(".Unsafe");
        return allowNatives;
    }

    /**
     * @return Returns the {@link org.jnode.vm.facade.Vm} instance.
     */
    @KernelSpace
    @Uninterruptible
    public static final Vm getVm() {
        return VM_INSTANCE;
    }

    /**
     *
     */
    @KernelSpace
    @Uninterruptible
    public static final void setVm(Vm vm) {
        if (VM_INSTANCE != null) {
            throw new SecurityException("Vm instance already set");
        }
        VM_INSTANCE = vm;
    }

    /**
     * Is JNode currently running.
     *
     * @return true or false
     */
    public static final boolean isRunningVm() {
        return ((VM_INSTANCE != null) && !VM_INSTANCE.isBootstrap());
    }

    /**
     * Is the bootimage being written?
     *
     * @return true or false.
     */
    public static final boolean isWritingImage() {
        return ((VM_INSTANCE == null) || VM_INSTANCE.isBootstrap());
    }

    /**
     * A new type has been resolved by the VM. Create a new MM type to reflect
     * the VM type, and associate the MM type with the VM type.
     *
     * @param vmType The newly resolved type
     */
    @Inline
    public static void notifyClassResolved(VmType<?> vmType) {
        if (VM_INSTANCE != null) {
            final VmHeapManager hm = VM_INSTANCE.getHeapManager();
            if (hm != null) {
                hm.notifyClassResolved(vmType);
            }
        }
    }

    public static boolean verifyAssertions() {
        return VERIFY_ASSERTIONS;
    }

    /**
     * Assert the given value to be true.
     *
     * @param value
     */
    public static void _assert(boolean value) {
        if (!value) {
            assertionFailed(null, null);
        }
    }

    /**
     * Assert the given value to be true.
     *
     * @param value
     */
    public static void _assert(boolean value, String msg) {
        if (!value) {
            assertionFailed(msg, null);
        }
    }

    /**
     * Assert the given value to be true.
     *
     * @param value
     */
    public static void _assert(boolean value, String msg, String msg2) {
        if (!value) {
            assertionFailed(msg, msg2);
        }
    }

    /**
     * Throw an AssertionError with the given messages.
     *
     * @param msg
     * @param msg2
     */
    @NoInline
    private static void assertionFailed(String msg, String msg2) {
        if ((msg == null) && (msg2 == null)) {
            msg = "Assertion failed";
        } else if (msg2 != null) {
            msg = msg + ": " + msg2;
        }
        throw new AssertionError(msg);
    }

    /**
     * Dump VM's statistics to the given {@link PrintWriter}.
     *
     * @param out
     */
    public static void dumpStatistics(PrintWriter out) {
        final Statistic[] stats = VM_INSTANCE.getStatistics();
        for (Statistic stat : stats) {
            out.println(stat);
        }
    }

    /**
     * Reset VM's counters.
     */
    public static void resetCounters() {
        final Statistic[] stats = VM_INSTANCE.getStatistics();
        for (final Statistic s : stats) {
            if (s instanceof Counter) {
                ((Counter) s).reset();
            }
        }
    }
}
