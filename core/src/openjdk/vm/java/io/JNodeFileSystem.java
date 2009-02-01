/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 
package java.io;

import gnu.classpath.SystemProperties;

/**
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 *
 */
class JNodeFileSystem extends FileSystem {
	private static final String dupSeparator;
	static
	{
		String separator = SystemProperties.getProperty("file.separator");
		dupSeparator = separator + separator;
	}

	@Override
	public String canonicalize(String path) throws IOException {
		// note : we expect that the File class from OpenJDK give an absolute path
		return VMFile.toCanonicalForm(path);
	}

	@Override
	public boolean checkAccess(File f, int access) {
		boolean canAccess;
	    if (! VMFile.exists(f.getPath()))
		      return false;

		switch(access)
		{
		case ACCESS_READ: canAccess = VMFile.canRead(f.getPath()); break;
		case ACCESS_WRITE:
			    if (VMFile.isDirectory(f.getPath()))
			    	canAccess = VMFile.canWriteDirectory(f);
			    else
			    	canAccess = VMFile.canWrite(f.getPath());
			    
				break;
		case ACCESS_EXECUTE: canAccess = VMFile.canExecute(f.getPath()); break;
		default: throw new IllegalArgumentException("invalid access : "+access);
		}
		return canAccess;
	}

	@Override
	public int compare(File f1, File f2) {
		final String f1Path = f1.getAbsolutePath(); 
		final String f2Path = f2.getAbsolutePath();
		return f1Path.compareTo(f2Path);
	}

	@Override
	public boolean createDirectory(File f) {
		return VMFile.mkdir(f.getPath());
	}

	@Override
	public boolean createFileExclusively(String pathname) throws IOException {
		return VMFile.create(pathname);
	}

	@Override
	public boolean delete(File f) {
		return VMFile.delete(f.getPath());
	}

	@Override
	public String fromURIPath(String path) {
		return path;
	}

	@Override
	public int getBooleanAttributes(File f) {
	    int attributes = 0;
	    
	    attributes |= (VMFile.exists(f.getPath()) ? BA_EXISTS : 0); 
	    attributes |= (VMFile.isFile(f.getPath()) ? BA_REGULAR : 0);
	    attributes |= (VMFile.isDirectory(f.getPath()) ? BA_DIRECTORY : 0);
	    attributes |= (VMFile.isHidden(f.getPath()) ? BA_HIDDEN : 0);

		return attributes;
	}

	@Override
	public String getDefaultParent() {
		return File.separator;
	}

	@Override
	public long getLastModifiedTime(File f) {
		return VMFile.lastModified(f.getPath());
	}

	@Override
	public long getLength(File f) {
	    return VMFile.length(f.getPath());
	}

	@Override
	public char getPathSeparator() {
		return SystemProperties.getProperty("path.separator").charAt(0);
	}

	@Override
	public char getSeparator() {
		return SystemProperties.getProperty("file.separator").charAt(0);
	}

	@Override
	public long getSpace(File f, int t) {
		long space = 0L;
		switch(t)
		{
		case SPACE_TOTAL: space = VMFile.getTotalSpace(f.getPath()); break; //TODO
		case SPACE_FREE: space = VMFile.getFreeSpace(f.getPath()); break; //TODO
		case SPACE_USABLE: space = VMFile.getUsableSpace(f.getPath()); break; //TODO
		}
		return space;
	}

	/**
	 * implemented by taking the UNIX way regarding javadoc of File.hashCode() from openjdk  
	 */
	@Override
	public int hashCode(File f) {
		return f.getPath().hashCode() ^ 1234321;
	}

	@Override
	public boolean isAbsolute(File f) {
		return f.getPath().startsWith(File.separator);
	}

	@Override
	public String[] list(File f) {
	    if (!f.exists() || !f.isDirectory())
	        return null;

	    // Get the list of files
	    return VMFile.list(f.getPath());
	}

	@Override
	public File[] listRoots() {
		return VMFile.listRoots();
	}

	@Override
	public String normalize(String path) {
	    char separatorChar = SystemProperties.getProperty("file.separator").charAt(0);
	    int dupIndex = path.indexOf(dupSeparator);
	    int plen = path.length();

	    if (dupIndex == -1)
	    {
	        // Ignore trailing separator (though on Windows "a:\", for
	        // example, is a valid and minimal path).
	        if (plen > 1 && path.charAt (plen - 1) == separatorChar)
		  {
		    if (! (separatorChar == '\\' && plen == 3 && path.charAt (1) == ':'))
		      return path.substring (0, plen - 1);
		  }
	      else
	    	  return path;
	    }

	    StringBuffer newpath = new StringBuffer(plen);
	    int last = 0;
	    while (dupIndex != -1)
	    {
	        newpath.append(path.substring(last, dupIndex));
	        // Ignore the duplicate path characters.
	        while (path.charAt(dupIndex) == separatorChar)
	        {
	        	dupIndex++;
	        	if (dupIndex == plen)
	        		return newpath.toString();
	        }
	        newpath.append(separatorChar);
	        last = dupIndex;
	        dupIndex = path.indexOf(dupSeparator, last);
	    }

	    newpath.append(path.substring(last, plen));

	    return newpath.toString();
	}

	@Override
	public int prefixLength(String path) {
		return path.lastIndexOf(File.separatorChar) + 1;
	}

	@Override
	public boolean rename(File f1, File f2) {
		return VMFile.renameTo(f1.getPath(), f2.getPath());
	}

	@Override
	public String resolve(String parent, String child) {
		return parent + File.separatorChar + child;
	}

	@Override
	public String resolve(File f) {
		if (isAbsolute(f))
	        return f.getPath();
		
        String currentDir = System.getProperty("user.dir");

        if (currentDir.endsWith(File.separator))
          return currentDir + f.getPath();
        else
          return currentDir + File.separator + f.getPath();
	}

	@Override
	public boolean setLastModifiedTime(File f, long time) {
	    return VMFile.setLastModified(f.getPath(), time);
	}

	@Override
	public boolean setPermission(File f, int access, boolean enable,
			boolean owneronly) 
	{
		boolean success = false;
		switch(access)
		{		
		case ACCESS_READ: success = VMFile.setReadable(f.getPath(), enable, owneronly); break;
		case ACCESS_WRITE: success = VMFile.setWritable(f.getPath(), enable, owneronly); break;
		case ACCESS_EXECUTE: success = VMFile.setExecutable(f.getPath(), enable, owneronly); break;	
		}
		return success;
	}

	@Override
	public boolean setReadOnly(File f) {
	    // Test for existence.
	    if (! VMFile.exists(f.getPath()))
	      return false;

	    return VMFile.setReadOnly(f.getPath());
	}
}
