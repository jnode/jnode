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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Properties;

import org.jnode.configure.Configure;
import org.jnode.configure.ConfigureException;
import org.jnode.configure.PropertySet;
import org.jnode.configure.PropertyType;
import org.jnode.configure.PropertySet.Property;

/**
 * This is a base class for File adapters that uses {@link java.util.Properties}
 * objects as an intermediate representation.
 * 
 * @author crawley@jnode.org
 */
public abstract class BasePropertyFileAdapter implements FileAdapter {
    
    interface ValueCodec {
        public String encodeText(String raw) throws ConfigureException;
        public String decodeText(String encoded) throws ConfigureException;
    }
	
	private final ValueCodec codec;
	private final boolean loadSupported;
	private final boolean saveSupported;

	protected abstract void loadFromFile(Properties props, InputStream imput) 
	    throws IOException;
	
	protected abstract void saveToFile(Properties props, OutputStream output, String comment) 
	    throws IOException;
	
	public BasePropertyFileAdapter(ValueCodec codec, boolean loadSupported, boolean saveSupported) {
		super();
		this.codec = codec;
		this.loadSupported = loadSupported;
		this.saveSupported = saveSupported;
	}
	
	/**
	 * Return <code>true</code> if this adapter supports loading of properties.
	 */
	public boolean isLoadSupported() {
		return loadSupported;
	}

	/**
     * Return <code>true</code> if this adapter supports saving of properties.
     * (If not, the file format requires the use of a template file.)
     */
	public boolean isSaveSupported() {
		return saveSupported;
	}

	public void load(PropertySet propSet, Configure configure) throws ConfigureException {
    	File file = propSet.getFile();
    	File defaultFile = propSet.getDefaultFile();
        Properties properties = new Properties();
        if (loadSupported) { 
        	InputStream in = null;
        	try {
        		if (!file.exists() && defaultFile != null) {
        			if (defaultFile.exists()) {
        				configure.output("Taking initial values for the '" + file + 
        						"' properties from '" + defaultFile + "'.");
        				file = defaultFile;
        			}
        		}
        		in = new BufferedInputStream(new FileInputStream(file));
        		loadFromFile(properties, in);
        	} catch (FileNotFoundException ex) {
        		// Fall back to the builtin default property values
        		configure.output("Taking initial values for the '" + file + 
        		"' properties from the builtin defaults.");
        	} catch (IOException ex) {
        		throw new ConfigureException(
        				"Problem loading properties from '" + file + "'.", ex);
        	} finally {
        		if (in != null) {
        			try {
        				in.close();
        			} catch (IOException ex) {
        				// ignore
        			}
        		}
        	}
        }
        for (Object key : properties.keySet()) {
            String value = (String) properties.get(key);
            Property prop = propSet.getProperty((String) key);
            if (prop != null) {
            	PropertyType type = prop.getType();
                prop.setDefaultValue(type.fromValue(value));
            }
        }
	}

	public void save(PropertySet propSet, Configure configure) throws ConfigureException {
		// Harvest the properties to be written into a Properties Object
		Properties properties = new Properties();
        for (Map.Entry<String, Property> entry : propSet.getProperties().entrySet()) {
            Property prop = entry.getValue();
            properties.setProperty(entry.getKey(), prop.getValue().getText());
        }
        OutputStream os = null;
        InputStream is = null;
        File toFile = propSet.getFile();
        File templateFile = propSet.getTemplateFile();
        try {
        	os = new FileOutputStream(toFile);
        	if (templateFile == null && saveSupported) {
        		saveToFile(properties, new BufferedOutputStream(os), 
        				"Expanded by JNode 'configure' tool");
        	} else {
        		try {
        			is = new FileInputStream(templateFile);
        		} catch (FileNotFoundException ex) {
        			throw new ConfigureException("Cannot read template file", ex);
        		}
        		expandToTemplate(properties, is, os, propSet.getMarker(), templateFile);
        	}
        } catch (IOException ex) {
        	throw new ConfigureException("Cannot save properties to '" + toFile + "'.", ex);
        } finally {
        	if (os != null) {
        		try {
        			os.close();
        		} catch (IOException ex) {
        			// ignore
        		}
        	}
        	if (is != null) {
        		try {
        			is.close();
        		} catch (IOException ex) {
        			// ignore
        		}
        	}
        }
	}

	/**
	 * Expand a '@...@' sequences in an input stream, writing the result to an output
	 * stream.  A sequence '@@' turns into a single '@'.  A sequence '@name@' expands
	 * to the value of the named property if it is defined in the property set, or the
	 * sequence '@name@' if it does not.  A CR, NL or EOF in an @...@ sequence is an error.
	 * 
	 * @param props the properties to be expanded
	 * @param is the source for the template
	 * @param os the sink for the expanded template
	 * @param marker the sequence marker character(defaults to '@')
	 * @param fileName the template filename for diagnostics
	 * @throws IOException
	 * @throws ConfigureException
	 */
	private void expandToTemplate(Properties props, InputStream is, OutputStream os, 
			char marker, File file)
		throws IOException, ConfigureException {
		int ch;
		int lineNo = 1;
		BufferedReader r = new BufferedReader(new InputStreamReader(is));
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(os));
		try {
			while ((ch = r.read()) != -1) {
				if (ch == marker) {
					StringBuffer sb = new StringBuffer(20);
					while ((ch = r.read()) != marker) {
						switch (ch) {
						case -1:
							throw new ConfigureException(
									"Encountered EOF in a " + marker + "..." + marker + " sequence");
						case '\r':
						case '\n':
							throw new ConfigureException(
									"Encountered end-of-line in a " + marker + "..." + marker + 
									" sequence: at " + file + " line " + lineNo);
						default:
							sb.append((char) ch);
						}
					}
					if (sb.length() == 0) {
						w.write(marker);
					} else {
						String propName = sb.toString();
						String propValue = props.getProperty(propName);
						if (propValue == null) {
							w.write(marker);
							w.write(propName);
							w.write(marker);
						} else {
							w.write(codec.encodeText(propValue));
						}
					}
				} else {
				    // FIXME ... make this aware of the host OS newline sequence.
					if (ch == '\n') {
						lineNo++;
					}
					w.write((char) ch);
				}
			}
		}
		finally {
			w.flush();
		}
	}
}
