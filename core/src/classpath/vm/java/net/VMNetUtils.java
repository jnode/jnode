/*
 * $Id$
 */
package java.net;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VMNetUtils {

	private static VMNetAPI api;
	private static Object token;
	
	static VMNetAPI getAPI() {
		return api;
	}
	
	public static void setAPI(VMNetAPI newApi, Object setToken) {
		if (api == null) {
			api = newApi;
			token = setToken;
		} else {
			throw new SecurityException("Cannot overwrite the NetAPI");
		}
	}
	
	public static void resetAPI(Object setToken) {
		if (token == setToken) {
			api = null;
			token = null;
		} else {
			throw new SecurityException("Cannot reset API with wrong token");
		}
	}
	
}
