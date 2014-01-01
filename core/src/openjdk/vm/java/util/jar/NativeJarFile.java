/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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
 
package java.util.jar;

import java.util.Enumeration;
import java.util.ArrayList;
import java.util.zip.ZipEntry;

/**
 * @see java.util.jar.JarFile
 */
class NativeJarFile {
    /**
     * @see java.util.jar.JarFile#getMetaInfEntryNames()
     */
    private static String[] getMetaInfEntryNames(JarFile instance) {
        ArrayList<String> ret = new ArrayList<String>();

        for (Enumeration<?> e = instance.entries(); e.hasMoreElements(); ) {
            String name = ((ZipEntry) e.nextElement()).getName();
            if (name.startsWith("META-INF/")) {
                ret.add(name);
            }
        }

        return ret.isEmpty() ? null : ret.toArray(new String[ret.size()]);
    }
}
