/*
 * $Id$
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
	protected static VMFileSystemAPI getAPI() 
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
