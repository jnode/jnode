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

import org.mmtk.utility.gcspy.Color;

import org.vmmagic.unboxed.*;
import org.vmmagic.pragma.*;

/**
 *
 * VM-neutral stub file to set up a GCspy Stream, by forwarding calls
 * to gcspy C library
 *
 * $Id$
 *
 * @author <a href="http://www.ukc.ac.uk/people/staff/rej">Richard Jones</a>
 * @version $Revision$
 * @date $Date$
 */

public class Stream implements  Uninterruptible {
  public Stream(ServerSpace driver,
          int id,       
          int dataType,
          String name,
          int minValue,         
          int maxValue,
          int zeroValue,
          int defaultValue,
          String stringPre,
          String stringPost,
          int presentation,
          int paintStyle,
          int maxStreamIndex,
          Color colour) {
  }
  public int getMinValue() { return 0; }
  public int getMaxValue() { return 0; }
}

