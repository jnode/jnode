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
* Copyright (C) 2004  Andreas H�nel
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
 * <description>
 * 
 * @author Trickkiste
 */
public class JIFSFmemory extends JIFSFile{

	public JIFSFmemory(FSEntry parent) {
		super("meminfo",parent);
		refresh();
	}
	
	public void refresh(){
		super.refresh();
		final Runtime rt = Runtime.getRuntime();
		addStringln("Memory size: \n\t" + rt.totalMemory());
		addStringln("Used memory: \n\t" + (rt.totalMemory()-rt.freeMemory()));
		addStringln("Free memory: \n\t" + rt.freeMemory());
	
	}

	
}