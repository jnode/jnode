package org.jnode.net.ipv4.udp;

import org.jnode.net.ipv4.IPv4ControlBlock;
import org.jnode.net.ipv4.IPv4ControlBlockList;
import org.apache.log4j.Logger;


/**
 * Created by IntelliJ IDEA.
 * User: mh
 * Date: 13-05-2004
 * Time: 11:44:36
 * To change this template use File | Settings | File Templates.
 */

public class UDPControlBlock extends IPv4ControlBlock implements UDPConstants
{
  /** My logger */
  private final Logger log = Logger.getLogger(getClass());

  public UDPControlBlock(IPv4ControlBlockList list, int protocol, int ttl)
  {
    super(list, protocol, ttl);
  }
}
