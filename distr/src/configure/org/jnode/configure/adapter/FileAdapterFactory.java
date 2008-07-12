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

import org.jnode.configure.ConfigureException;

public final class FileAdapterFactory {

    public static FileAdapter createAdapter(String fileFormat) throws ConfigureException {
        if (fileFormat.equalsIgnoreCase(FileAdapter.JAVA_PROPERTIES_FORMAT)) {
            return new PropertyFileAdapter();
        } else if (fileFormat.equalsIgnoreCase(FileAdapter.XML_PROPERTIES_FORMAT)) {
            return new XMLPropertyFileAdapter();
        } else if (fileFormat.equalsIgnoreCase(FileAdapter.XML_FORMAT)) {
            return new XMLFileAdapter();
        } else if (fileFormat.equalsIgnoreCase(FileAdapter.JAVA_SOURCE_FORMAT)) {
            return new JavaSourceFileAdapter();
        } else if (fileFormat.equalsIgnoreCase(FileAdapter.TEXT_FORMAT)) {
            return new TextFileAdapter();
        } else {
            // The format must actually be a class name ...
            try {
                Class<?> clazz = Class.forName(fileFormat);
                return (FileAdapter) clazz.newInstance();
            } catch (Exception ex) {
                throw new ConfigureException(
                        "Problem instantiating FileAdapter class '" + 
                        fileFormat + "': " + ex.getMessage(), ex);
            }
        }
    }
}
