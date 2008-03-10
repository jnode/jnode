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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.fs.jifs.files;

import org.jnode.fs.FSDirectory;
import org.jnode.fs.jifs.JIFSFile;
import org.jnode.util.NumberUtils;

/**
 * File, which contains information about memory usage.
 * 
 * @author Andreas H\u00e4nel
 */
public class JIFSFmemory extends JIFSFile{

	public JIFSFmemory() {
		super("meminfo");
	}
	
	public JIFSFmemory(FSDirectory parent) {
		this();
		setParent(parent);
	}
	
	public void refresh(){
		super.refresh();
		final Runtime rt = Runtime.getRuntime();
		addStringln("Memory size: \n\t" + NumberUtils.toBinaryByte(rt.totalMemory()));
		addStringln("Used memory: \n\t" + NumberUtils.toBinaryByte((rt.totalMemory()-rt.freeMemory())));
		addStringln("Free memory: \n\t" + NumberUtils.toBinaryByte(rt.freeMemory()));
	
	}

	
}
