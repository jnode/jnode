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

package org.jnode.driver.chipset.i440BX;

import java.util.Vector;
import org.apache.log4j.Logger;
import org.jnode.driver.Driver;
import org.jnode.driver.bus.pci.PCIDevice;
import org.jnode.driver.system.ram.RAMControler;
import org.jnode.driver.system.ram.RAMModuleCollection;

/**
 * i82443BX_HostPCIBridge.
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

public class i82443BX_HostPCIBridge extends Driver implements RAMControler {

    private static final Logger log = Logger.getLogger(i82443BX_HostPCIBridge.class);
    PCIDevice device = null;
    RAMModuleCollection ramModules = null;

    public i82443BX_HostPCIBridge() {
    }

    protected void stopDevice() throws org.jnode.driver.DriverException {
        /** @todo Implement this org.jnode.driver.Driver abstract method */
    }

    protected void startDevice() throws org.jnode.driver.DriverException {
        device = (PCIDevice) getDevice();
        initRAMControler();
    }

    /***********************************************************************************************
     *
     * RAMControler interface definitions
     */

    /**
     * Initialize the RAM controller
     */
    protected void initRAMControler() {
        long capacity = 0;
        double refreshRate = 0;
        ramModules = new RAMModuleCollection(8); // this chipset cannot handle more than 8 slots.

        int config = device.readConfigByte(0x57); // offset 0x56
        log.debug("RAM config:" + config);
        int type = (config & 0x18) >> 3; // bits 4-3
        switch (type) {
            case 0:
                type = RAMControler.RAM_EDO;
                break;
            case 1:
                type = RAMControler.RAM_SDRAM;
                break;
            case 2:
                type = RAMControler.RAM_REGISTERED_SDRAM;
                break;
            default:
                type = -1; // error
        }
        log.debug("RAM type:" + type);
        int refreshcode = (config & 0x07);
        switch (refreshcode) {

            case 0: // = Refresh Disabled
                refreshRate = 0;
                break;
            case 1: // 001 = 15.6 us
                refreshRate = 15600;
                break;
            case 2: //010 = 31.2 us
                refreshRate = 31200;
                break;
            case 3: //011 = 62.4 us
                refreshRate = 62400;
                break;
            case 4: //100 = 124.8 us
                refreshRate = 124800;
                break;
            case 5: //101 = 249.6 us
                refreshRate = 249600;
                break;
            case 6: //110 = Reserved
                refreshRate = -1;
                break;
            case 7: //111 = Reserved
                refreshRate = -1;
                break;
        }

        log.debug("Refresh rate:" + refreshRate + " ns");
        //int bank0=device.readConfigDword(0x18, 0);
        //int bank1=device.readConfigDword(0x19, 0);
        //log.debug("DRAM bank0:" + Integer.toHexString(bank0));
        //log.debug("DRAM bank1:" + Integer.toHexString(bank1));
        int[] slots = new int[8];
        for (int i = 0; i < 8; i++)
            slots[i] = device.readConfigByte(0x60 + i) & 0xff;
        //    slots[3]=(bank0 & 0xff000000) >> 24;
        //    slots[2]=(bank0 & 0x00ff0000) >> 16;
        //    slots[1]=(bank0 & 0x0000ff00) >> 8;
        //    slots[0]=(bank0 & 0x000000ff);
        //    slots[7]=(bank1 & 0xff000000) >> 24;
        //    slots[6]=(bank1 & 0x00ff0000) >> 16;
        //    slots[5]=(bank1 & 0x0000ff00) >> 8;
        //    slots[4]=(bank1 & 0x000000ff);
        capacity = slots[0];
        log.debug("Ram slot 0: " + slots[0] * 8 + "MB");
        for (int i = 1; i < 8; i++) {
            int diff = slots[i] - slots[i - 1];
            log.debug("Ram slot " + i + ": " + diff * 8 + "MB");
            capacity += diff;
        }
        capacity *= 8;
        log.info("Total RAM Capacity: " + capacity + "MB");
    }

    public RAMModuleCollection getModulesCollection() {
        return null;
    }

    public Vector getMemoryMap() {
        return null;
    }

    public long capacity() {
        return 0;
    }

    public boolean is512KHoleEnabled() {
        return false;
    }

    public boolean is15MHoleEnabled() {
        return false;
    }

    public void enable512KHole() {
    }

    public void enable15MHole() {
    }

    public void openSMRAM() {
    }

    public void closeSMRAM() {
    }

    public void eanbleSMRAM() {
    }

    public void lockSMRAM(boolean islocked) {
    }

    public void setSMRAMLocation(long location) {
    }

    public long pageSize() {
        return 0;
    }

    public int setPagingPolicy(int clocks) {
        return 0;
    }

    public int getPagingPolicy() {
        return 0;
    }

}
