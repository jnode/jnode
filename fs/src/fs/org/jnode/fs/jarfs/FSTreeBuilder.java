/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
 
package org.jnode.fs.jarfs;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.jnode.fs.util.FSUtils;

public class FSTreeBuilder
{
    public static final char separator = '/';
    @SuppressWarnings("unchecked")
    public static JarFSEntry build(JarFileSystem fs, JarFile jarFile, JarFSCache cache)
    {
        JarFSEntry root = new JarFSEntry(fs, null, null, jarFile.getName());
        Map<String, JarFSEntry> nameToJarFSEntry = new HashMap<String, JarFSEntry>();
        nameToJarFSEntry.put("", root);
        
        for(Enumeration<JarEntry> entries = jarFile.entries() ; entries.hasMoreElements() ; )
        {
            JarEntry entry = entries.nextElement();
            JarFSEntry fsEntry = getJarFSEntry(fs, nameToJarFSEntry, entry, null);
            cache.put(entry, fsEntry);
        }
        
        return root;
    }
    
    public static JarFSEntry getJarFSEntry(JarFileSystem fs, 
            Map<String, JarFSEntry> nameToJarFSEntry, JarEntry jarEntry, String fullName)
    {        
        fullName = (fullName == null) ? jarEntry.getName() : fullName;
        
        JarFSEntry fsEntry = nameToJarFSEntry.get(fullName);
        if(fsEntry == null)
        {
            String parent = FSUtils.getParentName(fullName, separator);
            JarFSEntry parentFSEntry;
            if("".equals(parent))
            {
                // parent is the root
                parentFSEntry = nameToJarFSEntry.get("");
            }
            else
            {
                // recursive call
                parentFSEntry = getJarFSEntry(fs, nameToJarFSEntry, null, parent);
            }
            
            String name = FSUtils.getName(jarEntry.getName(), separator);
            fsEntry = new JarFSEntry(fs, parentFSEntry, jarEntry, name);
            nameToJarFSEntry.put(fullName, fsEntry);
        }
        
        return fsEntry;
    }
}
