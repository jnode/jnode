/*
 * $Id$
 */
package org.jnode.driver.net.loopback;

import org.jnode.driver.net.NetworkException;
import org.jnode.driver.net.spi.AbstractNetDriver;
import org.jnode.net.HardwareAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetAddress;
import org.jnode.net.ethernet.EthernetConstants;

/**
 * @author epr
 */

/**
 * Driver for loopback device.
 *
 * @author epr
 */
public class LoopbackDriver extends AbstractNetDriver implements EthernetConstants
{

  private static final EthernetAddress hwAddress = new EthernetAddress("00-00-00-00-00-00");

  /**
   * Gets the hardware address of this device
   */
  public HardwareAddress getAddress()
  {
    return hwAddress;
  }

  /**
   * Gets the maximum transfer unit, the number of bytes this device can
   * transmit at a time.
   */
  public int getMTU()
  {
    return ETH_DATA_LEN;
  }

  /**
   * @see org.jnode.driver.net.spi.AbstractNetDriver#doTransmit(SocketBuffer, HardwareAddress)
   */
  protected void doTransmit(SocketBuffer skbuf, HardwareAddress destination) throws NetworkException
  {
    onReceive(skbuf);
  }

  /**
   * @see org.jnode.driver.net.spi.AbstractNetDriver#getDevicePrefix()
   */
  protected String getDevicePrefix()
  {
    return LOOPBACK_DEVICE_PREFIX;
  }
}