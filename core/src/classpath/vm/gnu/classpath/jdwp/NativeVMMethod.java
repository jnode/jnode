/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package gnu.classpath.jdwp;

import gnu.classpath.jdwp.util.LineTable;
import gnu.classpath.jdwp.util.VariableTable;

/**
 * @see gnu.classpath.jdwp.VMMethod
 */
class NativeVMMethod {
    /**
     * @see gnu.classpath.jdwp.VMMethod#getName()
     */
    private static String getName(VMMethod instance) {
        //todo implement it
        return null;
    }
    /**
     * @see gnu.classpath.jdwp.VMMethod#getSignature()
     */
    private static String getSignature(VMMethod instance) {
        //todo implement it
        return null;
    }
    /**
     * @see gnu.classpath.jdwp.VMMethod#getModifiers()
     */
    private static int getModifiers(VMMethod instance) {
        //todo implement it
        return 0;
    }
    /**
     * @see gnu.classpath.jdwp.VMMethod#getLineTable()
     */
    private static LineTable getLineTable(VMMethod instance) {
        //todo implement it
        return null;
    }
    /**
     * @see gnu.classpath.jdwp.VMMethod#getVariableTable()
     */
    private static VariableTable getVariableTable(VMMethod instance) {
        //todo implement it
        return null;
    }
}
