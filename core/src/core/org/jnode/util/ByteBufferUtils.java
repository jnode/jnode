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

package org.jnode.util;

import java.nio.ByteBuffer;

public class ByteBufferUtils
{
    /**
     * This method is the equivalent of System.arraycopy
     * But, instead of 2 arrays, it takes 2 ByteBuffers.
     *  
     * @param src
     * @param srcStart
     * @param dest
     * @param destStart
     * @param len
     */
    public static void buffercopy(ByteBuffer src, int srcStart,
            ByteBuffer dest, int destStart, int len)
    {
        src.position(srcStart);
        src.limit(srcStart + len);
        
        dest.position(destStart);
        dest.limit(destStart + len);
        
        dest.put(src);
    }
}
