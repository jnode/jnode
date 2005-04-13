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
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FSAccessRights;
import java.util.HashSet;



/**
 * JIFSFile <br> The JIFSFile holds information in a StringBuffer.
 *
 * 
 * @author Trickkiste
 */
public class JIFSFile implements FSEntry, FSFile {

	protected String name;
	protected StringBuffer data = new StringBuffer();
	protected boolean isvalid = true;
	
	private FSEntry parent = null;
	
	public JIFSFile(String name) {
		this.name = name;
	}
	
	public JIFSFile(String name, FSEntry parent) {
		this(name);
		this.parent = parent;
		refresh();
	}

	/**
	 * Flush the contents of this file to the persistent storage.<br>
	 * Does nothing, because the JIFSFile is a virtual File.
	 */
	public void flush() throws IOException {
	}

	public long getLength(){
		return data.length();
	}
	
	protected void cleardata(){
		data.delete(0,data.length());
	}
	
	protected void addString(String add){
		data.append(add);
	}
	
	protected void addStringln(String add){
		addString(add);
		addStringln();
	}
	
	protected void addStringln(){
		addString("\n");
	}
	
	public void refresh(){
		cleardata();
		addString("JIFSFile\nFilename :"+name+"\n");
	}
	
	public void read(long fileOffset, byte[] dest, int off, int len){
		refresh();
		byte[] readdata;
		readdata = data.toString().getBytes();
		for (long i=0 ; i < len ; i++ )
			{
				int doff = (int)(off+i);
				int foff = (int)(fileOffset+i);
				dest[doff] = readdata[foff];
			}
	}
	
	public void write(long fileOffset, byte[] src, int off, int len) throws IOException{
		throw new IOException("can not write to JNIFSFile..");
	}
	
	public void setLength(long length){
		return;
	}

	public boolean isValid(){
		return isvalid;
	}
	
	public FileSystem getFileSystem(){
		//TODO
		return null;
	}
	
	public boolean isDirty(){
		return false;
	}
	
	public FSAccessRights getAccessRights(){
		return null;
	}
	
	public FSDirectory getDirectory(){
		return null;
	}
	
	public FSFile getFile(){
		return this;
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
		return false;
	}
	
	public boolean isFile(){
		return true;
	}
	
	public FSDirectory getParent(){
		return (FSDirectory)parent;
	}
	
	public String getName(){
		return name;
	}
	
	public FSEntryIterator iterator(){
		return new JIFSDirIterator(new HashSet());
	}

}
