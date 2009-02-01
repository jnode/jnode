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
