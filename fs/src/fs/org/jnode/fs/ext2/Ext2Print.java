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
 
package org.jnode.fs.ext2;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.jnode.driver.block.BlockDeviceAPI;

/**
 * @author Andras Nagy
 */
public class Ext2Print {
    private static final Logger log = Logger.getLogger("EXT2");

    public static String hexFormat(int i) {
        String pad = "00000000";
        String res = Integer.toHexString(i);
        int len = Math.max(0, 8 - res.length());
        res = pad.substring(0, len) + res;
        return res;
    }

    private static int unsignedByte(byte i) {
        if (i < 0)
            return 256 + i;
        else
            return i;
    }

    public static String hexFormat(byte b) {
        int i = unsignedByte(b);
        String pad = "00";
        String res = Integer.toHexString(i);
        int len = Math.max(0, 2 - res.length());
        res = pad.substring(0, len) + res;
        return res;
    }

    public static void dumpData(BlockDeviceAPI api, int offset, int length) {
        byte[] data = new byte[length];
        try {
            api.read(offset, ByteBuffer.wrap(data));
        } catch (Exception e) {
            return;
        }
        int pageWidth = 16;
        for (int i = 0; i < length; i += pageWidth) {
            System.out.print(hexFormat(i) + ": ");
            for (int j = 0; j < pageWidth; j++)
                if (i + j < length) {
                    log.info(hexFormat(data[i + j]) + " ");
                    if ((i + j) % 4 == 3)
                        System.out.print(" - ");
                }
            System.out.println();
        }
    }
}
