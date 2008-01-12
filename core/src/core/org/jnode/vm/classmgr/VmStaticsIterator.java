/*
 * $
 */
package org.jnode.vm.classmgr;

import java.util.Iterator;

/**
 * @author Levente S\u00e1ntha
 */
public class VmStaticsIterator extends  VmStaticsBase implements Iterator<VmType> {
    private VmStatics statics;
    private VmType next;
    private byte[] types;
    private int size;
    private int current;

    public VmStaticsIterator(VmStatics statics){
        if(statics == null) throw new IllegalArgumentException();
        this.statics = statics;
        this.types = this.statics.getAllocator().getTypes();
        this.size = types.length;
        this.current = 0;
    }

    public boolean hasNext() {
        while(current < size){
            if(types[current] == TYPE_CLASS){
                try {
                    next = statics.getTypeEntry(current);
                    if(next != null){
                        current++;
                        return true;
                    }
                } catch (NullPointerException npe){
                    //todo fix this
                    //apparently if the VmType object was garbage collected then
                    //statics.getTypeEntry(current); will throw an NPE
                }
            }
            current++;
        }
        next = null;
        return false;
    }

    public VmType next() {
        return next;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
