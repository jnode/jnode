/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 
package org.jnode.awt.font.truetype;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author epr
 */
public class TTFURLInput extends TTFMemoryInput {

	/**
	 * @param url
	 * @throws IOException
	 */
	public TTFURLInput(URL url) 
	throws IOException {
		super(getData(url));
	}

	private static byte[] getData(URL url) 
	throws IOException {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		final InputStream is = url.openStream();
		final byte[] buf = new byte[4096];
		int len;
		while ((len = is.read(buf)) > 0) {
			os.write(buf, 0, len);
		}
		is.close();
		return os.toByteArray();
	}
}
