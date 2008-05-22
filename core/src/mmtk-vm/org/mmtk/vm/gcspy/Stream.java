/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Common Public License (CPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/cpl1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
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
