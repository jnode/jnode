/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 * You should have received a copy of the GNU General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.shell.command.driver.system.acpi;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Licence: GNU LGPL</p>
 * <p> </p>
 * @author Francois-Frederic Ozog
 * @version 1.0
 */


import java.io.IOException;

import javax.naming.NameNotFoundException;

import org.jnode.driver.bus.smbus.SMBusControler;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.help.Help;

public class SMBusCommand {


  public static Help.Info HELP_INFO =
          new Help.Info(
                  "smbus",
                  "test smbus"
          );

  public static void main(String[] args) throws Exception {
          //ParsedArguments cmdLine = HELP_INFO.parse(args);
          SMBusControler smbusctrl=null;

          try {
            smbusctrl = InitialNaming.lookup(SMBusControler.NAME);
          }
          catch (NameNotFoundException ex2) {
            System.out.println("Could not connect to SMBusControler:" + ex2.getMessage());
            return;
          }

          byte res=0;
          for (byte i=0;i<8; i++) {

              try {
                res = smbusctrl.readByte( (byte) (0xa0 |( i<<1)), (byte) 2);
                System.out.println("DIMM " + i + " type :" + Integer.toHexString(res));
              }
              catch (IOException ex) {
                System.out.println("DIMM " + i + " not present");
              }


          }
  }

}

