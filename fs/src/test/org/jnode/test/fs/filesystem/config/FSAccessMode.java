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
 
package org.jnode.test.fs.filesystem.config;

public class FSAccessMode
{
    /**
     * Do tests for access in readOnly mode
     */
    public static final FSAccessMode READ_ONLY = new FSAccessMode("ReadOnly", true, false);
    
    /**
     * Do tests for access in readWrite mode
     */
    public static final FSAccessMode READ_WRITE  = new FSAccessMode("ReadWrite", false, true);
    
    /**
     * Do tests for access in readOnly and in readWrite mode
     */
    public static final FSAccessMode BOTH = new FSAccessMode("Both", true, true);
    
    public String toString()
    {
        return name;
    }

    public boolean doReadOnlyTests()
    {
        return doReadOnlyTests;
    }
    
    public boolean doReadWriteTests()
    {
        return doReadWriteTests;
    }

    private FSAccessMode(String name, boolean doReadOnlyTests, boolean doReadWriteTests)
    {
        this.name = name;
        this.doReadOnlyTests = doReadOnlyTests;
        this.doReadWriteTests = doReadWriteTests;
    }
    private String name;
    private boolean doReadOnlyTests;
    private boolean doReadWriteTests;
}
