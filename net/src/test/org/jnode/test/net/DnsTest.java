/*
 * $Id$
 */

package org.jnode.test.net;


import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Martin Hartvig
 */

public class DnsTest
{

  public static void main(String[] args)
  {
    if (args.length == 1)
    {
      try
      {
        System.out.println("inetAddress find "+args[0]);

        InetAddress inetAddress = InetAddress.getByName(args[0]);

        System.out.println("inetAddress "+inetAddress.getHostAddress());
      }
      catch (UnknownHostException e)
      {
        e.printStackTrace();  //To change body of catch statement use Options | File Templates.
      }
    }
    else
    {
      System.out.println("insert url as arg.");
    }
  }
}

