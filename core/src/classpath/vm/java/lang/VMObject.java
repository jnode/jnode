/*
 * VMObject.java -- Reference implementation for VM hooks used by Object
 * Copyright (C) 1998, 2002 Free Software Foundation
 * 
 * This file is part of GNU Classpath.
 * 
 * GNU Classpath is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2, or (at your option) any later version.
 * 
 * GNU Classpath is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * GNU Classpath; see the file COPYING. If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but
 * you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

package java.lang;

import org.jnode.vm.MonitorManager;
import org.jnode.vm.VmSystem;

/**
 * Object is the ultimate superclass of every class (excepting interfaces). As
 * such, it needs help from the VM.
 * 
 * @author John Keiser
 * @author Eric Blake <ebb9@email.byu.edu>
 */
final class VMObject {

    /**
     * Gets the class of a given object.
     * @param o
     * @return
     */
	public static Class getClass(Object o) {
		return VmSystem.getClass(o);
	}

    /**
     * The VM is expected to make a field-for-field shallow copy of the
     * argument. Thus, the copy has the same runtime type as the argument. Note,
     * however, that the cloned object must still be finalizable, even if the
     * original has already had finalize() invoked on it.
     * 
     * @param c
     *            the Cloneable to clone
     * @return the clone
     */
    static Object clone(Cloneable c) {
        return VmSystem.clone(c);
    }

    /**
     * Wakes up one of the threads that is waiting on this Object's monitor.
     * Only the owner of a lock on the Object may call this method. The Thread
     * to wake up is chosen arbitrarily.
     *
     * @param o the object doing the notify
     * @throw IllegalMonitorStateException if this Thread does not own the
     *        lock on the Object
     */
    static void notify(Object o) throws IllegalMonitorStateException {
        MonitorManager.notify(o);
    }

    /**
     * Wakes up all of the threads waiting on this Object's monitor. Only the
     * owner of the lock on this Object may call this method.
     * 
     * @param o
     *            the object doing the notifyAll
     * @throws IllegalMonitorStateException
     *             if this Thread does not own the lock on the Object
     */
    static void notifyAll(Object o) throws IllegalMonitorStateException {
        MonitorManager.notifyAll(o);
    }

    /**
     * Waits a specified amount of time for notify() or notifyAll() to be called
     * on this Object. The VM does not have to pay attention to the ns argument,
     * if it does not have that much granularity.
     * 
     * @param o
     *            the object to suspend on
     * @param ms
     *            milliseconds to wait (1,000 milliseconds = 1 second)
     * @param ns
     *            nanoseconds to wait beyond ms (1,000,000 nanoseconds == 1
     *            millisecond)
     * @throws IllegalMonitorStateException
     *             if this Thread does not own the lock on the Object
     * @throws InterruptedException
     *             if some other Thread interrupts this Thread
     */
    static void wait(Object o, long ms, int ns)
            throws IllegalMonitorStateException, InterruptedException {
        MonitorManager.wait(o, ms);
    }
}