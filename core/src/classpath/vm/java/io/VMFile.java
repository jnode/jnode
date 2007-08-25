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
 
package java.io;

import org.jnode.java.io.VMFileHandle;

import gnu.classpath.SystemProperties;
import gnu.java.io.PlatformHelper;

/**
 * @author Michael Koch (konqueror@gmx.de)
 */
public final class VMFile {
    // FIXME: We support only case sensitive filesystems currently.
    static final boolean IS_CASE_SENSITIVE = true;

    static final boolean IS_DOS_8_3 = false;

    /*
     * This native method does the actual work of getting the last file
     * modification time. It also does the existence check to avoid the overhead
     * of a call to exists()
     */
    static long lastModified(String path) {
        try {
            return VMIOUtils.getAPI().getLastModified(getNormalizedPath(path));
        } catch (IOException ex) {
            return 0;
        }
    }

    /*
     * This native method sets the permissions to make the file read only.
     */
    static boolean setReadOnly(String path) {
        try {
            VMIOUtils.getAPI().setReadOnly(getNormalizedPath(path));
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * This method is used to create a temporary file
     */
    static boolean create(String path) throws IOException {
        // moved from JNode File.createInternal(File file) v1.3 :
        if (exists(path))
            return false;
        VMFileHandle vmFileHandler = VMIOUtils.getAPI().open(getNormalizedPath(path),
                VMOpenMode.WRITE);
        vmFileHandler.close();
        return true;
    }

    /*
     * This native function actually produces the list of file in this directory
     */
    static String[] list(String path) {
        try {
            return VMIOUtils.getAPI().list(getNormalizedPath(path));
        } catch (IOException ex) {
            return new String[0];
        }
    }

    /*
     * This native method actually performs the rename.
     */
    static boolean renameTo(String targetpath, String destpath) {
        try{
            //todo improve it
            FileInputStream fis = new FileInputStream(targetpath);
            FileOutputStream fos = new FileOutputStream(destpath);
            byte[] buf = new byte[64*1024];
            int c = 0;
            while((c = fis.read(buf)) >= 0){
                fos.write(buf, 0, c);
            }
            fis.close();
            fos.close();
            return new File(targetpath).delete();
        } catch(Exception e){
            return false;
        }
    }

    /*
     * This native method actually determines the length of the file and handles
     * the existence check
     */
    static long length(String path) {
        try {
            return VMIOUtils.getAPI().getLength(getNormalizedPath(path));
        } catch (IOException ex) {
            return 0;
        }
    }

    /*
     * This native method does the actual checking of file existence.
     */
    static boolean exists(String path) {
        try {
            return VMIOUtils.getAPI().fileExists(getNormalizedPath(path));
        } catch (IOException ex) {
            return false;
        }
    }

    /*
     * This native method handles the actual deleting of the file
     */
    static boolean delete(String path) {
        try {
            VMIOUtils.getAPI().delete(getNormalizedPath(path));
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    /*
     * This method does the actual setting of the modification time.
     */
    static boolean setLastModified(String path, long time) {
        try {
            VMIOUtils.getAPI().setLastModified(getNormalizedPath(path), time);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    /*
     * This native method actually creates the directory
     */
    static boolean mkdir(String path) {
        try {
            return VMIOUtils.getAPI().mkDir(getNormalizedPath(path));
        } catch (IOException io) {
            return false;
        }
    }

    /*
     * This native method does the actual check of whether or not a file is a
     * plain file or not. It also handles the existence check to eliminate the
     * overhead of a call to exists()
     */
    static boolean isFile(String path) {
        try {
            return VMIOUtils.getAPI().isFile(getNormalizedPath(path));
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * This native method checks file permissions for writing
     */
    static synchronized boolean canWrite(String path) {
        try {
            return VMIOUtils.getAPI().canWrite(getNormalizedPath(path));
        } catch (IOException ioe) {
            return (false);
        }
    }

    /**
     * This methods checks if a directory can be written to.
     */
    static boolean canWriteDirectory(File dir) {
        try {
            String filename = IS_DOS_8_3 ? "tst" : "test-dir-write";
            File test = File.createTempFile(filename, null, dir);
            return (test != null && test.delete());
        } catch (IOException ioe) {
            return false;
        }
    }

    /**
     * This native method checks file permissions for reading
     */
    static synchronized boolean canRead(String path) {
        try {
            return VMIOUtils.getAPI().canRead(getNormalizedPath(path));
        } catch (IOException ex) {
            return false;
        }
    }

    static  synchronized boolean canExecute(String path) {
        try {
        	return VMIOUtils.getAPI().canExecute(getNormalizedPath(path));
        } catch (IOException ex) {
            return false;
        }
	}

	public static boolean setReadable(String path, boolean enable,
			boolean owneronly) {
        try {
        	return VMIOUtils.getAPI().setReadable(getNormalizedPath(path), enable, owneronly);
        } catch (IOException ex) {
            return false;
        }		
	}

	public static boolean setWritable(String path, boolean enable,
			boolean owneronly) {
        try {
        	return VMIOUtils.getAPI().setWritable(getNormalizedPath(path), enable, owneronly);
        } catch (IOException ex) {
            return false;
        }
	}

	public static boolean setExecutable(String path, boolean enable,
			boolean owneronly) {
        try {
        	return VMIOUtils.getAPI().setExecutable(getNormalizedPath(path), enable, owneronly);
        } catch (IOException ex) {
            return false;
        }
	}

    /*
     * This method does the actual check of whether or not a file is a directory
     * or not. It also handle the existence check to eliminate the overhead of a
     * call to exists()
     */
    static boolean isDirectory(String path) {
        try {
            return VMIOUtils.getAPI().isDirectory(getNormalizedPath(path));
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * This method returns an array of filesystem roots. Some operating systems
     * have volume oriented filesystem. This method provides a mechanism for
     * determining which volumes exist. GNU systems use a single hierarchical
     * filesystem, so will have only one "/" filesystem root.
     * 
     * @return An array of <code>File</code> objects for each filesystem root
     *         available.
     * @since 1.2
     */
    static File[] listRoots() {
        // ClassPath implementation
        // File[] roots = new File[1];
        // roots[0] = new File("/");
        // return roots;

        // JNode implementation
        try {
            return VMIOUtils.getAPI().getRoots();
        } catch (IOException ex) {
            return new File[0];
        }
    }

    /**
     * This method tests whether or not this file represents a "hidden" file. On
     * GNU systems, a file is hidden if its name begins with a "." character.
     * Files with these names are traditionally not shown with directory listing
     * tools.
     * 
     * @return <code>true</code> if the file is hidden, <code>false</code>
     *         otherwise.
     * @since 1.2
     */
    static boolean isHidden(String path) {
        // FIXME: this only works on UNIX
        return getName(path).startsWith(".");
    }

    /**
     * This method returns the name of the file. This is everything in the
     * complete path of the file after the last instance of the separator
     * string.
     * 
     * @return The file name
     */
    static String getName(String path) {
        int pos = PlatformHelper.lastIndexOfSeparator(path);
        if (pos == -1)
            return path;

        if (PlatformHelper.endWithSeparator(path))
            return "";

        return path.substring(pos + File.separator.length());
    }

    /**
     * This method returns a canonical representation of the pathname of the
     * given path. The actual form of the canonical representation is different.
     * On the GNU system, the canonical form differs from the absolute form in
     * that all relative file references to "." and ".." are resolved and
     * removed.
     * <p>
     * Note that this method, unlike the other methods which return path names,
     * can throw an IOException. This is because native method might be required
     * in order to resolve the canonical path
     * 
     * @exception IOException
     *                If an error occurs
     */
    final static String toCanonicalForm(String path) throws IOException {
        // FIXME: this only works on UNIX
        return PlatformHelper.toCanonicalForm(path);
    }
    
    /**
     * Make an absolate and canonical path from the given path.
     * @param path
     * @return
     */
    public static final String getNormalizedPath(String path) {
        if (!path.startsWith(File.separator)) {
            path = SystemProperties.getProperty("user.dir") + File.separator + path;
        }
        path = PlatformHelper.toCanonicalForm(path);
        // Strip leading seperator
        if (path.startsWith(File.separator)) {
            path = path.substring(1);
        }
        // Strip trailing seperator
        if (path.endsWith(File.separator)) {
            path = path.substring(0, path.length()-1);
        }
        return path;
    }

	public static long getTotalSpace(String path) {
        try {
            return VMIOUtils.getAPI().getTotalSpace(getNormalizedPath(path));
        } catch (IOException ex) {
            return 0L;
        }
	}

	public static long getFreeSpace(String path) {
        try {
            return VMIOUtils.getAPI().getFreeSpace(getNormalizedPath(path));
        } catch (IOException ex) {
            return 0L;
        }
	}

	public static long getUsableSpace(String path) {
        try {
            return VMIOUtils.getAPI().getUsableSpace(getNormalizedPath(path));
        } catch (IOException ex) {
            return 0L;
        }
	}
}
