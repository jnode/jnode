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
 * You should have received a copy of the GNU General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.driver.block;

import java.io.IOException;

public class BlockDeviceAPIHelper
{
    public static void checkBounds(BlockDeviceAPI api, long devOffset) throws IOException
    {
        if(devOffset < 0)
            throw new IOException("devOffset < 0");
        
        if(devOffset >= api.getLength())
            throw new IOException("devOffset (" + devOffset +") > upper bound (" + api.getLength() + ")");
    }
    
    public static void checkBounds(BlockDeviceAPI api, long devOffset, long length) throws IOException
    {
        checkBounds(api, devOffset);
        
        if(length <= 0)
            throw new IOException("length <= 0");
                
        checkBounds(api, devOffset+length-1); // don't forget to substract 1 !
    }

    public static void checkAlignment(int sectorSize, BlockDeviceAPI api, long devOffset, int length) throws IOException
    {
        if ((devOffset % sectorSize) != 0) {
            throw new IOException("Non aligned devOffset not allowed. Size requested = " + devOffset + " Sector size = " + sectorSize);
        }
        if ((length % sectorSize) != 0) {
            throw new IOException("Non aligned length not allowed. Size requested = " + length + " Sector size = " + sectorSize);
        }
    }    
}
