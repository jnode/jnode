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

package org.jnode.test.fs.filesystem.config;

import org.jnode.util.OsUtils;

public class OsType
{
    /**
     * An OS that is the JNode OS
     */
    public static final OsType JNODE_OS = new OsType("JNodeOS");
    
    /**
     * An OS that is or is not the JNode OS
     */
    public static final OsType BOTH_OS  = new OsType("BothOS");
    
    /**
     * An OS that is not the JNode OS
     */
    public static final OsType OTHER_OS = new OsType("OtherOS");
    
    private OsType(String name)
    {
        this.name = name;
    }
    
    /**
     * 
     * @return
     */
    public boolean isCurrentOS()
    {
        if(this == BOTH_OS)
            return true;
        else if(OsUtils.isJNode() && (this == JNODE_OS))
            return true;
        else if(!OsUtils.isJNode() && (this == OTHER_OS))
            return true;
        else
            return false;
    }
        
    public String toString()
    {
        return name;
    }
    
    private String name;
}
