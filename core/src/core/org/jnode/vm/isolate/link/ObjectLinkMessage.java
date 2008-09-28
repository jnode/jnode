package org.jnode.vm.isolate.link;

public class ObjectLinkMessage extends LinkMessageImpl {
    
    private final Object obj;
    
    private ObjectLinkMessage(Object cr) {
        this.obj = cr;
    }
    
    public static ObjectLinkMessage newMessage (Object obj) {
        return new ObjectLinkMessage(obj);
    }

    @Override
    public Object extract() {
        return obj;
    }

    @Override
    LinkMessageImpl CloneMessage() {
        return new ObjectLinkMessage(obj);
    }
}
