/*
 * $Id$
 */
package org.jnode.net.ipv4.util;

import org.jnode.driver.net.NetworkException;
import org.jnode.net.ProtocolAddress;
import org.jnode.net.Resolver;
import org.jnode.net.ipv4.IPv4Address;
import org.xbill.DNS.*;

import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * @author Martin Hartvig
 */

public class ResolverImpl implements Resolver
{
  private static ExtendedResolver resolver;
  private static Hashtable resolvers;

  private static Hashtable hosts;

  private static Resolver res = null;

  static
  {
    hosts = new Hashtable();

    // this will have to come from hosts file
    final String localhost = "localhost";
    ProtocolAddress[] protocolAddresses = new ProtocolAddress[]{new IPv4Address("127.0.0.1")};

    hosts.put(localhost, protocolAddresses);
  }

  private ResolverImpl()
  {
  }

  /**
   * Singleton
   *
   * @return the singleton of the resolver
   */

  public static Resolver getInstance()
  {
    if (res == null)
    {
      res = new ResolverImpl();
    }

    return res;
  }

  /**
   * List all the dns servers
   */

  public static void printDnsServers()
  {
    if (resolvers == null)
    {
      return;
    }

    Iterator iterator = resolvers.keySet().iterator();
    String dnsServer;

    while (iterator.hasNext())
    {
      dnsServer = (String) iterator.next();

      System.out.println(dnsServer);
    }
  }

  /**
   * Add a dns server
   *
   * @param _dnsserver
   * @throws NetworkException
   */

  public static void addDnsServer(ProtocolAddress _dnsserver) throws NetworkException
  {

    if (resolvers == null)
    {
      resolvers = new Hashtable();
    }

    if (resolver == null)
    {
      try
      {
        if (resolver == null)
        {
          String[] server = new String[]{_dnsserver.toString()};
          resolver = new ExtendedResolver(server);

          Lookup.setDefaultResolver(resolver);

          resolvers.put(_dnsserver.toString(), resolver);
        }
      }
      catch (UnknownHostException e)
      {
        throw new NetworkException("Can't add DnsServer", e);
      }
    }

    try
    {
      String key = _dnsserver.toString();

      if (!resolvers.containsKey(key))
      {
        SimpleResolver simpleResolver = new SimpleResolver(key);

        resolver.addResolver(simpleResolver);
        resolvers.put(key, simpleResolver);
      }
    }
    catch (UnknownHostException e)
    {
      throw new NetworkException("Can't add DnsServer", e);
    }
  }

  /**
   * removes a dns server
   *
   * @param _dnsserver
   */

  public static void removeDnsServer(ProtocolAddress _dnsserver)
  {

    if (resolver == null)
    {
      return;
    }

    String key = _dnsserver.toString();
    if (resolvers.containsKey(key))
    {
      org.xbill.DNS.Resolver resolv = (org.xbill.DNS.Resolver) resolvers.remove(key);

      if (resolver.getResolvers().length == 1)
        resolver = null;
      else
        resolver.deleteResolver(resolv);
    }
  }

  /**
   * Get from hosts file.
   *
   * @param _hostname
   * @return
   */

  private ProtocolAddress[] getFromHostsFile(String _hostname)
  {
    // this should check for changes of the host file

    return (ProtocolAddress[]) hosts.get(_hostname);
  }

  /**
   * Gets the address(es) of the given hostname.
   *
   * @param hostname
   * @return All addresses of the given hostname. The returned array is at least 1 address long.
   * @throws java.net.UnknownHostException
   */

  public ProtocolAddress[] getByName(String hostname) throws UnknownHostException
  {
    ProtocolAddress[] protocolAddresses = null;

    protocolAddresses = getFromHostsFile(hostname);

    if (protocolAddresses != null)
    {
      return protocolAddresses;
    }

    Lookup lookup = null;
    Lookup.setDefaultResolver(resolver);

    try
    {
      lookup = new Lookup(hostname);
    }
    catch (TextParseException e)
    {
      throw new UnknownHostException(hostname);
    }

    lookup.run();

    if (lookup.getResult() == Lookup.SUCCESSFUL)
    {
      Record[] records = lookup.getAnswers();

      protocolAddresses = new ProtocolAddress[records.length];

      for (int i = 0; i < records.length; i++)
      {
        Record record = records[i];
        protocolAddresses[i] = new IPv4Address(record.rdataToString());
      }

    }
    else
    {
      throw new UnknownHostException(lookup.getErrorString());
    }

    return protocolAddresses;
  }

  /**
   * Gets the hostname of the given address.
   *
   * @param address
   * @return All hostnames of the given hostname. The returned array is at least 1 hostname long.
   * @throws java.net.UnknownHostException
   */

  public String[] getByAddress(ProtocolAddress address) throws UnknownHostException
  {
    return new String[0]; //To change body of implemented methods use Options | File Templates.
  }

}
