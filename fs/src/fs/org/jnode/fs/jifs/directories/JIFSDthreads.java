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
import org.jnode.fs.FSEntry;
import org.jnode.fs.jifs.*;
import org.jnode.fs.jifs.files.*;
import org.jnode.fs.FSDirectory;


/**
 * Directory containing one file for each java.lang.thread. Based on the thread command.
 * 
 * @author Andreas H�nel
 */
public class JIFSDthreads extends JIFSDirectory {
	
	public JIFSDthreads(FSDirectory parent)throws IOException{
		super("threads", parent);
		refresh();
	}
	
	public void refresh(){
		super.refresh();
		ThreadGroup grp = Thread.currentThread().getThreadGroup();
		while (grp.getParent() != null) {
			grp = grp.getParent();
		}
		addGroup(grp);
	}
	
	private void addGroup(ThreadGroup grp) {

		final int max = grp.activeCount() * 2;
		final Thread[] ts = new Thread[max];
		grp.enumerate(ts,false);
		for (int i = 0; i < max; i++) {
			final Thread t = ts[i];
			if (t != null) {
				JIFSFile F = new JIFSFthread(t.getName(),t, this);
	            entries.add(F);
			}
		}
		
		final int gmax = grp.activeGroupCount() * 2;
		final ThreadGroup[] tgs = new ThreadGroup[gmax];
		grp.enumerate(tgs, false);
		
		for (int i = 0; i < gmax; i++) {
			final ThreadGroup tg = tgs[i];
			if (tg != null) {
				addGroup(tg);
			}
		}
	}
	
	public FSEntry getEntry(String name){
		return super.getEntry(name);
	}
}