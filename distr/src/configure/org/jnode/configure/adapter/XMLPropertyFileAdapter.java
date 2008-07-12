/*
 * $Id $
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
package org.jnode.configure.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Property file adapter that loads and saves property files in XML format.
 * 
 * @author crawley@jnode.org
 */		
public class XMLPropertyFileAdapter extends BasePropertyFileAdapter {
	private static BasePropertyFileAdapter.ValueCodec XML_VALUE_CODEC =
	    new XMLValueCodec();

	public XMLPropertyFileAdapter() {
		super(XML_VALUE_CODEC, true, true);
	}

	@Override
	protected void loadFromFile(Properties props, InputStream input)
		throws IOException {
		props.loadFromXML(input);
	}

	@Override
	protected void saveToFile(Properties props, OutputStream output, String comment)
		throws IOException {
		props.storeToXML(output, comment);
	}
}
