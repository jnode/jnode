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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.mmtk.vm.gcspy;

import org.mmtk.utility.gcspy.Color;
import org.vmmagic.pragma.Uninterruptible;

/**
 * VM-neutral stub file to set up a GCspy Stream, by forwarding calls to gcspy C
 * library $Id$
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author <a href="http://www.ukc.ac.uk/people/staff/rej">Richard Jones</a>
 * @version $Revision$
 * @date $Date$
 */

public class Stream implements Uninterruptible {
    public Stream(ServerSpace driver, int id, int dataType, String name,
            int minValue, int maxValue, int zeroValue, int defaultValue,
            String stringPre, String stringPost, int presentation,
            int paintStyle, int maxStreamIndex, Color colour) {
    }

    public int getMinValue() {
        return 0;
    }

    public int getMaxValue() {
        return 0;
    }
}
