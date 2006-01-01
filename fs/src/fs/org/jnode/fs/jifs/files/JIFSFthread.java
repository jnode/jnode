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
 
package org.jnode.fs.jifs.files;

import org.jnode.fs.FSDirectory;
import org.jnode.fs.jifs.JIFSFile;

/**
 * File, which contains information about the given Thread.
 * 
 * @author Andreas H\u00e4nel
 */
public class JIFSFthread extends JIFSFile{

	private Thread t;
	
	public JIFSFthread() {
		return;
	}
	
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
	public JIFSFthread(String name, Thread t, FSDirectory parent) {
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
