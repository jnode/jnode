/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Common Public License (CPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/cpl1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */
 
package org.mmtk.vm;

import org.jnode.system.BootLog;
import org.jnode.vm.Unsafe;
import org.jnode.vm.Vm;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Assert {
    public static final boolean VERIFY_ASSERTIONS = false;

    /**
     * This method should be called whenever an error is encountered.
     * 
     * @param str
     *            A string describing the error condition.
     */
    public static void error(String str) {
        BootLog.error(str);
    }

    /**
     * Logs a message and traceback, then exits.
     * 
     * @param message
     *            the string to log
     */
    public static void fail(String message) {
        BootLog.fatal(message);
        if (Vm.isRunningVm()) {
            Unsafe.die(message);
        } else {
            throw new Error(message);
        }
    }

    /**
     * ?
     * @param rc
     */
    public static void exit(int rc) {
        // TODO Understand me
    }

    /**
     * Checks that the given condition is true. If it is not, this method does a
     * traceback and exits.
     * 
     * @param cond
     *            the condition to be checked
     */
    public static void _assert(boolean cond) {
        if (!cond) {
            Unsafe.die("Assertion failed");
        }
    }

    /**
     * <code>true</code> if assertions should be verified
     */
    public static final boolean VerifyAssertions = false;

    public static void _assert(boolean cond, String s) {
        if (!cond) {
            Unsafe.die(s);
        }
    }

    public static final void dumpStack() {
    }

    /**
     * Throw an out of memory exception.
     */
    public static void failWithOutOfMemoryError() {
        Unsafe.die("Out of memory");
    }

    /**
     * Checks if the virtual machine is running. This value changes, so the
     * call-through to the VM must be a method. In Jikes RVM, just returns
     * VM.runningVM.
     * 
     * @return <code>true</code> if the virtual machine is running
     */
    public static boolean runningVM() {
        return Vm.isRunningVm();
    }

}
