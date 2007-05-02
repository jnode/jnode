/*
 * $Id$
 */
package org.jnode.driver.net.via_rhine;

import org.jnode.system.ResourceManager;
import org.jnode.system.MemoryResource;
import org.jnode.net.SocketBuffer;
import org.apache.log4j.Logger;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.*;
import org.vmmagic.unboxed.Address;


/**
 * @author Levente Sántha
 */
public class RxDesc {
    protected final Logger log = Logger.getLogger(getClass());
    byte[] desc;
    int descOffs;
    MemoryResource descMr;
    byte[] data;
    int dataOffs;
    MemoryResource dataMr;
    int descAddr;
    RxDesc(ResourceManager rm){
        desc = new byte[16 + 32];
        descMr = rm.asMemoryResource(desc);
        Address memAddr = descMr.getAddress();

        int addr = memAddr.toInt();
		descOffs = 0;
		// Align on 32-byte boundary
		while ((addr & 31) != 0) {
			addr++;
			descOffs++;
		}
		descAddr = memAddr.add(descOffs).toInt();

        descMr.setInt(descOffs, 0x80000000);
        descMr.setInt(descOffs + 4, PKT_BUF_SZ);
        
        data = new byte[PKT_BUF_SZ + 32];
        dataMr = rm.asMemoryResource(data);

        memAddr = dataMr.getAddress();
        addr = memAddr.toInt();
		dataOffs = 0;
		// Align on 32-byte boundary
		while ((addr & 31) != 0) {
			addr++;
			dataOffs++;
		}
        
        descMr.setInt(descOffs + 8, memAddr.add(dataOffs).toInt());
    }

    void setOwnBit(){
        descMr.setInt(descOffs, descMr.getInt(descOffs) | 0x80000000);
    }

    boolean isOwnBit(){
        return (descMr.getInt(descOffs) & 0x80000000) != 0;
    }

    void setNextDescAddr(int addr){
        descMr.setInt(descOffs + 12, addr);
    }

    int frameLength(){
        return descMr.getChar(descOffs + 2) & 0x000007FF;
    }

    SocketBuffer getPacket(){
        int ln = frameLength();
        log.debug("packetlength: " + ln);
        byte[] buf = new byte[ln];
        System.arraycopy(data, dataOffs, buf, 0, ln);
        return new SocketBuffer(buf, 0, ln);
    }
}
