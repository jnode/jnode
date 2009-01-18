/*

JTestServer is a client/server framework for testing any JVM implementation.
 
Copyright (C) 2008  Fabien DUMINY (fduminy@jnode.org)

JTestServer is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

JTestServer is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package org.jtestserver.common;

import java.io.File;
import java.util.Properties;

public class ConfigUtils {

    public static int getInt(Properties properties, String name, int defaultValue) {
        String valueStr = properties.getProperty(name);
        int value = defaultValue;

        if ((valueStr != null) && !valueStr.trim().isEmpty()) {
            try {
                value = Integer.parseInt(valueStr);
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        return value;
    }

    public static File getDirectory(Properties properties, String name, File defaultDir) {
        String dirStr = properties.getProperty(name);
        File dir = defaultDir;

        if ((dirStr != null) && !dirStr.trim().isEmpty()) {
            dir = new File(dirStr);

            if (!dir.exists()) {
                dir.mkdirs();
            } else if (!dir.isDirectory()) {
                dir = defaultDir;
            }
        }

        return dir;
    }

    public static String[] getStringArray(Properties properties, String name) {
        String str = properties.getProperty(name);
        String[] array = null;

        if ((str != null) && !str.trim().isEmpty()) {
            array = str.split(",");
        }

        return (array == null) ? new String[0] : array;
    }

    public static boolean getBoolean(Properties properties, String name, boolean defaultValue) {
        String str = properties.getProperty(name);
        boolean value = defaultValue;

        if ((str != null) && !str.trim().isEmpty()) {
            value = "true".equals(str) || "1".equals(str) || "yes".equals(str) || "on".equals(str);
        }

        return value;
    }

    public static File getDirectory(Properties properties, String name, boolean mustExist) {
        return getFile(properties, name, mustExist, true); // directory=true
    }

    public static File getFile(Properties properties, String name, boolean mustExist) {
        return getFile(properties, name, mustExist, false); // directory=false (aka we want a file)
    }
    
    private static File getFile(Properties properties, String name, boolean mustExist, boolean directory) {
        String fileStr = properties.getProperty(name);
        File file = null;
        
        if ((fileStr != null) && !fileStr.trim().isEmpty()) {
            file = new File(fileStr);

            boolean validType = (directory && file.isDirectory()) || (!directory && file.isFile()); 
            if ((!file.exists() && mustExist) || !validType) {
                file = null;
            }
        }
        
        if (file == null) {
            final String type = directory ? "directory" : "file";
            final String msg;
            if (mustExist) {
                msg = "parameter " + name + " must be an existing " + type;
            } else {
                msg = "parameter " + name + " must be a " + type;
            }
            throw new IllegalArgumentException(msg + " (value: " + fileStr + ")");
        }

        return file;
    }

    public static String getString(Properties properties, String name) {
        String value = properties.getProperty(name, null);
        if ((value == null) || value.trim().isEmpty()) {
            throw new IllegalArgumentException("property " + name + " must be specified");
        }
        
        return value;
    }

    /**
     * @param properties
     * @param string
     * @param b
     * @return
     */
    public static String getClasspath(Properties properties, String name, boolean mustExist) {
        String classpath;
        
        if (mustExist) {
            classpath = getString(properties, name);
        } else {
            classpath = properties.getProperty(name, null);
        }
        
        if (classpath != null) {
            classpath = classpath.replace(',', File.pathSeparatorChar);
        }
        
        return classpath;
    }
}
