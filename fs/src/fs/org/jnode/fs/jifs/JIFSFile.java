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
 
package org.jnode.fs.jifs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Iterator;

import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;

/**
 * JIFSFile <br>
 * The JIFSFile holds information in a StringBuffer.
 * 
 * @author Andreas H\u00e4nel
 */
public class JIFSFile implements ExtFSEntry, FSFile {

	protected String name;
	protected StringBuffer data = new StringBuffer();
	protected boolean isvalid = true;
	
	private FSDirectory parent = null;
	
	public JIFSFile() {
		refresh();
	}
	
	public JIFSFile(String name) {
		this.name = name;
	}
	
	public JIFSFile(String name, FSDirectory parent) {
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
	
	//public void read(long fileOffset, byte[] dest, int off, int len){
    public void read(long fileOffset, ByteBuffer destBuf){
		refresh();
		byte[] readdata = data.toString().getBytes();
        destBuf.put(readdata, (int)fileOffset, destBuf.remaining());
	}
	
	//public void write(long fileOffset, byte[] src, int off, int len) throws IOException{
    public void write(long fileOffset, ByteBuffer src) throws IOException{
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
		return System.currentTimeMillis();
	}
	
	public void setName(String name){
		this.name=name;
	}
	
	public boolean isDirectory(){
		return false;
	}
	
	public boolean isFile(){
		return true;
	}
	
	public FSDirectory getParent(){
		return parent;
	}
	
	public void setParent(FSDirectory parent){
		this.parent=parent;
	}
	
	public String getName(){
		return name;
	}
	
	public Iterator<FSEntry> iterator(){
		return new JIFSDirIterator(new HashSet<FSEntry>());
	}

}
