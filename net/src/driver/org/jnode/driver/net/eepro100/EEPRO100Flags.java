/*
 * Created on 13-Apr-2004
 *
 */
package org.jnode.driver.net.eepro100;

import org.jnode.driver.net.ethernet.spi.Flags;



/**
 * @author flesire
 *
 */
public class EEPRO100Flags implements Flags {
    
    private final String name;
    
    public EEPRO100Flags(String name){
        this.name = name;
    }
        
    /* (non-Javadoc)
     * @see org.jnode.driver.net.ethernet.Flags#getName()
     */
    public String getName() {
        return this.name;
    }

}
