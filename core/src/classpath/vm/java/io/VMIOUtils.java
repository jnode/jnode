/*
 * $Id$
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

import org.jnode.vm.annotation.SharedStatics;
import org.jnode.java.io.VMFileHandle;

//todo serious review is needed
/**
 * Helper class for connecting java.io to JNode.
 *
 * @author epr
 */
@SharedStatics
public class VMIOUtils {

	/** The filesystem API of JNode */
	private static VMFileSystemAPI api;
	private static Object token;

    /**
	 * Gets the JNode FileSystemService instance.
	 *
	 * @return
	 */
	/*
	 * protected static FileSystemService getFileSystemService() throws IOException { if (fss ==
	 * null) { try { fss = (FileSystemService)InitialNaming.lookup(FileSystemService.NAME); } catch
	 * (NameNotFoundException ex) { throw new IOException("Cannot lookup FileSystemService", ex); } }
	 * return fss;
	 */

	/**
	 * Gets the JNode FileSystemAPI.
	 *
	 * @return the file system api
     * @throws IOException in circumstances in the current version
	 */
    public static VMFileSystemAPI getAPI() throws IOException {
    	if(api == null) {
//            final String msg = "VMFileSystemAPI not yet initialized";
//    		BootLog.error(msg);
//            org.jnode.vm.Unsafe.debugStackTrace(1000);
            api = new NoFileSystemAPI();
//            throw new IOException(msg);
    	}

		return api;
	}

	public static void setAPI(VMFileSystemAPI newApi, Object newToken) {
		if (token == null) {
			api = newApi;
			token = newToken;
		} else {
			throw new SecurityException("Cannot overwrite the API");
		}
	}

	public static void resetAPI(Object resetToken) {
		if (token == resetToken) {
			api = null;
            token = null;
        } else {
			throw new SecurityException("Cannot reset the API with a different token");
		}
	}

    public static boolean isConnected() {
        return token != null;
    }

    private static final class NoFileSystemAPI implements VMFileSystemAPI {
        public boolean canExecute(String file) throws IOException {
            return false;
        }

        public boolean canRead(String file) throws IOException {
            return false;
        }

        public boolean canWrite(String file) throws IOException {
            return false;
        }

        public void delete(String file) throws IOException {
            throw new FileNotFoundException(file);
        }

        public boolean fileExists(String file) {
            return false;
        }

        public long getFreeSpace(String normalizedPath) throws IOException {
            return 0;
        }

        public long getLastModified(String file) {
            return 0;
        }

        public long getLength(String file) {
            return 0;
        }

        public File[] getRoots() {
            return new File[0];
        }

        public long getTotalSpace(String normalizedPath) throws IOException {
            return 0;
        }

        public long getUsableSpace(String normalizedPath) throws IOException {
            return 0;
        }

        public boolean isDirectory(String file) {
            return false;
        }

        public boolean isFile(String file) {
            return false;
        }

        public String[] list(String directory) throws IOException {
            return new String[0];
        }

        public boolean mkDir(String file) throws IOException {
            return false;
        }

        public boolean mkFile(String file, VMOpenMode mode) throws IOException {
            return false;
        }

        public VMFileHandle open(String file, VMOpenMode mode) throws IOException {
            throw new FileNotFoundException(file);
        }

        public boolean setExecutable(String normalizedPath, boolean enable, boolean owneronly) throws IOException {
            return false;
        }

        public void setLastModified(String file, long time) throws IOException {
            throw new FileNotFoundException(file);
        }

        public boolean setReadable(String normalizedPath, boolean enable, boolean owneronly) throws IOException {
            return false;
        }

        public void setReadOnly(String file) throws IOException {
            throw new FileNotFoundException(file);
        }

        public boolean setWritable(String normalizedPath, boolean enable, boolean owneronly) throws IOException {
            return false;
        }
    }
}
