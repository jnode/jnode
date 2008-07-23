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

import org.jnode.configure.Configure;
import org.jnode.configure.ConfigureException;
import org.jnode.configure.PropertySet;

public interface FileAdapter {

    /**
     * This value denotes a classic Java properties file.
     */
    public static final String JAVA_PROPERTIES_FORMAT = "properties";

    /**
     * This value denotes an XML Java properties file (Java 1.5 and later).
     */
    public static final String XML_PROPERTIES_FORMAT = "xmlProperties";

    /**
     * This value denotes an XML file with an unspecified DTD. Property values
     * are encoded on the assumption that the XML encoding is UTF-8 and that
     * they will be expanded into an element content.
     * <p>
     * Note that files with this format cannot be read by the Configure tool.
     * A'templateFile' is mandatory.
     */
    public static final String XML_FORMAT = "xml";

    /**
     * This value denotes a Java source file. Property values are encoded on the
     * assumption that they will be expanded into a Java String literal.
     * <p>
     * Note that files with this format cannot be read by the Configure tool.
     * A'templateFile' is mandatory.
     */
    public static final String JAVA_SOURCE_FORMAT = "java";

    /**
     * This value denotes a text file.
     * <p>
     * Note that files with this format cannot be read by the Configure tool.
     * A'templateFile' is mandatory.
     */
    public static final String TEXT_FORMAT = "text";

    public boolean isLoadSupported();

    public boolean isSaveSupported();

    public void load(PropertySet propSet, Configure configure) throws ConfigureException;

    public void save(PropertySet propSet, Configure configure) throws ConfigureException;
}
