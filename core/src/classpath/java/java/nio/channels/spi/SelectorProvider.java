/* SelectorProvider.java
   Copyright (C) 2002, 2003, 2004  Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package java.nio.channels.spi;

import gnu.java.nio.SelectorProviderImpl;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.Channel;


/**
 * @author Michael Koch
 * @since 1.4
 */
public abstract class SelectorProvider
{
  private static SelectorProvider systemDefaultProvider;
    
  /**
   * Initializes the selector provider.
   *
   * @exception SecurityException If a security manager has been installed and
   * it denies @see RuntimePermission ("selectorProvider").
   */
  protected SelectorProvider()
  {
    SecurityManager sm = System.getSecurityManager();
    if (sm != null)
      sm.checkPermission(new RuntimePermission("selectorProvider"));
  }
  
  /**
   * Opens a datagram channel.
   *
   * @return a new datagram channel object
   * 
   * @exception IOException if an error occurs
   */
  public abstract DatagramChannel openDatagramChannel()
    throws IOException;
  
  /**
   * Opens a pipe.
   *
   * @return a new pipe object
   * 
   * @exception IOException if an error occurs
   */
  public abstract Pipe openPipe() throws IOException;
  
  /**
   * Opens a selector.
   *
   * @return a new selector object
   * 
   * @exception IOException if an error occurs
   */
  public abstract AbstractSelector openSelector() throws IOException;
  
  /**
   * Opens a server socket channel.
   *
   * @return a new server socket channel object
   * 
   * @exception IOException if an error occurs
   */
  public abstract ServerSocketChannel openServerSocketChannel()
    throws IOException;
  
  /**
   * Opens a socket channel.
   *
   * @return a new socket channel object
   * 
   * @exception IOException if an error occurs
   */
  public abstract SocketChannel openSocketChannel() throws IOException;
    
  /**
   * Returns the system-wide default selector provider for this invocation
   * of the Java virtual machine.
   *
   * @return the default seletor provider
   */
  public static synchronized SelectorProvider provider()
  {
    if (systemDefaultProvider == null)
      {
	String propertyValue =
	  System.getProperty("java.nio.channels.spi.SelectorProvider");

	if (propertyValue == null || propertyValue.equals(""))
	  systemDefaultProvider = new SelectorProviderImpl();
	else
  {
	    try
      {
		systemDefaultProvider =
		  (SelectorProvider) Class.forName(propertyValue)
		                          .newInstance();
	      }
	    catch (Exception e)
	      {
		System.err.println("Could not instantiate class: "
		                   + propertyValue);
		systemDefaultProvider = new SelectorProviderImpl();
	      }
	  }
      }
    
    return systemDefaultProvider;
  }

    //jnode + openjdk

    /**
     * Returns the channel inherited from the entity that created this
     * Java virtual machine.
     *
     * <p> On many operating systems a process, such as a Java virtual
     * machine, can be started in a manner that allows the process to
     * inherit a channel from the entity that created the process. The
     * manner in which this is done is system dependent, as are the
     * possible entities to which the channel may be connected. For example,
     * on UNIX systems, the Internet services daemon (<i>inetd</i>) is used to
     * start programs to service requests when a request arrives on an
     * associated network port. In this example, the process that is started,
     * inherits a channel representing a network socket.
     *
     * <p> In cases where the inherited channel represents a network socket
     * then the {@link java.nio.channels.Channel Channel} type returned
     * by this method is determined as follows:
     *
     * <ul>
     *
     *  <li><p> If the inherited channel represents a stream-oriented connected
     *  socket then a {@link java.nio.channels.SocketChannel SocketChannel} is
     *  returned. The socket channel is, at least initially, in blocking
     *  mode, bound to a socket address, and connected to a peer.
     *  </p></li>
     *
     *  <li><p> If the inherited channel represents a stream-oriented listening
     *  socket then a {@link java.nio.channels.ServerSocketChannel
     *  ServerSocketChannel} is returned. The server-socket channel is, at
     *  least initially, in blocking mode, and bound to a socket address.
     *  </p></li>
     *
     *  <li><p> If the inherited channel is a datagram-oriented socket
     *  then a {@link java.nio.channels.DatagramChannel DatagramChannel} is
     *  returned. The datagram channel is, at least initially, in blocking
     *  mode, and bound to a socket address.
     *  </p></li>
     *
     * </ul>
     *
     * <p> In addition to the network-oriented channels described, this method
     * may return other kinds of channels in the future.
     *
     * <p> The first invocation of this method creates the channel that is
     * returned. Subsequent invocations of this method return the same
     * channel. </p>
     *
     * @return  The inherited channel, if any, otherwise <tt>null</tt>.
     *
     * @throws  IOException
     *		If an I/O error occurs
     *
     * @throws	SecurityException
     *	 	If a security manager has been installed and it denies
     *		{@link RuntimePermission}<tt>("inheritedChannel")</tt>
     *
     * @since 1.5
     */
   public Channel inheritedChannel() throws IOException {
	return null;
   }
}
