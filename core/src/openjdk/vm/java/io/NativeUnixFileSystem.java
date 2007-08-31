/*
 * $Id$
 */
package java.io;

/**
 * @author Levente Sántha
 */
public class NativeUnixFileSystem {
    private static String canonicalize0(UnixFileSystem ufs, String path) throws IOException {
        // note : we expect that the File class from OpenJDK give an absolute path
		return VMFile.toCanonicalForm(path);
    }

    private static int getBooleanAttributes0(UnixFileSystem ufs, File f) {
        int attributes = 0;

	    attributes |= (VMFile.exists(f.getPath()) ? FileSystem.BA_EXISTS : 0);
	    attributes |= (VMFile.isFile(f.getPath()) ? FileSystem.BA_REGULAR : 0);
	    attributes |= (VMFile.isDirectory(f.getPath()) ? FileSystem.BA_DIRECTORY : 0);
	    attributes |= (VMFile.isHidden(f.getPath()) ? FileSystem.BA_HIDDEN : 0);

		return attributes;
    }

    private static boolean checkAccess(UnixFileSystem ufs, File f, int access) {
        boolean canAccess;
	    if (! VMFile.exists(f.getPath()))
		      return false;

		switch(access)
		{
		case FileSystem.ACCESS_READ: canAccess = VMFile.canRead(f.getPath()); break;
		case FileSystem.ACCESS_WRITE:
			    if (VMFile.isDirectory(f.getPath()))
			    	canAccess = VMFile.canWriteDirectory(f);
			    else
			    	canAccess = VMFile.canWrite(f.getPath());

				break;
		case FileSystem.ACCESS_EXECUTE: canAccess = VMFile.canExecute(f.getPath()); break;
		default: throw new IllegalArgumentException("invalid access : "+access);
		}
		return canAccess;
    }

    private static long getLastModifiedTime(UnixFileSystem ufs, File f) {
        return VMFile.lastModified(f.getPath());
    }

    private static long getLength(UnixFileSystem ufs, File f) {
        return VMFile.length(f.getPath());
    }

    private static boolean setPermission(UnixFileSystem ufs, File f, int access, boolean enable, boolean owneronly) {
        boolean success = false;
		switch(access)
		{
		case FileSystem.ACCESS_READ: success = VMFile.setReadable(f.getPath(), enable, owneronly); break;
		case FileSystem.ACCESS_WRITE: success = VMFile.setWritable(f.getPath(), enable, owneronly); break;
		case FileSystem.ACCESS_EXECUTE: success = VMFile.setExecutable(f.getPath(), enable, owneronly); break;
		}
		return success;
    }

    private static boolean createFileExclusively(UnixFileSystem ufs, String path) {
        try {
            return VMFile.create(path);
        } catch(IOException ioe){
            return false;
        }
    }

    private static boolean delete0(UnixFileSystem ufs, File f) {
        return VMFile.delete(f.getPath());
    }

    private static String[] list(UnixFileSystem ufs, File f) {
        if (!f.exists() || !f.isDirectory())
	        return null;

	    // Get the list of files
	    return VMFile.list(f.getPath());
    }

    private static boolean createDirectory(UnixFileSystem ufs, File f) {
        return VMFile.mkdir(f.getPath());
    }

    private static boolean rename0(UnixFileSystem ufs, File f1, File f2) {
        return VMFile.renameTo(f1.getPath(), f2.getPath());
    }

    private static boolean setLastModifiedTime(UnixFileSystem ufs, File f, long time) {
        return VMFile.setLastModified(f.getPath(), time);
    }

    private static boolean setReadOnly(UnixFileSystem ufs, File f) {
        // Test for existence.
	    if (! VMFile.exists(f.getPath()))
	      return false;

	    return VMFile.setReadOnly(f.getPath());
    }

    private static long getSpace(UnixFileSystem ufs, File f, int t) {
        long space = 0L;
		switch(t)
		{
		case FileSystem.SPACE_TOTAL: space = VMFile.getTotalSpace(f.getPath()); break; //TODO
		case FileSystem.SPACE_FREE: space = VMFile.getFreeSpace(f.getPath()); break; //TODO
		case FileSystem.SPACE_USABLE: space = VMFile.getUsableSpace(f.getPath()); break; //TODO
		}
		return space;
    }

    private static void initIDs() {

    }
}
