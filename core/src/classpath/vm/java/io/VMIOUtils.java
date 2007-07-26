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

import org.jnode.system.BootLog;
import org.jnode.vm.annotation.SharedStatics;

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
	 * @return
	 */
    public static VMFileSystemAPI getAPI() 
	throws IOException {
    	if(api == null)
    	{
    		final String msg = "VMFileSystemAPI not yet initialized";
    		BootLog.error(msg);
    		throw new IOException(msg);
    	}
    	
		return api;
	}

	public static void setAPI(VMFileSystemAPI newApi, Object newToken) {
		if (api == null) {
			api = newApi;
			token = newToken;
		} else {
			throw new SecurityException("Cannot overwrite the API");
		}
	}

	public static void resetAPI(Object resetToken) {
		if (token == resetToken) {
			api = null;
		} else {
			throw new SecurityException("Cannot reset the API with a different token");
		}
	}
}
