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

	NFS2Client nfsClient = getNFS2Client();

	LookupResult lookupResult;
	try {
	    lookupResult = nfsClient.lookup(fileHandle, name);
	} catch (NFS2Exception e) {
	    throw new IOException("Can not call the rpc method." + e.getMessage(), e);
	}

	NFS2Entry entry = new NFS2Entry((NFS2FileSystem) getFileSystem(), this, name, lookupResult.getFileHandle(),
		lookupResult.getFileAttribute());

	return entry;

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

	final NFS2Client nfsClient = getNFS2Client();

	try {
	    List<Entry> entryList = AccessController.doPrivileged(new PrivilegedExceptionAction<List<Entry>>() {
		public List<Entry> run() throws Exception {

		    List<Entry> entryList = new ArrayList<Entry>();
		    boolean eof = false;
		    byte[] cookie = new byte[NFS2Client.COOKIE_SIZE];
		    while (!eof) {

			ListDirectoryResult result = nfsClient.listDirectory(fileHandle, cookie, 2048);

			entryList.addAll(result.getEntryList());

			if (result.isEof()) {
			    eof = true;
			} else {
			    // I guess that the list contains at lest one entry
			    // .
			    cookie = result.getEntryList().get(result.getEntryList().size() - 1).getCookie();
			}

		    }

		    return entryList;

		}
	    });

	    if (entryList.size() == 0) {
		return EMPTY_ENTRY_ITERATOR;
	    }

	    LookupAction lookupAction = new LookupAction(nfsClient);

	    List<NFS2Entry> nfsEntryList = new ArrayList<NFS2Entry>(entryList.size());
	    for (int i = 0; i < entryList.size(); i++) {

		Entry entry = entryList.get(i);

		lookupAction.setEntry(entry);
		LookupResult lookupResult = AccessController.doPrivileged(lookupAction);

		NFS2Entry nfsEntry = new NFS2Entry((NFS2FileSystem) getFileSystem(), this, entry.getName(),
			lookupResult.getFileHandle(), lookupResult.getFileAttribute());

		nfsEntryList.add(nfsEntry);

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

	NFS2Client nfsClient = getNFS2Client();

	try {
	    CreateDirectoryResult result = nfsClient.createDirectory(fileHandle, name, new boolean[] { true, true,
		    true, true, false, true, true, false, true }, -1, -1, -1, new Time(), new Time());

	    return new NFS2Entry((NFS2FileSystem) getFileSystem(), this, name, result.getFileHandle(), result
		    .getFileAttribute());

	} catch (NFS2Exception e) {

	    throw new IOException("Can not create the directory " + name + "." + e.getMessage(), e);
	}

    }

    public FSEntry addFile(String name) throws IOException {

	NFS2Client nfsClient = getNFS2Client();

	try {
	    CreateFileResult result = nfsClient.createFile(fileHandle, name, new boolean[] { true, true, false, true,
		    false, false, true, false, false }, -1, -1, -1, new Time(), new Time());

	    return new NFS2Entry((NFS2FileSystem) getFileSystem(), this, name, result.getFileHandle(), result
		    .getFileAttribute());

	} catch (NFS2Exception e) {

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

	NFS2Client nfsClient = getNFS2Client();

	if (entry.isDirectory()) {
	    try {
		nfsClient.removeDirectory(fileHandle, name);
	    } catch (NFS2Exception e) {
		throw new IOException("Can not remove directory " + name + "." + e.getMessage(), e);
	    }
	} else {
	    try {
		nfsClient.removeFile(fileHandle, name);
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
	    throw new UnsupportedOperationException();
	}

    }

}