/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
 
package java.io;

/**
 * Helper class for connecting java.io to JNode.
 * 
 * @author epr
 */
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
