/*
 * $Id$
 *
 * JNode.org
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
 
package org.jnode.test;

import org.jnode.driver.DeviceUtils;
import org.jnode.driver.input.PointerAPI;
import org.jnode.driver.input.PointerEvent;
import org.jnode.driver.input.PointerListener;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PointerTest {

    public static void main(String[] args)
        throws Exception {

        final String devId = (args.length > 0) ? args[0] : "ps2mouse";
        PointerAPI api = (PointerAPI) DeviceUtils.getAPI(devId, PointerAPI.class);
        api.addPointerListener(new MyListener());
    }

    public static class MyListener implements PointerListener {


        /**
         * @see org.jnode.driver.input.PointerListener#pointerStateChanged(org.jnode.driver.input.PointerEvent)
         */
        public void pointerStateChanged(PointerEvent event) {
            System.out.println(
                "x,y,z abs: " + event.getX() + "," + event.getY() + "," + event.getZ() + " " + event.isAbsolute());
            // TODO Auto-generated method stub

        }

    }
}
