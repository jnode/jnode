/*
 * $Id$
 */

package org.jnode.driver.net.rtl8139;

import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.vm.Address;

/**
 * @author Martin Husted Hartvig
 */

public class RTL8139TxBuffer implements RTL8139Constants
{

  private static final int DPD_SIZE = 16;
  private static final int FRAME_SIZE = EthernetConstants.ETH_FRAME_LEN;

  /** The actual data */
  private final byte[] data;

  /** MemoryResource mapper around data */
  private final MemoryResource mem;

  /** Offset within mem of first DPD */
  private final int firstDPDOffset;

  /** 32-bit address first DPD */
  private final Address firstDPDAddress;

  /**
   * Create a new instance
   * 
   * @param rm 
   */

  public RTL8139TxBuffer(ResourceManager rm)
  {

    // Create a large enough buffer
    final int size = (DPD_SIZE + FRAME_SIZE) + 16 /*alignment*/;
    this.data = new byte[size];
    this.mem = rm.asMemoryResource(data);

    final Address memAddr = mem.getAddress();
    //int addr = Address.as32bit(memAddr);
    int offset = 0;

    this.firstDPDOffset = offset;
    this.firstDPDAddress = Address.add(memAddr, firstDPDOffset);
  }

  /**
   * Initialize this ring to its default (empty) state
   */
  public void initialize(SocketBuffer src) throws IllegalArgumentException
  {
    // Setup the DPD

    // Copy the data from the buffer
    final int len = src.getSize();
    if (len > FRAME_SIZE)
    {
      throw new IllegalArgumentException("Length must be <= ETH_FRAME_LEN");
    }

    src.get(data, firstDPDOffset, 0, len);
  }


  /**
   * Gets the address of the first DPD in this buffer.
   */
  public Address getFirstDPDAddress()
  {
    return firstDPDAddress;
  }
}
