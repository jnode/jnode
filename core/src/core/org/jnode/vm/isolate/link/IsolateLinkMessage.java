/*
 * $Id$
 */
package org.jnode.vm.isolate.link;

import javax.isolate.Isolate;

import org.jnode.vm.isolate.VmIsolate;


final class IsolateLinkMessage extends LinkMessageImpl {

    private final VmIsolate value;

    /**
     * Message constructor
     *
     * @param value
     */
    IsolateLinkMessage(VmIsolate isolate) {
        this.value = isolate;
    }

    /**
     * @see org.jnode.vm.isolate.LinkMessageImpl#CloneMessage()
     */
    @Override
    LinkMessageImpl cloneMessage() {
        return new IsolateLinkMessage(value);
    }

    /**
     * @see javax.isolate.LinkMessage#extract()
     */
    @Override
    public Object extract() {
        return extractIsolate();
    }

    /**
     * @see javax.isolate.LinkMessage#containsIsolate()
     */
    @Override
    public boolean containsIsolate() {
        return true;
    }

    /**
     * @see javax.isolate.LinkMessage#extractIsolate()
     */
    @Override
    public Isolate extractIsolate() {
        return value.getIsolate();
    }
}
