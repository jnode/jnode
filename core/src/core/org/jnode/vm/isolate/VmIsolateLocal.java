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
     * Map used for non-root isolates
     */
    private BootableHashMap<VmIsolate, T> map;

    /**
     * Gets the stored object reference.
     *
     * @return
     */
    public T get() {
        if (VmIsolate.isRoot()) {
            return rootObject;
        } else {
            if (map != null) {
                return map.get(VmIsolate.currentIsolate());
            } else {
                return null;
            }
        }
    }

    /**
     * Sets the stored object reference for the current isolate.
     */
    public void set(T object) {
        if (VmIsolate.isRoot()) {
            rootObject = object;
        } else {
            if (map == null) {
                map = new BootableHashMap<VmIsolate, T>();
            }
            map.put(VmIsolate.currentIsolate(), object);
        }
    }
}
