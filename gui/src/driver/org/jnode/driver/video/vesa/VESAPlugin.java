/*
 * $Id: DeviceFinderPlugin.java 2224 2006-01-01 12:49:03Z epr $
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

package org.jnode.driver.video.vesa;

import javax.naming.NameNotFoundException;

import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.util.NumberUtils;
import org.jnode.vm.Unsafe;
import org.jnode.vm.x86.UnsafeX86;
import org.vmmagic.unboxed.Address;

/**
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 * 
 */
public class VESAPlugin extends Plugin {

    /**
     * @param descriptor
     */
    public VESAPlugin(PluginDescriptor descriptor) {
        super(descriptor);
    }

    /**
     * Start this plugin
     * 
     * @throws PluginException
     */
    protected void startPlugin() throws PluginException {
        /*
         * 72 | vbe_control_info | (present if flags[11] is set) 76 |
         * vbe_mode_info | 80 | vbe_mode | 82 | vbe_interface_seg | 84 |
         * vbe_interface_off | 86 | vbe_interface_len
         */
        Unsafe.debug("\nstartPlugin. address=");
        Address vbeInfos = UnsafeX86.getVbeInfos();
        if (vbeInfos.isZero()) {
            Unsafe.debug("No vbeInfos, VESA plugin won't start");
            return;
        }

        Unsafe.debug(vbeInfos);

        dump("vbeInfos bytes", vbeInfos, 32);

        // Address vbeControlInfo = Address.fromInt(vbeInfos.loadInt());
        Address vbeControlInfo = UnsafeX86.getVbeControlInfos();
        VbeInfoBlock vbeInfoBlock = new VbeInfoBlock(vbeControlInfo);
        dump("vbeControlInfo", vbeControlInfo, 512); // 256 for vbe 2

        // Address vbeModeInfo = Address.fromInt(vbeInfos.add(4).loadInt());
        Address vbeModeInfo = UnsafeX86.getVbeModeInfos();
        ModeInfoBlock modeInfoBlock = new ModeInfoBlock(vbeModeInfo);
        dump("vbeModeInfo", vbeModeInfo, 256);

        short vbeMode = vbeInfos.add(8).loadShort();
        Unsafe.debug("vbeMode=" + NumberUtils.hex(vbeMode) + "\n");

        short vbeInterfaceSeg = vbeInfos.add(10).loadShort();
        Unsafe.debug("vbeInterfaceSeg=" + NumberUtils.hex(vbeInterfaceSeg) + "\n");

        short vbeInterfaceOff = vbeInfos.add(12).loadShort();
        Unsafe.debug("vbeInterfaceOff=" + NumberUtils.hex(vbeInterfaceOff) + "\n");

        int vbeInterfaceLen = vbeInfos.add(14).loadInt();
        // int vbeInterfaceLen = 0x000FFFFF;
        Unsafe.debug("vbeInterfaceLen=" + NumberUtils.hex(vbeInterfaceLen) + "\n");

        int physBasePtr = modeInfoBlock.getRamBase();
        Unsafe.debug("physBasePtr=" + NumberUtils.hex(physBasePtr) + "\n");
        Address vbeMemory = Address.fromInt(physBasePtr);

        Unsafe.debug(NumberUtils.hex(vbeMemory.toInt()));
        /*
         * short color = (short) 0x0000FFFF; Address addr = vbeMemory; for(int i =
         * 0 ; i < 0xFFFFF ; i++) { addr.store((short) color); //color++; addr =
         * addr.add(2); }
         */
        Unsafe.debug("\nend\n");

        try {
            System.out.println("VESA detected : " + VESACommand.detect());
        } catch (NameNotFoundException e) {
            Unsafe.debugStackTrace();
            Unsafe.debug("error : " + e.getMessage());
        } catch (ResourceNotFreeException e) {
            Unsafe.debugStackTrace();
            Unsafe.debug("error : " + e.getMessage());
        }
    }

    /**
     * Stop this plugin
     * 
     * @throws PluginException
     */
    protected void stopPlugin() throws PluginException {
        // Do nothing
    }

    private void dump(String message, Address address, int size) {
        // StringBuilder sb = new StringBuilder("\n");
        // sb.append(message).append(" at address ");
        //
        // if(address.isZero())
        // {
        // sb.append("NULL");
        // }
        // else
        // {
        // sb.append(NumberUtils.hex(address.toInt())).append(" :\n");
        //
        // Address addr = address;
        // for(int i = 0 ; i < size ; i++) {
        // String str = NumberUtils.hex((byte) (addr.loadByte() & 0xFF) );
        // str = str.substring(str.length() - 2);
        // sb.append(str).append(' ');
        // if((i%16) == 0) sb.append('\n');
        //
        // addr = addr.add(1);
        // }
        // }
        //
        // Unsafe.debug(sb.append("\n").toString());
    }
}
