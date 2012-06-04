/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package sun.java2d;

import java.awt.image.IndexColorModel;

/**
 * @author Levente S\u00e1ntha
 */
class NativeSurfaceData {
    private static void initIDs(){}
    /**
     * @see sun.java2d.SurfaceData#isOpaqueGray(java.awt.image.IndexColorModel)
     */
    protected static boolean isOpaqueGray(IndexColorModel icm){
        //todo implement it
        return false;
    }
}
