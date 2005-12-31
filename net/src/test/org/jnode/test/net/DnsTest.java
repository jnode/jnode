/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
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

