/*
 * $Id$
 */
package org.jnode.driver.net.via_rhine;

import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.PKT_BUF_SZ;
import org.apache.log4j.Logger;
import org.vmmagic.unboxed.Address;

/**
 * @author Levente Sántha
 */
class ViaRhineDesc {
    private static final int OWN_BIT_MASK = 0x80000000;
    protected final Logger log = Logger.getLogger(getClass());
    byte[] desc;
    int descOffs;
    MemoryResource descMr;
    byte[] data;
    int dataOffs;
    MemoryResource dataMr;
    int descAddr;

    public ViaRhineDesc(ResourceManager rm) {
        desc = new byte[16 + 32];
        descMr = rm.asMemoryResource(desc);
        Address descma = descMr.getAddress();
        descOffs = align32(descma);
        descAddr = descma.add(descOffs).toInt();
        data = new byte[PKT_BUF_SZ + 32];
        dataMr = rm.asMemoryResource(data);
        Address datma = dataMr.getAddress();
        dataOffs = align32(datma);
        descMr.setInt(descOffs + 8, datma.add(dataOffs).toInt());        
    }

    void setOwnBit(){
        descMr.setInt(descOffs, descMr.getInt(descOffs) | OWN_BIT_MASK);
    }

    boolean isOwnBit(){
        return (descMr.getInt(descOffs) & OWN_BIT_MASK) != 0;
    }

    void setNextDescAddr(int addr){
        descMr.setInt(descOffs + 12, addr);
    }

    /**
     * Align an addres on 32-byte boundary.
     * @param addr the address
     * @return the the aligned address offset relative to addr
     */
    private int align32(Address addr){
        int i_addr = addr.toInt();
		int offs = 0;

		while ((i_addr & 31) != 0) {
			i_addr++;
			offs++;
		}

        return offs;
    }
}
