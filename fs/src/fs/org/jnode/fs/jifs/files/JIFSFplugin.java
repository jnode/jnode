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
package org.jnode.fs.jifs.files;

import org.jnode.fs.jifs.*;
import org.jnode.fs.FSEntry;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.PluginManager;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.plugin.PluginPrerequisite;
import javax.naming.NameNotFoundException;

/**
 * File, which contains information about the plugin with the same name.
 * 
 * @author Andreas Hänel
 */
public class JIFSFplugin extends JIFSFile{
	
	/**
	 * Creates a file, which contains information about a Plugin.
	 * 
	 *@param name
	 *			Name of this file <u>and</u> name of the plugin, whose information are stored in this file.
	 *@param parent
	 *			Parent FSEntry, in this case it is an instance of JIFSDplugins.  
	 */
	public JIFSFplugin(String name, FSEntry parent) {
		super(name,parent);
		refresh();
	}
	
	public void refresh(){
		super.refresh();
		try{
			final PluginManager mgr = (PluginManager) InitialNaming.lookup(PluginManager.NAME);
			final PluginDescriptor descr = mgr.getRegistry().getPluginDescriptor(name);
			if (descr != null) {     
				addStringln("Name:");
				addStringln("\t"+descr.getId());
				addStringln("Provider:");
				addStringln("\t"+descr.getProviderName());
				addStringln("State :");
				try {
					if (descr.getPlugin().isActive()) {
						addStringln("\tactive");
					} else {
						addStringln("\tinactive");
					}
				} catch (PluginException PE){
					System.err.println(PE);
				}
				
				addStringln("Prerequisites:");
				PluginPrerequisite[] allPreqs = descr.getPrerequisites();
				PluginPrerequisite current;
				for (int i =0 ; i<allPreqs.length; i++){
					current = allPreqs[i];
					addStringln("\t"+current.getPluginId()+"\t\t"+current.getPluginVersion());
				}
							
				           
			} else {
				isvalid = false;
			}
		} catch (NameNotFoundException N){
			System.err.println(N);			
		}
	}

	

}