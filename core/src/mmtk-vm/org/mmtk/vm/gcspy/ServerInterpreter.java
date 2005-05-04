/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 
package org.mmtk.vm.gcspy;

import org.vmmagic.unboxed.*;
import org.vmmagic.pragma.*;

/**
 * VM-neutral stub file for generic GCspy server interpreter
 *
 * This class implements the GCspy server. 
 * Mostly it forwards calls to the C gcspy library.
 *
 * $Id$
 *
 * @author <a href="http://www.ukc.ac.uk/people/staff/rej">Richard Jones</a>
 * @version $Revision$
 * @date $Date$
 */
public class ServerInterpreter implements Uninterruptible {
  public static void init (String name,
                           int port,
                           String[] eventNames,
                           boolean verbose,
                           String generalInfo) {}
  public static boolean isConnected (int event) { return false; }
  public static void startServer(boolean wait) {}
  public static boolean shouldTransmit(int event) { return false; }
  public static void startCompensationTimer() {}
  public static void stopCompensationTimer() {}
  public static void serverSafepoint (int event) {}
  public static int computeHeaderSize() { return 0; }
}
