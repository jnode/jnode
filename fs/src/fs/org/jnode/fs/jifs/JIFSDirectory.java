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
 
package org.jnode.fs.jifs;

import java.io.IOException;

import org.jnode.fs.FSEntryIterator;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSFile;
import org.jnode.fs.FSEntry;
import org.jnode.fs.ReadOnlyFileSystemException;
import org.jnode.fs.jifs.files.*;
import org.jnode.fs.jifs.directories.*;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FSAccessRights;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @author Andreas Haenel
 */
public class JIFSDirectory implements FSDirectory, FSEntry {

	private boolean root = false;
	private String label;
	private FSDirectory parent = null;
	protected Set entries;

	public JIFSDirectory(String name) throws IOException {
		label = name;
		entries = new HashSet();
	}
	 	 
	public JIFSDirectory(String name, FSDirectory parent) throws IOException {
		this(name);
		this.parent = parent;
	}
	
	public JIFSDirectory(String name, boolean root) throws IOException {
		this(name);
		this.root = root;
		JIFSDirectory dir;
		JIFSFile file;
		//file
		entries.add(new JIFSFuptime(this));
		entries.add(new JIFSFmemory(this));
		entries.add(new JIFSFversion(this));
		//directory
		entries.add(new JIFSDthreads( this));
		entries.add(new JIFSDplugins(this));
	}
	
	public void refresh(){
		entries.clear();
	}
	
	public void addFSE(FSEntry entry){
		entries.add(entry);
	}

	/**
	 * Flush the contents of this directory to the persistent storage
	 */
	public void flush() throws IOException {
	}

	public FSEntry addDirectory(String name){
		return null;
	}
	
	public FSEntry addFile(String name){
		return null;
	}

	public FSEntryIterator iterator(){
		return new JIFSDirIterator(entries);
	}
	
	
	public void remove(String name) throws IOException{
		throw new ReadOnlyFileSystemException("you can not remove from JNIFS..");
	}

	public boolean isValid(){
		return true;
	}
	
	public boolean isDirty(){
		return false;
	}
	
	public FileSystem getFileSystem(){
		//TODO
		return null;
	}
	
	public FSEntry getEntry(String name){
		Iterator it = entries.iterator();
		FSEntry rueck;
		while (it.hasNext()){
			rueck = (FSEntry)it.next();
			if (rueck.getName().equals(name)) {
				return rueck;
			}
		}
		return null;
	}
	
	public FSAccessRights getAccessRights(){
		return null;
	}
	
	public FSDirectory getDirectory(){
		return this;
	}
	
	public FSFile getFile(){
		return null;
	}
	
	public void setLastModified(long l){
		return;
	}
	
	public long getLastModified(){
		return 0;
	}
	
	public void setName(String name){
		return;
	}
	
	public boolean isDirectory(){
		return true;
	}
	
	public boolean isFile(){
		return false;
	}
	
	public FSDirectory getParent(){
		return parent;
	}
	
	public String getName(){
		return label;
	}

}
