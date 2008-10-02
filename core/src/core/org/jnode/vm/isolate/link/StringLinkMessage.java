/*
 * $Id$
 */
package org.jnode.vm.isolate.link;


final class StringLinkMessage extends LinkMessageImpl {

    private final String value;

    /**
     * Message constructor
     *
     * @param value
     */
    StringLinkMessage(String value) {
        this.value = value;
    }

    /**
     * @see org.jnode.vm.isolate.LinkMessageImpl#CloneMessage()
     */
    @Override
    LinkMessageImpl cloneMessage() {
        return new StringLinkMessage(new String(value));
    }

    /**
     * @see javax.isolate.LinkMessage#extract()
     */
    @Override
    public Object extract() {
        return extractString();
    }

    /**
     * @see javax.isolate.LinkMessage#containsString()
     */
    @Override
    public boolean containsString() {
        return true;
    }

    /**
     * @see javax.isolate.LinkMessage#extractString()
     */
    @Override
    public String extractString() {
        return value;
    }
}
