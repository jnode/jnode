/*
 * $Id$
 */
package org.jnode.driver.net.via_rhine;

import org.apache.log4j.Logger;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.net.SocketBuffer;
import static org.jnode.net.ethernet.EthernetConstants.*;
import org.vmmagic.unboxed.Address;
import java.util.Arrays;


/**
 * @author Levente Sántha
 */
public class TxDesc {
    protected final Logger log = Logger.getLogger(getClass());
    byte[] desc;
    int descOffs;
    MemoryResource descMr;
    byte[] data;
    int dataOffs;
    MemoryResource dataMr;
    int descAddr;
    TxDesc(ResourceManager rm){
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
        descMr.setInt(descOffs + 4, ViaRhineConstants.PKT_BUF_SZ);

        data = new byte[ViaRhineConstants.PKT_BUF_SZ + 32];
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

    void setFrameLength(int size){
        descMr.setInt(descOffs + 4, ((descMr.getInt(descOffs + 4) & ~0x000007FF) | (size & 0x000007FF)));
    }

    void setPacket(SocketBuffer sb){
        int ln = sb.getSize();
        log.debug("packetlength: " + ln);
        sb.get(data, dataOffs, 0, ln);

        if(ln < ETH_ZLEN + 10){
            Arrays.fill(data, dataOffs + ln, dataOffs + ETH_ZLEN + 10, (byte)0);
            setFrameLength(ETH_ZLEN + 10);
        }

    }
}
