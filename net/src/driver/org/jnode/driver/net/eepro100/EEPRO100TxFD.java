/*
 * Created on 21-Apr-2004
 *
 */
package org.jnode.driver.net.eepro100;

import org.jnode.system.MemoryResource;

/**
 * @author flesire
 *
 */
public class EEPRO100TxFD {
    
    private int bufferAddress;
    private MemoryResource mem;

    /**
     * 
     */
    public EEPRO100TxFD(MemoryResource mem) {
        this.mem = mem;
        
    }

}
