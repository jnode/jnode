/*
 * Created on 22-Apr-2004
 *  
 */
package org.jnode.driver.net.eepro100;

import org.apache.log4j.Logger;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;

/**
 * @author flesire
 *  
 */
public class EEPRO100Buffer implements EEPRO100Constants {

    protected final Logger log = Logger.getLogger(getClass());

    static final public int DATA_BUFFER_SIZE = 1536;

    public EEPRO100TxFD[] txRing = new EEPRO100TxFD[TX_RING_SIZE];
    public EEPRO100RxFD[] rxRing = new EEPRO100RxFD[RX_RING_SIZE];
    EEPRO100RxFD[] rxPackets = new EEPRO100RxFD[128];
    //private int tx_threshold;
    private int curTx;
    private int curRx;
    private int dirtyTx;
    private int dirtyRx;

    private EEPRO100RxFD last_rxf;
    private int rxPacketIndex;

    byte[] data;
    MemoryResource mem;

    /**
     *  
     */
    public EEPRO100Buffer(ResourceManager rm) {
        /* Set up the Tx queue early.. */
		curTx = 0;
		dirtyTx = 0;
        
        this.data = new byte[DATA_BUFFER_SIZE];
        this.mem = rm.asMemoryResource(data);
    }

    /* Initialize the Rx and Tx rings, along with various 'dev' bits. */
    public final void initRxRing() {
        EEPRO100RxFD rxf = null;
        int i;

        curRx = 0;
        rxPacketIndex = 0;

        for (i = 0; i < rxPackets.length; i++)
            rxPackets[i] = new EEPRO100RxFD(mem);

        log.debug("rxPacket 0: " + Integer.toHexString(rxPackets[0].getBufferAddress()));

        for (i = 0; i < RX_RING_SIZE; i++) {
            rxf = rxPackets[rxPacketIndex++];
            rxPacketIndex &= (rxPackets.length - 1);
            rxRing[i] = rxf;
            if (last_rxf != null) last_rxf.setLink(rxf.getBufferAddress());
            last_rxf = rxf;
            rxf.setStatus(1); 						/* '1' is flag value only. */
            rxf.setLink(0); 						/* None yet. */
            /* This field unused by i82557, we use it as a consistency check. */
            rxf.setRxBufferAddress(0xffffffff);
            rxf.setCount(DATA_BUFFER_SIZE << 16);
            rxf.cleanHeader();
        }
        dirtyRx = i - RX_RING_SIZE;
        /* Mark the last entry as end-of-list. */
        last_rxf.setStatus(0xC0000002); 			/* '2' is flag value only. */
        last_rxf.cleanHeader();
        //		last_rxf = last_rxf;
        	int rxRingSize = rxRing.length;
        	for(i = 0; i < rxRingSize; i++) log.debug(rxRing[i].print());

    }

    public final void initTxRing() {
        for (int i = 0; i < txRing.length; i++) {
            //txRing[i] = new TxFD();
        }
        //log.debug("txRing:"+
        // Integer.toHexString(txRing[0].getBufferAddress()));

    }
    /**
     * @return Returns the curRx.
     */
    public int getCurRx() {
        return curRx;
    }
    /**
     * @param curRx The curRx to set.
     */
    public void setCurRx(int curRx) {
        this.curRx = curRx;
    }
    /**
     * @return Returns the curTx.
     */
    public int getCurTx() {
        return curTx;
    }
    /**
     * @param curTx The curTx to set.
     */
    public void setCurTx(int curTx) {
        this.curTx = curTx;
    }
    /**
     * @return Returns the dirtyRx.
     */
    public int getDirtyRx() {
        return dirtyRx;
    }
    /**
     * @param dirtyRx The dirtyRx to set.
     */
    public void setDirtyRx(int dirtyRx) {
        this.dirtyRx = dirtyRx;
    }
    /**
     * @return Returns the dirtyTx.
     */
    public int getDirtyTx() {
        return dirtyTx;
    }
    /**
     * @param dirtyTx The dirtyTx to set.
     */
    public void setDirtyTx(int dirtyTx) {
        this.dirtyTx = dirtyTx;
    }
}