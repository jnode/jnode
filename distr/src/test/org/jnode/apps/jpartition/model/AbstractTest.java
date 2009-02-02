/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.apps.jpartition.model;

import org.jnode.apps.jpartition.utils.device.DeviceUtils;
import org.junit.Assert;

public abstract class AbstractTest {
    static {
        // when not in JNode, must be called before anything
        // invoking InitialNaming
        DeviceUtils.initJNodeCore();
    }

    protected void assertEquals(long start, long size, boolean used, Partition part) {
        Assert.assertEquals(start, part.getStart());
        Assert.assertEquals(size, part.getSize());
        Assert.assertEquals(start + size - 1, part.getEnd());
        Assert.assertEquals(used, part.isUsed());
    }
}
