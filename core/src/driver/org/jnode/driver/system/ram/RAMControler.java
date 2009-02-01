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
 
package org.jnode.driver.system.ram;

import java.util.Vector;

/**
 * RAMController.
 * <p/>
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Licence: GNU LGPL
 * </p>
 * <p>
 * </p>
 *
 * @author Francois-Frederic Ozog
 * @version 1.0
 */

public interface RAMControler {

    public static int RAM_EDO = 0;
    public static int RAM_SDRAM = 1;
    public static int RAM_REGISTERED_SDRAM = 2;
    public static int RAM_DRDRAM = 3;

    public RAMModuleCollection getModulesCollection();

    public Vector getMemoryMap();

    public long capacity();

    public boolean is512KHoleEnabled();

    public boolean is15MHoleEnabled();

    public void enable512KHole();

    public void enable15MHole();

    public void openSMRAM();

    public void closeSMRAM();

    public void eanbleSMRAM();

    public void lockSMRAM(boolean islocked);

    public void setSMRAMLocation(long location);

    public long pageSize();

    public int setPagingPolicy(int clocks);

    public int getPagingPolicy();

}
