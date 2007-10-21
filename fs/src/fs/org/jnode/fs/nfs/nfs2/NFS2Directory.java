/*
 * $Id: FTPFSDirectory.java 2260 2006-01-22 11:10:07Z lsantha $
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

package org.jnode.fs.nfs.nfs2;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.nfs.nfs2.rpc.nfs.CreateDirectoryResult;
import org.jnode.fs.nfs.nfs2.rpc.nfs.CreateFileResult;
import org.jnode.fs.nfs.nfs2.rpc.nfs.Entry;
import org.jnode.fs.nfs.nfs2.rpc.nfs.ListDirectoryResult;
import org.jnode.fs.nfs.nfs2.rpc.nfs.LookupResult;
import org.jnode.fs.nfs.nfs2.rpc.nfs.NFS2Client;
import org.jnode.fs.nfs.nfs2.rpc.nfs.NFS2Exception;
import org.jnode.fs.nfs.nfs2.rpc.nfs.Time;

/**
 * @author Andrei Dore
 */
public class NFS2Directory extends NFS2Object implements FSDirectory {

    private static final Iterator<NFS2Entry> EMPTY_ENTRY_ITERATOR = new EmptyIterator();

    private byte[] fileHandle;

    NFS2Directory(NFS2FileSystem fileSystem, byte[] fileHandle) {
	super(fileSystem);
	this.fileHandle = fileHandle;
    }

    /**
     * Gets the entry with the given name.
     * 
     * @param name
     * @throws java.io.IOException
     */
    public NFS2Entry getEntry(String name) throws IOException {

	NFS2Client nfsClient = ((NFS2FileSystem) getFileSystem()).getNFSClient();

	LookupResult lookupResult;
	try {
	    lookupResult = nfsClient.lookup(fileHandle, name);
	} catch (NFS2Exception e) {
	    throw new IOException("Can not call the rpc method");
	}

	NFS2Entry nfs2Entry = new NFS2Entry((NFS2FileSystem) getFileSystem(), this, name, lookupResult.getFileHandle(),
		lookupResult.getFileAttribute());

	return nfs2Entry;

    }

    private class LookupAction implements PrivilegedExceptionAction<LookupResult> {
	private final NFS2Client nfsClient;
	private Entry entry;

	public LookupAction(NFS2Client nfsClient) {
	    this.nfsClient = nfsClient;
	}

	public LookupResult run() throws Exception {
	    return nfsClient.lookup(fileHandle, entry.getName());
	}

	public void setEntry(Entry entry) {
	    this.entry = entry;
	}
    }

    /**
     * Gets an iterator used to iterate over all the entries of this directory.
     * All elements returned by the iterator must be instanceof FSEntry.
     */
    public Iterator<? extends NFS2Entry> iterator() throws IOException {

	final NFS2Client nfsClient = ((NFS2FileSystem) getFileSystem()).getNFSClient();

	try {
	    ListDirectoryResult result = AccessController
		    .doPrivileged(new PrivilegedExceptionAction<ListDirectoryResult>() {
			public ListDirectoryResult run() throws Exception {
			    return nfsClient.listDirectory(fileHandle, new byte[NFS2Client.COOKIE_SIZE], 1024);
			}
		    });

	    if (result.isEof()) {
		return EMPTY_ENTRY_ITERATOR;
	    }

	    LookupAction lookupAction = new LookupAction(nfsClient);

	    Entry entry = result.getEntry();

	    List<NFS2Entry> nfsEntryList = new ArrayList<NFS2Entry>();
	    while (entry != null) {

		lookupAction.setEntry(entry);
		LookupResult lookupResult = AccessController.doPrivileged(lookupAction);

		NFS2Entry nfs2Entry = new NFS2Entry((NFS2FileSystem) getFileSystem(), this, entry.getName(),
			lookupResult.getFileHandle(), lookupResult.getFileAttribute());

		nfsEntryList.add(nfs2Entry);

		entry = entry.getNextEntry();

	    }

	    return nfsEntryList.iterator();

	} catch (PrivilegedActionException e) {
	    Exception x = e.getException();
	    if (x instanceof NFS2Exception)
		throw new IOException(
			"An error occurs when the nfs file system tried to fetch the content of the  directory",
			(NFS2Exception) x);
	    else if (x instanceof IOException)
		throw (IOException) x;
	    else
		throw new RuntimeException(x);
	}

    }

    public FSEntry addDirectory(String name) throws IOException {

	NFS2Client client = ((NFS2FileSystem) getFileSystem()).getNFSClient();

	
	
	try {
	    CreateDirectoryResult result = client.createDirectory(fileHandle, name, new boolean[]{true,true,true,true,false,true,true,false,true}, -1, -1,
		    -1, new Time(), new Time());

	    return new NFS2Entry((NFS2FileSystem) getFileSystem(), this, name, result.getFileHandle(), result
		    .getFileAttribute());

	} catch (NFS2Exception e) {
	    System.out.println(e.getMessage());
	    throw new IOException("Can not create the directory " + name + "." + e.getMessage(), e);
	}

    }

    public FSEntry addFile(String name) throws IOException {
	
	NFS2Client client = ((NFS2FileSystem) getFileSystem()).getNFSClient();

	try {
	    CreateFileResult result = client.createFile(fileHandle, name, new boolean[]{true,true,false,true,false,false,true,false,false}, -1,-1,
		    -1, new Time(), new Time());

	    return new NFS2Entry((NFS2FileSystem) getFileSystem(), this, name, result.getFileHandle(), result
		    .getFileAttribute());

	} catch (NFS2Exception e) {
	    System.out.println(e.getMessage());
	    throw new IOException("Can not create the file " + name + "." + e.getMessage(), e);
	}
    }

    public void flush() throws IOException {
	// TODO Auto-generated method stub

    }

    public void remove(String name) throws IOException {

	NFS2Entry entry = getEntry(name);
	if (entry == null) {
	    return;
	}

	NFS2Client client = ((NFS2FileSystem) getFileSystem()).getNFSClient();

	if (entry.isDirectory()) {
	    try {
		client.removeDirectory(fileHandle, name);
	    } catch (NFS2Exception e) {
		throw new IOException("Can not remove directory " + name + "." + e.getMessage(), e);
	    }
	} else {
	    try {
		client.removeFile(fileHandle, name);
	    } catch (NFS2Exception e) {
		throw new IOException("Can not remove file " + name + "." + e.getMessage(), e);
	    }
	}

    }

    byte[] getFileHandle() {
	return fileHandle;
    }

    private static final class EmptyIterator implements Iterator<NFS2Entry> {

	public boolean hasNext() {
	    return false;
	}

	public NFS2Entry next() {
	    return null;
	}

	public void remove() {

	}

    }

}
