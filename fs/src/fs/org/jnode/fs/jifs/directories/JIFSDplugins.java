/*
*	This file is part of 
*
*	Jnode Information FileSystem (jifs)
*
* jifs is used within JNode (see jnode.sourceforge.net for details).
* jifs provides files with information about JNode internals.
* For a new version of jifs see jifs.org.
* To contact me mail to : mail@jifs.org
*
* Copyright (C) 2004  Andreas Haenel
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*
*/
package org.jnode.fs.jifs.directories;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.jnode.naming.InitialNaming;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginManager;
import org.jnode.fs.FSEntry;
import org.jnode.fs.jifs.*;
import org.jnode.fs.jifs.files.*;
import javax.naming.NameNotFoundException;
import org.jnode.fs.FSDirectory;


/**
 * <description>
 * 
 * @author Andreas Hänel
 */
public class JIFSDplugins extends JIFSDirectory {
	
	public JIFSDplugins(FSDirectory parent)throws IOException{
		super("plugins", parent);
		refresh();
	}
	
	public void refresh(){
		super.refresh();
		// this has to be improved
		// just add new ones and delete old ones
		// now it does delete all files and (re)create all ones
		final ArrayList rows = new ArrayList();
		try {
			final PluginManager mgr = (PluginManager) InitialNaming.lookup(PluginManager.NAME);
			entries.clear();
	        for (Iterator i = mgr.getRegistry().getDescriptorIterator(); i
	                .hasNext();) {
	            PluginDescriptor descr = (PluginDescriptor) i.next();
	            rows.add(descr.getId());
	        }
	        Collections.sort(rows);
	        for (Iterator i = rows.iterator(); i.hasNext(); ) {
	        	JIFSFile F = new JIFSFplugin((String)i.next(), this);
	            entries.add(F);
	        }
		} catch (NameNotFoundException N){
			System.err.println(N);
		}
	}
	
	public FSEntry getEntry(String name){
		return super.getEntry(name);
	}
}