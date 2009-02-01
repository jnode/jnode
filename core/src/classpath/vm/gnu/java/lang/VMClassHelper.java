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
 
package gnu.java.lang;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class VMClassHelper {

    /**
     * Strip the last portion of the name (after the last dot).
     * 
     * @param name
     *            the name to get package of
     * @return the package name, or "" if no package
     */
    public static String getPackagePortion(String name) {
        int lastInd = name.lastIndexOf('.');
        if (lastInd == -1)
            return "";
        return name.substring(0, lastInd);
    }
    
    /**
     * Strip the package portion of the classname (before the last dot).
     * @param name
     * @return
     */
    public static String getClassNamePortion(String name) {
        int lastInd = name.lastIndexOf('.');
        if (lastInd == -1)
            return name;
        return name.substring(lastInd+1);        
    }

}
