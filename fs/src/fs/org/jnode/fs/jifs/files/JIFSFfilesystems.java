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

import javax.naming.NameNotFoundException;
import java.util.Iterator;
import org.jnode.fs.jifs.*;
import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FileSystemType;

public class JIFSFfilesystems extends JIFSFile{
	
	public JIFSFfilesystems(FSEntry parent){
		super("fs", parent);
	}
	
	public void refresh(){
		super.refresh();
		try {
         	FileSystemService fSS = (FileSystemService) InitialNaming.lookup(FileSystemService.NAME);
         	Iterator it = fSS.fileSystemTypes().iterator();
         	addStringln("Registered Filesystems:");
            while (it.hasNext()){
            	FileSystemType current = (FileSystemType)it.next();
            	addStringln("\t"+current.getName());
            
            }
	    } catch (NameNotFoundException e){
    	   	System.err.print(e);
        } 
	}
	
}