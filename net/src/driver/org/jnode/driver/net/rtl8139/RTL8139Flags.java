/*
 * $Id$
 */

package org.jnode.driver.net.rtl8139;

import org.jnode.driver.net.ethernet.Flags;


/**
 * @author Martin Husted Hartvig
 */

public class RTL8139Flags implements Flags
{
  private final String name;


	/**
	 * Create a new instance of the flags
	 * @param name
	 */

  public RTL8139Flags(String name)
  {
    this.name = name;
  }


	/**
	 * Gets the name of the device
	 */

  public String getName()
  {
    return name;
  }
}

