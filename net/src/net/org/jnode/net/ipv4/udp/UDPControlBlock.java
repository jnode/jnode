/*
 * $Id$
 */
package org.jnode.net.ipv4.udp;

import org.jnode.net.ipv4.IPv4ControlBlock;
import org.jnode.net.ipv4.IPv4ControlBlockList;
import org.apache.log4j.Logger;


public class UDPControlBlock extends IPv4ControlBlock implements UDPConstants
{
  /** My logger */
  private final Logger log = Logger.getLogger(getClass());

  public UDPControlBlock(IPv4ControlBlockList list, int protocol, int ttl)
  {
    super(list, protocol, ttl);
  }
}
