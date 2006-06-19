/*
 * $Id: $
 */
package org.jnode.vm.scheduler;

import org.jnode.vm.VmStackReader;

abstract class VmThreadProxyQueue extends VmThreadQueue {

    protected VmThreadProxy head;

    /**
     * @param name
     */
    VmThreadProxyQueue(String name) {
        super(name);
    }

    /**
     * Enqueue the given proxy.
     * @param proxy
     */
    protected abstract void enqueue(VmThreadProxy proxy);

    /**
     * @see org.jnode.vm.scheduler.VmThreadQueue#dump(boolean, org.jnode.vm.VmStackReader)
     */
    @Override
    void dump(boolean dumpStack, VmStackReader stackReader) {
        // TODO Auto-generated method stub
        
    }
}
