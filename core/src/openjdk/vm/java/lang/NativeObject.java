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
 
package java.lang;

import org.jnode.vm.VmSystem;
import org.jnode.vm.scheduler.MonitorManager;

/**
 * @author Levente S\u00e1ntha
 * @see Object
 */
class NativeObject {
    private static void registerNatives() {
    }

    /**
     * @see Object#getClass()
     */
    private static Class<?> getClass(Object instance) {
        return VmSystem.getClass(instance);
    }

    /**
     * @see Object#hashCode()
     */
    private static int hashCode(Object instance) {
        return VmSystem.getHashCode(instance);
    }

    /**
     * @see Object#clone()
     */
    private static Object clone(Object instance) throws CloneNotSupportedException {
        if (instance instanceof Cloneable)
            return VmSystem.clone((Cloneable) instance);
        throw new CloneNotSupportedException("Object not cloneable");
    }

    /**
     * @see java.lang.Object#notify()
     */
    private static void notify(Object instance) {
        MonitorManager.notify(instance);
    }

    /**
     * @see Object#notifyAll() ()
     */
    private static void notifyAll(Object instance) {
        MonitorManager.notifyAll(instance);
    }

    /**
     * @see Object#wait(long)
     */
    private static void wait(Object instance, long timeout) throws InterruptedException {
        MonitorManager.wait(instance, timeout);
    }
}
