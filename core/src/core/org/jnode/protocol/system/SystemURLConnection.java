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
 
package org.jnode.protocol.system;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.jnode.vm.VmSystem;

/**
 * @author epr
 */
public class SystemURLConnection extends URLConnection {

	private final String path;

	/**
	 * @param url
	 */
	public SystemURLConnection(URL url) {
		super(url);
		this.path = url.getPath();
	}

	/**
	 * @see java.net.URLConnection#connect()
	 */
	public void connect() throws IOException {
		/* Do nothing */
	}

	/**
	 * @see java.net.URLConnection#getInputStream()
	 */
	public InputStream getInputStream() throws IOException {
		InputStream is = VmSystem.getSystemClassLoader().getResourceAsStream(path);
		if (is == null) {
			throw new IOException("Unknown system resource " + path);
		} else {
			return is;
		}
	}
}
