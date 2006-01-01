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
import org.jnode.vm.VmSystem;

/**
 * File, which contains the uptime of the system.
 * 
 * @author Andreas H\u00e4nel
 */
public class JIFSFuptime extends JIFSFile{
	
	public JIFSFuptime(){
		super("uptime");
		refresh();
	}
	
	public JIFSFuptime(FSDirectory parent){
		this();
		setParent(parent);
	}
	
	public void refresh(){
		super.refresh();
		String h="";
		String m="";
		String s="";
		long sinceboot = VmSystem.currentKernelMillis();
		int hours = new Long(sinceboot / (1000 * 60 * 60)).intValue();
		int minutes = new Long((sinceboot - (hours * (1000 * 60 * 60))) / (1000 * 60)).intValue();
		int seconds = new Long((sinceboot - (((hours * 60) + minutes) * 1000 * 60)) / 1000).intValue();
		if (hours<10) h="0";
		if (minutes<10) m="0";
		if (seconds<10) s="0";
		addStringln("Time since booting JNode:");
		addStringln("\t"+h+new Integer(hours).toString()+":"+m+new Integer(minutes).toString()+":"+s+new Integer(seconds).toString());
	}	
	
}
