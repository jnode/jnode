/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 
package org.jnode.fs.jifs.files;

import javax.naming.NameNotFoundException;

import org.jnode.fs.FSDirectory;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.jifs.JIFSFile;
import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;

/**
 *  
 * @author Andreas H\u00e4nel
 */
public class JIFSFfilesystems extends JIFSFile{
	
	public JIFSFfilesystems(){
		super("fs");
	}
	
	public JIFSFfilesystems(FSDirectory parent){
		this();
		setParent(parent);
	}
	
	public void refresh(){
		super.refresh();
		try {
         	FileSystemService fSS = InitialNaming.lookup(FileSystemService.NAME);
         	addStringln("Registered Filesystems:");
            for (FileSystemType current : fSS.fileSystemTypes()) {
            	addStringln("\t"+current.getName());
            
            }
	    } catch (NameNotFoundException e){
    	   	System.err.print(e);
        } 
	}
	
}
