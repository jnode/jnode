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
* Copyright (C) 2004  Andreas Hänel
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

/**
 * File, which contains information about the given Thread.
 * 
 * @author Andreas Hänel
 */
public class JIFSFthread extends JIFSFile{

	private Thread t;
/**
 * Creates the file, which contains information about the given Thread.
 * 
 * @param String name
 * 			Filename.
 * @param Thread t
 * 			The Thread, whose information is presented via this file.
 * @param parent
 *			Parent FSEntry, in this case it is an instance of JIFSDplugins.
 */
	public JIFSFthread(String name, Thread t, FSEntry parent) {
		super(name,parent);
		this.t = t;
		refresh();
	}
	
	public void refresh(){
		if (t == null) {
			isvalid=false;
			return;
		}
		super.refresh();
		addStringln("ID:");
		addStringln("\t"+t.getId());
		addStringln("Name:");
		addStringln("\t"+t.getName());
		addStringln("Priority:");
		int i = new Integer((60 / (Thread.MAX_PRIORITY - Thread.MIN_PRIORITY)) * t.getPriority()).intValue();
		String hashes="";
		String blanks="";
		int j = 60-i;
		while (i>0){
			hashes += "#";
			i--;
		}
		while (j>0){
			blanks += " ";
			j--;
		}
		addStringln("\tLOW["+hashes+blanks+"]HIGH");
	}

	

}