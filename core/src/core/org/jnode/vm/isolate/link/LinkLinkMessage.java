/*
 * $Id$
 */
package org.jnode.vm.isolate.link;

import javax.isolate.Link;


final class LinkLinkMessage extends LinkMessageImpl {

    private final VmDataLink value;
    
    /**
     * Message constructor
     * @param value
     */
    LinkLinkMessage(VmDataLink link) {
        this.value = link;
    }
    
    /**
     * @see org.jnode.vm.isolate.LinkMessageImpl#CloneMessage()
     */
    @Override
    LinkMessageImpl CloneMessage() {
        return new LinkLinkMessage(value);
    }

    /**
     * @see javax.isolate.LinkMessage#extract()
     */
    @Override
    public Object extract() {
        return extractLink();
    }

    /**
     * @see javax.isolate.LinkMessage#containsLink()
     */
    @Override
    public boolean containsLink() {
        return true;
    }

    /**
     * @see javax.isolate.LinkMessage#extractLink()
     */
    @Override
    public Link extractLink() {
        return value.asLink();
    }        
}
