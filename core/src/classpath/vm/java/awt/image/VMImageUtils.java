/*
 * $Id$
 */
package java.awt.image;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class VMImageUtils {
	private static VMImageAPI api;
	private static Object token;
	
	static VMImageAPI getAPI() {
		return api;
	}
	
	public static void setAPI(VMImageAPI newApi, Object setToken) {
		if (api == null) {
			api = newApi;
			token = setToken;
		} else {
			throw new SecurityException("Cannot overwrite the ImageAPI");
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
