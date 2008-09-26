/*
 * $Id$
 */
package org.jnode.vm.isolate;

import org.jnode.util.BootableHashMap;
import org.jnode.vm.VmSystemObject;

/**
 * Holder for variables specific to an isolate.
 * This class can be compared to ThreadLocal.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmIsolateLocal<T> extends VmSystemObject {

    /**
     * Object used in root isolate
     */
    private T rootObject;

    /**
     * Gets the stored object reference.
     *
     * @return
     */
    public T get() {
        if (VmIsolate.isRoot()) {
            return rootObject;
        } else {
            return (T) VmIsolate.currentIsolate().getIsolateLocalMap().get(this);
        }
    }

    /**
     * Sets the stored object reference for the current isolate.
     */
    public void set(T object) {
        if (VmIsolate.isRoot()) {
            rootObject = object;
        } else {
            VmIsolate.currentIsolate().getIsolateLocalMap().put(this, object);
        }
    }
}
