/*
 * $Id$
 */
package org.jnode.net.ipv4.bootp;

import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.driver.net.NetworkException;
import org.jnode.naming.InitialNaming;
import org.jnode.net.NetPermission;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.IPv4Constants;
import org.jnode.net.ipv4.config.IPv4ConfigurationService;

import javax.naming.NameNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * @author epr
 * @author markhale
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */
public class BOOTPClient
{

  /**
   * My logger
   */
  private final Logger log = Logger.getLogger(getClass());

  private static final int RECEIVE_TIMEOUT = 10 * 1000; // 10 seconds

  public static final int SERVER_PORT = 67;

  public static final int CLIENT_PORT = 68;

  protected MulticastSocket socket;

  /**
   * Configure the given device using BOOTP
   *
   * @param device
   */
  public final void configureDevice(final Device device) throws IOException
  {
    final SecurityManager sm = System.getSecurityManager();
    if (sm != null)
    {
      sm.checkPermission(new NetPermission("bootpClient"));
    }

    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        public Object run() throws IOException
        {
          // Get the API.
          final NetDeviceAPI api;
          try
          {
            api = (NetDeviceAPI) device.getAPI(NetDeviceAPI.class);
          }
          catch (ApiNotFoundException ex)
          {
            throw new NetworkException("Device is not a network device", ex);
          }

          // Open a socket
          socket = new MulticastSocket(CLIENT_PORT);
          try
          {
            // Prepare the socket
            socket.setBroadcast(true);
            socket.setNetworkInterface(NetworkInterface
                .getByName(device.getId()));
            socket.setSoTimeout(RECEIVE_TIMEOUT);

            // Create the BOOTP header
            final Inet4Address myIP = null; // any address
            final int transactionID = (int) (System
                .currentTimeMillis() & 0xFFFFFFFF);
            BOOTPHeader hdr = new BOOTPHeader(BOOTPHeader.BOOTREQUEST, transactionID, 0, myIP,
                api.getAddress());

            // Send the packet
            final DatagramPacket packet = createRequestPacket(hdr);
            packet.setAddress(IPv4Address.BROADCAST_ADDRESS);
            packet.setPort(SERVER_PORT);
            socket.send(packet);

            boolean configured;
            do
            {
              // Wait for a response
              socket.receive(packet);

              // Process the response
              configured = processResponse(device, api,
                  transactionID, packet);
            }
            while (!configured);

          }
          finally
          {
            socket.close();
          }
          return null;
        }
      });
    }
    catch (PrivilegedActionException ex)
    {
      throw (IOException) ex.getException();
    }
  }

  /**
   * Create a BOOTP request packet
   */
  protected DatagramPacket createRequestPacket(BOOTPHeader hdr)
      throws IOException
  {
    return new BOOTPMessage(hdr).toDatagramPacket();
  }

  /**
   * Process a BOOTP response
   *
   * @param packet
   * @return true if the device has been configured, false otherwise
   */
  protected boolean processResponse(Device device, NetDeviceAPI api,
                                    int transactionID, DatagramPacket packet) throws IOException
  {

    final BOOTPHeader hdr = new BOOTPHeader(packet);
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

    configureNetwork(device, hdr);

    return true;
  }

  /**
   * Performs the actual configuration of a network device based on the
   * settings in a BOOTP header.
   */
  protected void configureNetwork(Device device, BOOTPHeader hdr)
      throws NetworkException
  {
    log.info("Got Client IP address  : " + hdr.getClientIPAddress());
    log.info("Got Your IP address    : " + hdr.getYourIPAddress());
    log.info("Got Server IP address  : " + hdr.getServerIPAddress());
    log.info("Got Gateway IP address : " + hdr.getGatewayIPAddress());

    final IPv4ConfigurationService cfg;
    try
    {
      cfg = (IPv4ConfigurationService) InitialNaming.lookup(IPv4ConfigurationService.NAME);
    }
    catch (NameNotFoundException ex)
    {
      throw new NetworkException(ex);
    }
    cfg.configureDeviceStatic(device, new IPv4Address(hdr.getYourIPAddress()),
        null, false);

    IPv4Address network_address;

    if (hdr.getServerIPAddress().getAddress()[0] < IPv4Constants.NETWORK_CLASSA)
    {
      network_address = new IPv4Address(hdr.getServerIPAddress().getAddress()[0] + "." +
          hdr.getServerIPAddress().getAddress()[1] + "." +
          hdr.getServerIPAddress().getAddress()[2] + ".0");
    }
    else if (hdr.getServerIPAddress().getAddress()[0] < IPv4Constants.NETWORK_CLASSB)
    {
      network_address = new IPv4Address(hdr.getServerIPAddress().getAddress()[0] + "." +
          hdr.getServerIPAddress().getAddress()[1] + ".0.0");
    }
    else if (hdr.getServerIPAddress().getAddress()[0] < IPv4Constants.NETWORK_CLASSC)
    {
      network_address = new IPv4Address(hdr.getServerIPAddress().getAddress()[0] + ".0.0.0");
    }
    else
      network_address = new IPv4Address(hdr.getServerIPAddress());


    if (hdr.getGatewayIPAddress().isAnyLocalAddress())
    {
//      cfg.addRoute(new IPv4Address(hdr.getServerIPAddress()), null, device, false);
      cfg.addRoute(network_address, null, device, false);

    }
    else
    {
//      cfg.addRoute(new IPv4Address(hdr.getServerIPAddress()), new IPv4Address(hdr.getGatewayIPAddress()), device, false);
      cfg.addRoute(network_address, new IPv4Address(hdr.getGatewayIPAddress()), device, false);
    }
  }
}
