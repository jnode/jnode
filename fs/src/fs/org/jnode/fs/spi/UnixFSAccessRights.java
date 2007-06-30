package org.jnode.fs.spi;

import java.io.IOException;
import java.security.Principal;
import java.security.acl.Group;
import java.util.BitSet;

import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FileSystem;

import sun.security.acl.GroupImpl;

import com.sun.security.auth.UserPrincipal;

/**
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 *
 */
public class UnixFSAccessRights implements FSAccessRights 
{
	final private FileSystem filesystem;
	
	private Principal owner;
	private Group group;

	final private Rights ownerRights = new Rights(true, true, true);
	final private Rights groupRights = new Rights();
	final private Rights worldRights = new Rights();
	

	public UnixFSAccessRights(FileSystem filesystem)
	{
		if(filesystem == null)
		{
			throw new NullPointerException("filesystem can't be null");
		}
		this.filesystem = filesystem;
		
		// TODO manages users & groups in JNode
		owner = new UserPrincipal("root");
		group = new GroupImpl("admins");
		group.addMember(owner);
	}
	
	private Principal getUser()
	{
		// TODO manages users & groups in JNode
		// we should find the user from the context
		return owner;
	}
	
	private Rights getUserRights()
	{
		Principal user = getUser();
		
		Rights rights = worldRights;
		if(owner.equals(user))
		{
			rights = ownerRights;
		}
		else if(group.isMember(user))
		{
			rights = groupRights;
		}
		
		return rights;
	}
	
	public boolean canExecute() {
		return getUserRights().isExecute();
	}

	public boolean canRead() {
		return getUserRights().isRead();
	}

	public boolean canWrite() {
		return getUserRights().isWrite();
	}

	public Principal getOwner() {
		return owner;
	}

	public boolean setExecutable(boolean enable, boolean owneronly)
	{
		if(!owner.equals(getUser()))
		{
			return false;
		}
		
		ownerRights.setExecute(enable);
		if(!owneronly)
		{
			groupRights.setExecute(enable);
			worldRights.setExecute(enable);
		}
		return true;
	}

	public boolean setReadable(boolean enable, boolean owneronly)
	{
		if(!owner.equals(getUser()))
		{
			return false;
		}
		
		ownerRights.setRead(enable);
		if(!owneronly)
		{
			groupRights.setRead(enable);
			worldRights.setRead(enable);
		}
		return true;
	}

	public boolean setWritable(boolean enable, boolean owneronly)
	{
		if(!owner.equals(getUser()))
		{
			return false;
		}
		
		ownerRights.setWrite(enable);
		if(!owneronly)
		{
			groupRights.setWrite(enable);
			worldRights.setWrite(enable);
		}
		return true;
	}

	public FileSystem getFileSystem() {
		return filesystem;
	}

	public boolean isValid() {
		return true;
	}

	private static class Rights
	{
		private boolean read = false; 
		private boolean write = false;
		private boolean execute = false;

		public Rights()
		{
			this(false, false, false);
		}
		
		public Rights(boolean read, boolean write, boolean execute) {
			this.read = read;
			this.write = write;
			this.execute = execute;
		}
		
		public boolean isRead() {
			return read;
		}
		public void setRead(boolean read) {
			this.read = read;
		}
		public boolean isWrite() {
			return write;
		}
		public void setWrite(boolean write) {
			this.write = write;
		}
		public boolean isExecute() {
			return execute;
		}
		public void setExecute(boolean execute) {
			this.execute = execute;
		}
	}
}
