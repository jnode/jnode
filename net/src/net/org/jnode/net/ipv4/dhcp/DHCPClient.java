/*
 * $Id$
 */
package org.jnode.net.ipv4.dhcp;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.driver.net.NetworkException;
import org.jnode.naming.InitialNaming;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.bootp.BOOTPClient;
import org.jnode.net.ipv4.bootp.BOOTPHeader;
import org.jnode.net.ipv4.config.IPv4ConfigurationService;
import org.jnode.net.ipv4.util.ResolverImpl;

import javax.naming.NameNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Console DHCP client.
 *
 * @author markhale
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */
public class DHCPClient extends BOOTPClient
{

  private final Logger log = Logger.getLogger(getClass());

  /**
   * Create a DHCP discovery packet
   */
  protected DatagramPacket createRequestPacket(BOOTPHeader hdr)
      throws IOException
  {
    DHCPMessage msg = new DHCPMessage(hdr, DHCPMessage.DHCPDISCOVER);
    return msg.toDatagramPacket();
  }

  protected boolean processResponse(Device device, NetDeviceAPI api,
                                    int transactionID, DatagramPacket packet) throws IOException
  {
    DHCPMessage msg = new DHCPMessage(packet);
    BOOTPHeader hdr = msg.getHeader();
    if (hdr.getOpcode() != BOOTPHeader.BOOTREPLY)
    {
      // Not a response
      return false;
    }
    if (hdr.getTransactionID() != transactionID)
    {
      // Not for me
      return false;
    }

    // debug the DHCP message
    if (log.isDebugEnabled())
    {
      log.debug("Got Client IP address  : " + hdr.getClientIPAddress());
      log.debug("Got Your IP address    : " + hdr.getYourIPAddress());
      log.debug("Got Server IP address  : " + hdr.getServerIPAddress());
      log.debug("Got Gateway IP address : " + hdr.getGatewayIPAddress());
      for (int n = 1; n < 255; n++)
      {
        byte[] value = msg.getOption(n);
        if (value != null)
        {
          if (value.length == 1)
            log.debug("Option " + n + " : " + (int) (value[0]));
          else if (value.length == 2)
            log.debug("Option " + n + " : "
                + ((value[0] << 8) | value[1]));
          else if (value.length == 4)
            log.debug("Option " + n + " : "
                + InetAddress.getByAddress(value).toString());
          else
            log.debug("Option " + n + " : " + new String(value));
        }
      }
    }

    switch (msg.getMessageType())
    {
      case DHCPMessage.DHCPOFFER:
        byte[] serverID = msg
            .getOption(DHCPMessage.SERVER_IDENTIFIER_OPTION);
        byte[] requestedIP = hdr.getYourIPAddress().getAddress();
        hdr = new BOOTPHeader(BOOTPHeader.BOOTREQUEST, transactionID, hdr
            .getClientIPAddress(), hdr.getClientHwAddress());
        msg = new DHCPMessage(hdr, DHCPMessage.DHCPREQUEST);
        msg.setOption(DHCPMessage.REQUESTED_IP_ADDRESS_OPTION, requestedIP);
        msg.setOption(DHCPMessage.SERVER_IDENTIFIER_OPTION, serverID);
        packet = msg.toDatagramPacket();
        packet.setAddress(IPv4Address.BROADCAST_ADDRESS);
        packet.setPort(SERVER_PORT);
        socket.send(packet);
        break;
      case DHCPMessage.DHCPACK:
        configureNetwork(device, msg);
        return true;
      case DHCPMessage.DHCPNAK:
        break;
    }
    return false;
  }

  /**
   * Performs the actual configuration of a network device based on the
   * settings in a DHCP message.
   */
  protected void configureNetwork(Device device, DHCPMessage msg)
      throws NetworkException
  {
    configureNetwork(device, msg.getHeader());
    byte[] routerValue = msg.getOption(DHCPMessage.ROUTER_OPTION);
    if (routerValue != null && routerValue.length >= 4)
    {
      IPv4Address routerIP = new IPv4Address(routerValue, 0);
      log.info("Got Router IP address : " + routerIP);
      final IPv4ConfigurationService cfg;
      try
      {
        cfg = (IPv4ConfigurationService) InitialNaming
            .lookup(IPv4ConfigurationService.NAME);
      }
      catch (NameNotFoundException ex)
      {
        throw new NetworkException(ex);
      }
      cfg.addRoute(IPv4Address.ANY, routerIP, device, false);
    }


    // find the dns servers and add to the resolver
    byte[] dnsValue = msg.getOption(DHCPMessage.DNS_OPTION);
    IPv4Address dnsIP;

    if (dnsValue != null)
    {
      for (int i = 0; i < dnsValue.length; i += 4)
      {
        dnsIP = new IPv4Address(dnsValue, i);

        log.info("Got Dns IP address    : " + dnsIP);
        ResolverImpl.addDnsServer(dnsIP);
      }
    }
  }
}